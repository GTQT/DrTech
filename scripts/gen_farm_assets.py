import json

BASE = "src/main/resources/assets/drtech"
TYPES = {
    "environmental_enhancement": ("Environmental Enhancement Unit", "环境强化单元"),
    "growth_acceleration": ("Growth Acceleration Unit", "生长加速单元"),
    "fertilizer": ("Fertilizer Unit", "肥料单元"),
    "advanced_harvesting": ("Advanced Harvesting Unit", "高级收割单元"),
    "overclocked_growth_acceleration": ("Overclocked Growth Acceleration Unit", "超频生长加速单元"),
}
VARIANTS = [
    ("mv", "MV"), ("hv", "HV"), ("ev", "EV"), ("iv", "IV"),
    ("luv", "LuV"), ("zpm", "ZPM"), ("uv", "UV"), ("uhv", "UHV"),
    ("uev", "UEV"), ("uiv", "UIV"), ("uxv", "UXV"), ("opv", "OpV"),
]


def write_blockstates():
    for block, _ in TYPES.items():
        variants = {}
        for index, (variant, _) in enumerate(VARIANTS):
            variants[variant] = {
                "textures": {
                    "all": "drtech:blocks/industrial_farm/%s_%d" % (block, index + 2)
                }
            }
        data = {
            "forge_marker": 1,
            "defaults": {"model": "minecraft:cube_all"},
            "variants": {"variant": variants},
        }
        path = "%s/blockstates/%s.json" % (BASE, block)
        with open(path, "w", encoding="utf-8") as handle:
            json.dump(data, handle, indent=2)
            handle.write("\n")


def append_missing(path, entries):
    with open(path, "r", encoding="utf-8") as handle:
        text = handle.read()
    fresh = [line for line in entries if line.split("=", 1)[0] not in text]
    if not fresh:
        return
    if not text.endswith("\n"):
        text += "\n"
    text += "\n".join(fresh) + "\n"
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text)


def write_lang():
    for block, (english, chinese) in TYPES.items():
        en = [
            "tile.drtech.%s.%s.name=%s (%s)" % (block, variant, english, label)
            for variant, label in VARIANTS
        ]
        zh = [
            "tile.drtech.%s.%s.name=%s（%s）" % (block, variant, chinese, label)
            for variant, label in VARIANTS
        ]
        append_missing("%s/lang/en_us.lang" % BASE, en)
        append_missing("%s/lang/zh_cn.lang" % BASE, zh)

    # 种子床也补上 OpV 一档
    append_missing(
        "%s/lang/en_us.lang" % BASE,
        ["tile.drtech.seed_bed.opv.name=Seed Bed (OpV)"],
    )
    append_missing(
        "%s/lang/zh_cn.lang" % BASE,
        ["tile.drtech.seed_bed.opv.name=种子床（OpV）"],
    )


write_blockstates()
write_lang()
print("generated")
