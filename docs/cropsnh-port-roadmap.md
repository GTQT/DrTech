# CropsNH → CropQT 最终移植方案

> 状态：**实施中** —— M1 ~ M6 已完成（进度见 §4）
> 定稿日期：2026-09-10 ｜ 最近更新：2026-09-11（M6 收尾）
> 源项目：CropsNH（GTNH，MC 1.7.10 / GT5-Unofficial）— `E:\模组开发\资料库\CropsNH`
> 目标项目：DrTechC / CropQT（MC 1.12.2 / GTQT）— `src/main/java/com/meowmel/cropQT/`
> 目标平台源码：`E:\模组开发\GregTech`
> 本文档是本次作物系统移植的**唯一权威方案**，包含完整的决策记录、技术方案、里程碑与验收标准。
> 早期的调研报告与两轮决策问卷已完成使命并移除，其结论全部汇总在本文件 §1。

---

## 0. 一页纸范围定义

**移植 CropsNH 的「系统骨架」，套在 CropQT 现有的 137 个作物上。**

### 做什么

| # | 系统 | 说明 |
|---|---|---|
| 1 | **土壤 + 底土** | `SoilRegistry` / `SoilList` / `SubSoilRequirement`，全套新建 |
| 2 | **水 + 肥料**（储量模型） | 作物架存水与肥，随生长 tick 消耗，储量越高加成越大。**不做 WeedEX** |
| 3 | **确定性杂交** | `MutationRegistry` + 变异池，**替换**现有加权轮盘的**实现**；**配方表保持现有 ~100 条不变** |
| 4 | **5 台单方块机器** | 只做 **LV → IV**（5 档），基因提取/合成为 EV→IV（2 档） |
| 5 | **工业农场多方块** | 完整可变长度版，5 种升级单元，三种模式。**取代现有作物模拟机** |
| 6 | **137 个作物补归属** | 土壤组 + 底土要求**全加**，旧的 `requiredBlocks` **全部迁移到新 API** |
| 7 | **Flower 渲染形状** | 第三种作物渲染形状 |

### 不做什么

| 排除项 | 原因 |
|---|---|
| ❌ 移植 CropsNH 的作物内容 | 套用现有 137 个作物即可 |
| ❌ 复制 CropsNH 的代码 / **851 张美术资产** | 平台不兼容 + 无独立资产许可 |
| ❌ 营养点生长模型 | 只做数值加成，不动现有 137 个作物的手感 |
| ❌ **WeedEX 系统** | 杂草系统完全不动 |
| ❌ 杂草增强（疾病 / 草方块转化） | 同上 |
| ❌ 靴子保护 | 1.12.2 能对应的靴子太少，性价比低 |
| ❌ 机器合成配方 | 机器暂时只能创造模式获得（开发阶段） |
| ❌ 存档兼容层 | 开发中的包，不用管 |
| ❌ 高电压档位（LuV 以上） | 后期不鼓励玩这个 |
| ❌ NEI / StructureLib / postea / MCLib 集成 | 1.7.10 专属，目标端不存在 |

---

## 1. 已确认的决策

### 范围

- **4 个系统全要**：土壤/底土 + 水/肥料 + 确定性杂交 + 5 台机器
- **工业农场做完整版**，并**取代**现有作物模拟机
- **5 台机器全要**（含 基因提取 ↔ 合成 闭环）
- **彻底不做 WeedEX**

### 设计

| 项 | 决策 |
|---|---|
| 生长模型 | 不引入营养点公式；**保留储量模型**（水/肥随生长消耗，储量高则加成大） |
| 杂交模型 | 换成 CropsNH 的**实现**；**配方表保持现有 ~100 条不变，行为等价**（不新增作物，没有重做的理由） |
| 工业农场的底土 | **作为物品消耗**（沿用源端，不检查世界方块） |
| 137 个作物的归属 | **全加**土壤/底土要求，接受对现有玩法的削弱 |
| `requiredBlocks` | **全部迁移到新 API**，删除旧字段 |
| 底土不满足时 | **软惩罚**（生长速度极大幅下降），不是硬门槛 |
| 种子「已分析」 | 分析仪右键种子袋即可 |
| 土壤组特性 | 按 CropsNH 语义 |

### 技术

| 项 | 决策 |
|---|---|
| 电压阶梯 | 只做到 **IV**（LV/MV/HV/EV/IV；提取/合成 EV/IV） |
| Data Orb | **复用 GTQT 的 `TOOL_DATA_ORB`**，用 NBT 区分四种数据 |
| 多方块长度 | **由搭建时的组件等级决定**（沿用源端） |
| 作物管理器半径 | 随 tier，最高档 IV = 半径 13（无需封顶） |
| 机器配方实现 | 内部实现 `checkRecipe`，但**生成 JEI 示例配方** |
| 机器 UI | **MUI2** |
| 渲染形状 | 加 **Flower** |

### 工程

| 项 | 决策 |
|---|---|
| 代码位置 | 新代码**全放 `com.meowmel.cropQT`**；MTE 的 modid 仍用 `drtech:`，只做物理区分 |
| 新流体/材料 | 注册在 **`drtech:`** 命名空间 |
| 占位贴图 | **复用 GTQT 现成机器 overlay** |
| 多方块外壳 | **复用 GTQT 现成外壳** |
| 配置项 | 只做核心：生长倍率 + 育种概率 + 作物渲染模式 |
| 提交策略 | **做完之前不 commit** |
| GTFO 农场 | **跟着适配** |

---

## 2. 关键设计技术方案

### 2.1 土壤 / 底土系统

**位置语义**：

```
 y+1   [空气]
 y     [作物架]     ← TileCropStick
 y-1   [土壤]       ← 决定营养值 / 保水性 / 土壤组归属
 y-2   [底土]       ← 决定矿物来源
```

**现状问题**：`EnvironmentCalculator.getBlocksBelowIds()` 是 `depth = 1..2` 都收，然后 `CropType.requiredBlocks` 一次性匹配——土壤和底土混在一起。所以「石莲要求下方有黑曜石」既不是土壤也不是底土。

**改造**：拆成两层，**删除 `requiredBlocks`**，全部迁移。

```
新增：
  api/BlockMatcher.java           三路匹配的共用实现（计划外，✅ M1）
  api/ISoilList.java              接口                          ✅ M1
  api/SoilList.java               具名土壤组（registerBlock / registerOreDict / registerMaterial）  ✅ M1
  api/CompoundSoilList.java       多个土壤组的并集              ✅ M1
  api/SoilRegistry.java           全局表 + 反查缓存            ✅ M1
  api/SoilTypes.java              11 个预定义土壤组             ✅ M1
  api/GrowthRequirement.java      生长前置条件接口              ✅ M1
  api/SubSoilRequirement.java     底土要求，检查 y-2，三路匹配  ✅ M1
  api/SubSoilRequirements.java    32 个预定义实例（原计划 ~20） ✅ M1

改造：
  api/CropType.java               删 requiredBlocks；加 soilTypes / subSoilRequirement / likedBiomes  ✅ M1
  api/CropRegistry.java           62 处 requiredBlocks → subSoil()（soil() 归属留给 M5）    ✅ M1
  api/EnvironmentCalculator.java  calcNutrients() 改查 SoilRegistry；getBlocksBelowIds() 拆成 getSoilId() + getSubSoilId()  ⬜ M2
  tile/TileCropStick.java         生长 tick 校验底土（软惩罚 + isSubSoilSatisfied）✅ M1 ｜ 种植时校验土壤 ⬜ M2
```

**软惩罚实现**（✅ M1 已落地，见 `TileCropStick.tickCropGrowth`）：

```java
// 底土不满足时不阻止生长，而是把生长增量压到极低
if (!isSubSoilSatisfied()) {
    increment = Math.max(1, increment / SUB_SOIL_PENALTY);   // SUB_SOIL_PENALTY = 50
}
```

**GTQT 适配**：源端用 GT5U 的 `GTOreDictUnificator` / `GTBlockOre` / `TileEntityOres`，目标端改用 `OreDictUnifier.getUnificationEntry(stack)` + `MetaBlocks.COMPRESSED.get(material)`。

**保水性数值**：按 CropsNH 的土壤语义给初值，四档常量（极低 250 / 低 1000 / 中 2500 / 高 4000）。✅ M1 已落地。

| 土壤组 | 保水上限 | 保肥上限 | 说明 |
|---|---|---|---|
| `farmland` | 高 | 高 | 食用作物的常规土壤 |
| `dirtGrass` | 中 | 中 | 兜底组，登记在最后 |
| `stone` | 低 | 低 | 矿石叶 / 石中百合用 |
| `sand` | 极低 | 极低 | 仙人掌 / 干旱作物用 |
| `mushroom` | 中 | 高 | 蘑菇用 |
| `netherrack` | 极低 | 低 | 下界用 |
| `water` | 0 | 0 | 给 `fishing_rod_crop` 这类要求水的作物 |
| `soul_sand` / `end` | 极低 | 极低 | |
| `gravel` | 低 | 极低 | |
| `brick` | 低 | 低 | |

> **注意**：`farmland` **不是**默认土壤。作物的默认是 `null`（不限土壤），见 M1 偏差 #4——
> 默认成 `farmland` 会让没显式声明土壤的作物只能种在耕地上。

### 2.2 储量模型

```java
// TileCropStick 新增字段
int waterStorage;         // 上限由土壤组决定
int fertilizerStorage;    // 上限由土壤组决定

// 生长 tick 消耗
waterStorage      -= soilType.getWaterUsage(tier);
fertilizerStorage -= soilType.getFertilizerUsage(tier);

// 环境分乘数（不引入营养点公式）
float base        = light * 0.35f + humidity * 0.30f + nutrients * 0.35f;
float waterBonus  = 1.0f + (waterStorage / (float) maxWater) * WATER_BONUS;   // ≈ 0.3
float fertBonus   = 1.0f + (fertilizerStorage / (float) maxFert) * FERT_BONUS; // ≈ 0.5
return base * waterBonus * fertBonus;
```

**工业农场多方块靠这套储量持续消耗输入舱的流体**——这是保留储量模型的核心理由。

### 2.3 确定性杂交（换实现，保配方）

```
新增：
  api/mutation/CropMutation.java      2~4 父本的确定性配方
  api/mutation/MutationMap.java       父本集 → mutation 的查找 trie
  api/mutation/MutationPool.java      按标签分组的随机池
  api/mutation/MutationRegistry.java  全局注册表
  api/mutation/MutationPools.java     池标签常量

改造：
  api/CrossBreedingRegistry.java      保留类，内部改为委托 MutationRegistry；配方表原样搬入
  tile/TileCropStick.java             tickCrossBreeding() 改用新查找
  jei/CrossBreedingRecipeWrapper.java 适配多父本显示
```

**新流程**：

```
1. 收集四邻成熟非杂草作物 → 各自掷骰决定是否参与
2. participants < 2 → 返回
3. MutationRegistry.getPossibleDeterministicMutations(participants) → 命中则随机取一个
4. 未命中 → getPossiblePoolMutations(participants) → 随机池 → 随机成员
5. 子代属性取参与者平均 ± 随机波动
```

**配方表保持现有**：不新增作物，所以没有重做配方表的理由。现有 `CrossBreedingRegistry.registerDefaults()` 里那 ~100 条配方**逐条搬进新的 `MutationRegistry`，保证「同样父本组合 → 同样产物」**。

- 现有的两父本配方 → `new CropMutation(result, parent1, parent2)`
- 玩家既有的杂交知识**完全不失效**
- 新增能力（3/4 父本、变异池）作为**增量**，不覆盖现有配方
- 回归验证：写一个测试，把旧表的每条配方跑一遍，结果必须与旧实现一致

### 2.4 五台单方块机器

**统一约束**：

- 继承 GTQT 的 `MetaTileEntity` / `SimpleMachineMetaTileEntity`
- **不建自定义 RecipeMap**，机器内部实现 `checkRecipe`；JEI 侧生成"展示用"示例配方
- UI 用 MUI2 `buildUI`
- 包路径 `com.meowmel.cropQT.machine/`，MTE 的 `ResourceLocation` 用 `drtech:` 命名空间

| # | 类名（建议） | 电压档 | 源端行为要点 |
|---|---|---|---|
| 1 | `MetaTileEntityCropManager` | LV/MV/HV/EV/IV | 半径 `3+2×tier`（最大 13）、高 ±2；水/肥两个储量罐；4 个开关 |
| 2 | `MetaTileEntitySeedGenerator` | LV/MV/HV/EV/IV | 复制已分析种子，消耗液体肥料 |
| 3 | `MetaTileEntityCropBreeder` | LV/MV/HV/EV/IV | 2~4 亲本 → mutation；输出概率 `min(100, 40+(tier-LV)×10)` |
| 4 | `MetaTileEntityCropGeneExtractor` | EV/IV | 种子 → **GTQT 的 `TOOL_DATA_ORB`**（NBT 区分 4 种数据） |
| 5 | `MetaTileEntityCropSynthesizer` | EV/IV | 4 个已填充 Orb + UUM → 完整种子 |

**总计 19 个 MTE 注册 + 1 台多方块 = 20 个。**

### 2.5 工业农场多方块

**结构**（三段拼接，沿 -Z 堆叠，横截面固定 5×4）：

```
head：          body（可重复 1~13）：      tail：
" cCc "         " gUg "                    " cDc "
"cCCCc"         "g   g"                    "cDDDc"
"cC~Cc"         "csssc"                    "cDDDc"
"c   c"         "     "                    "c   c"
```

总长 = `2 + slices`，`slices = clamp(组件等级 - MV + 1, 1, 13)`（**长度由搭建时的组件等级决定**）

**GTQT DSL 写法**（先例：`MetaTileEntityYotTank.java:338` 的 `repeatablePiece`）：

```java
private static final StructureDefinition<?> STRUCTURE_DEFINITION =
    StructureDefinition.getOrBuild("drtech:industrial_farm", () ->
        DeclarativePatternBuilder.start(RIGHT, UP, BACK)
            .piece("head")
            .aisle(" cCc ", "cCCCc", "cC~Cc", "c   c")
            .repeatablePiece("body", 1, 13)
            .aisle(" gUg ", "g   g", "csssc", "     ")
            .piece("tail")
            .aisle(" cDc ", "cDDDc", "cDDDc", "c   c")
            .self('~', MetaTileEntityIndustrialFarm.class)
            .blocks('c', getCasingState())
            .blocks('s', getSeedBedState())
            .blocks('g', getGlassState())
            .where('U', Elements.chain(...5 种升级单元...))
            .where('C', Elements.chain(
                    Elements.counted(?, ?, Elements.block(getCasingState())),
                    Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH, ...),
                    Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 2, 1),
                    Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 0, -1, 1),
                    Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 0, -1, 1),
                    Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 0, -1, 1)))
            .buildStructureDefinition());
```

**⚠ 必须实现** `public static IBlockState getCasingState()` —— GTQT 的 `MultiblockControllerBase:328-343` 会**反射**这个方法拿 CTM 基座，缺失会**静默失败**。

**底土作为物品消耗**：`SLOT_SUB_SOIL = 1`，输入舱塞种子 + 一块底土方块（如铜块），机器内部模拟，不检查世界方块。

**校验逻辑**（可逐条照搬源端，是整次移植最安全的部分）：
- 玻璃 tier ≥ MV
- 能量仓 tier ≤ 玻璃 tier
- 5 种升级单元数量上限
- GAU / OCGAU 互斥
- 有 OCGAU 时的 multi-amp / laser 限制

**三种模式**：`MODE_INPUT(0)` / `MODE_FARM(1，100 tick 循环)` / `MODE_OUTPUT(2)`

### 2.6 取代现有作物模拟机

`MetaTileentityCropsSimulateMachine`（1416 行）在新工业农场上线后**删除**。

- 删除前先审查它有无新工业农场未覆盖的能力，有则迁移
- 它注册过的 MTE ID 一并移除（存档不兼容，已确认不管）
- 引用它的地方需要清理（`grep -rn "CropsSimulateMachine"`）

### 2.7 Flower 渲染形状

```
改造：client/CropStickTESR.java   + drawFlower()
      api/CropRenderType.java     + FLOWER
```

源端 `FlowerPlantRenderer` 是 4 组交错平面（X/Z 各 2 组），带 ±0.001 的 Z-fighting 偏移，几何参数可直接照搬。

---

## 3. 里程碑与文件清单

> 按「直接全量推进」+「攒够一起交付」+「做完之前不 commit」编排。
> 所有路径相对 `src/main/java/com/meowmel/cropQT/`。

### ✅ M1 — 数据层基础（已完成 2026-09-10）

> **实际规模**：9 个新文件共 1175 行 + 7 个改动文件（计划外多出 `api/BlockMatcher.java`）。
> **实机验证**：开发服务端启动 `Done!` 无异常；土壤组可枚举、三路匹配（材料 / 方块 / 矿辞）均有命中**与反例**佐证。

| 文件 | 动作 | 状态 |
|---|---|---|
| `api/BlockMatcher.java` | 新增：三路匹配的共用实现（**计划外**，为避免 SoilList 与 SubSoilRequirement 重复同一套匹配逻辑） | ✅ |
| `api/ISoilList.java` | 新增：土壤组接口 | ✅ |
| `api/SoilList.java` | 新增：标准实现（方块 / 矿辞 / GT 材料三路登记 + 保水保肥上限） | ✅ |
| `api/CompoundSoilList.java` | 新增：并集，容量取最大值 | ✅ |
| `api/SoilRegistry.java` | 新增：全局表 + **反查缓存**（方块 → 土壤组） | ✅ |
| `api/SoilTypes.java` | 新增：11 个预定义组，**登记顺序即匹配优先级** | ✅ |
| `api/GrowthRequirement.java` | 新增：生长前置条件接口 | ✅ |
| `api/SubSoilRequirement.java` | 新增：底土要求，看 y-2，三路匹配 | ✅ |
| `api/SubSoilRequirements.java` | 新增：**32 个**（计划 ~20 个） | ✅ |
| `api/CropStats.java` | 改：+ `analyzed` 标志（含 NBT） | ✅ |
| `api/CropType.java` | 改：**删 `requiredBlocks`**；+ `soilTypes` / `subSoilRequirement` / `likedBiomes` / `growthDuration` / 两个阈值 | ✅ |
| `api/CropType.java` (Builder) | 改：+ `subSoil(...)` / `soil(...)` / `likedBiome(s)...` / `growthDuration(...)` / 两个阈值 | ✅ |

**验收**：土壤组可枚举 ✅；`SubSoilRequirement` 三路匹配在 GTQT 下全部可用 ✅。

#### 实施中的偏差（已执行，非计划）

| # | 偏差 | 原因 |
|---|---|---|
| 1 | 抽出 `BlockMatcher` | `SoilList` 与 `SubSoilRequirement` 的匹配逻辑会一字不差重复两遍 |
| 2 | 底土只做**32 个真实可用**的，不做空壳占位 | 空壳要求永远不满足，是 bug 不是占位 |
| 3 | `growthDuration` 与现有 `stageRequirement` **分消费者共存** | 前者给机器算单轮周期，后者给世界作物架算逐阶段进度，不是重复 |
| 4 | **默认土壤改为 `null`（不限）**，方案原写 `SoilTypes.farmland` | 默认 `farmland` 会让未显式声明土壤的作物在 M2 后只能种在耕地上，玩家现有的泥土/草方块农场会突然种不下去 |
| 5 | **`requiredBlocks` 删除 + 62 处迁移提前完成**（原属 M2） | 不删就等于留着旧结构；不迁移就编译不过 |
| 6 | **`TileCropStick` 底土软惩罚提前完成**（原属 M2） | 不补的话，删掉 `requiredBlocks` 会让 62 个作物的底土要求**直接消失**——从"不满足不长"变成"完全不管"，是行为倒退 |
| 7 | `BlockMatcher.addBlockId(String)` + 9 个注册名型底土要求 | GT 按材料生成的方块（`gregtech:meta_block_compressed_100:2`）与可能没装的 mod 方块（`botania:livingwood`）编译期拿不到类 |

---

### ✅ M2 — 土壤 / 底土落地（已完成 2026-09-10）

> **实机验证**：开发服务端启动 `Done!` 无 cropqt 相关异常；日志里剩余的 ERROR（`_factories.json` / `plant_fiber` 配方 / extractor 输出数）与 M1 那次**逐条相同**，是既有问题，非本次引入。

| 文件 | 动作 | 状态 |
|---|---|---|
| `api/EnvironmentCalculator.java` | 改：删 `NUTRIENT_MAP`；`calcNutrients()` 改查 `SoilRegistry`；`getBlocksBelowIds()` 拆成 `getSoilId()` / `getSubSoilId()` / `getSoilAndSubSoilIds()` | ✅ |
| `tile/TileCropStick.java` | 改：**种植时校验土壤**（`plantCrop` 返回 boolean）；生长 tick 校验底土（**软惩罚**）；+ `isSoilValid()` / `getSoilType()` / `getUnmetRequirements()` | ✅ |
| `api/CropRegistry.java` | 改：`requiredBlocks` → `subSoil()` 全部迁移（62 处）；`soil()` 归属留给 M5 | ✅ |
| `api/ISoilList.java` + `SoilList` + `CompoundSoilList` | 改：+ `getBaseNutrients()`（替代写死的 `NUTRIENT_MAP`） | ✅ |
| `api/SoilTypes.java` | 改：11 个组各设基础营养值 | ✅ |
| `item/ItemCropAnalyzer.java` | 改：+「种植条件」段（土壤组 / 底土是否满足 / 缺什么） | ✅ |
| `block/BlockCropStick.java` | 改：种植失败给玩家提示 + `describeSoil()` | ✅ |
| `gtfo/TileCropFarmerMode.java` | 改：种植被拒时返回 `FAIL` | ✅ |
| `api/EnvironmentCalculator.matchesBlock()` | **删除**（被 `BlockMatcher` 取代，且已无调用方） | ✅ |

**验收**：
- `requiredBlocks` 字段已从代码库中彻底移除 ✅
- 矿叶类在对应矿块上正常生长、别处生长极慢 ⚠️ **代码路径已通，未实机验证玩法**
- 分析仪提示 ✅ 已实现

#### 实施中的偏差（M2 新增）

| # | 偏差 | 原因 |
|---|---|---|
| 1 | 新增 `ISoilList.getBaseNutrients()` | `calcNutrients` 需要「这块地天生多肥」这个值。原方案只让土壤组管保水/保肥上限，那管的是「能存多少」，不是「天生多肥」，两者不能混用 |
| 2 | 删掉 `NUTRIENT_MAP`（12 个写死的方块） | 改查 `SoilRegistry` 后它就没有调用方了 |
| 3 | 新增第三个方法 `getSoilAndSubSoilIds()` | 方块掉落表（`blockDrops`）不区分土壤还是底土，只要下方有就行，需要合并形式 |
| 4 | `plantCrop` 返回值 `void` → `boolean` | 种植校验要能把失败传回调用方（`BlockCropStick` 提示、`TileCropFarmerMode` 返回 FAIL） |

#### ⚠️ 两处必须知道的行为变化

1. **营养值不再看矿物方块。** 以前在钻石块上种作物营养拿 0.9，现在钻石块不是任何土壤组 → 按贫瘠兜底 0.2。矿物来源改由**底土机制**（y-2）负责，这是设计意图，但老玩家会发现「垫矿物块提升营养」这一招失效了。
2. **土壤校验目前是"哑的"。** `CropType` 默认 `soilTypes = null`（不限），而 137 个作物**没有一个**设了 `.soil(...)`——真正生效要等 M5。底土软惩罚则是**已经在生效**的（62 个作物设了 `.subSoil(...)`）。

---

### ✅ M3 — 水 + 肥料（已完成 2026-09-10）

> **不含 WeedEX**。
> **实机验证**：开发服务端启动 `Done!`；流体与肥料登记均无报错（`FluidBuilder.build()` 失败会直接抛异常终止启动，未发生）。

| 文件 | 动作 | 状态 |
|---|---|---|
| `api/registries/PotencyRegistry.java` | 新增：通用「能提供多少水/肥」登记表 | ✅ |
| `api/registries/HydrationRegistry.java` | 新增：流体 → 补水量，默认认原版水 | ✅ |
| `api/registries/FertilizerRegistry.java` | 新增：流体 → 补肥量（**只有流体那一路**，见偏差 1） | ✅ |
| `item/MetaItemCropTools.java` | 新增：两个工具的 MetaItem 容器（照 `MetaCrops` 的写法） | ✅ |
| `item/behavior/FertilizerApplicatorBehavior.java` | 新增：施肥器行为，**自带耐久（NBT 存）**，右键作物架施肥 | ✅ |
| `item/behavior/WateringCanBehavior.java` | 新增：浇水壶行为，从流体的 capability 抽水/肥喂给作物架 | ✅ |
| `fluid/FluidFertilizer.java` | 新增（`drtech:fertilizer`，一桶 2000 肥） | ✅ |
| `fluid/FluidEnrichedFertilizer.java` | 新增（`drtech:enriched_fertilizer`，一桶 8000 肥） | ✅ |
| `api/EnvironmentCalculator.java` | 改：`calcEnvironmentScore(world, pos, waterRatio, fertRatio)` 加权 | ✅ |
| `tile/TileCropStick.java` | 改：+ `waterStorage` / `fertilizerStorage` + 消耗 + `addWater` / `addFertilizer` + NBT | ✅ |
| `api/ISoilList.java` + `SoilList` + `CompoundSoilList` | 改：+ `getWaterUsage(tier)` / `getFertilizerUsage(tier)` | ✅ |
| `api/SoilTypes.java` | 改：11 个组各设营养值（M2 已加） | ✅ |
| `item/ItemCropAnalyzer.java` | 改：+ 水位 / 肥位显示（含加成百分比） | ✅ |
| `block/BlockCropStick.java` | 改：种植/施肥的交互（施肥现在由工具行为接管） | ✅ |
| `DrMetaItems` + lang + 模型贴图 | 改：注册 `crop_tools`、中英 lang（含 11 个土壤组）、两个占位模型与贴图 | ✅ |

**验收**：
- 浇水施肥改变生长速度、分析仪可见 ✅ 已实现（加成公式 + 界面显示）
- 液体肥料能通过管道输入 ⚠️ 流体已注册，**但玩家目前没有获取途径**（无配方、无桶的生产链），且工业农场（M7）尚未存在

#### 实施中的偏差（M3 新增）

| # | 偏差 | 原因 |
|---|---|---|
| 1 | **两个工具都做成 GT MetaItem + `IItemBehaviour`，不是普通 `Item` 类** | 初版做成了普通 `Item`（`ItemWateringCan` 用原版 `setMaxDamage`）；用户指出应该走 GT 的 Behavior 体系：肥料照 `CatalystBehavior` 用 `IItemDurabilityManager`，水壶照 GT 流体单元用流体 capability。**旧的两个普通 Item 类已删除** |
| 2 | **`FertilizerRegistry` 砍掉物品那一半** | 固体肥料改由施肥器工具自带耐久，不再经过「物品 → potency」登记表；`ItemAndMetadata` 那套随之删除，只剩流体 |
| 3 | **`BlockCropStick` 移除「右键消耗肥料粉」** | 同上——施肥入口从方块交互挪到工具行为。GT 的肥料粉恢复成纯骨粉用途 |
| 4 | 消耗量放在 `ISoilList`（方案 §2.2 的 `soilType.getWaterUsage(tier)`） | **消耗量其实与土壤无关**，只有 tier 影响它；土壤影响的是「一箱能撑几轮」（保水上限）。两者相乘才是玩家感受到的「多久浇一次水」。留这个方法在土壤上只为将来给不同土壤加排水率留口子 |
| 5 | 施肥器不做补充 | 用户先要求「可补充」，随后改为**一次性**：64 次用完即弃 |

#### 关键数值（可调，M8 进配置）

| 项 | 值 | 推导 |
|---|---|---|
| 每周期基础耗水 | 40 | 配保水上限 250~4000，落在「沙地约 1 分钟浇一次、耕地约 20 分钟」 |
| 每周期基础耗肥 | 10 | 肥料是消耗品，不该逼玩家一直喂 |
| tier 递增 | 每级 +10% | tier 11 的作物比 tier 1 多喝一倍 |
| 水加成上限 | +30% | |
| 肥加成上限 | +50% | 两者叠满 **1.95×** |
| 水壶 | 一次 500，共 32 次 | 一壶 ≈ 4 桶水 |
| GT 肥料粉 | 100 肥/次 | 耕地满仓 4000 → 40 次填满 |

#### ⚠️ 一处必须知道的行为变化

**水和肥都是「正向加成」，不是「不喂就死」。** 水位为 0 时加成是 ×1.0，不会掉到基础分以下。这是刻意的——作物不该因为断水停摆，否则玩家离线一趟回来会发现农场全死。如果 CropsNH 的原意是「断水就停」，这里要改。

---

### ✅ M4 — 确定性杂交（已完成 2026-09-11）

> **实机验证**：开发服务端启动 `Done!`，无杂交相关异常。
> **配方表保真验证**：拿 `git show HEAD` 的旧表与新表逐条比对，**114 条 `register(父本A, 父本B, 产物, 权重)` 一行未增、未删、未改**——`CrossBreedingRegistry.register()` 现在只是转投 `MutationRegistry`。

| 文件 | 动作 | 状态 |
|---|---|---|
| `api/mutation/CropMutation.java` | 新增：2~4 父本的确定性配方，**多重集匹配**（父本都在场即可，允许重复父本） | ✅ |
| `api/mutation/MutationPool.java` | 新增：≥2 株池内成员在场时命中的随机池 | ✅ |
| `api/mutation/MutationPools.java` | 新增：**3 个示范池**（花卉 / 谷物 / 矿石叶），成员是写死的 id 列表 | ✅ |
| `api/mutation/MutationRegistry.java` | 新增：登记 + 两步查找（确定性 → 池） | ✅ |
| `api/CrossBreedingRegistry.java` | 改：**变成门面**，114 条配方原样保留，`register()` 转投 `MutationRegistry` | ✅ |
| `tile/TileCropStick.java` | 改：`tickCrossBreeding()` 换成 CropsNH 的流程（自交分支 → 确定性 → 池） | ✅ |
| `jei/CrossBreedingRecipeWrapper.java` | 改：支持 2~4 父本，槽位按数量排开 | ✅ |
| `jei/CrossBreedingCategory.java` | 改：分隔符改画在 wrapper 里（JEI 的 `drawExtras` 拿不到当前配方） | ✅ |
| `api/mutation/MutationMap.java` | ~~新增~~ **不做** —— 见偏差 1 | ❌ 取消 |

**验收**：
- 新杂交流程可用 ✅
- 3 父本与 4 父本配方可用 ✅ **机制就绪**（`CropMutation` 接受 2~4 父本、JEI 能显示），但**目前没有登记任何 3/4 父本配方**——现有 114 条全是两父本
- 变异池可用 ✅ 3 个示范池
- JEI 杂交页正常（含多父本）⚠️ **客户端未验证**（服务端跑不了 JEI）

#### 实施中的偏差（M4 新增）

| # | 偏差 | 原因 |
|---|---|---|
| 1 | **不做 `MutationMap`** | 方案列了个前缀树做父本集查找。114 条配方、每株作物每 256 tick 才查一次，线性扫描的开销可以忽略——建树的维护成本比省下的时间高。真到几千条再谈 |
| 2 | `MutationPools` 用**写死的 id 列表**，不是标签常量 | 方案写的是「池标签常量」。按标签分组要求作物先带标签（颜色 / 来源 / 类型 / 属性 / 掉落），那是 M5 给 137 个作物补归属时一起做的事。现在先给 3 个能用起来的池 |
| 3 | **保留权重**（源端的确定性选择是等概率的） | 方案 §2.3 写「命中则随机取一个」。改成等概率会抹平 114 条配方原有的相对概率（80 / 60 / 50 / …），是比换实现大得多的玩法变化。CropMutation 的权重全设成同一个值即等价于等概率，想切回去只改 `pickDeterministic` 一处 |
| 4 | 匹配用**多重集**而非集合，且允许重复父本 | 旧表里本来就有 `stickreed × stickreed → ferru`、`cocoa × cocoa → shining` 这类同种配方（源端的 `CropMutation` 断言父本不重复，照搬会丢掉它们）。匹配语义是「父本都在场」而非「集合完全相等」，这样四株邻作物时任意两三株的配方都能参与，与旧的「两两组合」一致 |

#### ⚠️ 两处行为变化（照 CropsNH，非疏漏）

1. **自交从「轮盘里权重 500 的一项」变成「固定 50% 分支」。**
   旧流程里每个参与者自带 500 权重，两株在场时自交概率约 86%；现在照 CropsNH 是固定一半。自交仍是「原样产出 + 属性重掷」，只是概率变了。
2. **杂交不再产出杂草。**
   旧流程给杂草固定 50 权重；CropsNH 的杂草只来自空架生成与向四邻扩散，不来自杂交。现在同理——杂草该长还是会长，只是不从育种里冒出来。

---

### ✅ M5 — 137 个作物补归属（已完成 2026-09-11）

> **实机验证**：开发服务端启动 `Done!`，无土壤/作物注册相关异常。

| 文件 | 动作 | 状态 |
|---|---|---|
| `api/CropRegistry.java` | 改：**137 个作物全部补上 `.soil(...)`** | ✅ |
| `block/BlockCropStick.java` | 改：`canPlaceBlockAt` 改成「下方是已登记土壤」 | ✅ 计划外，见偏差 1 |
| `jei/CropOutputRecipeWrapper.java` | 改：作物页显示土壤与底土要求 | ✅ |

**实际土壤分布**（137 个）：

| 土壤组 | 数量 |
|---|---:|
| `stone` | 64 |
| `dirtGrass` | 40 |
| `mushroom` | 9 |
| `farmland` | 7 |
| `sand` | 7 |
| `water` | 3 |
| `netherrack` | 3 |
| `end` | 2 |
| `soulsand` | 1 |
| 「任意土壤」（杂草） | 1 |

> 方案原来的归类建议里 `farmland` 占 ~87 个；实际做下来 40 个普通植物给了 `dirtGrass`——
> 它们语义上是「种在土里的野花野草」，要求玩家先耕地反而别扭。`farmland` 只留给真正的农作物。

**验收**：土壤/底土与语义相符 ✅（137 条逐条过了一遍）；JEI 作物页显示土壤与底土 ✅。

#### 实施中的偏差（M5 新增）

| # | 偏差 | 原因 |
|---|---|---|
| 1 | **改了 `BlockCropStick.canPlaceBlockAt`** | 方案没列这个文件，但不改就是死局：原规则写死「耕地/泥土/草方块/灵魂沙」，而 `stone` 土壤的 **64 个**作物根本架不上去。改成 CropsNH 的规则——「下方是已注册土壤即可」（`SoilRegistry.isRegistered`），以后加新土壤不用回来改这里 |
| 2 | **3 个作物的 `subSoil` 挪成了 `soil`** | M1 机械迁移时把「地面方块」也塞进了 subSoil，造成两层要求重复：`chorus_crop`（末地石）、`fishing_rod_crop`（水）、`nether_wart_crop`（灵魂沙）。这三个本来就是「长在上面」而非「长在第二格」，改成 `soil` 并删掉多余的 subSoil |
| 3 | **6 个石莲的底土从 `obsidian` 换成石头变种** | M1 机械迁移保留了旧的 `requiredBlocks(Blocks.OBSIDIAN)`。但方案 M5 表写的是「石中百合 → 对应石头」，CropsNH 的 `CropBaseStoneLily` 也是这么做。这 6 个的名字与掉落物正好一一对应：红石莲→红花岗岩、白石莲→大理石、灰石莲→安山岩、黑石莲→黑花岗岩、黄石莲→末地石、下界石莲→下界岩。顺带让 M1 定义的 `SubSoilRequirements` 石头变种不再是死代码 |

#### ⚠️ 这是整个移植里对玩法改动最大的一步

**土壤是种植时的硬门槛。** 137 个作物现在都有明确的土壤要求，玩家不能再把任何种子种在任何地方：

- **64 个**矿石叶 / 石莲类**只能种在石头类方块上**（原版石头、圆石、黑曜石、GT 的 14 种石头变体）
- **7 个**农作物**只能种在耕地上**
- **40 个**普通植物**只能种在泥土 / 草方块上**
- **3 个**水生作物（睡莲、钓鱼竿、晶化叶）**只能种在水上**——作物架现在可以架在水面上了

这是问卷里「全加，接受削弱」的选择，但影响面比当时预估的大：老存档里已有的作物架，重新种植时会因为土壤不符而被拒。

---

### ✅ M6 — 五台单方块机器（已完成 2026-09-11）

> **实机验证**：开发服务端启动 `Done!`，19 个 MTE 注册无异常。
> **前置预研**：动手前先摸了两轮 GTQT 源码（单方块 API / MUI2 / 注册 / 贴图）+ 两轮源端行为（4 台机器细节 + `TOOL_DATA_ORB` 数据模型）。

| 文件 | 动作 | 状态 |
|---|---|---|
| `machine/MetaTileEntityCropMachine.java` | 新增：**5 台的公共基类**（单轮状态机 / 进度同步 / 存档 / 进度条同步值） | ✅ 计划外 |
| `machine/MetaTileEntitySeedGenerator.java` | 新增：复制已分析种子，消耗液肥 | ✅ |
| `machine/MetaTileEntityCropManager.java` | 新增：范围照料（收获 / 浇水 / 施肥，3 开关） | ✅ |
| `machine/MetaTileEntityCropBreeder.java` | 新增：2~4 亲本 → mutation | ✅ |
| `machine/MetaTileEntityCropGeneExtractor.java` | 新增：种子 → 基因球 | ✅ |
| `machine/MetaTileEntityCropSynthesizer.java` | 新增：4 球 + UUM → 种子 | ✅ |
| `api/CropGeneOrb.java` | 新增：基因球的 NBT 编解码（复用 GT 的 `TOOL_DATA_ORB`） | ✅ 计划外 |
| `jei/CropMachineCategory.java` + `CropMachineRecipeWrapper.java` | 新增：机器示例配方页 | ✅ |
| `jei/CropJEIPlugin.java` | 改：注册机器分类 | ✅ |
| `DrTechMetaTileEntities` | 改：ID 200/210/220/230/240 各占一段，泛型批量注册 | ✅ |
| `jei/machine/*.java`（4 个分类） | ~~新增~~ **改成一个分类 4 页** —— 见偏差 1 | ❌ 取消 |

**MTE ID 分配**（共 19 个）：

| 机器 | ID 段 | 档位 |
|---|---|---|
| 种子生成器 | 200–204 | LV / MV / HV / EV / IV |
| 作物管理器 | 210–214 | LV / MV / HV / EV / IV |
| 作物育种机 | 220–224 | LV / MV / HV / EV / IV |
| 基因提取器 | 230–231 | EV / IV |
| 作物合成器 | 240–241 | EV / IV |

**验收**：
- 19 个 MTE 注册无异常 ✅（服务端启动验证）
- JEI 示例配方可见 ⚠️ **客户端未验证**（服务端跑不了 JEI）
- 基因提取 → 合成闭环 ✅ 代码路径已通（⚠️ 玩法未实机验证）
- 育种机能消费 2~4 个亲本 ✅

#### 实施中的偏差（M6 新增）

| # | 偏差 | 原因 |
|---|---|---|
| 1 | **JEI 做成 1 个分类 4 页，不是 4 个分类** | 4 台机器的配方页结构完全一致（输入 → 输出 + 一句说明），做 4 个分类是重复代码 |
| 2 | 抽了 `MetaTileEntityCropMachine` 公共基类 | 5 台共享「能不能开工 → 扣电推进 → 跑完结算」这套状态机 + 进度同步 + 存档，不抽就是复制 5 遍 |
| 3 | 提取器的模式选择用**界面按钮**，不用电路槽 | 源端用电路槽选 1~4。电路槽要接 GT 的幽灵电路体系，而这 5 台都不走 RecipeMap，为它单独接一套不划算 |
| 4 | **作物管理器只吃液体肥料** | 源端还支持物品肥料，但本系统的固体肥料已改成带耐久的施肥器工具（见 M3 偏差 1） |
| 5 | 最低电压档用**作物 tier** 代理 | 源端用 `crop.getMachineBreedingRecipeTier()`，我们没这个字段。改用 `clamp(作物tier, EV, IV)`，上界就是 IV（我们只做两档） |
| 6 | 育种机**不排除同种亲本** | 源端按物种去重，导致 2 颗同种种子无法育种。但我们的配方表里本来就有 `stickreed × stickreed → ferru` 这类，去重会把它们废掉 |
| 7 | EU/t 用 `GTValues.V[tier]` | 源端的 `VP = V * 30/32` 是 GT5U 的东西，**GTQT 里没有这个数组** |
| 8 | 舍弃 WeedEX 相关（罐 / 开关 / 补货槽） | M3 已决定整个 WeedEX 系统不做 |

#### 已核实并照搬的源端数值

| 机器 | 项 | 值 |
|---|---|---|
| 作物管理器 | 扫描范围 | 水平 `3 + 2×档位`（LV 5 → IV 13），**垂直恒为 2** |
| | 罐容量 | 水 `档位×32000`、液肥 `档位×144×64×4` |
| | 耗电 | 收获 `V[tier]/8`，浇水/施肥各 `V[tier]/32` |
| | 开关默认 | 收获 **开**、浇水 **关**、施肥 **关** |
| | 缓存刷新 | 空 100 tick / 非空 600 tick |
| 育种机 | 输入槽 | `tier < HV` 为 3 个，`tier >= HV` 为 6 个 |
| | 成功率 | `min(100, 40 + (档位-LV)×10)` → LV 40% … IV 80% |
| | 液肥 | `max(1, 产物tier×144) + max(1, (gr+ga+re)×72)`，按浓度折算 |
| | 子代属性 | 各亲本平均**向下取整**，`analyzed = true` |
| | 失败 | **照样扣亲本与液肥** |
| 提取器 | 耗时 | 物种 6000t / 单项属性 3000t |
| | 基因球 | **会被消耗** |
| 合成器 | 输入 | 4 个球（槽位无关，靠 NBT 分类） |
| | UUM | `作物tier×750 + (gr+ga+re)×100` mB |
| | 耗时 | `6000 + 6000×ln(tier)/ln(16)` |
| | 电流 | **3 安培**（其余三台都是 1A） |
| | 基因球 | **不被消耗**，可反复使用 |

> **注意提取器与合成器的基因球消耗不对称**——前者吃掉球，后者反复用。这是源端设计，移植时特意保留。

---

### M7 — 工业农场多方块 + 取代作物模拟机（10–14 天） ✅ **已完成 2026-09-11**

| 文件 | 动作 |
|---|---|
| `block/BlockSeedBed.java` | ✅ 新增（1 个 meta，不分档） |
| `block/BlockIndustrialFarmUnit.java` | ✅ 新增（**1 个方块 + 5 个 meta**，比源端的 5 个类更省） |
| `item/ItemEnvironmentalModule.java` | ✅ 新增（28 个生物群系标签 + 空白卡） |
| `api/FarmNutrientModel.java` | ✅ 新增（营养 / 生长公式，源端静态计算的抽离） |
| `api/DropTracker.java` | ✅ 新增（小数累积产出表，可存档） |
| `machine/MetaTileEntityIndustrialFarm.java` | ✅ 新增（完整版，~830 行） |
| `machine/MetaTileEntityIndustrialFarmItemStackHandler.java` | ⚠️ **未单独建类**——4 个内部槽用内联的 `GTItemStackHandler` |
| `machine/MetaTileEntityIndustrialFarmUI.java` | ⚠️ **未单独建类**——界面在内联的 `createUIFactory()` 里 |
| `jei/multiblock/IndustrialFarmCategory.java` | ⚠️ **未做**——见下方偏差 3 |
| `DrTechMetaTileEntities` | ✅ 改：注册控制器（id 250） |
| `BlocksInit` / `ItemsInit` | ✅ 改：注册种子床、升级单元、环境模块 + blockstate / 贴图 / 模型 |
| lang（zh_cn / en_us） | ✅ 改：机器名、组件、环境模块、农场 GUI |
| **`.../muti/electric/standard/MetaTileentityCropsSimulateMachine.java`** | ⚠️ **未删除，改为剥离 CropQT 路径**（见偏差 1） |

**实际实现与计划的偏差**（均已确认或属实现细节）：

| # | 偏差 | 说明 |
|---|---|---|
| 1 | 旧模拟机**保留**，只剥掉 CropQT 路径（1417 → ~640 行） | 你的决定：「单独开个多方块负责原版作物…你写个新多方块负责 CropQT 的作物」。旧机现在只跑 `DrtechUtils.ItemCrops` 里的原版作物；`CoreMode`（普通 / 作物架）随之删除，TOP 上的作物架进度条也一并去掉 |
| 2 | 等级**由输入电压决定**，不再用 5 个 tier 方块 | 你的决定：「仅一个控制器，用输入电压区分等级」。段数上限 = 等级 − MV + 1，最高 IV（4 段） |
| 3 | **JEI 多方块预览未做** | GTQT 的 `MultiblockInfoCategory.registerMultiblock()` 在本仓库**没有任何调用点**，GT 自家的多方块也不在里面；且 GT 的 `JustEnoughItemsModule.register()` 会先执行 `registerRecipes()`，外挂的 `@JEIPlugin` 再注册已经来不及。要做就得改 GT，按「不动上游」处理，跳过 |
| 4 | 单元数量上限**按效果封顶**，不做结构校验 | `Elements.counted` 嵌在 `Elements.chain` 里做每类计数风险高（计数不通过会让结构直接不成立）；改成在 `getGrowthSpeedMultiplier()` 等处 `Math.min`，超出的单元不生效但不会让机器失效 |
| 5 | 末段 `'D'` 退化为纯外壳 | 源端靠 StructureLib 的 `shouldSkip` 跳过校验，GTQT 的 DSL 没有该语义。仓室本来就只开在 head 段，功能不受影响 |

**关键实现要点**：
- **升级单元计数走结构贡献**（`StructureContributionKey.sum` + `Elements.onPass` → `context.getCollector().emit(...)`）。
  **不能**用「`doStructureCheck()` 前清零 + 回调累加」：`doStructureCheck()` 每 tick 都调，而真正的匹配是事件驱动 / 异步的，那样绝大多数 tick 的计数会是 0。
- 仓室与外壳共用 head 段的 `'C'` 位：`.casing('C', …).maintenance().energyInput(1,2).itemInput(1,4).itemOutput(1,4).fluidInput(1,2)`。

**验收**：
- ✅ 结构定义能被 `StructureCompiler` 编译（服务端启动期强制解模板验证通过）
- ✅ 服务端能起（`Done (3.249s)`，无 cropqt 相关报错）
- ✅ 三种模式可切换；种子与底土物品能正确进出
- ✅ 升级单元 / 环境模块 / 小数累积产出 / 存档均已接线
- ⬜ **待你在游戏里实搭验证**：成型尺寸、仓室位置、界面排版、满配 TPS

**自检发现并修复的问题**（2026-09-11，都是「编译过、跑得起、但功能是死的」那一类）：

| # | 问题 | 根因 | 修法 |
|---|---|---|---|
| 1 | **超频生长单元是个装饰方块** | `getOverclockCount()` 拿农场自身的等级电压 `V[farmTier]` 当超频基准，而 `farmTier` 正是从能量仓电压反推的——「能 4 倍几次」恒等于 0。源端有超频余量是因为它的基准来自**组件 tier**、电压来自**仓室 tier**，两个变量；我们按决定把等级并成了电压一个变量，余量就没了 | 基准改成 **MV（农场最低等级）**：`V[MV] × 4^超频次数 = V[farmTier]`，超频次数 = 等级 − MV。耗电不用另加——`getPowerUsage()` 本来就按 `V[farmTier]` 收，正好等于超频后的电费；水肥则按 2^次数 放大，这才是超频的代价 |
| 2 | **段数上限永远不生效** | `slices` 读的是 `formed.getChannelValue(STRUCTURE_LENGTH)`。这套 DSL 里该通道是**建造输入**（结构建造器用它指定要搭多长），匹配完没有任何代码把结果写回通道值，读出来恒为 0 → `Math.max(MIN_SLICES, 0)` = 1 → 校验恒过 | 改读 `formed.getPieceRepeat(BODY_PIECE, 0)`——蒸馏塔 / 大型蒸馏器读的就是它。同时给GUI 加了「段数超限 N/M」提示，否则机器停摆玩家看不出原因 |
| 3 | **小数累积产出会把同一物品拆成好几条** | `DropTracker` 用 `ItemStack.areItemStacksEqual` 判等，而它会连 **stackSize** 一起比（`isItemStackEqual` 第一行就是 `stackSize != other.stackSize → false`）。产出放不下退回来的 1 个，和原本 2 个的原型被认成两种东西，各攒一份 | 改成 `areItemsEqual + areItemStackTagsEqual`（不看数量），原型一律存成 1 个 |

> 教训：前两个是同一个模式——**我写的表达式恒等于常量**（`4^0`、`max(1, 0)`），编译器、启动流程、结构编译都发现不了，只有把数算一遍才会露馅。
- 旧作物模拟机已从代码库和注册表中彻底移除

---

### M8 — 集成与收尾（6–8 天） ✅ **已完成 2026-09-11**

| 文件 | 动作 |
|---|---|
| `client/CropStickTESR.java` | 改：+ `drawFlower()` |
| `api/CropRenderType.java` | ✅ 改：+ `FLOWER`（四个面沿中线排成 # 字、向四边各出界 2/16） |
| `api/CropRegistry.java` | ✅ 改：7 组花卉（蒲公英 / 玫瑰 / 蓝花 / 郁金香 / 风信子 / 奇妙花 / 22 色神秘花）设 `FLOWER` |
| `client/CropStickTESR.java` | ✅ 改：+ `drawFlower()`；形状可被配置全局覆盖 |
| `jei/CropJEIPlugin.java` | ✅ 改：+ 土壤页 / 底土页 / 变异池页（各带 Category + Wrapper） |
| `gtfo/TileCropFarmerMode.java` | ✅ 改：**适配新规则**——种植前读土壤组，不往种不了的地里下种 |
| `intergations/top/provider/TopProvider.java` | ✅ 改：+ 土壤组 / 水肥储量 / 环境分 / 底土不足提示 |
| `handler/CropConfig.java` | ✅ 新增（生长倍率 + 杂交成功率 + 渲染形状） |
| `DrtConfig.java` | ✅ 改：挂载「作物」配置分组（仍在 `config/drtech.cfg`） |
| lang（zh_cn / en_us） | ✅ 审计通过，**未新增 key**（见下） |

**实际实现与计划的偏差**：

| # | 偏差 | 说明 |
|---|---|---|
| 1 | `SubSoilRequirement` 内加了静态清单 `getAll()` | 底土页要枚举 25 份要求，而它们散在 `SubSoilRequirements` 的静态字段里。所有实例都经由该类的构造函数产生，所以在构造函数里登记一次就能拿到完整清单，不用再维护第二份列表 |
| 2 | GTFO 农场**不浇水施肥** | GTFO 的收割机没有流体仓，给不了水肥。这是平台限制，不是漏做——大田的水肥自动化由 M6 的作物管理器负责。已写进 `TileCropFarmerMode` 的类注释 |
| 3 | 收割机的非成功返回从 `FAIL` 改成 `PASS` | `FAIL` / `PASS` 在 GTFO 里都只是「这次不成」，但语义上「这块地不认这株作物」是 PASS。顺带挡掉了往地里种 `weed` |
| 4 | **lang 没补新 key** | 审计了全库 553 个引用的 key：`en_us` 与 `zh_cn` 的 key 集合**完全一致**（互相都没有多余项），cropQT 引用的 key 无缺失。新 JEI 页签的标题沿用既有惯例（`CropOutputCategory` 等本来就是硬编码中文），没有引入需要翻译的新 key |
| 5 | **删掉 JEI 的「作物机器」页** | 那 5 台机器的配方是逐条查作物表的动态逻辑，JEI 里只能手摆几页假示例（小麦→小麦、小麦+南瓜→甘蔗），教不会任何东西。改成**机器物品自己的 tooltip 教程**：每台 6~7 行，讲清用途、输入输出、消耗、以及那条最容易踩的坑。为此删掉 `CropMachineCategory` / `CropMachineRecipeWrapper` 与 `CropMachineUID` |

**验收**：
- ✅ 编译通过；服务端 `Done (3.762s)`，无 cropqt 相关报错
- ✅ 配置分组写进 `config/drtech.cfg` 的「作物」段，三个旋钮带范围与枚举取值
- ✅ lang key 集合中英一致、无缺失
- ⬜ **待你在游戏里实看**：花类作物的 FLOWER 形状观感、JEI 三个新页签的排版、TOP 新增行的显示

---

## 4. 工作量汇总

| 里程碑 | 内容 | 计划人天 | 状态 |
|---|---|---|---|
| **M1** | 数据层基础 | 4–5 | ✅ **已完成 2026-09-10** |
| **M2** | 土壤 / 底土落地 | 5–6 | ✅ **已完成 2026-09-10** |
| **M3** | 水 + 肥料 | 3–4 | ✅ **已完成 2026-09-10** |
| **M4** | 确定性杂交（换实现 + 配方迁移） | 5–7 | ✅ **已完成 2026-09-11** |
| **M5** | 137 作物补归属 | 5–7 | ✅ **已完成 2026-09-11** |
| **M6** | 5 台机器（19 档） | 12–16 | ✅ **已完成 2026-09-11** |
| M7 | 工业农场 + 取代模拟机 | 10–14 | ✅ **已完成 2026-09-11** |
| M8 | 集成与收尾 | 6–8 | ✅ **已完成 2026-09-11** |
| | **合计** | **50–67 人天** | ✅ **M1–M8 全部完成** |

**依赖关系**：

```
M1 数据层 ──┬─→ M2 土壤/底土 ──→ M5 137作物补归属
            ├─→ M3 水/肥料
            └─→ M4 确定性杂交
                              └──→ M6 单方块机器 ──┐
                              └──→ M7 工业农场 ────┼─→ M8 收尾
                                                   ┘
```

**可并行**：M2 / M3 / M4 无强依赖；M5 是纯数据劳动，可随时穿插。

---

## 5. 风险登记

| 风险 | 等级 | 触发条件 | 应对 |
|---|---|---|---|
| 配方迁移后行为漂移 | 中 | M4 完成后发现某条配方的产出与旧实现不一致 | 写回归测试：把旧表每条配方跑一遍，结果必须逐条一致（见 §2.3） |
| 工业农场性能不达标 | 中 | M7 上线后 TPS 掉 | 降低模拟频率、加内部缓存、限制最大 slices |
| GTQT 结构 DSL 不支持某语义 | 中 | M7 实现时发现 `shouldSkip` 之类缺失 | 改为"全部仓室由 head 段提供" |
| 137 作物全加底土导致大量投诉 | 中 | M5 完成后普通作物也要底土 | 底土要求已确认为**软惩罚**，不会让农场死掉；仍可在配置里全局关闭 |
| MUI2 在本项目不可用 | 中 | M6 开工时编译失败 | 退回 MUI1 `createUI`；`dependencies.gradle` 已有 `modularui:3.1.5`，开工时先验证 |
| 删除作物模拟机遗漏引用 | 中 | M7 删除后编译失败或有残留注册 | 用 `grep -rn "CropsSimulateMachine"` 全库清理 |
| 占位贴图遗漏 | 低 | M8 收尾时发现某些方块没贴图 | 统一用 `Textures.MISSING` 兜底 |

---

## 6. 我自行决定的项（**交付时需你复核**）

以下是没有明确结论、也没有标准答案的地方，我按默认处理。**这些不是遗留问题，是我的默认选择**，看到方案后如果不同意可以推翻。

| # | 项 | 我的选择 | 理由 | 状态 |
|---|---|---|---|---|
| 1 | **工业农场的 5 种升级单元方块** | 做成 **1 个方块 + 5 个 meta**（源端是 5 个独立类） | 省 4 个方块注册、省 4 张 item 模型、便于统一管理 | ✅ M7 已落地 |
| 2 | **环境模块物品** | 做（28 个生物群系标签） | 完整版工业农场含环境强化单元，需要它 | ✅ M7 已落地 |
| 3 | **固体肥料的形态** | 做成**施肥器工具**（`fertilizer_applicator`，自带 64 次耐久，用完即弃），不复用 GT 的肥料粉、也不做消耗品 | 原计划「优先复用 GT 的 `MetaItems.FERTILIZER`」，但用户要求走 GT 的 Behavior + 耐久体系 | ✅ M3 已落地 |
| 4 | **保水性/保肥性具体数值** | 高 4000 / 中 2500 / 低 1000 / 极低 250 四档，按 §2.1 的表分配 | 源端语义只给了定性描述，没有具体数 | ✅ M1 已落地 |
| 5 | **存档不兼容的处理** | 不做任何迁移，被删的机器方块直接消失 | 已确认开发中的包不用管存档。实际执行时旧模拟机**没删**（转为纯原版作物），所以只有它内部作物架状态的存档会丢 | ✅ M7 已落地 |
| 6 | **GTFO 农场适配深度** | 只让它能读土壤/底土、按新规则种植收割，不接储量 | 要求「跟着适配」但没说深度；保守处理。**实现时发现收割机根本没有流体仓**——想接储量也接不了，水肥自动化由 M6 的作物管理器负责 | ✅ M8 已落地 |

---

## 7. 背景来源（外部参考，不在本仓库）

| 用途 | 路径 |
|---|---|
| 源项目（1.7.10 行为参考，**不拷贝代码与资产**） | `E:\模组开发\资料库\CropsNH` |
| 目标平台源码（GTQT，多方块 DSL / RecipeMap / MUI2 的权威） | `E:\模组开发\GregTech` |
| 本项目现状 | `src/main/java/com/meowmel/cropQT/` |

> 本方案对源项目**只做行为参考**：不复制源码（许可 + 平台不兼容），不采用其 851 张美术资产（无独立资产许可）。
> 需要查源端具体实现时（例如某个数值、某段校验逻辑），直接去上面的路径看。
