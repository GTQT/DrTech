#!/usr/bin/env bash
set -euo pipefail
cd "/e/模组开发/GTQT/DrTech"

cat >> docs/coprqt-implementation-progress.md <<'EOF'

## P6-6 物品 tooltip / 作物管理机 UI / 基因机改名（2026-11-14）

用户反馈三件事：物品 tooltip 没对齐 GTNH；作物管理机 UI 排版有问题；「基因组收割机」这名字奇怪。

物品 tooltip：
- `ItemCropSeed` 按 `ItemGenericSeed` 的顺序重写显示名与 tooltip：显示名改走
  `cropqt.seed.format`（作物名由 `CropType.getDisplayName` 本地化），空袋/未知/无效分别
  用 `cropqt.seed.empty/unknown/invalid`；tooltip 未分析只显示 `scanToViewStats`，已分析依次
  是三围（`TextFormattingUtil.formatNumbers`）、tier、土壤组、不能杂交/不能蔓延的提示、
  光照与湿度区间、`minSeedBedTier`（`GTValues.VN` + 颜色）。旧的硬编码中文全部删除。
- 五件工具补 `addInformation`：`crop_spade`/`reinforced_crop_spade` 用源端两段文案，
  `plant_lens`/`plant_cure`/`weedex_can` 各一句；分析仪沿用已有的
  `metaitem.crop_analyzer.tooltip.*`（本来就与源端一致）。
- 所有文本走 `gregtech.api.util.LocalizationUtils.format`，客户端用当前语言、服务端退回
  服务端翻译；语言键加在 `en_us.lang`/`zh_cn.lang` 末尾（`cropqt.seed.*`、
  `cropqt_tooltip.genericSeed.*`、`cropqt_tooltip.item.*`、`cropqt.soil.*.tooltip`）。
- `CropType.getDisplayName` 也切到 `LocalizationUtils.format`，中文包下种子名不再显示英文。

作物管理机 UI：
- 旧布局 176×196：输出 5×3 从 x=104 起，第 5 列顶到面板外；玩家背包自动贴底后压住肥料行、
  电池与开关。
- 新布局 176×206，与 GTNH 的 `MTECropManagerGui` 同构：左列 6 槽（WeedEX 2 + 肥料 2×2）、
  中间三条 10×54 液柱、右侧 5×3 输出（x=82..172，槽位只出不进）、开关一排放在液柱下方、
  电池在左列底部；开关用 CropsNH 的 `overlay_button` 贴图并带 enabled/disabled 提示。
- 新增 `client/CropUITextures`，贴图复制到 `assets/drtech/textures/gui/overlay_{button,slot}/`。

基因机改名：8~12 档中文「终极/史诗基因测序仪」，英文 `Ultimate/Epic Gene Sequencer`
（II/III/IV 保留），同步 `drtech_tooltip.cropGeneExtractor.name.*`。

验证：`./gradlew build` 通过；`build/libs/drtech-1.8.9.jar` 6,097,682 B，jar 内可见新
gui 贴图与新语言键。
EOF
EOF_MARK=1
echo done
