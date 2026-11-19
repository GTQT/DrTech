# coprQT × CropsNH 复刻实施进度

> 对应方案：[coprqt-cropsnh-full-parity-plan.md](coprqt-cropsnh-full-parity-plan.md)
> 约束：不编译、不启动游戏；每轮至少完成一个节点。
> 最近更新：2026-11-14

---

## 已完成

### P0-1 电压档位映射

- 新增 `src/main/java/com/meowmel/cropQT/api/VoltageMap.java`
- 内容：CropsNH → GTQT 的索引恒等映射（`UMV`→`UXV`、`UXV`→`OpV`），
  以及三组档位范围常量：
  - 单方块 `LV..UXV`（索引 1..12）
  - 基因机器 `EV..UXV`（4..12）
  - 农场/种子床/组件 `MV..OpV`（2..13），OpV 不可用时回退 `UXV`
- 用途：后续 300+ 个 MTE 注册循环统一从这里取范围，杜绝散落 `VoltageIndex.UMV`。

### P0-2 作物机器罐容量

- 新增 `src/main/java/com/meowmel/cropQT/api/CropMachineTankCapacity.java`
- 按 GT5U `MTEBasicMachine.getCapacityForTier` 重写：
  `8000 / 16000 / 32000 / 64000 / 128000 / 256000`，与 CEU 的
  `GTUtility.defaultTankSizeFunction`（HV 起分叉）区分。
- 三个缩放入口：
  - `forSeedGenerator(tier)`：`tier < IV ? base/10 : base`
  - `forBreeder(tier)`：`tier < IV ? base/5 : base`
  - `forSynthesizer(tier)`：不缩放
- 已接入：
  - `MetaTileEntitySeedGenerator`（替换写死的 16000）
  - `MetaTileEntityCropBreeder`（替换写死的 16000）
  - `MetaTileEntityCropSynthesizer`（替换写死的 16000）
  - `MetaTileEntityCropGeneExtractor` 无流体罐，不适用。

### P0-3 Potency 模型

- 新增：
  - `api/potency/IPotencyData.java` —— 点数 / 存储上限 / 每次消耗单位数，`compareTo` 按三元组比较。
  - `api/potency/PotencyData.java` —— 不可变实现，三个构造器（1/2/3 参）。
- 重写 `api/registries/PotencyRegistry.java`：
  - 值从 `int` 改为 `IPotencyData`；
  - `register` 校验 `potency > 0`、`units > 0`，`putIfAbsent` 语义（与源端一致）；
  - 提供 `get`（可空）、`getPotency`（0 回退）、`remove`。
- 重写 `api/registries/FertilizerRegistry.java`：
  - 普通液肥 `1/mB`、浓缩液肥 `10/mB`（`PotencyData(1)` / `(10)`）；
  - FU 组件专用表 `FERTILIZER_UNIT_FLUIDS`：浓缩液肥按 `1/mB`（不吃浓度加成）；
  - GT 肥料 `100/个`；骨粉 `(5, 30)`（点数 5、存储上限 30）。
- 重写 `api/registries/HydrationRegistry.java`：
  - 原版水 `1/mB`、蒸馏水 `2/mB`；保留 `getWater` / `isKnown` 兼容签名。
- 新增 `api/registries/WeedEXRegistry.java`：
  - 默认空表；若存在名为 `weedex` 的流体则按 `1/mB` 自动登记；
  - WeedEX 罐仍走物品耐久路径（每点耐久 = 10 点），不依赖流体。
- `CropInitHandler.init()` 新增 `WeedEXRegistry.registerDefaults()`。
- `CropBreederRecipeLogic.BASELINE_POTENCY` 改为引用
  `FertilizerRegistry.ENRICHED_FERTILIZER_FLUID_POTENCY`（10），与注册表统一。

### P0-4 CropType 源端字段（部分）

- `api/CropType.java` 新增 6 个字段 + getter + Builder setter：
  - `minSeedBedTier`（默认 -1 → 沿用 tier）：种子床可种的最低档
  - `machineBreedingRecipeTier`（默认 -1 → 沿用 tier）：育种机/基因机所需档
  - `dropChance`（默认 1.0）：平均收获轮数基数
  - `spreadsWeeds`（默认 true）：是否蔓延杂草
  - `duplicationCatalysts`（默认空）：种子机复制催化剂，`addDuplicationCatalystLate` 供 init 阶段追加
  - `hideFromNEI`（默认 false）：JEI 隐藏
- `machineOnlyGrowthRequirement` 暂未加：cropQT 没有 `IMachineGrowthRequirement` 接口，
  其签名依赖 `ISeedData`/`IGregTechTileEntity`，等 P4 农场重构时按实际调用点定义，避免先造错 API。
- 现有 38 个作物未改动，全部走默认值，行为不变。

### P0-5 常量集中 / 杂交字段 / 署名

- 新增 `api/CropConstants.java`：作物架、CropManager、四台单方块、工业农场的源端常量，
  每项注释标来源类；`FarmNutrientModel`/`FarmType`/`BlockSeedBed` 暂时保留自有副本，
  P1/P4 重构时统一改为引用本类。
- `api/mutation/CropMutation.java` 扩展：
  - `breedingMachineEUt`（默认 30 = LV）、`breedingMachineDuration`（默认 400）
  - `setBreedingMachineRecipeTier(int tier)`：`VA[clamp(tier, LV, UXV)]`，时长 `400 * 1.3^(tier-1)`
  - `setBreedingMachineRecipeEUt/Duration`、`getBreedingMachineRecipeEUt/Duration`
  - `machineOnly()` / `isMachineOnly()`：只允许机器触发的变异
  - 未加 `requirements`/NEI 列表：等 P4 农场与 JEI 重写时按真实调用点定义
- 新增 `docs/THIRD_PARTY.md`：记录 CropsNH（LGPL-3.0）的文件对应关系、
  资源来源与 GPL-3.0 兼容性说明。

---

## 状态与影响

- 现有调用点继续使用 `getFertilizer(Fluid)` / `getFertilizer(ItemStack)` /
  `getWater(Fluid)` 的 `int` 重载，签名未断；数值语义从「旧的自定义点数」
  切到源端 Potency（普通液肥 1、浓缩 10、GT 肥料 100、骨粉 5/30）。
- 繁殖机临时公式 `base * 10 / potency` 仍按旧结构折算 mB，P3 重写时会改成
  源端「直接扣 mB + mutation 自带时长/EU」。
- 种子机的 `ceil(100 / potency)` 同理，P3 会改成 `(gr+ga+re) * 36` mB。
- 未做任何编译/启动；文件级依赖已按 import 核对。

## P1-1 作物架存储三件套 + 生长循环（2026-11-14 完成）

重写 `tile/TileCropStick.java`（~640 行），对齐源端 `TileEntityCropSticks`：

- 三件套：`addWater/addFertilizer/addWeedEx(amount, threshold, maxStorage, simulate)`，
  阈值判定 `storage > threshold` 拒绝，否则 `min(maxStorage, storage+amount)`；
  旧 `int addWater(int)` / `addFertilizer(int)` 保留为便捷重载（返回实际加入量）。
- 生长：单条 `growthProgress` 对 `CropType.getGrowthDuration()`，每 256 tick 一次；
  `onGrowthTick` 顺序照抄源端（canGrow → 雨 → 病 → doGrowth → 杂草 → 空架杂交/生草 →
  末尾水/肥各 -1）。
- 公式：`getNutrientsPerCycle` 与 `getGrowthRate` 逐行照抄（含 `NUTRIENT_POINT_SCALE=5`、
  `NUTRIENTS_NEEDED_PER_TIER=10`）；营养分取 `BiomeDictionary` 喜好交集、湿度、见天、储量。
- 杂草/病害/世界杂交：`spawnWeed`/`spreadWeed`/`transferDisease`/`spreadDisease`/
  `cureDisease`/`attemptBreeding` 已接；31 抗性免杂草、WeedEX 挡病害与杂草。
- 手动施肥：`BlockCropStick` 右键肥料物品走 `addFertilizer(potency, 10, 100, false)`。
- 作物架方块交互：收获改用 `canHarvest()`（成熟 + 非杂草 + 条件满足）。
- 配置：`CropConfig` 新增 `enableWeeds/weedsWipePlants/weedSpawnChance/weedSpreadChance/breedingChance`，
  默认 100/50/3，与源端一致。
- NBT：写 `cropqt_v2` 标记；旧档按 阶段+百分比 折算到新进度，水/肥 clamp 0..200。

未完成（P1 剩余）：

- 收获掉落期望尚未切到源端 `dropChance * 1.03^gain` 模型，仍是旧 `rollDrops + yieldBonus`。
- 践踏（`onEntityCollision`/靴子保护/`MIN_FALL_DISTANCE_FOR_TRAMPLING`）未移植。
- 底土仍是软惩罚（`increment / 50`），源端是硬需求；已在代码注释标注为有意差异。
- `ICropStickTile` 接口未建。

API 风险已核对：1.12.2 `World` 无 `canLightningStrikeAt`（改用 `isRainingAt`）、
`BiomeDictionary.getTypesForBiome` 不存在（改用 `getTypes`）、`biome.rainfall` 字段
（改用 `getRainfall()`）。未编译、未启动。

## P1-2 收获期望 + 践踏 + 底土硬门槛（2026-11-14 完成）

- `CropType.rollDropsExpected(rand, gain, roundsMultiplier, blocksBelowIds)`：
  源端掉落模型 `dropChance * 1.03^gain * multiplier` 轮，每轮每条目按
  `(stackSize + (gain+1)/100) * chance` 展开，小数部分概率进位；方块特定掉落
  优先，否则默认表。旧 `rollDrops` 保留（兼容其它调用方）。
- `TileCropStick.getHarvestDrops(double roundsMultiplier)`：玩家 1.0，
  `CropHarvesterLogic` 传 `1 + 0.05*tier`（源端收获加成）；战利品表逻辑不动。
- 践踏：`BlockCropStick.onEntityCollision` → `TileCropStick.onEntityCollision`；
  `canTrample`（31 抗性免疫、杂草/无作物不踩）、`shouldTrampleFromRunning`
  （`100*0.95^tier` 判定 + 抗性抵抗）、`shouldTrampleFromFalling`
  （`fallDistance > 0.75` 且概率 = 超出距离），`trample()` 掉架子 + 收获物 +
  种子、耕地踩回泥土。
- 底土：`CropConfig.subSoilHardRequirement` 默认 true（源端硬门槛）；
  false 时退回旧的「增量 / 50」软惩罚。`canHarvest` 在硬模式下同时要求底土满足。
- 工业农场掉落期望同步：`FarmNutrientModel.getAvgDropRounds(dropChance, gain)`
  改为源端公式，固定/概率条目都乘 `(count + (gain+1)/100)`。

未完成（P1 剩余）：`BlockCropStick` 的铲/透镜/药/WeedEX 交互（依赖 P5 工具物品）；
`ICropStickTile` 接口未建。未编译、未启动。

## P1 计划清单（部分完成，见上）

P0 已完成（除资产逐帧核对，留到 P5 资源补齐时一并做）。下一轮开始 P1 作物架机制：

1. 新增 `api/ICropStickTile.java`（按源端接口裁剪到 cropQT 的类型：`CropType`/`CropStats`/`ItemCropSeed`）。
2. 重写 `tile/TileCropStick.java` 的存储三件套（`addWater/addFertilizer/addWeedEx(amount, threshold, maxStorage, simulate)`）与生长循环（养分→生长速率→进度）。
3. 世界内杂交、杂草、病害、践踏、手动施肥。
4. `BlockCropStick` 交互改用新接口；NBT 迁移。
5. 旧 `EnvironmentCalculator` 生长路径标注 deprecated，保留给其它调用方。

风险：P1 会触及 `TileCropStick` 的全部调用点（BlockCropStick、CropSupervisorLogic、
Harvester、IndustrialFarm、TESR、TOP、JEI），需要一次性改完编译面；
仍不编译，按 import/签名清单静态核对。

## 风险 / 待定

- D1：OpV hatch 未逐档确认；`VoltageMap.FARM_MAX_TIER` 一处可切。
- WeedEX 流体缺失：如后续决定新增 DrTech 材料流体，只需在
  `WeedEXRegistry.registerDefaults()` 增加一行。

## P2 作物管理机 CropManager（2026-11-14 完成）

- 新增 `machine/MetaTileEntityCropManager.java`（继承 `TieredMetaTileEntity`）：
  - 22 槽：0..1 WeedEX、2..5 固体肥料、6..20 输出、21 电池；输出槽通过
    `OutputViewHandler` 只允许抽、不允许塞。
  - 3 罐：水 `tier*32000`、WeedEX `tier*1500`、液肥 `tier*36864`。
  - 能量：`maxEUStore = V[tier] * (tier * V[tier])`，输入 `V[tier]`，1A；
    电池在高电量时放电、低电量时充电（与源端 `stored/2 > cap/3` / `stored < cap/3` 一致）。
  - 50 tick 一轮：能量门槛 → 缓存刷新（空 100t / 非空 600t）→ 二次功能（水/WeedEX/肥，各扣
    `V[tier]/32`）→ 收获（每株扣 `V[tier]/8`，加成 `1 + 0.05*tier`）。
  - 半径 `3 + 2*tier`，垂直 ±2；输出优先填 15 槽，溢出存 `mDropOverflow`（NBT 持久化）。
  - MUI2 界面：3 个流体槽、4 个开关（水/WeedEX/肥/收获）、2+4 输入槽、5×3 输出、电池槽。
- 注册重排：CropManager 300..311、SeedGenerator 312..323、Breeder 324..335、
  GeneExtractor 336..344（EV..UXV）、Synthesizer 345..353（EV..UXV）、Farm 仍 360。
- 旧 Supervisor / Harvester 仅在 `CropConfig.legacyCropMachines`（默认 false）时注册，
  ID 1100..1104 / 1110..1114；两块旧类保留但默认不进游戏。
- 已知缺口：
  - 旧存档 ID 重排没有 `MissingMappings` 重映射（旧 300 会被读成 CropManager LV），
    后续在 P6 清理阶段加物品/方块映射，或接受开发档不兼容。
  - WeedEX 只支持流体罐；源端「罐子耐久 → 点数」的物品补充随 P5 的 `ItemWeedEX` 落地。
  - 机器 tooltip 的 i18n key `drtech.machine.crop_manager.tooltip.1..5` 尚未加到 lang 文件（P5 统一补）。
- 未编译、未启动；按 import/签名静态核对（MUI2、Notifiable 处理器、能量容器均沿用现有机器写法）。

## P3 四台单方块（2026-11-14 完成机器部分）

### SeedGenerator

- `SeedGeneratorRecipeLogic` 按源端重写：`FERTILIZER_PER_STAT=36`、`BASE_DURATION=160`、
  `BASE_EUT=30`；只认浓缩液肥，按 `max(1, (gr+ga+re)*36)` mB 直接扣。
- 超频按 `calculateOverclockedNess(30,160)`：EU ×4 / 耗时 ÷2，次数受 `V[tier]` 限制。
- 输入槽 0 种子、1 可选催化剂（`crop.getDuplicationCatalysts()`，NBT 敏感），输出 1 份同种子。
- 机器罐容量接 `CropMachineTankCapacity.forSeedGenerator(tier)`，且 `canFillFluidType` 只收
  `DrtechMaterials.EnrichedFertilizer`。
- `CropRecipeMaps.SEED_GENERATOR` 的 `maxInputs` 由 1 改为 2（UI 槽位）。

### Breeder

- `CropBreederRecipeLogic`：液肥 = `max(1, tier*144) + max(1, (gr+ga+re)*72)` mB（浓缩液肥，直接扣）；
  时长 / EU 取 `CropMutation.getBreedingMachineRecipeDuration()/getBreedingMachineRecipeEUt()`，
  再按机器电压做 ×4 / ÷4 超频；`recipeEut > V[tier]` 直接跳过；成功率
  `min(100, 40 + (tier-LV)*10)`，失败仍扣亲本与液肥。
- 机器罐只收浓缩液肥；输入槽 3（<HV）/ 6（>=HV）维持源端口径。

### GeneExtractor

- 改为源端「电路选模式」：输入槽 0 种子、1 空数据球、2 编程电路（1 物种 / 2 生长 / 3 产量 / 4 抗性）。
- 成功消耗 1 种子 + 1 数据球，产出 `CropGeneOrb`（仍是 GT `TOOL_DATA_ORB` + `cropqt_gene` NBT）。
- 耗时 6000 / 3000 tick；EU = `VA[clamp(crop.tier, EV, UXV)]`；`CropRecipeMaps.GENE_EXTRACTOR`
  的 `maxInputs` 改为 3。

### Synthesizer

- `minimumTierFor` 上界从 IV 改为 UXV（源端 UMV 按数值映射）。
- EU = `VA[minTier] * 3`；UUM = `crop.tier*750 + (gr+ga+re)*100`；
  时长 `6000 * (1 + ln(max(1, crop.tier))/ln16)`；基因球不消耗。
- 机器罐只收 `Materials.UUMatter`；输入槽只接受本系统基因球。

### 未完成（P3 剩余）

- 4 张伪配方表的 loader（SeedGenerator/Breeder/GeneExtractor/Synthesizer 的 JEI/NEI 展示）——
  这 4 台机器都不查配方表，伪配方只用于 JEI 页面，放到 P5 与 JEI 一起做。
- 旧 `CropGeneOrb` → GT 数据球的迁移配方：当前提取器/合成器继续共用 `CropGeneOrb`
  （本来就是 GT 数据球），暂不需要迁移；若后续改成带 title 的数据球再补。
- 未编译、未启动；仅按签名/import 静态核对。

---

### P4-1 工业农场：档位/结构/超频口径（2026-11-14）

- 种子床扩到 **12 档 MV..OpV**：
  - `BlockSeedBed.MAX_TIER = GTValues.OpV`（源端 UXV 按索引映射），枚举新增 `OP("opv")`；
  - `CAPACITY` / `CONSUMPTION` / `getBaseEUt` / `getMultiLength` 自动覆盖新档；
  - 新增占位贴图 `industrial_farm/*_13.png`、`seed_bed_side_13.png`（复制自 tier 12），
    `blockstates/seed_bed.json` 增加 `opv` 变体；`en_us`/`zh_cn` 补齐 5 类组件 `opv` 名称。
- 农场结构：`MIN_SLICES = CropConstants.FARM_MIN_SLICES(1)`、`MAX_SLICES = CropConstants.FARM_MAX_SLICES(12)`，
  即源端「body 1..12 段、整机 3..14 层」。
- 删除 `MAX_OVERCLOCKS=8`：超频次数只由「`getPowerUsage()`（苗床档次 + 组件加成）」
  与「`energyContainer.getHighestInputVoltage()`」的落差决定，循环带溢出护栏。
- 删除「能量仓电压 > 组件档次 → 停机」规则（`ERROR_VOLTAGE_OVER_TIER` 保留为兼容占位、不再触发）；
  源端只校验玻璃等级与 OCGAU 的多安 hatch 规则。
- 新增校验：装了超频生长加速仓（OCGAU）但组件档次 < ZPM → `ERROR_TIER_INVALID`
  （源端 OCGAU 只从 ZPM 注册）。
- 组件注册扩到 12 档（MV..OpV），ID 重新分段：环境 370..381、生长 382..393、肥料 394..405、
  高级收割 406..417、超频 418..429（每种 12 个，共 60 个）。

### 未完成（P4 剩余）

- 5 类升级组件仍是 MTE（`MetaTileEntityFarmPart`），未按 D2 改成方块；因此结构不校验
  「玻璃等级 / 组件数量与档次来自结构方块」这两条源端规则，目前由 `agreeUpgradeTier` 与
  `FarmType` 计数代表。
- 玻璃等级（`glassTier`）未从结构捕获，源端 `hatch.getTierForStructure() > glassTier` 校验未移植。
- 模式/功率/水肥/掉落/GUI/handler 的逐项对照与 `getInfoData` 字段仍待做（多数为既有实现，需按源端复核）。
- 未编译、未启动；仅静态核对。

### P4-1b 输入并行与种子槽上限补正（2026-11-14）

- 对照源端 `MTEIndustrialFarm.tryAddSeedsToExisting/tryAddNewSeeds` 重写 `runInputMode()`：
  - 输入模式每次把种子槽补满到苗床容量，`parallels = min(容量缺口, 总线同种种子数, 已有底土+总线底土数)`；
  - 种子按 `parallels` 从总线抽取，底土只补差额，已有多余底土不回收；
  - 底土模板用 `findSubSoilTemplate()` 只读不消耗（避免旧 `findSubSoil()` 先抽一块导致数量少算）。
- 种子/底土槽上限改为 `getSeedBedCapacity()`（源端 `MTEIndustrialFarmItemStackHandler.getSlotLimit`），
  不再是固定 64；否则高档次苗床（容量最大 3481）永远只能跑 64 并行。
- 农场文件静态核对：大括号 185/185 平衡；未编译、未启动。

### P4-2 升级组件方块化 + 玻璃档捕获（2026-11-14）

- 新增 `com.meowmel.cropQT.block.BlockFarmUpgrade`：`VariantBlock<BlockSeedBed.SeedBedType>`，
  一种方块一个 `FarmType`，meta 与种子床共用 12 档（MV..OpV）。
  静态 `all()` / `getFarmType(IBlockState)` / `getTier(IBlockState|ItemStack)` 供结构匹配与校验使用。
- `BlocksInit` / `ItemsInit` 注册 5 个方块与对应 `VariantItemBlock`：
  `environmental_enhancement`、`growth_acceleration`、`fertilizer`、
  `advanced_harvesting`、`overclocked_growth_acceleration`。
- 结构 `U` 位从「能力仓」改成「五类组件方块 + 木框架」：`upgradeElement()` 用
  `Elements.chain` 拼 5×12 个 `onPass(Elements.block(...))`，命中时 `addUpgradeUnit(type)` 且
  `agreeUpgradeTier(tier)`；木框架用 `Elements.frames(gregtech Materials.Wood)`。
- 结构 `G` 位从固定 tempered 玻璃改成四种 GT 玻璃 `chain`：命中时 `agreeGlassTier(type.getGlassTier())`。
- `MetaTileEntityIndustrialFarm`：
  - 新增 `glassTier` 字段、`isGlassTierValid()`、`agreeGlassTier()`、`getGlassTier()`；
  - `getConfigError()` 在档次校验后加 `ERROR_GLASS_TIER`：玻璃必须 `MV..OpV` 且全结构同档，
    另外 `energyContainer.getHighestInputVoltage() > GTValues.V[glassTier]` 也判停机
    （源端 `hatch.getTierForStructure() > glassTier`；本仓库四种玻璃最高 UV，UMV 豁免永不触发）；
  - 覆写 `checkStructurePattern()`：每次结构检查前清空 `upgradeTier`/`glassTier`/`unitCounts`，
    避免增量复检把回调计数累加；
  - `formStructure()` 不再从 `IFarmPart` 能力汇总（组件不再是仓），计数全部来自结构匹配回调；
  - 删除 `DrtechMultiblockAbility` / `IFarmPart` 依赖与无用 import。
- 资源：5 个 `blockstates/<type>.json`（forge_marker + cube_all，12 变体指向
  `drtech:blocks/industrial_farm/<type>_2..13`）；`en_us`/`zh_cn` 各补 5×12 条
  `tile.drtech.<type>.<variant>.name`，另补 `tile.drtech.seed_bed.opv.name` 与
  `cropqt.farm.error.glass`。
- 未完成 / 已知缺口：
  - 旧 `MetaTileEntityFarmPart` 与 `DrTechMetaTileEntities` 里 370..429 的注册仍在（运行时无引用），
    P6 清理并释放 ID；
  - OCGAU 的「≤1 个 multi-amp、普通/多安不可混用、禁激光」规则未实现（需要按 hatch 实例判定，
    当前只做了电压 ≤ 玻璃档的近似校验）；
  - 农场 GUI 尚未显示 `glassTier`；`getInfoData` 字段复核留 P4-3；
  - 未编译、未启动；仅按签名/import 静态核对。

### P4-3 农场信息显示 + 输出模式并行（2026-11-14）

- **源端 `getInfoData()` 在 GTCEu 没有对应 API**（`MultiblockWithDisplayBase`/`MetaTileEntity`
  全库 grep 无 `getInfoData`），因此不能照抄 scanner 文本；改为两条显示路径：
  - **TOP**：`com.drppp.drtech.intergations.top.provider.TopProvider` 新增
    `MetaTileEntityIndustrialFarm` 分支，输出档次、苗床容量、收获倍率、生长倍率、
    水/周期、肥/周期（装了肥料单元时标为「浓缩液肥」）、营养分 `/55`、以及停机原因。
  - **GUI**：农场界面下方新增三行（y=66/78/90），分别是「收获/生长倍率」、
    「每周期水/肥（浓缩液肥时换键）」、有种子时的「营养分」；对应同步值
    `cropqt_farm_harvest_pct` / `growth_pct` / `water_per_cycle` / `fert_per_cycle` /
    `nutrient` / `fert_unit`，lang 键 `cropqt.farm.display.{modifiers,water_fert,water_fert.enriched,nutrient}`。
- `MetaTileEntityIndustrialFarm` 新增只读访问器：`getSeedStack()`、`getSeedCrop()`、
  `getNutrientScoreForDisplay()`（无种子返回 -1）、`hasFertilizerUnit()`，
  给 TOP/GUI 取数用，不改内部状态。
- **输出模式按源端重写**（`runOutputMode()`）：并行数 = 种子数与底土数的较小值，
  再用 `GTTransferUtils.insertItem(..., simulate=true)` 探测输出总线还能装多少（等价源端
  `ItemEjectionHelper` 先模拟再弹出），取两者较小值一次性弹出；少于并行数就只弹能装下的部分，
  不会出现「种子退完了底土卡在机器里」。
- **内部槽 handler 补齐源端规则**（`createFarmInventory()`）：
  - 运行中（`maxProgress > 0`）禁止插入与抽取；
  - 种子槽：已分析 + 无底土要求 + `minSeedBedTier <= upgradeTier` + 模拟有肥后生长速度 > 0；
  - 底土槽仍禁止手动插入；
  - 环境槽：`slot - SLOT_ENV_START < 环境强化单元数`，且模块必须带非空生物群系标签；上限 1；
  - 有底土时，种子/底土只能由输出模式取走（`extractItem` 返回 `ItemStack.EMPTY`）。
- GUI 面板高度 166 → **190**：新增信息行在 y=66/78/90，原高度下会被自动贴底的玩家背包压住。
- **OCGAU 的能量仓类型规则补齐**（`isEnergyHatchConfigurationValid()` + `ERROR_HATCH_TYPE=7`）：
  - 结构里出现 `INPUT_LASER` 能力 → 停机（源端禁激光）；
  - 多安仓判定为 `getInputAmperage() > 2`；没装 OCGAU 时出现多安仓 → 停机（源端 `SE_MULTI_AMP_NOT_ALLOWED`）；
  - 装了 OCGAU 时多安仓 > 1 → 停机（`MAX_MULTIAMP_EHATCH_AMOUNT=1`），多安与普通混用 → 停机（`SE_EHATCH_TYPE_NO_MIX`）；
  - 仅在结构成型后检查（成型前 `getAbilities` 为空会把「还没接仓」误判为合法）。
- 仍未做：`glassTier` 在 GUI 中单列显示（已在 TOP 显示）；沿用的旧组件 MTE 注册清理；
  未编译、未启动。

## P5-1 工具五件套（2026-11-14）

源端 CropsNH 没有自己的浇水壶，农业交互靠五件工具：铲、强化铲、植物透镜、植物药、WeedEX 罐。
本轮把这五件补齐，并把它们接进 `BlockCropStick` 的右键分流。

- 新增 `item/tool/ItemCropSpade`（`crop_spade` / `reinforced_crop_spade`）：
  - 右键作物架：先挡双层杂交架；杂草成熟 → 掉落 + 清架；作物「成熟但环境不达标收不了」
    → 掉落产物 + 返还种子并清架（源端 `doWork`）。
  - 强化铲返还种子数 = `resist/10 + (rand(10) < resist%10 ? 1 : 0)`，普通铲固定 1（源端两把铲的差别）。
  - 右键地面：草/泥土/菌丝 → 耕地，走 `ForgeEventFactory.onHoeUse`（源端 `UseHoeEvent`）。
- 新增 `item/tool/ItemPlantLens`（`plant_lens`）：右键作物架把种子标记为已分析，并在聊天栏列出
  生长百分比、水/肥/WeedEX、病害状态、未满足的底土要求（源端 `getPlantLensStatus` 的等价物）。
- 新增 `item/tool/ItemPlantCure`（`plant_cure`，100 次）：治病成功后补 25 点肥，消耗 1 耐久（源端同）。
- 新增 `item/tool/ItemWeedEX`（`weedex_can`，75 次）：每次注入 10 点 WeedEX，架子存储 > 90 时拒绝；
  `MAX_USES = 750/1/10`（源端常量来源）。
- `TileCropStick` 新增 `analyzeSeed()`：透镜入口，标记 `CropStats.analyzed` 并立即同步。
- `BlockCropStick.onBlockActivated` 顶部按「透镜 → 植物药 → WeedEX → 铲」的顺序分流；
  工具返回值 false 时继续走原来的种植/施肥/收获路径，所以空手/种子袋行为不变。
- **CropManager 的 WeedEX 罐补充**（补齐 P2 遗留「WeedEX 只有流体罐」）：
  - 新增 `weedExBuffer`（源端 `weedEXStored`），容量 `tier × 1500`；
  - `refillWeedEx()` 每周期把 0..1 槽的 WeedEX 罐耐久换成 10 点 WeedEX，罐子用尽即消失；
  - `applyWeedEx()` 优先扣缓冲，缓冲不足再扣流体罐；
  - 0..1 槽的 `isItemValid` 收紧为只收 `ItemWeedEX`；
  - 缓冲写入 NBT `mWeedExBuffer`。
  - 说明：本仓库没有 WeedEX 流体，因此罐子是默认来源；若整合包提供 `weedex` 流体，
    `WeedEXRegistry` 仍会按 1 点/mB 登记，两条路并存。
- 资源：从 CropsNH 复制 `spade/reinforcedSpade/plantLens/plantCure/weedEX` 五张物品贴图；
  新增五个 `models/item/*.json`（`item/generated`）；中英 lang 各 5 条 `item.drtech.*.name`。
- 静态核对：6 个改动文件大括号平衡；未编译、未启动。
- 未做：五件工具的合成配方（P5 剩余）、`weedExBuffer` 在 GUI/TOP 的独立读数（当前只在 NBT 与逻辑里）、
  WeedEX 罐对植物药的加速/除草粒子效果（源端无，略）。

## P5-2 合成配方整批移植（本轮）

新增 `src/main/java/com/meowmel/cropQT/recipes/CropCraftingRecipes.java`（316 行），
在 `CraftingRecipes.load()` 末尾调用 `CropCraftingRecipes.load()`。

- 工具：普通铲（三重钢板 + 铁铲 + 长木棍）、强化铲（致密钢板 + 普通铲 + 长木棍 + 橡胶板）、
  植物药（空喷罐 + 骨粉）、植物透镜（玻璃板 + 钢板 + LV 电路）、作物架 ×4（长木棍）。
- 单方块机器各 12/9 档循环：
  - 作物管理器 `"ASA","PHP","CIC"`（源 174-196）；
  - 种子生成器 `"SAW","CHC","BDB"`（源 198-222）；
  - 作物繁殖机 `"SAS","CHC","WDW"`（源 224-247）；
  - 基因提取器 `"SES","WHW","CSC"`（源 249-273，EV 起）；
  - 作物合成机 `"FCF","CHC","EEE"`（源 275-298，EV 起）。
- 工业农场 `"PWS","CMC","ICI"`，控制器为 MV 档（源 300-315）。
- 种子床 12 档（MV..OpV）`"DDD","CHC","PIP"`（源 317-336）。
- 五类升级组件各 12 档：高级收割 `"RSR","CHC","WBW"`（源 338-360）、
  环境强化 `"EEE","CHC","WBW"`（源 362-383）、肥料单元 `"RPS","CHC","WBW"`（源 563-586）、
  生长加速 `"RPE","CHC","WBW"`（源 588-611）、超频生长加速 `"FAF","CGC","WBW"`（源 613-636）。
- 替代件（源端 GT5U 特有，已在类头逐条记录）：输入仓/输入总线 →
  泵/传送带模块；水壶 → 泵模块；`BrickedAgriculturalCasing` → 同档机器外壳；
  `FIELD_GENERATOR` → GTCEu `FIELD_GENERATOR_IV`；World Accelerator → `FIELD_GENERATOR_IV`。
- 组件取值走 `CraftingComponent`，新增 `at(...)` 向下回退：GTCEu 的 UEV..OpV 组件
  受 `GregTechAPI.isHighTier()` 开关影响，关闭时自动回退到最近可用档位，避免 OpV 配方
  出现空配料。
- 静态核对：新文件大括号 36/36；`ModHandler.addShapedRecipe`/`addShapelessRecipe`、
  `CraftingComponent.*.getIngredient`、`GTValues.VN`、`MetaItems.FIELD_GENERATOR_IV.getStackForm()`
  均已核对；未编译、未启动。

P5 剩余：四张伪配方表的 loader（JEI 用）、JEI 分类页面、配置项、TOP/Waila 字段、
模型/材质逐帧核对（源端 3 株型几何）。

## P5-3 四张伪配方表 + JEI 分类（本轮）

新增 `src/main/java/com/meowmel/cropQT/recipes/CropFakeRecipes.java`（约 205 行），
由 `CropInitHandler.init()` 末尾调用（必须在作物与变异登记完成之后；不能在
`CraftingRecipes.load()` 里调用，因为那一步早于 `CropInitHandler.init()`，作物表还是空的）。

- 说明：四台机器的 `XxxRecipeLogic` 覆写 `shouldSearchForRecipes()` 恒为 false，运行时从不查表；
  这些配方只用于 JEI 展示，行为与功能不互相污染（源端同样是 fake recipe 思路）。
- 种子生成器：每株作物一条（跳过 `isHiddenFromNEI()` 与 `crossingThreshold < 0`），
  输入已分析种子（`CropStats(1,1,1,true)`）、液肥 `max(1,(1+1+1)*36)=108 mB`、输出 1 份同类种子；
  有 `getDuplicationCatalysts()` 的作物另加「种子 + 催化剂」配方（源端 SeedGeneratorFakeRecipeLoader）。
- 作物繁殖机：遍历 `MutationRegistry.getAllMutations()`，跳过结果已不存在的变异；
  输入全部亲本种子（≥2），液肥 `max(1,tier*144)+max(1,3*72)`，
  EU/时长取 mutation 的 `getBreedingMachineRecipeEUt()/Duration()`。
- 基因提取机：每株作物 4 条（电路 1=物种 6000t、2/3/4=growth/gain/resistance 3000t），
  EU = `VoltageMap.eu(clamp(machineBreedingRecipeTier, EV, UXV))`，输出对应 `CropGeneOrb`。
- 作物合成机：每株一条，四颗基因球 + `UUM = tier*750+300`，
  时长 `6000+6000*ln(tier)/ln(16)`，EU = `eu(clamp(tier, EV, UXV))*3`，输出已分析种子。
- JEI 分类图标：四个 `getPrimaryRecipeCategory().jeiIcon(机器本体)`。
- 语言文件：`en_us.lang`/`zh_cn.lang` 各加 4 条 `recipemap.<map>.name`
  （`crop_seed_generator`/`crop_breeder`/`gene_extractor`/`crop_synthesizer`）。
- 静态核对：新文件大括号 32/32；`GTUtility.copy`（无 `copyAmount`，已改）、
  `RecipeBuilder.circuitMeta/inputs/fluidInputs/outputs/buildAndRegister`、
  `CropGeneOrb.Kind`、`CropType.isHiddenFromNEI()`、`VoltageMap.clamp/eu` 均已核对；未编译、未启动。

P5 剩余：配置项、TOP/Waila 字段、模型/材质逐帧核对（源端 3 株型几何）。

## P5-4 配置项补齐 + 世界杂交属性变异对齐（本轮）

源端 `ConfigurationHandler` 的 `crops` / `weeds` 两段里，与机制直接相关的部分此前只搬了
生长倍率、杂草三项、杂交概率；本轮补齐**肥料档位与属性变异数组**，并把世界杂交的
属性生成改成源端算法。

### 配置（`handler/CropConfig.java`）

新增 10 项（全部挂在 `DrtConfig.crop` 下）：

| 配置键 | 默认 | 作用 |
|---|---|---|
| 中肥变异阈值（单亲） | 50 | 单亲自交用中档变异数组的肥料下限 |
| 高肥变异阈值（单亲） | 101 | 单亲自交用高档变异数组的肥料下限 |
| 中肥变异阈值（杂交） | 50 | 多亲杂交用中档变异数组的肥料下限 |
| 高肥变异阈值（杂交） | 101 | 多亲杂交用高档变异数组的肥料下限 |
| 单亲低/中/高肥变异 | `{-1,0,0}` / `{0}` / `{0,0,0,1,2,3,4}` | 单亲路径的属性变异候选 |
| 多亲低/中/高肥变异 | `{-1,-1,0,1,1}` / `{-2,-1,0,1,2}` / `{0,0,0,1,2,3,4}` | 多亲路径的属性变异候选 |

新增 `getStatVariation(boolean singleParent, int minFertilizer)`，逐字对应源端
`getStatVariation`；数组被玩家清空时回退到默认值，避免 `nextInt(0)`。

`@Config` 的 `int[]` 在 Forge 1.12.2 是受支持类型（`ConfigManager.ARRAY_REMAP`
把 `int[].class → Integer[].class`），已核对。

### 世界杂交（`tile/TileCropStick.java`）

删掉旧的「每株掷一次 `crossBreedChance`、再用 `CropStats.inheritFrom` 取均值 ±2」实现，
按源端重写：

- 新增 `canCross()`（`crossingThreshold >= 0` 且进度达标）与 `canBreed()`（进度 ≥ `breedingThreshold`）。
- `attemptBreeding()`：`1/breedingChance` → 一半概率走自交分支（随机选一株 `canCross` 的作物，
  并只保留同种参与株）→ 否则走杂交分支（`canBreed` 至少 2 株 → 确定性变异 → 变异池）。
- 属性：`variateStat` 取参与株均值 + 配置数组里随机一个变异，截断 1..31；参与株只算真正
  匹配该变异亲本的植株（源端 `neighbours.removeIf` 同义）。
- 新长出的作物是**未分析**状态（`new CropStats(g, a, r, false)`），与源端一致；旧实现会继承
  `analyzed` 标志，等于免费跳过分析仪。
- 结果生成前还检查土壤有效性与当前光照/湿度是否允许该作物生长（源端
  `isValidSoilForCrop` / `wouldCropBeAbleToGrow`）。

### 未做 / 备注

- `crossBreedChanceMultiplier` 配置项保留但已不参与杂交判定（源端没有这个旋钮）；
  下轮若要清理，可在 P6 一并移除或改作文档说明。
- 未编译、未启动；静态核对：`TileCropStick` 大括号 156/156、`CropConfig` 56/56。

## P5-5 TOP 读数：作物管理机（本轮）

`MetaTileEntityCropManager` 原本只有容器/罐的 getter，TOP 无法显示运行状态；
本轮补上只读访问器并接入 `TopProvider`。

### `machine/MetaTileEntityCropManager.java` 新增公开只读方法

- `isWaterEnabled()` / `isWeedExEnabled()` / `isFertilizerEnabled()`（`isHarvestEnabled()` 原已有）
- `getWeedExBuffer()`、`getWeedExBufferCapacity()`（`tier × 1500`）
- `getWorkingRadius()` = `3 + 2×tier`、`getVerticalRadius()` = `2`
- `getHarvestBonusChance()` = `0.05×tier`
- `getCachedCropCount()`（诊断用，缓存 Set 大小）

### `intergations/top/provider/TopProvider.java`

新增 `MetaTileEntityCropManager` 分支（在工业农场分支之前），显示：

- 机器档次（`GTValues.VN[tier]`）
- 工作范围（`(2r+1)×(2r+1)`）与垂直半径
- 已缓存作物架数量
- 水 / WeedEX（罐 + 缓冲合计，附缓冲值）/ 液肥 的「当前 / 上限」
- 四个开关（水 / 除草 / 施肥 / 收割，绿=开、深灰=关）
- 主耗电与辅助耗电（EU/t）
- 收割加成百分比

新增私有 `toggle(String, boolean)` 生成开关小标签。CropsNH 侧只有作物架的 WAILA
（`compatibility/waila/CropStickWailaProvider`），机器读数是我们额外补的对称信息，不改变机制。

### 验证

- `TopProvider` 大括号 19/19、`MetaTileEntityCropManager` 156/156；
- 所有引用方法（`getTier()`、`getFluidAmount()`、`getCapacity()`）均在既有 API 中存在；
- 未编译、未启动。

P5 剩余：模型/材质逐帧核对（3 株型几何、UV 朝向、阶段夹取、两套命名）。

### P5-6 作物株型渲染几何对齐（`client/CropStickTESR.java`）

按源端 `renderers/plantshape/*` 重写三种株型的几何，规则统一为**固定几何 + 整图 UV**：
生长阶段只决定用哪一帧，不再按进度缩放高度，也不再裁剪 UV 顶部。

- `CROSS`（XPlantRenderer）：高 `12/16`，四角内缩到 `2/16..14/16`，两片 45° 对角面各正反两面。
- `HASH`（HashPlantRenderer）：整格高 `1.0`，四面固定在 `x=4/16`、`z=4/16`、`z=12/16`、`x=12/16`，沿轴铺满 `0..1`。
- `FLOWER`（FlowerPlantRenderer）：高 `12/16`，四个方向各两段（`6..18` 与 `-2..10`，单位 1/16），
  两段用 `3.999/4.001`、`11.999/12.001` 的 0.002/16 深度错位避免 z-fighting。
- 顶点辅助方法由 `quad`/`quadFlat` 换成 `quadV`（四个顶点各带 UV）+ `vertex`，
  因为株型正反面的 UV 顺序会镜像，不能共用同一组角。

验证：`CropStickTESR` 大括号 24/24；`quadFlat`/`plantHeight`/`vCrop` 已无残留；
`frameOf` 的两套命名（`stage_N` 与 `N`）与阶段夹取逻辑保持不变。未编译、未启动。

P5 全部节点完成。P6 剩余：旧 `MetaTileEntityFarmPart` 与 370..429 注册清理、
旧机器 ID 的 MissingMappings、`crossBreedChanceMultiplier` 等遗留配置清理、全量验收清单。

### P6-1 旧组件 MTE 清理与存档兼容（`DrTechMetaTileEntities` / `cropQT` 包）

**存档标识的结论**：GTCEu 的 MTE 在存档里写的是 `MetaId`（注册名 `ResourceLocation`），
不是注册时用的数字 ID（`MetaTileEntityHolder.writeToNBT` 写 `metaTileEntityId.toString()`；
读档走 `MTERegistry.getObject(ResourceLocation)`）。因此 300..353 这次数字 ID 重排不影响旧存档，
只有**删除注册名**才会让旧方块读档失败。

- 删除 `machine/part/MetaTileEntityFarmPart.java`、`api/capability/IFarmPart.java`、
  `api/metaTileEntity/DrtechMultiblockAbility.java`；`machine/part` 目录删除。
- `DrTechMetaTileEntities`：删除 5 个 `*_FARM_PARTS` 数组与 370..429 的 60 条注册；
  `INDUSTRIAL_FARM`（360）保留。旧组件替换为 P4-2 的 5 类方块。
- `CropConfig`：删除源端没有的 `crossBreedChanceMultiplier` 及其 getter（P5-4 后已无调用方）。
- `MetaTileEntityIndustrialFarm`：删除未使用的 `getGlassesState()`（`glassElement()` 仍用玻璃枚举）。
- `FarmType` 类注释改为「升级方块，数方块种类与个数」。
- lang：删除 120 条 `drtech.machine.farm_part.*` 死键（中英各 60）。

### P6-2 静态验收与超频口径修正（`api/capability/impl/*`）

**超频口径**：源端单方块调 `MTEBasicMachine.calculateOverclockedNess(eut, duration)`，
其上限是 `V[mTier] * 安数`，每超一次 **EU×4、时长÷2**
（`OverclockCalculator.durationDecreasePerOC` 默认 2，见
`GT5-Unofficial/src/main/java/gregtech/api/util/OverclockCalculator.java:35` 与 `:411`）。

- 新增 `CropMachineRecipeLogic.overclock(long baseEut, int baseDuration, long ceiling)`，
  返回 `{eut, duration}`，`while (eut <= ceiling / 4) { eut *= 4; duration /= 2; }`。
- `CropBreederRecipeLogic`：原 `duration / 4` 修正为经公共 helper 的 `/2`，
  上限 `V[machine.getTier()]`；EU 仍取 mutation 的 `getBreedingMachineRecipeEUt()`。
- `CropGeneExtractorRecipeLogic`：补超频，基准 `VA[作物最低档]`，上限 `V[mTier]`，
  时长基准 6000/3000，经 helper 后可能缩短（机器档位高于作物档位时）。
- `CropSynthesizerRecipeLogic`：补超频，基准 `VA[作物最低档] * 3`，上限 `V[mTier] * 3`
  （机器能量仓 3A）；时长基准 `6000 * (1 + ln(cropTier)/ln16)`。
- `SeedGeneratorRecipeLogic`：`160 >> (tier-LV)` 与 `30 << (2*(tier-LV))` 与 helper 等价
  （GT 每档电压 ×4，EU 恰为 `V[tier]`，时长每档 ÷2），故不改写。

**静态复算**（`awk` 复算，非编译）：

| 公式 | 输入 | 结果 |
| --- | --- | --- |
| `getNutrientsPerCycle` | (2, 0.5, true, 100, 100) | 5+10+10+2+28 = **55** |
| `getGrowthRate` | np=2, tier=2, growth=6（np<need） | 12×(100−10×4)/100 = **7** |
| `getAvgDropRounds` | dropChance=1.0, gain=10 | 1.03^10 = **1.3439** |
| 种子床容量/消耗 | MV(2) / OpV(13) | 225/88、3481/1360 |
| Breeder 超频 | 30 EU, 400t, V[MV]=120 | 120 EU, 200t |
| Breeder 超频 | 30 EU, 400t, V[HV]=480 | 480 EU, 100t |

**逐项复核**：`addWater/addFertilizer/addWeedEx` 的 `storage > threshold` 短路与 `simulate`
分支；`onGrowthTick` 末尾水/肥各 −1、雨 `addWater(11,100,101)`；`spawnWeed` 的 resistance 31
免疫与 WeedEX 优先扣；`spreadWeed` 四邻病害/生草；践踏 `shouldTrampleFromFalling` 0.75 格；
CropManager 22 槽/3 罐/50t/÷8 与 ÷32/15 槽溢出/电池；种子机 2 槽与容量 ÷10；繁殖机 3|6 槽、
144+72、40+10%/档、mutation EU 门、均值统计、失败仍扣料；基因机 4 电路/6000/3000/tier clamp/
种子+球消耗/GT 数据球；合成机 4 球/UUM 750+100/3A/log16 时长/球不消耗；农场 1..12 段/3 模式/
5t 与 100t/功率公式/OC 无上限/水肥 ×2^OC/掉落小数/虚空保护；种子床与 5 类组件数值。

**未验证（需进游戏）**：JEI 页面实际排版、TOP 文本换行、结构成型与 hatch 交互、MUI2 控件位置、
配方在 JEI 里是否可点击。按用户要求未编译、未启动。

### 总览：P0~P6 全部节点完成

| 阶段 | 结果 |
| --- | --- |
| P0 | 常量/电压映射/容量/potency/CropType/CropMutation/THIRD_PARTY 完成 |
| P1 | 作物架机制整体重写（阈值存储、生长、雨、杂草、病害、践踏、杂交、收获 1.03^gain、NBT 迁移） |
| P2 | `MetaTileEntityCropManager` 12 档（300..311），22 槽/3 罐/电池/双功率/缓存/溢出；旧 Supervisor+Harvester 转 legacy |
| P3 | 四台单方块按源端数值重写；伪配方表供 JEI |
| P4 | 种子床 12 档 MV..OpV、5 类组件方块化、结构 1..12 段、功率/OC/水肥/掉落/输入并行/输出并行、glassTier 与 hatch 规则、GUI/TOP |
| P5 | 5 件工具 + CropManager WeedEX 缓冲、132 条合成配方、4 张伪配方表、配置与统计变异、TOP、3 种株型几何 |
| P6 | 旧组件 MTE 与 lang 死键清理、超频口径修正（Breeder ÷2、Gene/Synth 补超频）、静态复算与验收清单 |

- 规模：`src/main/java/com/meowmel/cropQT` 现 92 个 Java 文件 / 15,393 行（含本次新增）。
- 约束遵守：未编译、未启动游戏；所有验证均为静态核对与 `awk` 公式复算。
- 进游戏才能确认的项：JEI 排版、TOP 换行、结构成型、MUI2 控件位置、配方可点击性。

### P6-3 编译通过与报错修复（2026-11-14）

- 环境：`JAVA_HOME=C:\Program Files\Java\jdk1.8.0_361`，Gradle Wrapper 8.5；
  `./gradlew compileJava --console=plain` 首次 13 个错误，逐条修复后 **BUILD SUCCESSFUL（25s）**，
  随后 `./gradlew build --console=plain` 亦 **BUILD SUCCESSFUL（33s）**，产出
  `build/libs/drtech-1.8.9.jar`（含 148 个 cropQT class 与全部方块状态/贴图）。
- 修复的编译错误：
  1. `TileCropStick`：`BiomeGenBase` → `Biome`（1.12.2 类名）；
  2. `TileCropStick`：`world.canBlockSeeTheSky(x,y+1,z)` → `world.canSeeSky(pos.up())`；
  3. `TileCropStick`：`FERTILIZER_STORAGE_LIQUID_THRESHOLD` → `FERTILIZER_LIQUID_THRESHOLD`；
  4. `MetaTileEntityCropManager`：删除重复的 `isWaterEnabled/isWeedExEnabled/isFertilizerEnabled`（第二组）；
  5. `ItemCropSpade`：`SoundType.GRASS` → `SoundType.PLANT`；
  6. `MetaTileEntityIndustrialFarm`：编译依赖的 GTQT jar 里 `CasingType` 没有 `getGlassTier()`，
     改为本地 `glassTierOf(CasingType)` 映射（Tempered=MV、Cleanroom=HV、Laminated=IV、Fusion=UV）；
  7. `CropCraftingRecipes.addUnit`：7 对键值签名 → `Object...` 变长参数（五类组件传 6 对）。
- 额外静态检查：MTE 数字 ID 无重复；方块/物品注册名无冲突；`assets` 下 213 个 JSON 语法有效
  （9 个 BOM 文件为原有文件，Gson 可读）；`fertilizer` 方块的注册名/`blockstates`/贴图/语言键一致。
- 仍未做：进游戏验证（JEI 排版、TOP 换行、结构成型、MUI2 控件位置、配方可点击性）。
- 复验：`./gradlew --stop` 释放被占用的 `build/rfg` 后 `./gradlew clean clean build` 从零构建成功
  （2m37s，0 error），jar 内 122 个 cropQT class。

### P7 测试作物落地（2026-11-14）

用户要求“补齐作物”，范围取 `docs/cropsnh-crop-table.md` 已勾选的 20 条本体作物（不含任何联动作物）。
只补 `CropType` 数据与注册，**未改动机器代码**；仍未编译、未启动游戏。

#### 改动文件

| 文件 | 动作 | 内容 |
| --- | --- | --- |
| `src/main/java/com/meowmel/cropQT/api/CropParityCrops.java` | 新建（328 行） | 16 条 NH id 作物；`defaultDropChance(tier)=0.95^tier` |
| `src/main/java/com/meowmel/cropQT/api/CropRegistry.java` | 修改 | 4 条既有作物对齐 NH 数值；`VANILLA_CROP_IDS` 改为贴图目录清单；注册后追加 `CropParityCrops.registerAll()`；核对逻辑改为“目录必须被某作物 `getTexturePath()` 使用” |
| `src/main/java/com/meowmel/cropQT/api/SoilTypes.java` | 修改 | 新增 `oilSands`、`mushroomAll`、`sugarcane`、`oil` 复合土壤 |
| `src/main/java/com/meowmel/cropQT/api/SubSoilRequirements.java` | 修改 | 新增 `thorium`、`olivine`、`netherStar`，配套 `gem(...)` 辅助 |

#### 20 条作物

“改”= 复用既有 id 改数值；“新”= 新增 NH id（旧 id 如 `reed`/`nether_wart_crop`/`red_mushroom`/
`bonsai` 保留不动，供老存档与新体系并存）。

| 来源 | 实现 | ID | tier | 时长 | 阶段 | 土壤 | 子土壤 | 机器/种子床 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 改 | `wheat` | wheat | 1 | 1000 | 0~7 | farmland | — | LV/LV | 光照≥9，仅掉小麦 |
| 改 | `carrot` | carrot | 2 | 800 | 0~3 | farmland | — | LV/LV | 光照≥9 |
| 新 | `sugarCane` | sugarCane | 2 | 400 | 1~3 | sugarcane（沙+土） | — | LV/LV | 甘蔗×2，CROSS |
| 改 | `cactus` | cactus | 3 | 450 | 1~3 | sand | — | LV/LV | 光照 0，仙人掌×1，CROSS |
| 新 | `netherwart` | netherwart | 5 | 2000 | 0~2 | soulsand | — | LV/LV | dropChance 2.0 |
| 新 | `redMushroom` | redMushroom | 1 | 400 | 1~4 | mushroomAll（石+土+菌丝） | — | LV/LV | 光照≤9 |
| 新 | `blueOrchid` | blueOrchid | 2 | 600 | 1~4 | dirtGrass | — | LV/LV | 染料 75% + 花 25%，CROSS |
| 新 | `stone` | stone | 1 | 600 | 1~4 | stone | stone | LV/LV | 石粉×9，CROSS |
| 新 | `olivia` | olivia | 2 | 400 | 1~4 | stone | olivine | MV/MV | 粉 75% + 宝石 25% |
| 新 | `starWart` | starWart | 12 | 7200 | 1~4 | stone | netherStar | EV/EV | MachineOnly，下界之星粉 |
| 新 | `spaceFlower` | spaceFlower | 13 | 15000 | 1~4 | farmland | 无（源端 space 无对应） | IV/IV | MachineOnly，CROSS |
| 新 | `redstraw` | redstraw | 6 | 1500 | 1~7 | farmland | redstone | MV/MV | 光照 5~12，dropChance 0.85 |
| 新 | `magicalNightshade` | magicalNightshade | 13 | 23436 | 1~4 | farmland | 无（shadowmetal 无对应） | HV/HV | MachineOnly，0.95^13×0.75 |
| 新 | `godOfThunder` | godOfThunder | 9 | 3600 | 1~4 | stone | thorium | HV/HV | MachineOnly，钍粉 |
| 新 | `oilBerry` | oilBerry | 8 | 1200 | 1~4 | oil（沙+砾石+灵魂沙+油砂） | — | MV/MV | 油砂粉×2 |
| 新 | `zomplant` | zomplant | 3 | 1800 | 1~4 | soulsand（灰度墓地组退化为灵魂沙） | — | LV/LV | 腐肉 99.5% + 头 0.5%，CROSS |
| 新 | `trollplant` | trollplant | 6 | 4800 | 1~8 | brick | — | EV/EV | 红宝石 62.5% + 钚 12.5% + 植物球/废料各 12.5% |
| 新 | `bonsaiOak` | bonsaiOak | 1 | 1600 | 1~3 | dirtGrass | — | LV/LV | 树苗 30% + 原木×10 60% + 苹果×2 5%，CROSS |
| 改 | `weed` | weed | 1 | 600 | 1~4 | 不限 | — | LV/LV | hideFromNEI，无掉落 |
| 新 | `enderbloom` | enderbloom | 10 | 3000 | 1~4 | farmland | endStone | HV/HV | 末影粉 62.5% + 末影珍珠 37.5% |

#### 缺失材料替代

GTQT 里没有源端的自定义作物产物，按下表映射，代码里以 `[替代]` 注释标注：

| 源端产物 | 替代 |
| --- | --- |
| starWart / spaceFlower / magicalNightshade 的自定义物品 | `Materials.NetherStar` 粉 |
| thunderLeaf | `Materials.Thorium` 粉 |
| oilBerry | `Materials.Oilsands` 粉 |
| gemSpinel / screwFoolsRuby | `Materials.Ruby` 宝石 / 红宝石螺丝 |
| IC2 Plantball / Scrap | `MetaItems.PLANT_BALL` / `MetaItems.SCRAP` |
| 底土 shadowmetal / space / graveyard | 不设底土（硬门槛会永远长不出来），zomplant 退回 soulsand |

#### 静态核对（未编译）

- `CropType.Builder` 用到的 20 个方法全部存在；`CropRenderType` 没有源端 `X`，6 处已改用 `CROSS`。
- `CropQTMaterialIconType` 存在 grain/spore/flower/vanilla/magic/oreberry/bonsai/root。
- `CompoundSoilList` 保存成员引用、容量查询期取 max → `SoilTypes` 静态声明顺序不会取到 null。
- `Materials` 存在 Plutonium241/Thorium/Ruby/EnderPearl/Oilsands/Stone/NetherStar/Olivine/Redstone；
  `MetaItems.SCRAP`/`PLANT_BALL` 与 `MetaValueItem.getStackForm(int)` 均存在。
- 种子不需要逐作物注册：`ItemCropSeed` 以 NBT `cropId` 绑定，显示名/颜色/图标按 `CropRegistry` 动态取。
- 贴图：16 条新作物使用 1 起数字帧目录（`sugar_cane` 3 帧、`trollplant` 8 帧等），
  `CropStickTESR.frameOf` 缺帧会夹到末帧，不会数组越界；`starWart` 走 `star_wart` 4 帧目录。

#### 未验证（需进游戏）

图集是否拼上 16 个新目录、新作物在 JEI/创造栏的种子袋、复合土壤的实际匹配、
底土硬门槛与替代物配方是否可达。

#### P7 资产核对与清理（2026-11-14）

- 贴图：源端 `blocks/crops` 的 634 张阶段图已全量在库；脚本按目录名/下划线映射比对 511 张同名同帧文件，
  **507 张与源端字节一致**，差异只有 `lazulia` 4 帧（源端版本差），另 15 张源端文件在本地没有对应目录。20 株测试作物所用目录抽检全部一致
  （`sugar_cane`/`olivia`/`trollplant`/`enderbloom`/`blue_orchid` 逐帧 `cmp` 相同）。
- 模型：源端 `models/cropSticks.tcn` 是 1.7.10 Techne 格式，1.12.2 不能加载，**没有复制**；
  作物架本体继续用自写 JSON（`crop_stick_single/double`，贴图 `blocks/crop_stick/crop_stick.png` 为自有资源，
  与源端 `cropSticks.png` 的 md5 不同）；植物部分按源端几何在 `CropStickTESR` 里逐常量复刻
  （`-2..18` 外扩、`3.999/4.001` 防 z-fighting、`maxY=12`）。
- 清理：删除旧 137 作物时代遗留的 **95 个未引用目录 / 587 张 PNG**（`aluminum_leaf`、`copper_leaf`、
  `flax`、`slimeplant` 等）与 4 个无人引用的 blockstates（`drtech_crop_{flu,lapis,red_stone,xjc}.json`）。
- 清理后：`textures/blocks/crop` 剩 **176 个目录 / 739 张 PNG**；其中 52 个目录 / 279 张被当前 52 株作物使用，
  124 个目录是源端作物资产池（留给以后加作物，按 5.10 的“零重绘”策略保留，未删）。
- 语言文件：`cropqt.crop.<id>.name` 共 21 条，全部对应现役材料作物与 `fishing_rod_crop`，无死键。
- 约束：未编译、未启动；删除均为 git 已跟踪文件，可从版本库恢复。

## P6-4 单方块机器名字 / tooltip 对齐 GTNH（2026-11-14）

问题：五台单方块的 tooltip 与 GTNH 的 `cropsnh_tooltip.<machine>.*` 结构不对应——
管理器根本没有 lang 键（游戏里显示原始 key），其余四台是旧版文本（“输出 2 个种子”
“100 肥料点”“潜行 + 螺丝刀切模式”等），且名字键只有旧 5 档，扩档后高档显示 key。

做法：
- 新增 `api/CropTooltips.java`：键名生成 + 数值着色（EU 黄、流体白、百分比青、数字白，
  与 `TooltipHelper` 相同）、`DecimalFormat("0.##%")`。
- `MetaTileEntityCropMachine` 增加抽象 `getTooltipKey()`，并覆写 `getMetaName()`
  指向 `drtech_tooltip.<machine>.name.<tier>`；管理器单独覆写。
- 四台机器 `addInformation` 改为 GTNH 的行序与文本：`Machine Type: <类型>` + 逐行
  `.0`..`.n`；种子机/繁殖机带 `mv_warn`（tier <= MV）。
- 管理器：description + 类型 + `.tooltip.0..8`，数值取实际功率/半径/加成/三罐容量。
- lang：中英各 89 条 `drtech_tooltip.*`（含 seed/breeder/manager 1..12、
  gene/synth 4..12 的档位名）；删除 40 条旧 `drtech.machine.*` 死键。

CEU 细节：`MachineItemBlock.getTranslationKey()` 返回 `getMetaName()`，MC 再拼
`.name`，所以**物品显示名**的实际键是 `drtech_tooltip.<machine>.name.<tier>.name`；
tooltip 行键与 GTNH 完全一致（`drtech_tooltip.<machine>.<n>`）。

验证：`./gradlew compileJava` 与 `./gradlew build` 均通过；jar 内
124 个 cropQT class、89 条 `drtech_tooltip` 键。

## P6-5 单方块机器 overlay 对齐 GTNH（2026-11-14）

问题：四台机器的 overlay 指向 `machines/<name>`（无命名空间），`OrientedOverlayRenderer`
会解析到 `gregtech:...`；项目里虽然实际存在 `assets/gregtech/textures/blocks/machines/`
下的这四套贴图，但把本模组的贴图塞进 `gregtech` 命名空间既不符合 GTNH 的布局，也会和
GT 本体资源混在一起。管理器在 GTNH 里**有**自定义机壳图案（`MTECropManager.getTextureSet`
六面 = 该档 `MACHINE_CASINGS` + `Casing_CropHarvester_Cutter`，顶/底换成
`Casing_CropHarvester_Boxes`），这部分本轮一并补上。

做法：
- 从源端 `assets/cropsnh/textures/blocks/tileEntities/machines/<machine>/` 复制
  `OVERLAY_*_FRONT.png`、`_FRONT_ACTIVE.png`、`_FRONT_GLOW.png`、`_FRONT_ACTIVE_GLOW.png`
  到 `assets/drtech/textures/blocks/machines/<machine>/`，改名为 CEU 的
  `overlay_front.png`、`overlay_front_active.png`、`overlay_front_emissive.png`、
  `overlay_front_active_emissive.png`，动画贴图连同 `.mcmeta`（`frametime: 2`）一起复制。
- `Textures.init()` 四个路径加 `drtech:` 前缀；删除 `assets/gregtech/.../machines/` 下
  这四套重复贴图（`disassembly`/`industrial_apiary`/`type_filter` 保留）。
- 侧/顶/底面不铺 overlay：源端用的 `OVERLAY_SIDE_SCANNER` 等 GT5U 图标在 GTCEu 里不存在，
  GTNH 侧实际也是普通机壳；CEU 的 `SimpleMachineMetaTileEntity` 同样只画 front overlay。
- 管理器：GTNH 的 `MTECropManager.getTextureSet/getFront/getBack/getSides/getTop/getBottom`
  是「该档机壳 + 图案」两层；我们覆盖 `renderMetaTileEntity`，先让 `TieredMetaTileEntity`
  画 GTCEu 电压机壳，再对六个面分别叠 `Textures.CROP_HARVESTER_CUTTER`（前/后/左/右）与
  `Textures.CROP_HARVESTER_BOXES`（上/下），贴图来自源端
  `tileEntities/gt4/OVERLAY_CROP.png`、`OVERLAY_BOXES.png`，落在
  `assets/drtech/textures/blocks/casings/crop_harvester_{cutter,boxes}.png`。

验证：`./gradlew build` 通过；jar 内 4 台机器 × 4 张 front 贴图 + 2 个 `.mcmeta` 齐全，
`assets/gregtech` 下已无 crop 机器重复贴图。未启动游戏。

## P8 种子 / 产物贴图完全走 NH 路线（2026-11-14）

背景：种子 16 张已与源端逐字节一致，但缺第 9 种形状 `troll`；产物叶子仍走
`material_sets/dull/leaf` 灰度蒙版 + 材料色染色，源端 40 张 `materialLeaf` 彩色原图
虽在 `assets/drtech/textures/items/metaitems/crops/` 里却零引用（死资源）。

做法：

- 种子：新增 `CropQTMaterialIconType.troll`；`assets/gregtech/textures/items/material_sets/dull/troll1.png`、
  `troll2.png`（源端 `genericSeed/troll{1,2}.png` 逐字节拷贝）与 `dull/troll.json`；
  `ItemsInit` 的种子变体 8→9；`ItemCropSeed.SeedColorHandler` 改为 tintIndex 感知
  （第 0 层材料主色、第 1 层固定白色，还原源端 `ItemGenericSeed` 的双 pass）；
  `trollplant` 改用 troll 形状 + 颜色 `0x000000`。
- 产物：新建 `client/CropProduceModels.java`，在 `ModelBakeEvent` 里给带 `CropProperty`
  的材料的默认产物模型包一层 `ItemOverrideList`，命中时换成 `drtech:crop_produce/<art>`。
  20 个模型 `assets/drtech/models/item/crop_produce/*.json`：`layer0` 用全透明
  `drtech:items/blank`、`layer1` 用 `textures/items/metaitems/crops/<art>.png`（NH `materialLeaf`
  原图）；GT 只给 `tintIndex == 0` 染材料色，彩色原图不会被二次染色。
- 未采用“换图标集”方案：`FluidBuilder.determineTextures` 与 `Textures` 都从
  `material.getMaterialIconSet()` 取路径，给 20 种基础材料（铁/铜/…）换图标集会连带缺
  方块、流体、管道贴图。
- 约束：未编译、未启动；需进游戏验证第 9 种种子外观、20 个产物的物品栏/掉落物外观与 JEI 显示。

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

## P9 第二批测试作物（2026-11-14）

范围：作物表里“原版食物 4 条 + 盆景 6 条 + 原版花 9 条”。仍只补 `CropType` 数据/注册，
不动机器代码；未编译、未启动。

#### 改动文件

| 文件 | 动作 | 内容 |
| --- | --- | --- |
| `src/main/java/com/meowmel/cropQT/api/CropBonsais.java` | 新建（约 100 行） | 5 条盆景；`bonsaiRubber` 依赖 IC2 树苗/原木，跳过 |
| `src/main/java/com/meowmel/cropQT/api/CropVanillaFlowers.java` | 新建（约 100 行） | 8 条花：染料 75% + 花 25% |
| `src/main/java/com/meowmel/cropQT/api/CropRegistry.java` | 修改 | `potato`/`melon`/`pumpkin`/`cocoa`/`dandelion` 按源端改数值；`ensureRegistered()` 追加两个新类；`VANILLA_CROP_IDS` 增 13 个目录 |

#### 18 条

“改”= 复用既有 id 改数值；“新”= 新增 NH id。

| 来源 | ID | tier | 时长 | 阶段 | 土壤 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 改 | potato | 1 | 800 | 0~3 | farmland | 马铃薯×1 | 光照≥9，HASH |
| 改 | melon | 2 | 1200 | 1~4 | farmland | 西瓜×4 (66.66%) + 西瓜方块 (33.33%) | 光照≥9，grain 种子 |
| 改 | pumpkin | 2 | 1000 | 1~4 | farmland | 南瓜方块×1 | 光照≥9 |
| 改 | cocoa | 3 | 1700 | 1~4 | farmland | 可可豆×1 | 光照≥9，土壤改为耕地 |
| 改 | dandelion | 2 | 600 | 1~4 | dirtGrass | 黄染料 (75%) + 蒲公英 (25%) | HASH |
| 新 | bonsaiSpruce | 1 | 1600 | 1~3 | dirtGrass | 云杉树苗 30% + 云杉原木×10 60% | X |
| 新 | bonsaiBirch | 1 | 1600 | 1~3 | dirtGrass | 白桦树苗 30% + 白桦原木×10 60% | X |
| 新 | bonsaiJungle | 1 | 1600 | 1~3 | dirtGrass | 丛林树苗 30% + 丛林原木×10 60% | X |
| 新 | bonsaiAcacia | 1 | 1600 | 1~3 | dirtGrass | 金合欢树苗 30% + 金合欢原木×10 60% | X |
| 新 | bonsaiDarkOak | 1 | 1600 | 1~3 | dirtGrass | 深色橡木树苗 30% + 原木×10 60% + 苹果×2 5% | X |
| 新 | allium | 2 | 600 | 1~4 | dirtGrass | 品红染料 (75%) + 绒球葱 (25%) | HASH |
| 新 | azureBluet | 2 | 600 | 1~4 | dirtGrass | 淡灰染料 (75%) + 蓝花美耳草 (25%) | HASH |
| 新 | oxeyeDaisy | 2 | 600 | 1~4 | dirtGrass | 淡灰染料 (75%) + 滨菊 (25%) | HASH |
| 新 | pinkTulip | 2 | 600 | 1~4 | dirtGrass | 粉染料 (75%) + 粉郁金香 (25%) | HASH |
| 新 | redTulip | 2 | 600 | 1~4 | dirtGrass | 红染料 (75%) + 红郁金香 (25%) | HASH |
| 新 | orangeTulip | 2 | 600 | 1~4 | dirtGrass | 橙染料 (75%) + 橙郁金香 (25%) | HASH |
| 新 | whiteTulip | 2 | 600 | 1~4 | dirtGrass | 淡灰染料 (75%) + 白郁金香 (25%) | HASH |
| 新 | poppy | 2 | 600 | 1~4 | dirtGrass | 红染料 (75%) + 虞美人 (25%) | 旧 id `rose` 保留 |

所有条目 `dropChance` 都取源端默认 `0.95^tier`；喜好群系与源端逐条一致。

#### 与源端的差异

- 源端“替代种子”（`addAlternateSeed`）在 cropQT 没有对应字段，种子袋以 NBT `cropId` 绑定；
  树苗/花不写 `initVanillaSeedMap`——该表是 Item→id、不带 meta，无法区分 6 种树苗，
  也避免把原版花/树苗的种植行为整体改掉。
- 源端只有 `blueOrchid` 覆盖为 X 渲染，其余 8 种花是默认 HASH；本批按表统一 HASH。
- 西瓜的两个掉落源端权重 6666/3333，这里照抄权重。
- `bonsaiRubber` 依赖 `blockRubSapling`/`blockRubWood`/`IC2_Resin`，属于跨模组内容，跳过。

#### 静态核对（未编译）

- `CropType.Builder` 的 `addChanceDrop/texturePath/likedBiomes/dropChance` 均返回 Builder，链式调用可用；
  `Blocks.MELON_BLOCK`、`Blocks.LOG2`、`Blocks.YELLOW_FLOWER`、带 meta 的 `new ItemStack(Item,int,int)` 均存在。
- 贴图目录与帧数已核对：`potato`（8 帧 `stage_N`）、`melon/pumpkin/cocoa/dandelion`（`1..4` + `stage_0..4`）、
  `bonsai_*`（3 帧）、8 种花（4 帧）全部在位。
- 新 id 无冲突；`bonsaiOak` 与旧 `bonsai`、`poppy` 与旧 `rose` 沿用“新旧并存”约定。

#### 未验证（需进游戏）

图集是否拼上 13 个新目录、花的染料/花双掉落、盆景掉落 meta 是否正确、
种子袋在 JEI/创造栏的显示。

## P10 第三批测试作物：矿石莓 / 石中百合 / 怪物系（2026-11-14）

范围：矿石莓 12、怪物 12、石中百合 10，共 34 条。仍只补 `CropType` 数据/注册，
不动机器代码；未编译、未启动。

#### 改动文件

| 文件 | 动作 | 内容 |
| --- | --- | --- |
| `src/main/java/com/meowmel/cropQT/api/CropOreBerries.java` | 新建 | 7 条矿石莓（GT 材料能覆盖的 6 条 + 精华莓） |
| `src/main/java/com/meowmel/cropQT/api/CropStoneLilies.java` | 新建 | 9 条石中百合（`stone` 第一批已有） |
| `src/main/java/com/meowmel/cropQT/api/CropMobs.java` | 新建 | 10 条怪物系（`zomplant`/`enderbloom` 第一批已有） |
| `src/main/java/com/meowmel/cropQT/api/SubSoilRequirements.java` | 修改 | 新增 `cobalt`、`skull` 两条底土 |
| `src/main/java/com/meowmel/cropQT/api/CropRegistry.java` | 修改 | `ensureRegistered()` 追加三个新类；`VANILLA_CROP_IDS` 增 26 个目录 |
| `src/main/resources/.../textures/blocks/crop/slimeplant/` | 新增 | 从源端补 4 张贴图（这是唯一缺素材的怪物系作物） |

#### 矿石莓（7/12）

源端是 TiC 的莓子物品，本模组没有；产物换成对应金属的 **粒**（6 个莓子≈6 粒，
`cobalt` 源端本身就是 1 粒），精华莓换成经验瓶。tier / 6000t / 阶段 1~4 / 石头土壤 /
光照≤10 / 机器 MV 全部照源端。

| ID | Tier | 底土 | 掉落 | 群系 |
| --- | --- | --- | --- | --- |
| tinOreBerry | 4 | 锡块 | 锡粒×6 | MOUNTAIN/HILLS |
| aluminiumOreBerry | 5 | 铝块 | 铝粒×6 | COLD/DRY |
| copperOreBerry | 5 | 铜块 | 铜粒×6 | SAVANNA/HOT/SANDY |
| goldOreBerry | 5 | 金块 | 金粒×6 | MESA/SANDY |
| ironOreBerry | 5 | 铁块 | 铁粒×6 | MOUNTAIN/HILLS |
| cobaltOreBerry | 5 | 钴块 | 钴粒×1 | NETHER/DRY |
| essenceOreBerry | 5 | 头颅 | 经验瓶×1 | MAGICAL/DEAD |

跳过 5 条：`ardite`/`thaumium`/`void`/`thauminite`/`knightmetal` —— 这些金属只存在于
TiC / 神秘时代 / 暮色森林，GT 里没有同名材料，也没有可替代的方块。

#### 石中百合（9 条 + 已有 stone）

全部 tier 1 / 600t / 阶段 1~4 / 石头土壤 / X 形渲染 / vanilla 种子形状，
只有掉落、底土、群系不同；`clay`/`endStone` 850t + 机器 MV，`soulSand` tier 2/2000t/机器 HV。

| ID | 底土 | 掉落 |
| --- | --- | --- |
| sandLily | 沙子 | 沙子×4 |
| clayLily | 黏土块 | 黏土球×1（催化剂：黏土粉/黏土球） |
| netherStoneLily | 下界岩 | 下界岩粉×9 |
| endStoneLily | 末地石 | 末地石粉×2（催化剂：末地石粉） |
| redGraniteLily | 红花岗岩 | 红花岗岩粉×9 |
| blackGraniteLily | 黑花岗岩 | 黑花岗岩粉×9 |
| basaltLily | 玄武岩 | 玄武岩粉×9 |
| marbleLily | 大理石 | 大理石粉×9 |
| soulSandLily | 灵魂沙 | 灵魂沙×1（25%，催化剂：灵魂沙） |

#### 怪物系（10 条 + 已有 zomplant/enderbloom）

| ID | Tier | 时长 | 阶段 | 土壤 | 掉落 |
| --- | --- | --- | --- | --- | --- |
| goldfish | 4 | 600 | 1~3 | farmland | 生鱼 60%（源端自定义金鱼→替代） |
| inkbloom | 4 | 600 | 1~4 | dirtGrass | 墨囊 |
| spidernip | 4 | 2400 | 1~4 | farmland | 线 66.66% / 蜘蛛眼 16.67% / 蜘蛛网 16.67% |
| corpseplant | 5 | 800 | 1~4 | 灵魂沙 | 骨粉 62.5% / 腐肉 25% / 骨头 12.5% |
| blazereed | 6 | 3600 | 1~4 | netherrack | 烈焰粉 75% / 烈焰棒 25%（X 渲染，机器 MV） |
| corium | 6 | 800 | 1~4 | farmland | 皮革 |
| eggPlant | 6 | 1200 | 1~3 | farmland | 鸡蛋 60% / 羽毛 30% / 鸡肉 10%（光照≥9） |
| slimeplant | 6 | 1200 | 1~4 | dirtGrass | 黏液球（bonsai 种子形状） |
| creeperweed | 7 | 1000 | 1~4 | farmland | 火药 |
| tearstalks | 8 | 4800 | 1~4 | farmland | 恶魂之泪 66.66%（grain 种子形状） |

#### 与源端的差异

- 矿石莓：TiC 莓子 → GT 粒 / 经验瓶；`getEntityDamage()=1.0F`（踩踏伤害）本模组作物架没有
  对应机制，无法复刻。
- `goldfish` + `tearstalks` 的尖叫/音效行为、`corpseplant` 的 `graveyard` 土壤（本模组用灵魂沙）
  都是机制或方块层面的差异，不影响数据。
- 源端的 `alternateSeed`（替代种子）仍无对应字段，种子袋以 NBT `cropId` 绑定。

#### 静态核对（未编译）

- 三个新文件括号/花括号配平，`CropRegistry.register(new CropType.Builder(...)` 与 `.build());`
  数量一致（7 / 9 / 10）；全项目 61 个 Builder id 无重复；所有 `texturePath` 目录都存在。
- 新增 Builder 调用均为既有方法：`subSoil`、`duplicationCatalysts`、`machineBreedingRecipeTier`、
  `lightRequirementLess`、`addChanceDrop`。
- `Materials.Cobalt/Stone/Netherrack/Endstone/GraniteRed/GraniteBlack/Basalt/Marble/Clay`
  与 `Blocks.SKULL`、`Blocks.WEB`、`Blocks.SOUL_SAND`、`Items.EXPERIENCE_BOTTLE` 等均存在。

#### 未验证（需进游戏）

未装 TiC 时矿石莓只掉 GT 粒是否合理、石百合 X 渲染的朝向、灵魂沙/头颅底土的实际匹配、
怪物系作物在 JEI 的显示。
