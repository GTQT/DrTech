#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate docs/cropsnh-crop-table.md from build/crop_rows.json."""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ROWS = json.load(open(os.path.join(ROOT, "build", "crop_rows.json"), encoding="utf-8"))

VOLTAGE = {0: "ULV", 1: "LV", 2: "MV", 3: "HV", 4: "EV", 5: "IV", 6: "LuV",
           7: "ZPM", 8: "UV", 9: "UHV", 10: "UEV", 11: "UIV", 12: "UMV",
           13: "UXV", 14: "MAX"}

CAT_ORDER = [
    "盆景树", "BoP", "Natura·地狱菇", "Natura·地狱莓", "Natura·地狱",
    "食物·BoP", "食物·Natura", "食物·原版", "食物",
    "材料", "矿石莓", "神秘时代", "暮色森林", "怪物",
    "石百合", "石百合·Modern", "石百合·EtFuturum",
    "原版·花", "原版·蘑菇", "原版", "巫术", "匠魂", "特殊",
]

# 联动模组分类：不接入（仅保留在表里做参考，不勾选、不写进项目）
INTEROP_CATS = {
    "BoP", "食物·BoP", "Natura·地狱菇", "Natura·地狱莓", "Natura·地狱",
    "食物·Natura", "神秘时代", "暮色森林", "石百合·Modern",
    "石百合·EtFuturum", "巫术", "匠魂",
}

CAT_NOTE = {
    "盆景树": "原版树木；`bonsaiSlimy` 已移入匠魂组",
    "BoP": "联动 · 不接入（需 BiomesOPlenty）",
    "Natura·地狱菇": "联动 · 不接入（需 Natura）",
    "Natura·地狱莓": "联动 · 不接入（需 Natura）",
    "Natura·地狱": "联动 · 不接入（需 Natura）",
    "食物·BoP": "联动 · 不接入（需 BiomesOPlenty）",
    "食物·Natura": "联动 · 不接入（需 Natura）",
    "神秘时代": "联动 · 不接入（需 Thaumcraft）",
    "暮色森林": "联动 · 不接入（需 Twilight Forest）",
    "石百合·Modern": "联动 · 不接入（需 Botania / Chisel / EtFuturum 的石块）",
    "石百合·EtFuturum": "联动 · 不接入（需 EtFuturum + NewHorizonsCoreMod）",
    "巫术": "联动 · 不接入（需 Witchery）",
    "匠魂": "联动 · 不接入（需 TinkerConstruct）",
}

# 推荐测试集：全部为本体作物，覆盖机制分支；联动作物一律不选
SUGGEST = [
    ("wheat", "基线：tier1 / 1000t / 0~7 阶段 / 光照≥9 / 替代种子"),
    ("carrot", "食物通用：tier2 / 800t / 替代种子"),
    ("sugarCane", "X 渲染 + `sugarcane` 复合土壤（沙+土）"),
    ("cactus", "`sand` 土壤 + 实体伤害 1.0 + 阶段 1~3"),
    ("netherwart", "`soulsand` 土壤 + 阶段 0~2"),
    ("redMushroom", "`mushroom` 复合土壤（石+土+菌丝）"),
    ("blueOrchid", "花类：X 渲染 + `flower` 种子形状 + `dirtGrass`"),
    ("stone", "石百合：`stone` 土壤 + 子土壤=stone"),
    ("olivia", "MV 机器 / 子土壤=橄榄石 / 2 掉落 / 2 催化剂"),
    ("starWart", "高压：tier12 / 机器EV / 种子床EV / MachineOnly / 子土壤=下界之星"),
    ("spaceFlower", "tier13 / 机器IV / 种子床IV / 条件子土壤=space / 附魔种子"),
    ("redstraw", "光照窗口 Min5~Max12 + 子土壤=红石"),
    ("magicalNightshade", "非整时长 23436t / tier13 / HV 机器+种子床"),
    ("godOfThunder", "HV 机器+HV 种子床 / 子土壤=钍 / MachineOnly"),
    ("oilBerry", "`oil` 四组分复合土壤（沙+砾石+灵魂沙+油砂）"),
    ("zomplant", "`graveyard` 土壤（怪物系）"),
    ("trollplant", "`brick` 土壤 + 4 掉落 + 3 催化剂"),
    ("bonsaiOak", "盆景：1600t / 3 掉落 / 喜好群系 / 替代种子"),
    ("weed", "杂草机制（生成/扩散/清除）"),
    ("enderbloom", "末地系：子土壤=endStone"),
]

TICK = {cid for cid, _ in SUGGEST}


def vname(v):
    if v is None or v < 0:
        return "—"
    return VOLTAGE.get(v, str(v))


def esc(s):
    return (s or "").replace("|", "/")


MOD_NAMES = {
    "TinkerConstruct": "TiC", "BiomesOPlenty": "BoP", "Natura": "Natura",
    "TwilightForest": "TF", "Thaumcraft": "Thaumcraft",
    "ThaumicBases": "ThaumicBases", "Witchery": "Witchery",
    "EtFuturumRequiem": "EtFuturum", "NewHorizonsCoreMod": "NHCore",
    "Botania": "Botania", "Chisel": "Chisel", "Galacticraft": "Galacticraft",
}


def note_of(r):
    n = r["notes"]
    mods = r.get("mods") or []
    if mods:
        tag = "联动:" + "/".join(MOD_NAMES.get(m, m) for m in mods)
        n = (n + "; " + tag) if n else tag
    return esc(n)


def main():
    by_id = {}
    order = []
    for i, r in enumerate(sorted(ROWS, key=lambda x: x["order"])):
        r["no"] = "R%03d" % (i + 1)
        by_id[r["id"]] = r
        order.append(r)

    for r in order:
        r["interop"] = r["cat"] in INTEROP_CATS or bool(r.get("mods"))
    body = [r for r in order if not r["interop"]]
    interop = [r for r in order if r["interop"]]

    lines = []
    A = lines.append
    A("# CropsNH 作物表（静态提取 · 本体作物，供勾选做测试用例）")
    A("")
    A("> 数据来源：`E:\\模组开发\\资料库\\CropsNH`（LGPL-3.0），`crops/**` 类实现 + `loaders/CropLoader.java` 注册链。")
    A("> 提取方式：静态解析（未编译、未启动游戏），默认值按 `NHCropCard` / `CropCard` 继承链回填。")
    A("> 共 **%d** 条注册：**%d 条本体作物**（可接入）+ **%d 条联动作物**（标注“联动”，只作参考，不接入、不勾选）。"
      % (len(order), len(body), len(interop)))
    A("")
    A("## 怎么用")
    A("")
    A("1. 勾选只针对“范围=本体”的行；联动行保留在表里仅供查数，不会写进项目。")
    A("2. 在“勾选”列把 `[ ]` 改成 `[x]`（或直接回复编号，如 `R012 R044 R101`）。")
    A("3. 勾选后我把这些作物写成 cropQT 的测试用例（只补 `CropType`/注册数据，不改机器代码）。")
    A("4. 推荐测试集已全部避开联动作物，覆盖土壤/子土壤/光照/电压/种子床/渲染/杂草等机制分支。")
    A("")
    A("## 列说明与默认值")
    A("")
    A("| 列 | 含义 | 默认值 |")
    A("| --- | --- | --- |")
    A("| 范围 | 本体 = 可独立接入；联动 = 依赖其他模组，不接入 | — |")
    A("| Tier | 作物 tier（生长难度档，非电压） | 1 |")
    A("| 时长 | `getGrowthDuration()`，单位 tick | `600 × tier` |")
    A("| 阶段 | 可见生长阶段 min~max | 1~4 |")
    A("| 渲染 | `PlantRenderShape` | `Hash` |")
    A("| 土壤 | 主土壤组（`CropsNHSoilTypes`） | `farmland` |")
    A("| 子土壤 | 必须放在下方的方块组（`addSubSoilRequirement`） | 无 |")
    A("| 机器 | 杂交机器配方最低电压 | `LV` |")
    A("| 种子床 | 工业农场种子床最低电压 | 不限 |")
    A("| 掉落 | `addDrop` 条目数（类内+注册链） | — |")
    A("| 备注 | 生长需求 / 喜好群系 / 伤害 / 替代种子 / 催化剂 / NEI | — |")
    A("")
    A("土壤组：`farmland` 耕地、`sand` 沙、`soulsand` 灵魂沙、`dirtGrass` 土/草、`mycelium` 菌丝、")
    A("`end` 末地石、`stone` 石、`netherrack` 地狱岩、`brick` 砖、`thaumLogs` 神秘原木、")
    A("`graveyard` 墓地、`slimy` 粘液、`gravel` 砾石、`oilSands` 油砂；复合组：`mushroom`、")
    A("`netherMushroom`、`sugarcane`、`slimyDirt`、`oil`。")
    A("")
    A("## 推荐测试集（20 条，全部本体作物）")
    A("")
    A("| 勾选 | 编号 | ID | 机制覆盖 |")
    A("| --- | --- | --- | --- |")
    for cid, why in SUGGEST:
        r = by_id.get(cid)
        no = r["no"] if r else "?"
        mark = "x" if r else " "
        A("| [%s] | %s | `%s` | %s |" % (mark, no, cid, esc(why)))
    A("")
    A("## 全量作物表")
    A("")

    grouped = {}
    for r in order:
        grouped.setdefault(r["cat"], []).append(r)

    for cat in CAT_ORDER:
        rows = grouped.get(cat)
        if not rows:
            continue
        rows.sort(key=lambda x: (x["tier"] if x["tier"] is not None else 99, x["order"]))
        A("### %s（%d）" % (cat, len(rows)))
        if cat in CAT_NOTE:
            A("")
            A("> " + CAT_NOTE[cat])
        A("")
        A("| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |")
        A("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
        for r in rows:
            mark = "x" if r["id"] in TICK else " "
            scope = "联动" if r["interop"] else "本体"
            A("| [%s] | %s | %s | `%s` | %s | %s | %s~%s | %s | %s | %s | %s | %s | %s | %s |" % (
                mark, r["no"], scope, r["id"], r["tier"], r["dur"], r["min_stage"], r["max_stage"],
                r["render"], r["soil"], "/".join(r["subsoil"]) or "—",
                vname(r["machine"]), vname(r["minbed"]), r["drops"], note_of(r)))
        A("")

    cat_only = [r for r in interop if r["cat"] in INTEROP_CATS]
    gated = [r for r in interop if r.get("mods")]
    A("## 附：不接入的联动作物（并集 %d 条，仅备查）" % len(interop))
    A("")
    A("> 判定规则（满足其一即联动）：① 整组属于其他模组的联动作物（%d 条）；② 注册调用被 `ModUtils.<模组>.isModLoaded()` 包裹（%d 条）。两表有重叠。" % (len(cat_only), len(gated)))
    A("")
    A("### A. 整组联动（%d 条）" % len(cat_only))
    A("")
    for cat in CAT_ORDER:
        ids = [r["id"] for r in interop if r["cat"] == cat and r["cat"] in INTEROP_CATS]
        if ids:
            A("- **%s**：`%s`" % (cat, "`、`".join(ids)))
    A("")
    A("### B. 注册被模组条件包裹（%d 条）" % len(gated))
    A("")
    for mod in sorted({m for r in gated for m in r["mods"]}):
        ids = [r["id"] for r in gated if mod in r["mods"]]
        A("- **%s**：`%s`" % (MOD_NAMES.get(mod, mod), "`、`".join(ids)))
    A("")
    A("## 附：按机制挑选的索引（仅本体）")
    A("")
    A("- **子土壤**：`" + "`, `".join(sorted({s for r in body for s in r["subsoil"]})) + "`")
    A("- **非 farmland 土壤**：" + "、".join(sorted({r["soil"] for r in body if r["soil"] != "farmland"})))
    A("- **机器 > LV**：" + "、".join(r["id"] for r in body if r["machine"] > 1))
    A("- **种子床受限**：" + "、".join(r["id"] for r in body if r["minbed"] >= 0))
    A("- **X 渲染**：" + "、".join(r["id"] for r in body if r["render"] == "X"))
    A("- **有实体伤害**：" + "、".join(r["id"] for r in body if "伤害" in r["notes"]))
    A("- **NEI 隐藏**：" + "、".join(r["id"] for r in body if "NEI隐藏" in r["notes"]))
    A("")

    out = os.path.join(ROOT, "docs", "cropsnh-crop-table.md")
    with open(out, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(out, len(lines), "lines")


if __name__ == "__main__":
    main()
