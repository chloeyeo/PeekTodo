"""Renders the Play Store icon and feature graphic from the app's own resources.

    python store-listing/render_assets.py

Outputs
    store-listing/icon-512.png            512x512, 32-bit, full-bleed square
    store-listing/feature-1024x500.png    1024x500 feature graphic

The launcher icon is an adaptive icon: a flat colour background plus a
VectorDrawable foreground. There is no VectorDrawable rasteriser in Pillow,
so this file contains a small one that supports exactly what the icon uses
(M/L/H/V/C/Q/A/Z path data, groups with scale/translate, fills and strokes
with round joins/caps). It draws at 4x and downsamples for anti-aliasing.

The 512 icon shows the same crop a launcher shows: the central 72 dp of the
108 dp adaptive canvas, so the art fills the square the way it does on a
home screen. Play applies its own rounded mask on top.
"""

from __future__ import annotations

import math
import os
import re
import sys
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
OUT = os.path.dirname(os.path.abspath(__file__))

FOREGROUND = os.path.join(RES, "drawable", "ic_launcher_foreground.xml")
COLORS_XML = os.path.join(RES, "values", "colors.xml")

ANDROID = "{http://schemas.android.com/apk/res/android}"

# Brand colours, identical to ui/theme/Color.kt (sampled from design/shiba_wink.png).
KHAKI = (0xF3, 0xE2, 0xB3)
CLIPBOARD = (0xFF, 0xFB, 0xF3)
INK = (0x3A, 0x33, 0x2E)
INK_MUTED = (0x6B, 0x5F, 0x55)

SUPERSAMPLE = 4


# ---------------------------------------------------------------- path data

_TOKEN = re.compile(r"[MmLlHhVvCcQqAaZz]|[-+]?(?:\d*\.\d+|\d+\.?)(?:[eE][-+]?\d+)?")


def _arc(p0, rx, ry, phi_deg, large, sweep, p1, n=40):
    """SVG endpoint arc to a polyline (center parameterisation, W3C appendix B.2.4)."""
    x1, y1 = p0
    x2, y2 = p1
    if rx == 0 or ry == 0 or (x1, y1) == (x2, y2):
        return [p1]
    phi = math.radians(phi_deg)
    cp, sp = math.cos(phi), math.sin(phi)
    dx, dy = (x1 - x2) / 2, (y1 - y2) / 2
    x1p = cp * dx + sp * dy
    y1p = -sp * dx + cp * dy
    rx, ry = abs(rx), abs(ry)
    lam = x1p**2 / rx**2 + y1p**2 / ry**2
    if lam > 1:
        rx *= math.sqrt(lam)
        ry *= math.sqrt(lam)
    num = rx**2 * ry**2 - rx**2 * y1p**2 - ry**2 * x1p**2
    den = rx**2 * y1p**2 + ry**2 * x1p**2
    coef = math.sqrt(max(0.0, num / den)) if den else 0.0
    if large == sweep:
        coef = -coef
    cxp = coef * rx * y1p / ry
    cyp = -coef * ry * x1p / rx
    cx = cp * cxp - sp * cyp + (x1 + x2) / 2
    cy = sp * cxp + cp * cyp + (y1 + y2) / 2

    def ang(ux, uy, vx, vy):
        return math.atan2(ux * vy - uy * vx, ux * vx + uy * vy)

    t1 = ang(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry)
    dt = ang((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
    if not sweep and dt > 0:
        dt -= 2 * math.pi
    elif sweep and dt < 0:
        dt += 2 * math.pi
    pts = []
    for i in range(1, n + 1):
        t = t1 + dt * i / n
        pts.append((cx + rx * math.cos(t) * cp - ry * math.sin(t) * sp,
                    cy + rx * math.cos(t) * sp + ry * math.sin(t) * cp))
    return pts


def _bezier(points, n):
    out = []
    for i in range(1, n + 1):
        t = i / n
        pts = list(points)
        while len(pts) > 1:
            pts = [((1 - t) * a[0] + t * b[0], (1 - t) * a[1] + t * b[1]) for a, b in zip(pts, pts[1:])]
        out.append(pts[0])
    return out


def parse_path(d: str):
    """Returns a list of subpaths: (points, closed)."""
    tokens = _TOKEN.findall(d)
    i = 0
    cmd = None
    cur = (0.0, 0.0)
    start = (0.0, 0.0)
    subpaths = []
    pts = []

    def num():
        nonlocal i
        v = float(tokens[i])
        i += 1
        return v

    def flush(closed):
        nonlocal pts
        if len(pts) > 1:
            subpaths.append((pts, closed))
        pts = []

    while i < len(tokens):
        if re.match(r"[A-Za-z]", tokens[i]):
            cmd = tokens[i]
            i += 1
            if cmd in "Zz":
                flush(True)
                cur = start
                pts = [cur]
                continue
        rel = cmd.islower()
        c = cmd.upper()
        ox, oy = cur if rel else (0.0, 0.0)
        if c == "M":
            flush(False)
            cur = (num() + ox, num() + oy)
            start = cur
            pts = [cur]
            cmd = "l" if rel else "L"  # implicit lineto after moveto
        elif c == "L":
            cur = (num() + ox, num() + oy)
            pts.append(cur)
        elif c == "H":
            cur = (num() + ox, cur[1])
            pts.append(cur)
        elif c == "V":
            cur = (cur[0], num() + oy)
            pts.append(cur)
        elif c == "C":
            p1 = (num() + ox, num() + oy)
            p2 = (num() + ox, num() + oy)
            p3 = (num() + ox, num() + oy)
            pts.extend(_bezier([cur, p1, p2, p3], 32))
            cur = p3
        elif c == "Q":
            p1 = (num() + ox, num() + oy)
            p2 = (num() + ox, num() + oy)
            pts.extend(_bezier([cur, p1, p2], 24))
            cur = p2
        elif c == "A":
            rx, ry, rot = num(), num(), num()
            large, sweep = int(num()), int(num())
            end = (num() + ox, num() + oy)
            pts.extend(_arc(cur, rx, ry, rot, large, sweep, end))
            cur = end
        else:
            raise ValueError(f"unsupported path command {cmd}")
    flush(False)
    return subpaths


# ---------------------------------------------------------------- vector xml

def _color(value: str | None):
    if not value:
        return None
    v = value.lstrip("#")
    if len(v) == 6:
        return tuple(int(v[k:k + 2], 16) for k in (0, 2, 4)) + (255,)
    if len(v) == 8:
        a, r, g, b = (int(v[k:k + 2], 16) for k in (0, 2, 4, 6))
        return (r, g, b, a)
    raise ValueError(f"unsupported colour {value}")


def _attr(el, name, default=None):
    return el.get(ANDROID + name, default)


def load_vector(path: str):
    """Returns (viewport_w, viewport_h, layers). Each layer is a dict with resolved transform."""
    root = ET.parse(path).getroot()
    vw = float(_attr(root, "viewportWidth"))
    vh = float(_attr(root, "viewportHeight"))
    layers = []

    def walk(el, transform):
        for child in el:
            tag = child.tag.split("}")[-1]
            if tag == "group":
                sx = float(_attr(child, "scaleX", "1"))
                sy = float(_attr(child, "scaleY", "1"))
                px = float(_attr(child, "pivotX", "0"))
                py = float(_attr(child, "pivotY", "0"))
                tx = float(_attr(child, "translateX", "0"))
                ty = float(_attr(child, "translateY", "0"))

                def t(p, _t=transform, sx=sx, sy=sy, px=px, py=py, tx=tx, ty=ty):
                    x, y = p
                    return _t(((x - px) * sx + px + tx, (y - py) * sy + py + ty))

                walk(child, t)
            elif tag == "path":
                layers.append({
                    "subpaths": parse_path(_attr(child, "pathData")),
                    "fill": _color(_attr(child, "fillColor")),
                    "stroke": _color(_attr(child, "strokeColor")),
                    "stroke_width": float(_attr(child, "strokeWidth", "0")),
                    "cap": _attr(child, "strokeLineCap", "butt"),
                    "join": _attr(child, "strokeLineJoin", "miter"),
                    "transform": transform,
                    # The icon's group is uniform; strokes scale by that factor.
                    "scale": abs(float(_attr(el, "scaleX", "1"))) if el.tag.endswith("group") else 1.0,
                })

    walk(root, lambda p: p)
    return vw, vh, layers


def render_vector(path: str, canvas_px: int, crop: tuple[int, int, int, int] | None = None,
                  ss: int = SUPERSAMPLE) -> Image.Image:
    """Draws the vector onto a transparent canvas of canvas_px (the full 108 dp),
    optionally cropping (left, top, right, bottom) in output pixels."""
    vw, vh, layers = load_vector(path)
    big = canvas_px * ss
    unit = big / vw
    img = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    for layer in layers:
        tf = layer["transform"]
        to_px = lambda p, tf=tf: tuple(c * unit for c in tf(p))
        subpaths = [([to_px(p) for p in pts], closed) for pts, closed in layer["subpaths"]]

        if layer["fill"]:
            for pts, _ in subpaths:
                if len(pts) >= 3:
                    draw.polygon(pts, fill=layer["fill"])

        if layer["stroke"] and layer["stroke_width"] > 0:
            w = max(1, round(layer["stroke_width"] * layer["scale"] * unit))
            joint = "curve" if layer["join"] == "round" else None
            for pts, closed in subpaths:
                line = pts + [pts[0], pts[1]] if closed and len(pts) > 2 else pts
                draw.line(line, fill=layer["stroke"], width=w, joint=joint)
                if not closed and layer["cap"] == "round":
                    r = w / 2
                    for (x, y) in (pts[0], pts[-1]):
                        draw.ellipse((x - r, y - r, x + r, y + r), fill=layer["stroke"])

    if crop:
        l, t, r, b = (c * ss for c in crop)
        img = img.crop((l, t, r, b))
    return img.resize((img.width // ss, img.height // ss), Image.LANCZOS)


def launcher_icon(size: int) -> Image.Image:
    """The icon as a launcher shows it: central 72 dp of the 108 dp canvas on the flat background."""
    canvas = round(size * 108 / 72)
    offset = (canvas - size) // 2
    fg = render_vector(FOREGROUND, canvas, crop=(offset, offset, offset + size, offset + size))
    icon = Image.new("RGBA", (size, size), KHAKI + (255,))
    icon.alpha_composite(fg)
    return icon


# ---------------------------------------------------------------- outputs

def font(name: str, size: int):
    for candidate in (name, "seguisb.ttf", "segoeui.ttf", "arialbd.ttf", "DejaVuSans-Bold.ttf"):
        for folder in (r"C:\Windows\Fonts", "/usr/share/fonts/truetype/dejavu", "/System/Library/Fonts"):
            p = os.path.join(folder, candidate)
            if os.path.exists(p):
                return ImageFont.truetype(p, size)
    return ImageFont.load_default(size)


def make_icon():
    icon = launcher_icon(512)
    out = os.path.join(OUT, "icon-512.png")
    icon.save(out, "PNG", optimize=True)
    return out


def make_feature_graphic():
    W, H = 1024, 500
    img = Image.new("RGB", (W, H), KHAKI)
    draw = ImageDraw.Draw(img)

    # Icon on a clipboard-white tile with the app's thin ink outline. The tile
    # keeps it legible against the khaki ground (its own background colour).
    tile = 340
    tx, ty = 96, (H - tile) // 2
    radius = int(tile * 0.22)
    draw.rounded_rectangle((tx, ty, tx + tile, ty + tile), radius=radius, fill=CLIPBOARD, outline=INK, width=6)
    art = render_vector(FOREGROUND, round(300 * 108 / 72),
                        crop=((round(300 * 108 / 72) - 300) // 2,) * 2 + ((round(300 * 108 / 72) + 300) // 2,) * 2)
    img.paste(art, (tx + (tile - 300) // 2, ty + (tile - 300) // 2), art)

    # Wordmark and one short tagline, sized to stay inside a 64 px right margin.
    x = tx + tile + 72
    avail = W - 64 - x

    def fitted(name, text, start):
        size = start
        while size > 20:
            f = font(name, size)
            if draw.textbbox((0, 0), text, font=f)[2] <= avail:
                return f
            size -= 2
        return font(name, size)

    title_text, tag_text = "PeekTodo", "To-dos on your lock screen."
    title = fitted("seguisb.ttf", title_text, 104)
    tagline = fitted("segoeui.ttf", tag_text, 42)
    th = draw.textbbox((0, 0), title_text, font=title)[3]
    gh = draw.textbbox((0, 0), tag_text, font=tagline)[3]
    top = (H - (th + 24 + gh)) // 2
    draw.text((x, top), title_text, font=title, fill=INK)
    draw.text((x + 3, top + th + 24), tag_text, font=tagline, fill=INK_MUTED)

    out = os.path.join(OUT, "feature-1024x500.png")
    img.save(out, "PNG", optimize=True)
    return out


def check(path: str, size: tuple[int, int], max_bytes: int, mode: str | None = None):
    with Image.open(path) as im:
        ok = im.size == size and os.path.getsize(path) <= max_bytes and (mode is None or im.mode == mode)
        print(f"{os.path.relpath(path, ROOT)}: {im.size[0]}x{im.size[1]} {im.mode} "
              f"{os.path.getsize(path):,} bytes  {'OK' if ok else 'OFF-SPEC'}")
    return ok


if __name__ == "__main__":
    results = [
        check(make_icon(), (512, 512), 1_000_000, "RGBA"),
        check(make_feature_graphic(), (1024, 500), 15_000_000),
    ]
    sys.exit(0 if all(results) else 1)
