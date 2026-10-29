#!/usr/bin/env python3
"""Build the InfuseSMP-S3 Minecraft Java 26.3 resource pack."""
from pathlib import Path
import json, zipfile
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
OUT = ROOT.parent / "target"
ASSETS = ROOT / "assets"
OUT.mkdir(parents=True, exist_ok=True)

EFFECTS = {
    "emerald": (13, 252, 0), "feather": (190, 163, 202),
    "fire": (252, 120, 3), "frost": (122, 227, 252),
    "thunder": (252, 237, 0), "haste": (185, 108, 0),
    "heart": (252, 0, 70), "invisibility": (126, 0, 252),
    "ocean": (0, 90, 252), "regeneration": (252, 0, 221),
    "speed": (209, 164, 75), "strength": (158, 32, 32),
    "ender": (131, 17, 119),
}
BASE = 414001
AUGMENT_OFFSET = 100

def draw_icon(name, rgb, augmented):
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    c = tuple(rgb)
    dark = tuple(max(0, x // 3) for x in c)
    light = tuple(min(255, x + (255-x)//2) for x in c)
    # Crisp pixel-art potion silhouette with a distinct colored liquid.
    d.rectangle((12, 2, 19, 5), fill=(190, 190, 190, 255))
    d.rectangle((11, 5, 20, 8), fill=(115, 125, 135, 255))
    d.rectangle((9, 8, 22, 11), fill=(220, 232, 240, 255))
    d.rectangle((7, 11, 24, 23), fill=(215, 229, 239, 255))
    d.rectangle((9, 23, 22, 27), fill=(175, 194, 208, 255))
    d.rectangle((11, 27, 20, 29), fill=(140, 160, 178, 255))
    d.rectangle((10, 12, 21, 23), fill=(*c, 255))
    d.rectangle((12, 13, 15, 18), fill=(*light, 255))
    d.rectangle((16, 19, 20, 22), fill=(*dark, 255))
    # A tiny effect-specific rune makes each potion recognizable.
    if name in ("heart", "regeneration"):
        d.rectangle((13, 16, 15, 18), fill=(255, 255, 255, 255))
        d.rectangle((17, 16, 19, 18), fill=(255, 255, 255, 255))
        d.rectangle((14, 18, 18, 20), fill=(255, 255, 255, 255))
    elif name in ("thunder", "haste", "speed"):
        d.polygon([(18, 13), (14, 18), (17, 18), (14, 22), (21, 16), (18, 16)], fill=(255, 255, 230, 255))
    elif name in ("frost", "ocean", "feather"):
        d.polygon([(16, 13), (18, 17), (16, 16), (14, 20), (14, 16)], fill=(245, 255, 255, 255))
    elif name == "emerald":
        d.polygon([(16, 13), (20, 16), (18, 21), (14, 21), (12, 16)], fill=(215, 255, 200, 255))
    elif name == "ender":
        d.rectangle((13, 15, 18, 20), fill=(25, 0, 40, 255))
        d.rectangle((15, 16, 18, 18), fill=(220, 110, 255, 255))
    elif name == "invisibility":
        d.rectangle((13, 15, 18, 20), fill=(245, 245, 255, 255))
        d.rectangle((15, 17, 19, 21), fill=(*c, 255))
    elif name == "fire":
        d.polygon([(16, 13), (19, 17), (18, 20), (15, 22), (13, 19), (14, 16)], fill=(255, 245, 150, 255))
    elif name == "strength":
        d.rectangle((13, 15, 19, 20), fill=(255, 210, 170, 255))
        d.rectangle((15, 13, 17, 22), fill=(255, 210, 170, 255))
    if augmented:
        gold = (255, 205, 55, 255)
        d.rectangle((4, 4, 7, 7), fill=gold)
        d.rectangle((24, 4, 27, 7), fill=gold)
        d.rectangle((4, 24, 7, 27), fill=gold)
        d.rectangle((24, 24, 27, 27), fill=gold)
        d.rectangle((8, 30, 23, 31), fill=gold)
    return im

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

# One item definition dispatches all legacy integer CustomModelData values to
# the modern 1.21.4+ model system used by Java 26.3.
entries = []
for i, (name, color) in enumerate(EFFECTS.items(), start=1):
    for augmented, value in ((False, BASE + i - 1), (True, BASE + i - 1 + AUGMENT_OFFSET)):
        model_id = f"infusesmp:item/{name}" + ("_augmented" if augmented else "")
        entries.append({"threshold": value, "model": {"type": "minecraft:model", "model": model_id}})
        model_path = ASSETS / "infusesmp" / "models" / "item" / (name + ("_augmented" if augmented else "") + ".json")
        write_json(model_path, {"parent": "minecraft:item/generated", "textures": {"layer0": f"infusesmp:item/{name}" + ("_augmented" if augmented else "")}})
        texture_path = ASSETS / "infusesmp" / "textures" / "item" / (name + ("_augmented" if augmented else "") + ".png")
        texture_path.parent.mkdir(parents=True, exist_ok=True)
        draw_icon(name, color, augmented).save(texture_path)

write_json(ASSETS / "minecraft" / "items" / "potion.json", {
    "model": {
        "type": "minecraft:range_dispatch",
        "property": "minecraft:custom_model_data",
        "index": 0,
        "fallback": {"type": "minecraft:model", "model": "minecraft:item/potion"},
        "entries": entries,
    }
})
pack_meta = {
    "pack": {
        "description": {"text": "InfuseSMP-S3 • Java 26.3 • 13 effects + augmented variants"},
        "min_format": [97, 1],
        "max_format": [97, 1]
    }
}
write_json(ROOT / "pack.mcmeta", pack_meta)
draw_icon("emerald", EFFECTS["emerald"], True).resize((64, 64), Image.Resampling.NEAREST).save(ROOT / "pack.png")

zip_path = OUT / "InfuseSMP-S3-ResourcePack-26.3.zip"
with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as archive:
    archive.write(ROOT / "pack.mcmeta", "pack.mcmeta")
    archive.write(ROOT / "pack.png", "pack.png")
    for path in ASSETS.rglob("*"):
        if path.is_file():
            archive.write(path, path.relative_to(ROOT).as_posix())
print(f"Created {zip_path} ({zip_path.stat().st_size} bytes)")
