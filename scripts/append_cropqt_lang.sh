#!/usr/bin/env bash
set -euo pipefail
cd "/e/模组开发/GTQT/DrTech"

EN=src/main/resources/assets/drtech/lang/en_us.lang
ZH=src/main/resources/assets/drtech/lang/zh_cn.lang

cat >> "$EN" <<'EOF'

# --- CropQT: seeds / items / soil / crop manager toggles ---
cropqt.seed.format=%1$s Seeds
cropqt.seed.empty=Empty Seed Bag
cropqt.seed.unknown=Unknown Seeds
cropqt.seed.invalid=Invalid Seeds
cropqt_tooltip.genericSeed.scanToViewStats=§7Scan to view stats§r
cropqt_tooltip.genericSeed.growth=§a- Growth: %1$s§r
cropqt_tooltip.genericSeed.gain=§a- Gain: %1$s§r
cropqt_tooltip.genericSeed.resistance=§a- Resistance: %1$s§r
cropqt_tooltip.genericSeed.tier=Tier: %1$s
cropqt_tooltip.genericSeed.minSeedBedTier=Minimum Seed Bed tier: %1$s
cropqt_tooltip.genericSeed.cannotBreedOnSticks=This crop cannot breed with other crops using crop sticks
cropqt_tooltip.genericSeed.cannotSpreadToOtherSticks.0=This crop cannot spread itself to nearby crop sticks
cropqt_tooltip.genericSeed.cannotSpreadToOtherSticks.1=You need a crop synthesizer to improve or duplicate it
cropqt_tooltip.genericSeed.light=§7- Light: %1$s§r
cropqt_tooltip.genericSeed.humidity=§7- Humidity: %1$s§r
cropqt_tooltip.item.plantLens=Inspects crops
cropqt_tooltip.item.spade=Removes weeds and drops seeds when harvesting mature crops
cropqt_tooltip.item.reinforcedSpade=Removes weeds and drops more seeds when harvesting mature crops
cropqt_tooltip.item.plantCure=§7Cures diseased crops§r
cropqt_tooltip.item.weedEX=§7Applies Weed-EX to crop sticks§r
cropqt.soil.global.tooltip=Grows on any soil
cropqt.soil.farmland.tooltip=Grows on farmland
cropqt.soil.dirt_grass.tooltip=Grows on dirt or grass
cropqt.soil.stone.tooltip=Grows on stone
cropqt.soil.sand.tooltip=Grows on sand
cropqt.soil.mushroom.tooltip=Grows on mycelium
cropqt.soil.netherrack.tooltip=Grows on netherrack
cropqt.soil.end.tooltip=Grows on endstone
cropqt.soil.gravel.tooltip=Grows on gravel
cropqt.soil.soul_sand.tooltip=Grows on soul sand
cropqt.soil.brick.tooltip=Grows on bricks
cropqt.soil.water.tooltip=Grows in water
cropqt.soil.oil_sands.tooltip=Grows on oil sands
cropqt.soil.mushroom_all.tooltip=Grows on stone, dirt or mycelium
cropqt.soil.sugarcane.tooltip=Grows on sand or dirt
cropqt.soil.oil.tooltip=Grows on sand, gravel, soul sand or oil sands
drtech_tooltip.cropManager.toggle.water=Toggle auto-watering
drtech_tooltip.cropManager.toggle.weedEX=Toggle auto Weed-EX
drtech_tooltip.cropManager.toggle.fertilizer=Toggle auto-fertilizing
drtech_tooltip.cropManager.toggle.harvest=Toggle auto-harvesting
drtech_tooltip.cropManager.toggle.enabled=Enabled
drtech_tooltip.cropManager.toggle.disabled=Disabled
EOF

cat >> "$ZH" <<'EOF'

# --- CropQT：种子 / 物品 / 土壤 / 作物管理机开关 ---
cropqt.seed.format=%1$s种子
cropqt.seed.empty=空种子袋
cropqt.seed.unknown=未知种子
cropqt.seed.invalid=无效种子
cropqt_tooltip.genericSeed.scanToViewStats=§7扫描以查看属性§r
cropqt_tooltip.genericSeed.growth=§a- 生长：%1$s§r
cropqt_tooltip.genericSeed.gain=§a- 产量：%1$s§r
cropqt_tooltip.genericSeed.resistance=§a- 抗性：%1$s§r
cropqt_tooltip.genericSeed.tier=等级：%1$s
cropqt_tooltip.genericSeed.minSeedBedTier=最低苗床档次：%1$s
cropqt_tooltip.genericSeed.cannotBreedOnSticks=该作物无法通过作物架与其他作物杂交
cropqt_tooltip.genericSeed.cannotSpreadToOtherSticks.0=该作物无法自行蔓延到附近的作物架
cropqt_tooltip.genericSeed.cannotSpreadToOtherSticks.1=需要作物合成机才能改良或复制它
cropqt_tooltip.genericSeed.light=§7- 光照：%1$s§r
cropqt_tooltip.genericSeed.humidity=§7- 湿度：%1$s§r
cropqt_tooltip.item.plantLens=用于检查作物状态
cropqt_tooltip.item.spade=铲除杂草，并在收获成熟作物时掉落种子
cropqt_tooltip.item.reinforcedSpade=铲除杂草，并在收获成熟作物时掉落更多种子
cropqt_tooltip.item.plantCure=§7治疗患病作物§r
cropqt_tooltip.item.weedEX=§7对作物架使用 Weed-EX§r
cropqt.soil.global.tooltip=可在任意土壤上生长
cropqt.soil.farmland.tooltip=可在耕地上生长
cropqt.soil.dirt_grass.tooltip=可在泥土或草方块上生长
cropqt.soil.stone.tooltip=可在石头上生长
cropqt.soil.sand.tooltip=可在沙子上生长
cropqt.soil.mushroom.tooltip=可在菌丝上生长
cropqt.soil.netherrack.tooltip=可在下界岩上生长
cropqt.soil.end.tooltip=可在末地石上生长
cropqt.soil.gravel.tooltip=可在沙砾上生长
cropqt.soil.soul_sand.tooltip=可在灵魂沙上生长
cropqt.soil.brick.tooltip=可在砖块上生长
cropqt.soil.water.tooltip=可在水体中生长
cropqt.soil.oil_sands.tooltip=可在油砂上生长
cropqt.soil.mushroom_all.tooltip=可在石头、泥土或菌丝上生长
cropqt.soil.sugarcane.tooltip=可在沙子或泥土上生长
cropqt.soil.oil.tooltip=可在沙子、沙砾、灵魂沙或油砂上生长
drtech_tooltip.cropManager.toggle.water=自动浇水开关
drtech_tooltip.cropManager.toggle.weedEX=自动 Weed-EX 开关
drtech_tooltip.cropManager.toggle.fertilizer=自动施肥开关
drtech_tooltip.cropManager.toggle.harvest=自动收获开关
drtech_tooltip.cropManager.toggle.enabled=已启用
drtech_tooltip.cropManager.toggle.disabled=已禁用
EOF

grep -c "cropqt_tooltip" "$EN" "$ZH"
