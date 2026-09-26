"""Turns raw emulator screenshots into Play Store screenshot sets.

    python store-listing/make_screenshots.py

Input
    store-listing/raw/*.png           phone captures (any size, portrait)
    store-listing/raw/tablet/*.png    tablet captures (any size)

Output
    store-listing/phone/NN-name.png   exactly 1080x1920 (9:16)
    store-listing/tablet/NN-name.png  exactly 1920x1080 or 1080x1920

Each output has a caption bar at the top in the brand colours and the
screenshot scaled to fit (never stretched), letterboxed on the khaki ground
with the app's rounded-corner, ink-outlined card look. Captions come from the
file name (see CAPTIONS): name your raw files with those keywords, e.g.
01-lockscreen.png, 02-lockscreen-blur.png, 03-list.png, 04-settings.png.

The script prints dimensions and size for every output and exits non-zero
if anything is off-spec or over 8 MB.
"""

from __future__ import annotations

import os
import sys
import textwrap

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.join(HERE, "raw")
RAW_TABLET = os.path.join(RAW, "tablet")
OUT_PHONE = os.path.join(HERE, "phone")
OUT_TABLET = os.path.join(HERE, "tablet")

MAX_BYTES = 8 * 1024 * 1024

# Brand colours, identical to ui/theme/Color.kt.
KHAKI = (0xF3, 0xE2, 0xB3)
INK = (0x3A, 0x33, 0x2E)

# First keyword found in the file name (lower-cased) wins. Order matters:
# "blur" must be checked before "lock".
CAPTIONS = [
    ("blur", "Blur mode: only the count shows until you unlock"),
    ("lock", "Your to-dos, right on the lock screen"),
    ("edit", "Edit a task in place"),
    ("settings", "Two switches. That's the whole settings screen"),
    ("empty", "All clear. Shiba's keeping watch"),
    ("list", "Add, tick off and tidy up inside the app"),
]


def caption_for(name: str) -> str:
    stem = os.path.splitext(name)[0].lower()
    for key, caption in CAPTIONS:
        if key in stem:
            return caption
    # Fall back to the file name without its number prefix.
    words = stem.lstrip("0123456789-_ ").replace("-", " ").replace("_", " ")
    return words[:1].upper() + words[1:]


def font(size: int):
    for candidate in ("seguisb.ttf", "segoeui.ttf", "arialbd.ttf"):
        p = os.path.join(r"C:\Windows\Fonts", candidate)
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return ImageFont.load_default(size)


def compose(src: Image.Image, canvas: tuple[int, int], caption: str) -> Image.Image:
    W, H = canvas
    landscape = W > H
    bar = 150 if landscape else 230
    margin = 48 if landscape else 60
    img = Image.new("RGB", (W, H), KHAKI)
    draw = ImageDraw.Draw(img)

    # Caption, wrapped to at most two lines, centred in the bar.
    size = 44 if landscape else 54
    f = font(size)
    lines = textwrap.wrap(caption, width=34 if landscape else 30)[:2]
    total = sum(draw.textbbox((0, 0), ln, font=f)[3] for ln in lines) + 8 * (len(lines) - 1)
    y = (bar - total) // 2
    for ln in lines:
        box = draw.textbbox((0, 0), ln, font=f)
        draw.text(((W - box[2]) // 2, y), ln, font=f, fill=INK)
        y += box[3] + 8

    # Screenshot scaled to fit the remaining area, never stretched.
    avail_w, avail_h = W - 2 * margin, H - bar - margin
    scale = min(avail_w / src.width, avail_h / src.height)
    sw, sh = round(src.width * scale), round(src.height * scale)
    shot = src.convert("RGB").resize((sw, sh), Image.LANCZOS)

    # Rounded corners and a thin ink outline, like the app's cards. Not a device frame.
    radius = 36
    mask = Image.new("L", (sw, sh), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, sw - 1, sh - 1), radius=radius, fill=255)
    x = (W - sw) // 2
    y = bar + (avail_h - sh) // 2
    img.paste(shot, (x, y), mask)
    draw.rounded_rectangle((x, y, x + sw - 1, y + sh - 1), radius=radius, outline=INK, width=4)
    return img


def process(folder: str, out_dir: str, choose_canvas) -> list[bool]:
    if not os.path.isdir(folder):
        print(f"(no {os.path.relpath(folder, HERE)} folder, skipping)")
        return []
    files = sorted(f for f in os.listdir(folder) if f.lower().endswith(".png"))
    if not files:
        print(f"(no PNGs in {os.path.relpath(folder, HERE)}, skipping)")
        return []
    os.makedirs(out_dir, exist_ok=True)
    results = []
    for n, name in enumerate(files, start=1):
        with Image.open(os.path.join(folder, name)) as src:
            canvas = choose_canvas(src)
            img = compose(src, canvas, caption_for(name))
        stem = os.path.splitext(name)[0].lstrip("0123456789-_ ") or "shot"
        out = os.path.join(out_dir, f"{n:02d}-{stem}.png")
        img.save(out, "PNG", optimize=True)
        with Image.open(out) as check:
            size_ok = check.size == canvas
        bytes_ = os.path.getsize(out)
        ok = size_ok and bytes_ <= MAX_BYTES
        print(f"{os.path.relpath(out, HERE)}: {img.width}x{img.height} {bytes_:,} bytes  {'OK' if ok else 'OFF-SPEC'}")
        results.append(ok)
    return results


if __name__ == "__main__":
    phone = process(RAW, OUT_PHONE, lambda src: (1080, 1920))
    tablet = process(RAW_TABLET, OUT_TABLET,
                     lambda src: (1920, 1080) if src.width >= src.height else (1080, 1920))
    results = phone + tablet
    if not results:
        print("Nothing to do. Put phone captures in store-listing/raw/ and tablet captures in store-listing/raw/tablet/.")
        sys.exit(1)
    if len(phone) < 4:
        print(f"WARNING: {len(phone)} phone screenshots; Play needs at least 4 for promotion eligibility.")
    sys.exit(0 if all(results) else 1)
