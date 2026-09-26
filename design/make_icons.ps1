# Generates legacy launcher PNGs and the Play Store icon from shiba_wink.png.
# Run from anywhere:  powershell -File design\make_icons.ps1
# The adaptive icon (mipmap-anydpi) is what Android 8+ actually uses; these
# PNGs are fallbacks for launchers that ignore adaptive icons, plus the store art.

Add-Type -AssemblyName System.Drawing

$root   = Split-Path -Parent $PSScriptRoot
$source = Join-Path $PSScriptRoot "shiba_wink.png"
$res    = Join-Path $root "app\src\main\res"
$master = [System.Drawing.Bitmap]::FromFile($source)

function New-Canvas([int]$size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode   = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    return @($bmp, $g)
}

function Get-Scaled([int]$size) {
    $c = New-Canvas $size
    $c[1].DrawImage($master, 0, 0, $size, $size)
    $c[1].Dispose()
    return $c[0]
}

# Fills $path with the scaled art so the edge is anti-aliased (Region clips are not).
function Save-Masked([int]$size, [System.Drawing.Drawing2D.GraphicsPath]$path, [string]$dest) {
    $scaled = Get-Scaled $size
    $c = New-Canvas $size
    $brush = New-Object System.Drawing.TextureBrush $scaled
    $c[1].FillPath($brush, $path)
    $c[1].Dispose(); $brush.Dispose(); $scaled.Dispose()
    New-Item -ItemType Directory -Force (Split-Path $dest) | Out-Null
    $c[0].Save($dest, [System.Drawing.Imaging.ImageFormat]::Png)
    $c[0].Dispose()
}

function RoundedRectPath([int]$size, [double]$radiusFraction) {
    $r = [int]($size * $radiusFraction); $d = 2 * $r
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $p.AddArc(0, 0, $d, $d, 180, 90)
    $p.AddArc($size - $d, 0, $d, $d, 270, 90)
    $p.AddArc($size - $d, $size - $d, $d, $d, 0, 90)
    $p.AddArc(0, $size - $d, $d, $d, 90, 90)
    $p.CloseFigure()
    return $p
}

function CirclePath([int]$size) {
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $p.AddEllipse(0, 0, $size, $size)
    return $p
}

$densities = @{ mdpi = 48; hdpi = 72; xhdpi = 96; xxhdpi = 144; xxxhdpi = 192 }
foreach ($d in $densities.GetEnumerator()) {
    $size = $d.Value
    $dir = Join-Path $res "mipmap-$($d.Key)"
    Save-Masked $size (RoundedRectPath $size 0.22) (Join-Path $dir "ic_launcher.png")
    Save-Masked $size (CirclePath $size)           (Join-Path $dir "ic_launcher_round.png")
    Write-Output "mipmap-$($d.Key): ${size}px"
}

# Play Store listing icon: 512x512, full square, no transparency.
$store = Get-Scaled 512
$store.Save((Join-Path $PSScriptRoot "ic_playstore_512.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$store.Dispose()
$master.Dispose()
Write-Output "design/ic_playstore_512.png: 512px"
