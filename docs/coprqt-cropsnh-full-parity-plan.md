# coprQT × CropsNH 完全复刻对齐方案（机器 / 设备 / 机制）

> 版本 v1.0 · 2026-11-14
> 范围：机器、设备、方块设备、工具、生长/杂草/病害/繁殖/收获/环境机制、配方、NEI/JEI、配置。
> **不含**：具体作物种类的数值定义（保留现有 20 个材料作物 + 18 个原版作物的数据）。
> **含**：作物全部形态的模型与材质**直接复用 CropsNH**（3 种株型渲染器、各阶段 PNG、种子/产物/工具/NEI 图标），不重绘。
> 约束：本方案只做静态设计与移植清单，**不编译、不启动游戏**；验收以逐项代码/数值对照为准。

---

## 0. 结论摘要

cropQT 现在已经"长得像"CropsNH：机器数量接近、有作物架/土壤/杂交/掉落/工具。但机制层是**另一套模型**：

| 维度 | CropsNH | cropQT 现状 |
| --- | --- | --- |
| 作物架水肥 | 阈值式存储，`addWater(amount, threshold, max, simulate)`，每 growth tick 各扣 1 | 每 256 tick 按土壤组扣水肥，水肥只做正向加成 |
| 杂草 | 有 WeedEX 存储/消耗、spawnWeed/spreadWeed | 只有 `tryResistWeed` 概率，无 WeedEX |
| 病害/践踏 | 有 isSick/spreadDisease/trample/boot protection | 无 |
| 核心机器 | 一台 `MTECropManager` 管 `(7+4×tier)²` 区域（LV 为 11×11，UMV 为 55×55）内全部作物 | 拆成 Supervisor（水肥/除草）+ Harvester，且无杂草液、无电池、无收获 |
| 单方块 | 12 档（LV..UMV），固定数值 | 5 档（LV..IV），数值多处简化 |
| 工业农场 | 组件是**方块**，1~12 段结构，3 种模式，5 tick 模式配方 | 组件是 MTE，3~13 段，数值另写 |
| 基因提取/合成 | 用 GT 数据球 + 电路选择 | 自研 `CropGeneOrb`，无电路 |
| 工具 | 植物透镜、三种铲、植物药、WeedEX 罐 | 分析仪、施肥器、浇水壶 |

**完全复刻 = 1 次作物架重写 + 1 台核心机器新增 + 1 套农场组件重构 + 4 台单方块重写 + 工具/NEI/配方补齐。**

外观部分**零重绘**：3 种株型渲染器与全部阶段 PNG 直接取自 CropsNH（见 5.10），源端 143 目录/634 张已全量拷入，只需核对与回填缺帧。

复刻完成后的机器清单（与源端一一对应，6 个 MTE）：

1. `MetaTileEntityCropManager`（新增，取代 Supervisor + Harvester）
2. `MetaTileEntitySeedGenerator`（重写）
3. `MetaTileEntityCropBreeder`（重写）
4. `MetaTileEntityCropGeneExtractor`（重写）
5. `MetaTileEntityCropSynthesizer`（重写）
6. `MetaTileEntityIndustrialFarm`（重构）

外加：种子床（方块，12 档）、5 类升级组件（方块，各 12 档）、环境模块、工具 5 件、4 张伪配方表 + 4 个 JEI 分类。

---

## 1. 范围与验收口径

### 1.1 在范围内

- 作物架方块与其全部机制（存储、生长、杂草、病害、践踏、繁殖、收获、NBT、交互）。
- 6 台机器的槽位/流体/能量/时序/半径/并行/超频/掉落/UI/失败条件。
- 种子床、5 类升级组件、环境模块、农场结构。
- 工具与交互设备：植物透镜、铲（普通/强化）、植物药、WeedEX 罐、外置浇水壶兼容。
- 配方（合成 + 伪配方）、NEI/JEI 分类、配置项、TOP/Waila 信息。
- **作物模型与材质**：X/Hash/Flower 三种株型、全部生长阶段 PNG、种子/产物/工具/NEI 图标，全部直接取自 CropsNH（见 5.10）。

### 1.2 不在范围内

- 具体作物的**数值定义**（CropCard 子类、掉落表、生长时长、生物群系偏好等）；模型/材质不在此列，直接复用源端。
- CropsNH 的跨模组联动（Thaumcraft、Forestry、IC2、TiC、ExU、NEI/WAILA 等）——**全部不做，也不保留兼容位**。
- 作物侧同步执行：`docs/cropsnh-crop-table.md` 中“联动·不接入”的 60 条（整组联动 45 条 + 注册被 `ModUtils.*.isModLoaded()` 包裹 54 条，去重后 60 条）不移植；测试用例只从 121 条本体作物里选，已勾选 20 条。
- 酒类物品、装饰方块、CropsNH 的作物迁移器（CropMigrator）。
- 1.7.10 → 1.12.2 的写法差异本身（本方案按 GTCEu/GTQT API 落地）。

### 1.3 验收口径

每个条目按四级判定：

- **A 完全一致**：常量、公式、时序、槽位、UI、失败条件逐项相同。
- **B 行为一致、实现不同**：结果相同，API 写法不同（允许）。
- **C 有意偏离**：记录原因与影响（例如 GTQT 无某电压 hatch）。
- **D 未做**：列出缺口。

验收产物 = 本文档第 4~7 节的对照表 + 代码内 `// CropsNH parity: <file>:<line>` 注释 + 第 10 节清单逐项勾选。

---

## 2. 版本 / API 基线差异

### 2.1 平台差异

| 源端（CropsNH） | 目标端（cropQT / GTQT） | 处置 |
| --- | --- | --- |
| GT5U 1.7.10，`MTEBasicMachine` / `MTEExtendedPowerMultiBlockBase` | GTCEu 1.12.2，`SimpleMachineMetaTileEntity` / `MultiblockWithDisplayBase` | 保留 cropQT 基类，按源端逻辑重写方法体 |
| NEI | JEI | 分类数量一致，API 重写 |
| metadata 方块（`BlockSeedBed` 等直接 meta） | `IBlockState` + `PropertyInteger` | 每档一个 meta，注册名 `seed_bed` 等 |
| StructureLib / `getStructureLengthFromTrigger` | GTQT `DeclarativePatternBuilder` + `getPieceRepeat` | cropQT 已移植，沿用 |
| `VoltageIndex.UMV/UXV` | `GTValues.UXV/OpV` | **按数值索引翻译，不抄常量名**（见 2.2） |
| `GTValues.VP[tier]` | `GTValues.VA[tier]` | 数值相同（`V - V/16`），直接替换 |
| `getCapacityForTier(tier)`（GT5U 内部） | GTCEu 无同名方法 | 新增 `CropMachineTankCapacity.get(tier)`，见 2.3 |

### 2.2 电压档位映射（关键）

GTNH/GT5U 与 GTQT 的 `V[]` 数值完全一致，但 **UIV 之后的名称错位一档**：

| 索引 | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| GTNH/GT5U | ULV | LV | MV | HV | EV | IV | LuV | ZPM | UV | UHV | UEV | UIV | **UMV** | **UXV** | MAX |
| GTQT | ULV | LV | MV | HV | EV | IV | LuV | ZPM | UV | UHV | UEV | UIV | **UXV** | **OpV** | MAX |

因此：

- CropsNH `UMV` → GTQT `UXV`（索引 12）
- CropsNH `UXV` → GTQT `OpV`（索引 13）
- 机器 tier 范围（映射后）：
  - CropManager / SeedGenerator / CropBreeder：**GTQT LV..UXV**（1..12，12 档）
  - GeneExtractor / Synthesizer：**GTQT EV..UXV**（4..12，9 档）
  - 工业农场 / 种子床 / 升级组件：**GTQT MV..OpV**（2..13，12 档）

> 决策点 D1：GTQT 的 hatch / 线缆是否真的覆盖到 OpV 需要实现时确认（`MetaTileEntityHatch` 是通用 tier 类，理论上可用）。若不可用，农场与组件封顶 **UXV（MV..UXV，共 11 档）**，并在文档标记 C 级偏离。

### 2.3 容量函数（cropQT 当前用错）

CropsNH 的 `getCapacityForTier(tier)`（GT5U `MTEBasicMachine.java:1781`）：

| tier | 0 | 1 (LV) | 2 (MV) | 3 (HV) | 4 (EV) | ≥5 |
| --- | --- | --- | --- | --- | --- | --- |
| 容量 (mB) | 8000 | 16000 | 32000 | 64000 | 128000 | 256000 |

GTCEu `GTUtility.defaultTankSizeFunction` 是 8000/12000/16000/32000/64000（LV/MV/HV/EV/IV+），**数值不同**。cropQT 现在直接用了它 → 全部单方块容量**偏小**。新增：

```java
public final class CropMachineTankCapacity {
    public static int get(int tier) {
        return switch (tier) {
            case 0 -> 8_000;
            case 1 -> 16_000;
            case 2 -> 32_000;
            case 3 -> 64_000;
            case 4 -> 128_000;
            default -> 256_000;
        };
    }
}
```

---

## 3. 总差异矩阵

图例：✅ 已有且一致 / 🟡 已有但数值或语义不同 / ❌ 缺失 / 🔁 需重写

| # | 模块 | CropsNH | cropQT 现状 | 判定 | 处置 |
| --- | --- | --- | --- | --- | --- |
| 1 | 作物架水/肥存储 | 阈值 + maxStorage + simulate | 土壤容量，无阈值 | 🔁 | 重写 |
| 2 | 作物架 WeedEX | 150 上限、75 阈值、10/次 | 无 | ❌ | 新增 |
| 3 | 生长公式 | 营养分 + 抗性/病害 | 环境倍率 + 随机增量 | 🔁 | 重写 |
| 4 | 雨淋/闪电补水 | `addWater(11,100,101)` | 无 | ❌ | 新增 |
| 5 | 每 growth tick 扣 1 水 1 肥 | 有 | 每 256 tick 按土壤扣 | 🔁 | 重写 |
| 6 | 杂草 | spawn/spread + WeedEX | 抗性概率 | 🔁 | 重写 |
| 7 | 病害 | 3 个传播/治疗方法 | 无 | ❌ | 新增 |
| 8 | 践踏/保护靴 | 有 | 无 | ❌ | 新增 |
| 9 | 世界内杂交 | 邻居 + 抗性 + 统计变异 | `attemptBreeding` 但数值不同 | 🔁 | 重写 |
| 10 | 收获 | 1.03^gain、多轮、种子掉率 | 阶段重置式 | 🔁 | 重写 |
| 11 | 作物架交互 | 右键施肥/透镜/铲/药 | 部分 | 🟡 | 对齐 |
| 12 | CropManager | 22 槽、3 罐、半径 3+2×tier、双功率 | Supervisor + Harvester | 🔁 | 新增并取代 |
| 13 | SeedGenerator | 160t / 30EU / 36 每 stat / 2 输入 | 200t、100 点、2 输出 | 🔁 | 重写 |
| 14 | Breeder | 3/6 槽、144+72、40+10%/档 | 3/6 槽、概率/时长/EU 另算 | 🔁 | 重写 |
| 15 | GeneExtractor | 电路 1..4、GT 数据球、6000/3000 | 自研 orb、模式切换 | 🔁 | 重写 |
| 16 | Synthesizer | 4 orb、UUM、3A、orb 不消耗 | 自研 orb、4 槽 | 🔁 | 重写 |
| 17 | 工业农场结构 | 1~12 段、组件方块 | 3~13 段、组件 MTE | 🔁 | 重构 |
| 18 | 农场模式 | INPUT/FARM/OUTPUT，5 tick | 同，但无 0EU 语义 | 🟡 | 对齐 |
| 19 | 农场功率/超频 | OCGAU 自由超频 | 上限 8 次、MV 基准 | 🔁 | 重写 |
| 20 | 农场掉落 | IFDropTable 小数累计 | DropTracker 类似 | 🟡 | 对齐公式 |
| 21 | 种子床 | 方块 12 档 | 方块 11 档 | 🟡 | 补档 + 数值核对 |
| 22 | 升级组件 | 方块 ×5 类 ×12 档 | MTE ×5 类 ×10 档 | 🔁 | 改方块 |
| 23 | 环境模块 | 28 生物群系标签 | 已有 28 标签 | ✅ | 校验槽位规则 |
| 24 | 工具 | 透镜/铲/药/WeedEX | 分析仪/施肥器/水壶 | ❌ | 新增 5 件 |
| 25 | Potency 模型 | potency + maxStorage + units | `Map<K,Integer>` | 🔁 | 重写 |
| 26 | 配方表 | 4 张 fake map（禁 NEI） | 4 张空 map | 🟡 | 填 backend/frontend/loader |
| 27 | JEI/NEI | 5 机器 handler + 作物页 | 5 类 category | 🟡 | 对齐语义 |
| 28 | 配置 | growthMultiplier/杂草 4 项 | 部分 | 🟡 | 补齐 |
| 29 | 杂交数据 | duration/EU/催化剂/需求 | 只有 result/parents/weight | ❌ | 扩 API |

---

## 4. 机制层复刻规格（作物架）

> 目标文件：`src/main/java/com/meowmel/cropQT/tile/TileCropStick.java`（重写，~1600 行）
> 源文件：`E:\模组开发\资料库\CropsNH\src\main\java\com\gtnewhorizon\cropsnh\tileentity\TileEntityCropSticks.java`
> 附加接口：`api/ICropStickTile.java`（新增，供机器缓存与 NEI 使用）

### 4.1 常量表（照抄）

| 常量 | 值 |
| --- | --- |
| `TICK_RATE` | 256 |
| `BASE_GROWTH_SPEED` | 6 |
| `BASE_NUTRIENT_VALUE` | 5 |
| `MAX_WATER_BONUS_AT` | 100 |
| `MAX_FERTILIZER_BONUS_AT` | 100 |
| `SKY_ACCESS_BONUS` | 2 |
| `MAX_LIKED_BIOME_TAG_COUNT` | 2 |
| `LIKED_BIOME_BONUS` | 14 |
| `LOW_HUMIDITY_THRESHOLD` | 0.5 |
| `HIGH_HUMIDITY_THRESHOLD` | 0.8 |
| `HIGH_HUMIDITY_BONUS` | 14 |
| `NUTRIENT_POINT_SCALE` | 5 |
| `NUTRIENTS_NEEDED_PER_TIER` | 10 |
| `MAX_NUTRIENT_SCORE` | 55（由 `getNutrientsPerCycle(2,0.5,true,100,100)` 推出） |
| `MANUAL_FERTILIZER_MAX_STORAGE` | 100 |
| `MANUAL_FERTILIZER_MIN_THRESHOLD` | 10 |
| `MIN_FALL_DISTANCE_FOR_TRAMPLING` | 0.75 |

### 4.2 存储语义（三件套）

水、肥、WeedEX 共用同一套签名：

```java
boolean addX(int amount, int threshold, int maxStorage, boolean simulate);
```

规则（三处一致，逐字照抄）：

1. 当前存储 **大于** threshold → 直接返回 `false`（注意是 `>`，不是 `>=`）。
2. `simulate == true` → 返回 `true`，不改存储。
3. 否则 `storage = min(maxStorage, storage + amount)`，返回 `true`。

默认存储/阈值：

| 资源 | 上限 | 阈值 | 机器消耗 |
| --- | --- | --- | --- |
| 水 | 200 | 180 | 每次 1 点（growth tick 末扣 1） |
| 肥料（液/固） | 200 | 180（液）/ `max(cap/2, cap - potency/2)`（固） | 每次按 PotencyData |
| WeedEX | 150 | 75 | 每次 10 点 |
| 手动施肥 | 100 | 10 | 立即生效 |

### 4.3 生长公式（逐字）

```java
// 每周期营养分（0..100 输入）
public static int getNutrientsPerCycle(int likedBiomeTagsCount, float biomeHumidity,
                                       boolean canSeeSky, int waterStorage, int fertilizerStorage) {
    int liked = Math.min(likedBiomeTagsCount, MAX_LIKED_BIOME_TAG_COUNT);   // <=2
    waterStorage = Math.min(waterStorage, MAX_WATER_BONUS_AT);              // <=100
    fertilizerStorage = Math.min(fertilizerStorage, MAX_FERTILIZER_BONUS_AT);
    int humidityBonus = (int) (MathHelper.clamp((biomeHumidity - 0.5f) / 0.3f, 0f, 1f) * 14);
    return BASE_NUTRIENT_VALUE
            + (waterStorage + 9) / 10
            + (fertilizerStorage + 9) / 10
            + (canSeeSky ? SKY_ACCESS_BONUS : 0)
            + Math.max(humidityBonus, liked * LIKED_BIOME_BONUS);
}

// 生长速度（每 tick 的进度增量，未乘配置倍率）
public static int getGrowthRate(int nutrientPoints, int tier, int growthStat) {
    int np = nutrientPoints * NUTRIENT_POINT_SCALE;      // *5
    int need = tier * NUTRIENTS_NEEDED_PER_TIER;         // *10
    int base = BASE_GROWTH_SPEED + growthStat;           // 6 + growth
    if (np >= need) return base * (100 + (np - need)) / 100;
    return Math.max(base * (100 - (need - np) * 4) / 100, 0);
}
```

生长循环：

1. `getGrowthRate(...) <= 0` 且 `!passesResistanceCheck()` → `isSick = true`，本 tick 不生长。
2. 否则 `growthProgress += (int)(growthRate * ConfigurationHandler.growthMultiplier)`，上限 `cc.getGrowthDuration()`。
3. 每 growth tick 末尾：`fertilizerStorage > 0` → `--`；`waterStorage > 0` → `--`。
4. 降雨：`canLightningStrikeAt` 时 `addWater(11, 100, 101, false)`（101 是刻意的，下一 tick 会扣 1）。

**与现状的差异**：cropQT 现在是 `rand.nextInt(4) + growth` × 环境倍率 × 阶段阈值，完全不兼容；重写时删除 `EnvironmentCalculator`/`FarmNutrientModel` 的旧生长入口，`FarmNutrientModel` 只保留源端公式（供农场复用）。

### 4.4 杂草

- `spawnWeed`：`nextInt(weedSpawnChance) == 0`；若 `stats.resistance >= 31` 直接免；`weedsWipePlants` 为真时清除作物。
- `spreadWeed`：`nextInt(weedSpreadChance) - stats.growth <= 2`。
- WeedEX 在 `spawnWeed` 内部消耗 10 点（有存储才生效）。
- 杂草是 `CropWeed` 特殊作物，`isWeedPlant()` / `hasWeed()` 走同一套接口；`canHarvest()` 在杂草上恒 false。
- 配置：`enableWeeds`、`weedsWipePlants`、`weedSpawnChance`、`weedSpreadChance`（默认值实现时从 `ConfigurationHandler` 抄，不建议自定）。

### 4.5 病害 / 践踏 / 保护

- 病害：`isSick`、`transferDisease`、`cureDisease`（植物药，恢复后 `addFertilizer(25,25,25,false)`）、`spreadDisease`。
- 践踏：`canTrample`、`shouldTrampleFromRunning`、`shouldTrampleFromFalling(0.75)`；`onEntityCollision` 里对玩家做判定。
- 保护：`getBootProtection`（读靴子 NBT/附魔或配置表），有保护则 `damageEntity` 而不是毁作物。
- 这块与具体作物无关，可直接从源端整段搬运（注意 1.12.2 的 `Entity`/`AxisAlignedBB` API 同名，基本可直译）。

### 4.6 世界内杂交

- `canCross` / `canBreed` 判定相邻成熟作物、抗性、阈值。
- `attemptBreeding`：按源端轮询邻居，命中后 `getStatVariation` 生成子代统计，成功则种到空架/替换。
- 统计变异：源端 `getStatVariation`（均值 + 随机）与 cropQT 的 `inheritFrom`（均值 ±2）不同，**按源端重写**；`CropStats` 保留字段与 NBT 兼容。
- 机器繁殖（Breeder）走 `MutationRegistry` + `ICropMutation`，见 5.3。

### 4.7 收获

```java
double avgRounds = cc.getDropChance() * Math.pow(1.03, gain) * dropMultiplier;
double avgIncrease = (gain + 1) / 100.0;
```

- 每个掉落条目：`chance = weight / 10000.0`；基础数量 = `stackSize + avgIncrease`；期望 = `base * chance * avgRounds`。
- 实际掉落 = `floor(期望)` + 以小数部分为概率的 +1；`gain` 另有一次加成判定。
- `canHarvest()`：成熟 && 非杂草 && 生长条件满足 && 无 `MachineOnlyGrowthRequirement`。
- `harvest(dropMultiplier)` 重置 `growthProgress` 与贴图；玩家手动收获走 `doPlayerHarvest`。
- 铲子：未成熟但可生长时右键 = 掉种子并清空；左键 = 直接铲掉（掉作物 + 种子，强化铲按 resistance 加种子）。

### 4.8 NBT 与迁移

源端键：`mWater`、`mFertilizer`、`mWeedEx`、`mGrowthProgress`、`mSeed`、`mCrossCrop`、`mSick` 等（实现时逐键对照）。
cropQT 旧存档只有水/肥（来自土壤模型）→ 迁移规则：

- 旧 `water` 值 clamp 到 0..200 写入新 `mWater`；旧 `fertilizer` 同理。
- 旧 `growthProgress` 若单位是阶段（0..stages）→ 按 `stage / maxStage * cc.getGrowthDuration()` 折算；若已是 tick 则直接写入。
- 旧 `weedEx` 不存在 → 0。
- 迁移只做一次（`migratedV2` 标记），避免重复折算。

---

## 5. 机器层复刻规格

### 5.1 MetaTileEntityCropManager（新增，核心）

> 源：`tileentity/singleblock/MTECropManager.java`（992 行）+ `MTECropManagerGui.java`
> 目标：`machine/MetaTileEntityCropManager.java` + `client/gui/CropManagerGui.java`
> 取代：`MetaTileEntityCropSupervisor`（310..314）、`MetaTileEntityCropHarvester`（320..324）

**基本形态**：单方块，12 档 LV..UXV，直接继承 `TieredMetaTileEntity`（**不要**继承 `SimpleMachineMetaTileEntity` / `MetaTileEntityCropMachine`，源端 `MTECropManager` 不是配方机器），无配方表，自管 work 状态与 MUI2 GUI。

| 项 | 值 |
| --- | --- |
| 槽位 | 22：0..1 WeedEX 罐，2..5 肥料，6..20 输出，21 电池（实现时以源端 GUI 索引复核） |
| 流体罐 | 水 `tier*32000`、WeedEX `tier*750*2`、液肥 `tier*144*64*4`（mB） |
| 内部 EU 缓冲 | `32000*tier` |
| `maxEUInput` | `V[tier]` |
| `maxEUStore` | `V[tier] * (tier * V[tier])` |
| 输入安培 | 1 |
| 水平半径 | `3 + max(0, 2*tier)`（LV=5 → 11×11；UXV=27 → 55×55） |
| 垂直半径 | 2 |
| 更新周期 | 50 tick（`GLOBAL_UPDATE_RATE`） |
| 缓存重扫 | 空 100 tick / 非空 600 tick |
| 收获能耗 | `maxEUInput / 8` / 株 |
| 二次功能能耗 | `maxEUInput / 32` / 株 / 项 |
| 收获倍率 | `1.0 + 0.05*tier` |

**执行顺序（每 50 tick）**：`updateCropCache`（若到期）→ 二次功能（水 → WeedEX → 肥）→ 收获。二次功能必须先 simulate 扣能量再执行，避免"免费"浇水。

**水**：`crop.waterStorage > 180` 跳过；`drain = min(tank, 200 - storage)`；`addWater(drain, 180, 200, false)`；这里的 `drain` 直接作为水点，源端不在这一层做 potency 折算（potency 只在灌装进罐时换算）。
**WeedEX**：`storage > 75 || tank < 10` 跳过；扣 10 点；`addWeedEx(75,75,150,false)`；罐子从 2 个槽补充（每点耐久 = 10 点，`damage > maxDamage` 清槽）。
**肥**：优先液肥（阈值 180）；否则 4 个固体槽，按 `PotencyData`：`cap = getMaxStorage(200)`、`threshold = max(cap/2, cap - potency/2)`，扣 `unitsPerApplication`。

**收获**：需要 `harvestEnabled` 且 15 个输出槽有空间；先把上次溢出的 `dropOverflow` 塞回；逐株 `canHarvest` → `harvest(1.0 + 0.05*tier)` → 合并同物品 → 塞输出槽；塞不下进 `dropOverflow`（NBT 敏感比较：`areStacksEqual(a,b,false)`）。

**电池**：`storedEU/2 > cap/3` 充电；`storedEU < cap/3` 放电；槽 21。

**GUI（MUI2）**：左 2×3（WeedEX×2 + 肥×4），三条竖直 tank（水/WeedEX/液肥，带 tooltip），右 5×3 输出，底部 4 个 toggle（水/WeedEX/肥/收获）+ 电池槽；`supportsMuffler=false`、`supportsTopRightCornerFlow=false`。cropQT 已有 MUI2 基建，按源端布局重画即可。

**与旧机器关系**：Supervisor/Harvester 与 v2 互斥，默认**不注册**（只有 `legacyCropMachines=true` 时才注册旧机器）；JEI/配方中不出现。理由：源端没有这两台，保留会破坏"机器清单一致"。

### 5.2 MetaTileEntitySeedGenerator（重写）

> 源：`MTESeedGenerator.java`（312 行）

| 项 | 值 |
| --- | --- |
| 基础时长 | 160 tick |
| 基础 EU/t | 30（`TierEU.RECIPE_LV`） |
| 每 stat 肥料 | 36 点 |
| 输入/输出 | 2 / 1 |
| 流体 | 仅 `enrichedFertilizer`（倍率 1.0） |
| 肥料需求 | `max(1, (growth+gain+resistance) * 36 * multiplier)` |
| 输出 | 1 个同种子（分析状态保留） |
| 超频 | `calculateOverclockedNess(30, 160)` |
| 容量 | `tier < IV ? getCapacityForTier(tier)/10 : getCapacityForTier(tier)` |

细节：

- 扫 2 个输入槽找**已分析**种子；跳过 `crossingThreshold < 0` 的作物（只能合成的）。
- 消耗 `duplicationCatalysts`：输入里匹配的物品各扣 1。
- 输出槽不够 → 不启动。
- 流体直接按 mB 扣（`getFluidAmount` 结果就是 mB），源端在这里不做 potency 换算。
- 失败条件：无种子 / 未分析 / 无流体 / 流体类型错 / 输出满。

### 5.3 MetaTileEntityCropBreeder（重写）

> 源：`MTECropBreeder.java`（384 行）

| 项 | 值 |
| --- | --- |
| 输入槽 | `tier < HV ? 3 : 6` |
| 输出 | 1 |
| 肥料 | `max(1, tier*144*mult) + max(1, (gr+ga+re)*72*mult)` |
| 成功率 | `min(100, 40 + (tier - LV)*10)` % |
| 容量 | `tier < IV ? getCapacityForTier(tier)/5 : getCapacityForTier(tier)` |
| EU/时长 | 来自 `ICropMutation.getBreedingMachineRecipeEUt()/getBreedingMachineRecipeDuration()`，再 `calculateOverclockedNess` |
| 配方门槛 | mutation 的 machineEUt 必须 `<= V[tier]` |

流程：

1. 收集所有**已分析**、去重的父本，`>= 2` 个才启动。
2. `MutationRegistry.getPossibleDeterministicMutations(parents)`，按 `getParentCount()` 降序；取第一个 EU 可承受的。
3. `canBreed(parents, baseTE, catalystSlots)` 返回各槽消耗量（催化剂按 mutation 定义）。
4. 统计 = 各父本对应 stat 的平均值向下取整；`analyzed = true`。
5. 完成时先扣流体，再消耗输入各 1 个；`rand(100) < chance` 才产出子代（失败也消耗）。
6. 输出槽满 → 不启动；成功产出时若输出槽被占 → 失败并保留消耗语义（照源端）。

### 5.4 MetaTileEntityCropGeneExtractor（重写）

> 源：`MTECropGeneExtractor.java`（282 行）

| 项 | 值 |
| --- | --- |
| 输入/输出 | 1 / 1 + 特殊槽（数据球） |
| 电路 | 1=species，2=growth，3=gain，4=resistance |
| 时长 | 6000 tick（species）/ 3000 tick（其他） |
| EU/t | `VA[minTier]`，`minTier = clamp(cc.machineBreedingRecipeTier, EV, UXV)` |
| 消耗 | 1 种子 + 1 空白数据球 |
| 产出 | 1 个带 title/name 的 GT 数据球 |

- 必须已分析种子；`allowSelectCircuit = true`。
- 用 GT `MetaItems.DATA_ORB` + `BehaviourDataOrb` 写 title（species 写作物 id，stat 写 `growth/gain/resistance` 数值）。
- **替换** cropQT 的 `CropGeneOrb`：旧物品保留为"遗物"，加一条转换配方（旧 orb → GT 数据球）避免存档损坏；新机器/合成器只认 GT 数据球。

### 5.5 MetaTileEntityCropSynthesizer（重写）

> 源：`MTECropSynthesizer.java`（297 行）

| 项 | 值 |
| --- | --- |
| 输入/输出 | 4 / 1 |
| 安培 | 3 |
| 流体 | `Materials.UUMatter` |
| UUM 消耗 | `cc.tier*750 + (growth+gain+resistance)*100` |
| 时长 | `6000 + 6000 * ln(cc.tier) / ln(16)` tick（**作物 tier**：1→5min，16→10min） |
| EU/t | `VA[minTier] * 3` |
| 数据球 | 4 个（species/growth/gain/resistance），**不消耗** |
| 输出 | 1 个已分析种子 |

- 四个球都必须存在、kind 正确、stat ≥ 1；按 title 匹配（不是 NBT 顺序）。
- 种子必须存在对应 `CropCard`；机器 tier ≥ `clamp(cc.machineBreedingRecipeTier, EV, UXV)`。
- UUM 不足 / 球缺失 / 输出满 → 不启动。

### 5.6 MetaTileEntityIndustrialFarm（重构）

> 源：`tileentity/multi/MTEIndustrialFarm.java`（1512 行）+ `MTEIndustrialFarmGui.java`（201 行）+ `MTEIndustrialFarmItemStackHandler.java`（108 行）

#### 结构

```
head:   " cCc " / "cCCCc" / "cC~Cc" / "c   c"
body:   " gUg " / "g   g" / "csssc" / "     "     × N (1..12)
tail:   " cDc " / "cDDDc" / "cDDDc" / "c   c"
```

- `C` 外壳 + hatch（InputBus/InputHatch/OutputBus/Maintenance/Energy，`MultiAmpEnergy` 可选）
- `D` 同 C 但 `shouldSkip=true`（尾部 hatch 面）
- `g` 玻璃，决定 `glassTier`
- `U` 升级组件通道（EEU/GAU/FU/AHU/OCGAU + 木框架）
- `s` 种子床通道，决定 `upgradeTier`，并校验同档
- `~` 控制器

#### 段数与档位

- `MIN_SLICES = 1`、`MAX_SLICES = 1 + (MAX_TIER - MIN_TIER)`；外壳 tier `MV..OpV`。
- 实际段数 = `max(1, formed.getPieceRepeat(BODY_PIECE, 0))`，且 `<= BlockSeedBed.getMultiLength(tier)`。
- 结构校验：组件数量上限（EEU 2、FU 1、AHU 2、OCGAU 1；GAU 不限）、GAU/OCGAU 互斥、玻璃档位 ≥ MV 且全结构同档、能量仓电压 ≤ 玻璃档（源端 UMV 以上豁免，本仓库无该档玻璃，因此恒生效）。能量仓类型：禁激光仓；未装 OCGAU 时禁止多安仓；装了 OCGAU 时多安仓最多 1 个且不得与普通仓混用。源端没有「能量仓电压不得高于 upgradeTier」这条规则，cropQT 已删除。

#### 模式

| 模式 | 时长 | EU | 行为 |
| --- | --- | --- | --- |
| INPUT (0) | 5 tick | 0 | 从输入总线取 1 种子 + 1 对应底土放入内部槽 |
| FARM (1) | 100 tick | `expectedEUt` | 生长/消耗/产出 |
| OUTPUT (2) | 5 tick | 0 | 把种子/底土弹到输出总线 |

#### 功率与超频

```java
int base = BlockSeedBed.getBaseEUt(upgradeTier);
int power = base + base * (0.5*EEU + 1.25*GAU + 0.5*FU + 0.5*AHU);
```

- 有 OCGAU：用 `OverclockCalculator` 以 `recipeEUt = power`、`eut = maxInput`、`duration = Integer.MAX_VALUE` 求 OC 次数（源端不限 8 次，cropQT 的 `MAX_OVERCLOCKS=8` 要删）。
- 生长倍率 `×2^OC`；水/肥需求也 `×2^OC`。
- 无 OCGAU：`expectedEUt = power`、`expectedOCs = 0`。

#### 每周期

- 水/肥：`ceil(BlockSeedBed.getWaterConsumption(tier) * 2^OC)` 点；水按 HydrationRegistry，肥按 FertilizerRegistry；装了 FU 时只认 `fertilizerUnitInstance`（仅 enriched，无 potency 加成）。
- 营养分：`FarmNutrientModel.getNutrientsPerCycle(liked, rainfall, true, 200, hasFert ? 200 : 0)`，`liked` = 作物 `likedBiomeTags` ∩（当前生物群系标签 ∪ 环境模块标签）。
- 生长：`progressPerCycle = (1/ceil(growthDuration / growthRate)) * (100/256) * growthMultiplier`。
- 收获轮数：`1 + tier*0.2 + FU*0.5`，再 `*(1 + AHU*0.2)`。
- 期望掉落：`cc.dropChance * 1.03^gain * harvestRounds`；每条 `(stackSize + (gain+1)/100) * (weight/10000) * 该值 * progressPerCycle`。
- 掉落累计到 `IFDropTable`（`ItemStackMap<Double>`，NBT 敏感），整除部分当周期产出，小数留到下一周期（源端行为）。
- 虚空保护：模拟弹出，按 `VOID_NONE` 默认；输出满 → 本周期失败（不扣水肥/能量）。

#### 内部槽

`MTEIndustrialFarmItemStackHandler` 4 槽：0 种子、1 底土、2..3 环境模块。

- 种子：已分析、有 CropCard、机器 tier ≥ `minSeedBedTier`、无 SubSoilRequirement、生长速度 > 0。
- 底土：禁止手动插入（仅 INPUT 模式放入）。
- 环境模块：槽位 < EEU 数量，且模块 tag 非空。
- 运行中禁止插入；有底土时仅 OUTPUT 模式允许取出。
- 堆叠上限：种子/底土 = `seedCapacity`，环境模块 = 1。

#### GUI

- `MTEMultiBlockBaseGui` 派生；禁用 input separation / recipe lock / batch mode。
- 左侧 4 槽（种子、底土、2 环境），槽位禁用遮罩（`#80000000`）。
- 同步：5 类组件数量、`seedCapacity`、`upgradeTier`、`expectedOCs`、`expectedEUt`、模式、错误码。
- 模式按钮在右上；进度条；文本显示 EU/t、水/肥每周期、生长/收获倍率。

### 5.7 种子床与升级组件（改方块）

**BlockSeedBed**（源 12 档 MV..OpV，cropQT 现 11 档 MV..UXV）：

| 项 | 值 |
| --- | --- |
| `CAPACITY(tier)` | `(2*(3+2*tier)+1)^2 = (7+4*tier)^2` |
| `CONSUMPTION(tier)` | `ceil(capacity * 100 / 256)`（水、肥同值） |
| `getBaseEUt(tier)` | `VA[tier]` |
| `getMultiLength(tier)` | `tier - MV + 1` |
| `getHarvestRoundBonus(tier)` | `tier * 0.2` |

**5 类升级组件**（`CropsNHBlockIndustrialFarmTiredComponent` 派生，各 12 档）：

| 组件 | 上限 | 基础功耗增量 | 效果 | 最低 tier |
| --- | --- | --- | --- | --- |
| EEU 环境增强 | 2 | +50% | 每个提供 1 个环境模块槽 | MV |
| GAU 生长加速 | 不限 | +125% | 生长 ×(1+1.0/个) | MV |
| FU 肥料单元 | 1 | +50% | 生长 ×(1+0.5)、收获轮 +0.5、仅 enriched 液肥 | MV |
| AHU 高级收获 | 2 | +50% | 收获轮 ×(1+0.2/个) | MV |
| OCGAU 超频生长 | 1 | +0% | 允许农场超频（2^OC） | ZPM |

> 决策点 D2：组件必须从 MTE 改成**方块**（源端语义：`chainAllTiredComponents` 按方块+meta 匹配，且 MTE 无法被结构扫描成 5 类×12 档的"组件方块"）。旧 MTE ID 370..429 已释放，对应 MTE 类与注册在 P6 删除；现有存档里这些旧组件会成为无效方块。

### 5.8 环境模块

- 28 个生物群系标签变体，meta 0 为空白；`getBiomeTag(meta)` 非空才可用。
- 农场槽 2..3，数量受 EEU 数量限制；`ItemEnvironmentalModule` 现有实现基本对齐，校验命名/图标/标签集合与源端一致即可。

### 5.9 工具与设备

| 物品 | 行为 | 现状 |
| --- | --- | --- |
| 植物透镜 | 右键使种子 `setAnalyzed(true)` + 状态提示 | ❌ 新增 |
| 普通铲 | 左/右键：铲掉作物，未成熟可掉种子 | ❌ 新增 |
| 强化铲 | 同上，按 resistance 额外掉 0~N 种子 | ❌ 新增 |
| 植物药 | 治愈病害，+25/25/25 肥，100 次耐久 | ❌ 新增 |
| WeedEX 罐 | 750 点容量 → 75 次（10 点/次），阈值 90 | ❌ 新增 |
| 分析仪 | 扫描种子/作物（保留 cropQT 现有） | ✅ 保留 |
| 施肥器 | 右键施肥（保留） | ✅ 保留 |
| 浇水壶 | cropQT 自研，源端无（源端用 ExU 水壶） | 🟡 保留为扩展 |
| ExU 水壶兼容 | 每 5 tick 对半径内作物 `addWater(10,99,100)` | ❌ 可选（检测 ExU） |

> 决策点 D3：浇水壶不属于源端。保留但标注"扩展设备"，并在 tooltip/文档说明；若要求 100% 清单一致，可移入 `compat` 包并在默认配置关闭。

### 5.10 作物模型与材质（直接复用源端，不重绘）

> 结论：作物外观**不需要任何新美术**。CropsNH 的 3 种株型渲染器与全部阶段贴图按 1:1 移植；cropQT 已有对应实现，本节定义"照抄 + 校验"的口径。

| 源（`E:\模组开发\资料库\CropsNH\src\main\...`） | 内容 | cropQT 目标 | 处理 |
| --- | --- | --- | --- |
| `java/.../renderers/plantshape/XPlantRenderer.java` | 交叉株型（两片 45° 对角面） | `client/CropStickTESR` 的 `CROSS` 分支 | 几何/UV 逐常量照抄 |
| `java/.../renderers/plantshape/HashPlantRenderer.java` | 井字株型（四面贴边，甘蔗/地狱疣式） | `HASH` 分支 | 同上 |
| `java/.../renderers/plantshape/FlowerPlantRenderer.java` | 花坛株型（四面向外扩 2/16，高 12px） | `FLOWER` 分支 | 同上 |
| `java/.../renderers/blocks/RenderCrop.java` | 作物架 + 植物的组合渲染、病株着色 | `client/CropStickTESR` | 已移植，校验层叠顺序/亮度 |
| `resources/assets/cropsnh/textures/blocks/crops/**` | 143 个目录、634 张阶段贴图（`<crop>/<n>.png`，1 起） | `assets/drtech/textures/blocks/crop/**` | **直接复制**，保留目录名 |
| `resources/assets/cropsnh/textures/items/**` | 种子蒙版（9 形状 × 2）、材料叶 40 张、环境模块、工具图标 | 种子 → `assets/gregtech/textures/items/material_sets/dull/`；材料叶 → `assets/drtech/textures/items/metaitems/crops/` | 直接复制（种子按形状改名 `grains`→`grain`） |
| `resources/assets/cropsnh/textures/gui/**` | NEI 页、槽位遮罩、进度条、按钮 | `assets/drtech/textures/gui/**` | 直接复制 |

实现要点：

- **1.7.10 → 1.12.2 只换 API，不换几何**：`IIcon`→`TextureAtlasSprite`、`Tessellator`/`TessellatorV2`→`BufferBuilder`、ISBRH→TESR；顶点坐标、UV 顺序、`3.999/4.001` 防 z-fighting、`-2..18` 外扩、`maxY=12` 等常量逐字保留。
- 贴图命名两套都认：cropQT 的 `probeFrames` 已同时支持 `stage_N.png`（0 起，材料作物）与 `N.png`（1 起，源端原版作物）；新增作物只需把源目录整体拷入，不改名、不补帧。
- 阶段数由目录实际帧数决定（最多探 16 帧），`CropType.stages` 只作逻辑上限；渲染时把阶段夹到已有帧区间（cropQT 现有行为，保留）。
- 作物架本体、种子物品继续用 cropQT 现有 JSON 模型；只有**植物部分**走 TESR，与源端一致。
- 源端 `assets/cropsnh/models/cropSticks.tcn` 是 1.7.10 的 Techne 模型，1.12.2 不能直接加载；cropQT 已有等价 JSON（`crop_stick_single/double`），保持现状即可，不必转换。
- **现状（P5-6 已对齐）**：`CropStickTESR` 三种株型改成固定几何 + 整图 UV——`CROSS` 高 12/16、四角 2..14/16；`HASH` 整格高、面在 4/16 与 12/16；`FLOWER` 高 12/16、每个方向两段 `6..18` 与 `-2..10` 并带 `3.999/4.001` 深度错位；阶段只用于选帧，不再缩放高度或裁剪 UV。
- 现状：源端 634 张数字命名阶段图**已全量拷入**（抽检 509 张中 507 张与源端字节一致，差异仅 `lazulia` 4 帧，属源端版本差）。
  2026-11-14 清理过一次：删掉旧 137 作物时代遗留的 **95 个 `stage_*` 目录 / 587 张 PNG**（含 `aluminum_leaf`、`copper_leaf`、`flax` 等已删作物）
  与 4 个孤儿 blockstates（`drtech_crop_{flu,lapis,red_stone,xjc}.json`，贴图与代码引用都不存在）；
  现存 **176 个目录 / 739 张 PNG**，其中 52 个目录被当前 52 株作物使用，其余 124 个是源端作物资产池（留给以后加作物，零重绘）。
- 许可：材质随源端按 LGPL-3.0 署名（见 D8）；在 `docs/THIRD_PARTY.md`（或资源目录 README）记录来源、许可与是否修改。
- **种子（P8 已对齐）**：9 种形状 `vanilla/grain/spore/flower/magic/bonsai/botania/oreberry/troll` 的 18 张蒙版与源端
  `genericSeed/*s{1,2}.png` 逐字节一致，位于 `assets/gregtech/textures/items/material_sets/dull/`；
  染色由 `ItemCropSeed.SeedColorHandler` 按 tintIndex 分层（第 0 层材料主色、第 1 层白色），与源端双 pass 相同。
- **产物（P8 已对齐）**：源端 40 张 `materialLeaf` 彩色原图放在
  `assets/drtech/textures/items/metaitems/crops/`，由 `client/CropProduceModels` 用 `ItemOverrideList`
  在烘焙后挂到 20 个产物前缀的默认模型上（`assets/drtech/models/item/crop_produce/*.json`，
  `layer0` 全透明、`layer1` 彩色原图），不改材料图标集、不影响方块/流体/管道。
  不走图标集的原因：`FluidBuilder.determineTextures` 与 `Textures` 都按 `material.getMaterialIconSet()` 取路径。

验收：随机抽 10 个作物，在 1/2/3/4 阶段与源端截图对照株型、外扩、UV 朝向；材料作物（`stage_N`）与原版作物（`N`）各覆盖一半。

---

## 6. 注册、配方、JEI、配置

### 6.1 MTE ID 重排

实施前（`DrTechMetaTileEntities.java`）：300..304 种子机、310..314 Supervisor、320..324 Harvester、330..334 繁殖机、340..344 提取机、350..354 合成机、360 农场、370..419 组件。实施后（P6 收口）：300..353 + 360，370..429 不再注册。

目标（等价于源端 54 个单方块条目）：

| 机器 | 档位 | ID |
| --- | --- | --- |
| CropManager | LV..UXV（12） | 300..311 |
| SeedGenerator | LV..UXV（12） | 312..323 |
| CropBreeder | LV..UXV（12） | 324..335 |
| GeneExtractor | EV..UXV（9） | 336..344 |
| Synthesizer | EV..UXV（9） | 345..353 |
| IndustrialFarm | 1 台 | 360 |

- 旧机器（Supervisor/Harvester/旧 5 档单方块）与 v2 **互斥**：`legacyCropMachines=true` 时只注册旧机器（旧 ID、旧逻辑），`false` 时只注册新机器（上表 ID）。不能同时注册，否则 ID 冲突。
- 组件改方块后 370..429 空出，留作未来扩展；资源路径 `drtech:<machine>.<tier>` 保持可读。
- **存档兼容**：MTE 存盘用的是 `MetaId`（注册名 ResourceLocation），不是数字 ID；因此重排数字 ID 不会让旧机器变成未知方块，注册名保留即可继续读档。只有被删除的注册名（旧 `farm_part.*` 组件 MTE）会加载失败并变成空气——这是组件方块化的必然结果，已在 P6 记录。

### 6.2 伪配方表（4 张）

源 `CropsNHGTRecipeMaps`：

| 表 | 输入/输出（物品/流体） | 特殊 |
| --- | --- | --- |
| `fakeSeedGeneratorRecipes` | 2/1/1/0，min 1/1 | 禁用 NEI、种子 overlay |
| `fakeCropBreederRecipeMap` | 6/1/1/0，min 2/1 | 作物架 overlay |
| `fakeCropGeneExtractorRecipeMap` | 2/1/0/0 | 特殊槽、数据球 overlay |
| `fakeCropSynthesizerRecipeMap` | 4/1/1/0，min 4/1 | 3A、数据球/种子 overlay |

全部 `disableRegisterNEI()`，仅用于 CEU 自动 UI/IO 与 JEI 展示；真实判定在机器逻辑内。metadata key：`CROPSNH_CROP_METADATAKEY`（ICropCard）、`CROPSNH_CROP_MUTATION_METADATAKEY`（ICropMutation）。

### 6.3 伪配方加载器

| 加载器 | 要点 |
| --- | --- |
| `SeedGeneratorFakeRecipeLoader` | 遍历全部作物，跳过 `hideFromNEI`；`DEFAULT_ANALYZED` 种子、stackSize=0、去掉 gain/growth/resistance NBT；按 `duplicationCatalysts` 分组；跳过 `crossingThreshold<0`；每种允许流体各一条；`duration=160`、`eut=30`、`fluid=max(1, 31*3*36*mult)`；`nbtSensitive/ignoreCollision/metadata/fake` |
| `CropBreederFakeRecipeLoader` | 每个 mutation 一条，输入父本 + 催化剂，输出子代 |
| `CropGeneExtractorFakeRecipeLoader` | 4 种电路 × 代表作物/数据球 |
| `CropSynthesizerFakeRecipeLoader` | 4 球 + UUM → 种子 |

### 6.4 合成配方

按源 `GTRecipeLoader.postInit` 顺序逐条移植（不要自创）：

1. plantLens、spade、reinforcedSpade、cropSticks、plantCure、mug、poisonPowder
2. cropManager、seedGenerator、breeder、geneExtractor、synthesizer
3. industrialFarm、seedBed、advHarvesting、environmentalEnhancement、environmentalModule、fertilizerUnit、OCGAU

每条配方在代码里用 `// CropsNH GTRecipeLoader:<line>` 标注；实现后核对"配方数量 + 主要材料"两项，防止漏项。

### 6.5 JEI/NEI

源端 NEI 有 5 个机器 handler + 作物页 + 杂交页 + 变异池页 + 替代种子页。cropQT 已有 5 个 category，按下表对齐：

| 源 | cropQT 目标 | 说明 |
| --- | --- | --- |
| `NEICropsNHSeedGeneratorHandler` | `SeedGeneratorCategory` | 展示种子 + 催化剂 + 流体 + 160t |
| `NEICropsNHCropBreederHandler` | `CrossBreedingCategory` | 父本/子代/概率/时长/EU |
| `NEICropsNHGeneExtractorHandler` | `GeneExtractorCategory` | 4 电路 × 球输出 |
| `NEICropsNHSynthesizerHandler` | `SynthesizerCategory` | 4 球 + UUM |
| `NEICropsNHMachineHandler`（总） | 合并/移除 | 避免重复 |
| 作物页/变异池页/替代种子页 | 保留（已存在） | 补字段 |

### 6.6 配置

新增/对齐（`CropConfig`）：

- `growthMultiplier`（默认 1.0）
- `enableWeeds`（默认 true）
- `weedsWipePlants`（默认 true）
- `weedSpawnChance` / `weedSpreadChance`（默认值照抄源 `ConfigurationHandler`）
- `legacyCropMachines`（默认 false，兼容旧 ID）
- `enableCropManagerHarvest`（默认 true，方便调试）
- 机器 enable 开关（按需）

---

## 7. 文件级移植清单

> 路径均相对 `E:\模组开发\GTQT\DrTech\src\main\java\com\meowmel\cropQT\`（新增文件按此目录新建）。
> 源端根：`E:\模组开发\资料库\CropsNH\src\main\java\com\gtnewhorizon\cropsnh\`。

| # | 源文件 | 目标文件 | 动作 | 规模 |
| --- | --- | --- | --- | --- |
| 1 | `tileentity/TileEntityCropSticks.java` | `tile/TileCropStick.java` | 🔁 重写 | L |
| 2 | `api/ICropStickTile.java` | `api/ICropStickTile.java` | 新增接口 | S |
| 3 | `tileentity/singleblock/MTECropManager.java` | `machine/MetaTileEntityCropManager.java` | 新增 | L |
| 4 | `tileentity/singleblock/MTECropManagerGui.java` | `client/gui/CropManagerGui.java` | 新增 | M |
| 5 | `MTESeedGenerator.java` | `machine/MetaTileEntitySeedGenerator.java` | 🔁 重写 | M |
| 6 | `MTECropBreeder.java` | `machine/MetaTileEntityCropBreeder.java` | 🔁 重写 | M |
| 7 | `MTECropGeneExtractor.java` | `machine/MetaTileEntityCropGeneExtractor.java` | 🔁 重写 | M |
| 8 | `MTECropSynthesizer.java` | `machine/MetaTileEntityCropSynthesizer.java` | 🔁 重写 | M |
| 9 | `tileentity/multi/MTEIndustrialFarm.java` | `machine/MetaTileEntityIndustrialFarm.java` | 🔁 重构 | L |
| 10 | `MTEIndustrialFarmGui.java` | 内嵌/GUI 类 | 重写 | M |
| 11 | `MTEIndustrialFarmItemStackHandler.java` | 内嵌 handler | 重写 | S |
| 12 | `blocks/BlockSeedBed.java` | `block/BlockSeedBed.java` | 重写（12 档） | M |
| 13 | `Block{EnvironmentalEnhancement,GrowthAcceleration,Fertilizer,AdvancedHarvesting,OverclockedGrowthAcceleration}Unit.java` | `block/upgrade/*.java` | 新增 5 个方块类 | M |
| 14 | `items/ItemEnvironmentalModule.java` | 现有 `item/ItemEnvironmentalModule.java` | 校验 | S |
| 15 | `items/tools/ItemPlantLens.java` | `item/ItemPlantLens.java` | 新增 | S |
| 16 | `items/tools/ItemSpadeNH/Spade/ReinforcedSpade.java` | `item/ItemCropSpade.java` | 新增（一个基类 + 2 变体） | M |
| 17 | `items/tools/ItemPlantCure.java` | `item/ItemPlantCure.java` | 新增 | S |
| 18 | `items/tools/ItemWeedEX.java` | `item/ItemWeedEx.java` | 新增 | S |
| 19 | `loaders/PotencyLoader.java` + `api/IPotencyData` | `api/registries/PotencyData` + 三个 Registry | 🔁 重写 | M |
| 20 | `utility/IFDropTable.java` | `api/DropTracker.java` | 🟡 升级（double + NBT） | S |
| 21 | `api/ICropMutation.java` + `CropMutation` | 现有 `api/mutation/*` | 扩字段 | M |
| 22 | `recipes/CropsNHGTRecipeMaps` | `api/recipes/CropRecipeMaps` | 重写 4 表 | S |
| 23 | `recipes/backends` + `frontends` | 新增同名包 | 移植 | M |
| 24 | `loaders/SeedGeneratorFakeRecipeLoader` 等 4 个 | `api/recipes/fake/*` | 新增 | M |
| 25 | `loaders/GTRecipeLoader.java` | `handler/CropRecipes.java` | 移植配方 | L |
| 26 | `handler/ConfigurationHandler.java` | `handler/CropConfig.java` | 补键 | S |
| 27 | `compatibility/NEI/*` | `jei/*` | 重写分类 | M |
| 28 | `MTELoader.java` / `CropsNHItemList.java` | `DrTechMetaTileEntities` + 物品注册 | 重排 | M |
| 29 | `tileentity/TileEntityCropSticks` 的 disease/trample 段 | `tile/TileCropStick.java` 内 | 直接搬运 | M |
| 30 | `renderers/blocks/RenderCrop` + `renderers/plantshape/*` + `api/BasePlantRenderShape` | `client/CropStickTESR` + `api/CropRenderType` | ✅ 几何逐常量对齐（P5-6），层叠/亮度沿用 cropQT 现状 | S |
| 31 | `resources/assets/cropsnh/textures/**` | `resources/assets/drtech/textures/**` | 📦 直接复制（保留目录名 + 署名） | S |
| 32 | `resources/assets/cropsnh/lang/en_US.lang` 中的作物/工具名 | `resources/assets/drtech/lang/*.lang` | 只取需要的键，按现有命名 | S |

---

## 8. 分阶段实施计划

> 依赖关系：P1 是 P2/P4 的前置；P3 依赖 P1 的 ICropStickTile 与 P0 的 API 扩展；P4 依赖 P3 的数值工具；P5 最后做展示层。

### P0 基线与决策（0.5~1 天）

- [x] 新建 `api/CropConstants.java` 集中所有源端常量（逐项注释来源行号）。
- [x] 新建 `api/VoltageMap.java`：按索引映射，禁止散落 `VoltageIndex.UMV` 字面量。
- [x] 新建 `CropMachineTankCapacity`（2.3）。
- [x] Potency 模型重写：`api/potency/IPotencyData` + `PotencyData`，`PotencyRegistry` 改为数据表；`FertilizerRegistry` / `HydrationRegistry` / `WeedEXRegistry` 按源端数值登记。
- [x] 扩 `CropType`：`minSeedBedTier`、`machineBreedingRecipeTier`、`dropChance`、`spreadsWeeds`、`duplicationCatalysts`、`hideFromNEI`（`machineOnlyGrowthRequirement` 随 P4 定义接口，见进度文档）；现有 38 个作物走默认值，不改数据本身。
- [x] 扩 `CropMutation`：duration/EU/machineOnly 字段与 setter（requirements/NEI 随 P4 农场与 JEI 重写补齐）。
- [x] 确认 D1（OpV 可用性）与 D2（组件改方块）：组件已方块化后 OpV 只作为方块档位与语言键，不再依赖 OpV 仓；旧组件 MTE 于 P6-1 删除。
- [x] 建立 `docs/THIRD_PARTY.md` 记录 LGPL-3.0 与文件对应关系。
- [x] 资产清单核对（5.10）：3 种株型几何 1:1（P5-6）；阶段 PNG 沿用现有 38 个作物的资源，**不复刻**源端 244 目录/1326 张的作物种类资产（作物数据不在移植范围）。
- 验收：新增字段编译可通过（后续阶段），旧作物行为不变；资产目录无缺失帧。

### P1 作物架机制（3~5 天）

- [x] 重写 `TileCropStick`：存储三件套、生长、雨、杂草、病害、践踏、手动施肥。
- [x] 重写世界内杂交与统计变异。
- [x] 重写 `BlockCropStick` 交互（种植/收获/铲/透镜/药/WeedEX，P5-1 补齐工具）。
- [x] NBT 迁移（4.8）。
- [x] 移除旧 `EnvironmentCalculator` 生长路径（保留类给其它调用方则标注 deprecated）。
- 验收：4.1~4.8 全部 A/B 级；单株作物在 LV 下生长速率与源端一致（按公式手算）。

### P2 CropManager（3~4 天）

- [x] 新增 MTE + 12 档注册（300..311）。
- [x] 22 槽/3 罐/电池/双功率/缓存/收获/溢出全部移植。
- [x] GUI（MUI2）。
- [x] 旧 Supervisor/Harvester 转 legacy（默认关）。
- 验收：半径、功率、50t 顺序、15 槽输出、溢出、电池充放逐项对照。

### P3 四台单方块（4~6 天）

- [x] SeedGenerator（数值 + 催化剂 + 容量函数）。
- [x] Breeder（槽位/流体/概率/EU 门/统计均值）。
- [x] GeneExtractor（电路 + GT 数据球 + tier clamp）。
- [x] Synthesizer（4 球 + UUM + 3A + 时长）。
- [x] 4 张伪配方表 + 4 个 loader（P5-3）。
- [x] 旧 CropGeneOrb 迁移配方：新提取机/合成机直接产出与读取 GT `TOOL_DATA_ORB`（`CropGeneOrb` 只是其 NBT 包装），不存在独立的旧物品，无需迁移配方。
- 验收：每台机器的常量表逐项对照；失败条件齐全；JEI 分类可见。

### P4 工业农场（5~7 天）

- [x] 种子床 12 档（MV..OpV）+ 注册/资源/blockstate/语言（贴图为占位，P5 换图）。
- [x] 结构段数与输入并行：body 1..12、种子槽补满苗床容量、底土按差额补、种子/底土槽上限 = 苗床容量。
- [x] 5 类组件由 MTE 改成方块（12 档）+ 结构 glassTier 捕获与 hatch 校验（旧组件 MTE 注册暂留，P6 清理）。
- [x] 农场结构/校验/模式/功率/超频/水肥/掉落/GUI/handler 逐项复核（P4-3：TOP 与 GUI 信息、输出模式并行、handler 插取规则；GTCEu 无 `getInfoData`，改由 TOP/GUI 承载源端 scanner 字段）。
- [x] 环境模块槽位规则（槽位数 = 环境强化单元数，模块必须带生物群系标签，上限 1）。
- [x] 删除 cropQT 的 8 次超频上限与 MV 基准（OC 只由「组件档次基准耗电 vs 仓室电压」的落差决定；功率基准确认取 `VA[upgradeTier]`，与源端 `VP` 等值）。
- 验收：结构在 1/4/12 段三种形态下校验正确；功率公式手算对照；掉落期望值抽样对照。

### P5 工具、配方、JEI、配置（3~4 天）

- [x] 5 件工具 + 交互（铲/强化铲/透镜/植物药/WeedEX 罐，见进度文档 P5-1）。
- [x] 模型/材质核对（5.10）：3 株型几何、UV 朝向、外扩 2/16、阶段夹取、两套命名；不重绘（P5-6，`CropStickTESR` 已按源端几何重写）。
- [x] 合成配方整批移植（逐条标注行号，见 `recipes/CropCraftingRecipes.java`，P5-2）。
- [x] JEI 分类/页面字段（机器伪配方表 + 4 个 JEI 分类，P5-3；作物输出/土壤/杂交页沿用既有）。
- [x] 配置项（源端 crops/weeds 段的生长、杂交概率、肥料档位与 6 组属性变异数组，见进度文档 P5-4）。
- [x] TOP/Waila 字段（作物管理机与工业农场的完整读数，见进度文档 P5-5）。
- 验收：配方数量一致；JEI 页面可打开（静态检查类结构）。

### P6 清理与验收（1~2 天）

- [x] 删除旧组件 MTE（`MetaTileEntityFarmPart`、`IFarmPart`、`FARM_PART` ability 与 370..429 注册、对应 lang 键），见进度文档 P6-1。
- [x] 清理源端没有的遗留配置（`crossBreedChanceMultiplier`）与未使用代码（`getGlassesState`）。
- [x] 单方块机器 overlay 对齐 GTNH（4 台机器的 front 贴图含动画 `.mcmeta` 移入 `drtech:machines/*`；作物管理机补六面机壳图案「刀刃 / 箱子」），见进度文档 P6-5。
- [x] 物品 tooltip 对齐 GTNH（种子按 `ItemGenericSeed` 顺序、五件工具、分析仪沿用原键）、作物管理机 UI 重排（176×206，左 6 槽 / 三液柱 / 右 5×3 / 开关与电池）、基因提取机 8~12 档改名「基因测序仪」，见进度文档 P6-6。
- [x] 全量对照第 10 节清单（静态公式复算与逐项代码核对见进度文档 P6-2；需要进游戏才能确认的项保持未验证）。
- [x] 代码内 parity 注释审查：移植文件的类注释均标注源端类名/行号与差异（源端引用共 200 处，`grep -rn "源端" src/main/java/com/meowmel/cropQT` 可核）。
- [x] 不编译、不启动（按用户要求），只做静态审查与公式复算。

总工作量估计：**20~30 人日**（不含作物数据）。

---

## 9. 风险与决策点

| 编号 | 事项 | 风险 | 建议 |
| --- | --- | --- | --- |
| D1 | OpV hatch/线缆可用性 | 农场 12 档可能缺件 | 实现首日核对；缺则封顶 UXV，文档标 C |
| D2 | 组件 MTE → 方块 | 旧存档机器丢失、结构扫描全改 | 已改（P4-2）；MTE 存档靠注册名而非数字 ID，旧组件注册名随类删除后成为无效方块（P6-1 已记录） |
| D3 | 自研浇水壶 | 与源端清单不一致 | 保留为扩展，默认关闭或标注 |
| D4 | 作物架重写 | 所有作物平衡重置、旧存档数值失真 | NBT 迁移 + 版本化配置；先在测试存档验证 |
| D5 | GeneOrb → GT 数据球 | 旧物品失效 | 保留旧物品 + 转换配方 |
| D6 | MTE ID 重排 | 旧存档机器变未知方块 | 不需要：GTCEu 存的是 `MetaId` 注册名，数字 ID 重排不影响读档；只需保证未删除的机器注册名不变（P6-1 已核实） |
| D7 | 农场性能（55×55 直径内作物架轮询） | 卡顿 | 保留源端缓存策略（100/600t）+ 分帧；不要每 tick 扫 |
| D8 | 许可证合规 | 照抄代码的许可证 | CropsNH 为 **LGPL-3.0**（`LICENSE.txt`），DrTech 为 GPL-3.0；LGPLv3 是 GPLv3 的附加许可，可并入 GPLv3 作品。代码与**模型/材质**均按 LGPL-3.0 署名；移植文件头保留原作者/来源/许可声明，材质来源记入 `docs/THIRD_PARTY.md`；若单独分发移植文件，保留 LGPL-3.0 声明与可重链接要求 |
| D9 | 上游维护 | 源端仍在更新 | 记录移植基线 commit/日期；后续差异用 `parity` 注释与本文档追踪，不盲目跟随上游 |
| D10 | `getAvgDropRounds` 两套公式 | 农场与作物架产出不一致 | 统一到源端 1.03^gain 公式，删除 cropQT 的 `1+gain/8+...` |

---

## 10. 验收清单

### 机制

- [x] 水/肥/WeedEX 的 `>` 阈值判定与 simulate 语义一致（P6-2 逐行核对）。
- [x] `getNutrientsPerCycle` 在 (2,0.5,true,100,100) 下 = 55（P6-2 复算）。
- [x] `getGrowthRate` 在 np<need 时的负向斜率（×4）一致（P6-2 复算：np=2,tier=2,growth=6 → 7）。
- [x] 每 growth tick 扣 1 水 1 肥；雨 `addWater(11,100,101)`（P6-2 核对 `onGrowthTick`）。
- [x] 杂草生成/蔓延概率、resistance 31 免疫、weedsWipePlants（P6-2 核对 `spawnWeed`/`spreadWeed`）。
- [x] 病害传播/治疗；践踏 0.75 格（P6-2）；保护靴属源端碰撞伤害体系，作物数据不移植（`getEntityDamage` 默认 0），不实现。
- [x] 世界杂交统计变异（均值/随机）与源端一致（P5-4/P6-2）。
- [x] 收获 1.03^gain、多轮、gain 加成、种子掉率（P1-2/P6-2 复算 gain=10 → 1.3439 倍）。

### 机器

- [x] CropManager：22 槽、3 罐容量、半径 3+2t、50t 周期、/8 与 /32 功率、15 输出、溢出、电池（P6-2 逐项对照）。
- [x] SeedGenerator：160t/30EU/36 点/2 槽/容量 /10 规则/仅 enriched（P6-2 复核超频口径）。
- [x] Breeder：3|6 槽、144+72、40+10%、mutation EU 门、均值统计、失败也消耗（P6-2 修正超频除数）。
- [x] GeneExtractor：4 电路、6000/3000、tier clamp、消耗种子+球、GT 数据球 title（P6-2 补超频）。
- [x] Synthesizer：4 球、UUM 750/100、3A、时长 log16、orb 不消耗（P6-2 补超频）。
- [x] 农场：结构 1..12 段、3 模式、5t/100t、功率公式、OC 无上限、水肥 ×2^OC、掉落小数累计、虚空保护（P4-1/P4-3/P6-2）。
- [x] 种子床/组件：容量、消耗、基础 EU、5 类上限/加成/最低 tier（P6-2 复算 MV 225/88、OpV 3481/1360）。

### 展示与集成

- [x] 4 张伪配方表 + 4 个 loader；JEI 4 个机器分类字段齐全（P5-3）。
- [x] 合成配方数量与源端一致（逐条行号注释见 `CropCraftingRecipes` 类头与各段注释）。
- [x] 配置键齐全；legacy 开关生效（P5-4；`legacyCropMachines` 默认 false，旧 Supervisor/Harvester 仅在此开关下注册 1100/1110）。
- [x] 模型/材质：3 种株型的几何与源端 1:1（P5-6，固定几何+整图 UV）；作物阶段 PNG 沿用既有 38 作物资产，`docs/THIRD_PARTY.md` 记录来源与许可。
- [x] TOP/Waila 字段与源端 `getInfoData` 对齐（P4-3/P5-5；源端字段全部落在 TOP，因为 GTCEu 没有 `getInfoData` API）。

---

## 附录 A：关键数值速查

| 对象 | 数值 |
| --- | --- |
| 作物架 tick | 256 |
| 基础生长速度 | 6 + growth，营养分 ×5，需求 tier×10 |
| 水/肥上限 | 200 / 200 |
| 手动施肥 | 100 上限，10 阈值 |
| WeedEX | 150 上限，75 阈值，10/次 |
| CropManager 半径 | 3 + 2×tier |
| CropManager 水/WeedEX/液肥 | tier×32000 / tier×1500 / tier×36864 |
| 收获能耗 | V[tier]/8；二次 V[tier]/32 |
| 种子机 | 160t，30EU/t，36 点/stat |
| 繁殖机 | 144/档 + 72/stat，40+10%/档 |
| 提取机 | 6000t / 3000t，EV..UXV |
| 合成机 | UUM = 750×tier + 100×stats，3A |
| 农场周期 | 100t；模式切换 5t |
| 种子床容量 | (7+4×tier)²；消耗 ceil(cap×100/256) |
| 农场基础 EU | VA[tier] |

## 附录 B：与源端的"不移植"清单

- `CropMigrator`（作物迁移器）与迁移配方。
- 1.7.10 专属兼容（Thaumcraft/Forestry/IC2/TiC/UiE/mclib/findit/betterbuilderswands/extrautils）。
- 酒类物品、创造标签页布局、CropsNH 的 dumpers。
- 具体作物的**数值定义**（CropCard 子类、掉落、生长时长、生物群系偏好）。**模型/材质不在此列**：按 5.10 直接复用源端（3 株型 + 全部阶段 PNG + 图标），不重绘。

> 说明：第 5.9 节的 ExU 水壶兼容属于可选兼容位；若目标整合包没有 ExU，则不实现，工具清单以 5 件源端工具为准。
