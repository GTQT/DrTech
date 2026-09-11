# M7 工业农场 — 设计稿

> 状态：**设计完成，待确认 2 个开放问题后开工**
> 制定日期：2026-09-11
> 上游方案：[cropsnh-port-roadmap.md](cropsnh-port-roadmap.md) §M7
> 预研来源：CropsNH `MTEIndustrialFarm`(1489 行) + GTQT 多方块 API 全量 + 待删机器审计

---

## 0. 先说三个「推翻方案原计划」的发现

### 发现 1：GTQT 的 DSL **没有 `shouldSkip`**，源端 `'D'` 那一段要重做

源端最后一层的 `'D'` 用了 `shouldSkip((t, tile) -> true)`——「末层不校验仓室、不允许自动搭建放仓室」。
GTQT 的 `DeclarativePatternBuilder` **没有这个语义**。

**应对**：`'D'` 退化成纯外壳，仓室全部由 `head` 段的 `'C'`（单面）承担。
功能不受影响（源端本来也只让 `'C'` 放仓室），只是末层少了"装饰性仓室提示"。

### 发现 2：源端「组件等级决定长度」在 GTQT 里要**反过来推**

源端：`slices = clamp(upgradeTier - MV + 1, 1, 13)`——长度由**组件等级**隐式决定，且必须正好相等。
GTQT：长度就是**玩家实际搭了几段**（结构通道读出来的），不是推导出来的。

**应对**：保留源端的平衡，但改成**校验式**——
```
农场等级 = 控制器电压档（MV/HV/EV/IV）
slices  = 通道读到的实际段数（1~13）
校验：    slices ≤ 农场等级 - MV + 1
```
即「MV 控制器最多搭 1 段、IV 最多搭 4 段」。玩家想搭长就得升级控制器——和源端的约束等价，但符合 GTQT 的搭法。

### 发现 3：⚠️ 待删的旧机器有**两块**能力，新农场只能接一块

`MetaTileentityCropsSimulateMachine`(1416 行) 的 `configureDisplayText` 暴露了它有两种部署模式：

| 能力 | 对应方法 | 新农场能否覆盖 |
|---|---|---|
| **CropQT 作物架模拟** | `deployCropRackCrops` / `tickCropRackEntry` / `prepareCropRackCycle` | ✅ 能（这正是 CropsNH 工业农场做的事） |
| **原版作物模拟** | `deployNormalCrops` / `runNormalEntry` / `getStemFruit` | ❌ **不能**——CropsNH 的农场只处理种子+底土物品，没有"模拟原版耕地作物"这条路 |

删掉旧机器 = **连同原版作物模拟一起删掉**。这是 §5 的第 1 个待决问题。

---

## 1. 该继承什么

**`MultiblockWithDisplayBase`**（不是 `RecipeMapMultiblockController`）。

理由：不用 RecipeMap（配方逻辑是动态的、逐条查作物表），但要 MUI2 界面、物品槽、流体槽、维护仓。GTQT 里同类场景（`MetaTileEntityDataBank`）就是这么做的。

**必须实现 4 个方法**：
```java
public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder)
protected void updateFormedValid()                              // 工作循环
protected @NotNull StructureDefinition<?> createStructureDefinition()
@SideOnly(Side.CLIENT) public ICubeRenderer getBaseTexture(IMultiblockPart part)
```

**三个陷阱**（都会静默出错）：
1. `buildUI` 在 `MultiblockWithDisplayBase` 上是 **`final`** —— 定制走 `createUIFactory()`
2. 成型回调必须调 **`formStructureWithDisplay(formed)`**，不是 `super.formStructure(formed)`
3. **必须有 `public static IBlockState getCasingState()`** —— 反射拿不到会让所有组件**静默失去 CTM**

---

## 2. 结构映射

源端三段 → GTQT 的 `piece` + `repeatablePiece`：

```java
DeclarativePatternBuilder.start(RIGHT, UP, BACK)
    .piece("head")
        .aisle(" cCc ", "cCCCc", "cC~Cc", "c   c")
    .repeatablePiece("body", 1, 13)
        .aisle(" gUg ", "g   g", "csssc", "     ")
        .withAisleChannel(GTStructureChannels.STRUCTURE_LENGTH.getName())
    .piece("tail")
        .aisle(" cDc ", "cDDDc", "cDDDc", "c   c")
    .self('~', MetaTileEntityIndustrialFarm.class)
    ...
```

| 字符 | 方块 | GTQT 写法 |
|---|---|---|
| `c` | 农业外壳 | `.casing('c', getCasingState())` |
| `C` | 外壳 + 仓室（单面） | 用 `CasingSlot` 的 `.maintenance() .energyInput(1,2) .itemInput(0,4) .itemOutput(1,4) .fluidInput(1,2)` |
| `D` | 末层外壳（见发现 1） | `.blocks('D', getCasingState())` |
| `g` | 玻璃 | `.blocks('g', <GT 玻璃>)` |
| `U` | 升级单元（5 种） | `.where('U', Elements.chain(...5 种...))` |
| `s` | 种子床 | `.blocks('s', getSeedBedState())` |
| `~` | 控制器 | `.self('~', ...)` |

**长度读取**：`formStructure` 里 `formed.getChannelValue(GTStructureChannels.STRUCTURE_LENGTH)` ✓
**JEI 预览自动**：挂了 `withAisleChannel` 后 GTQT 会**自动**给该通道生成滑动条，玩家能拖 1~13 实时预览——**不需要写 JEI 分类**。

> **方案偏差**：方案里的 `jei/multiblock/IndustrialFarmCategory.java` **不用写**（GTQT 全自动）。

---

## 3. 方块清单

| 方块 | meta 数 | 说明 |
|---|---|---|
| `BlockSeedBed` | 1 | 种子床。不分 tier（见发现 2：等级由控制器决定） |
| `BlockIndustrialFarmUnit` | **5** | 5 种升级单元，一个方块用 meta 区分（源端是 5 个独立类各 12 个 meta） |
| `ItemEnvironmentalModule` | 29 | meta 0 = 空白卡 + 28 个 BiomeDictionary.Type（照源端） |

**5 种升级单元**（meta 0~4）：

| meta | 单元 | 上限 | 效果 |
|---:|---|---:|---|
| 0 | 环境强化单元 | 2 | +0.5×基础耗电/个；解锁一个环境模块槽 |
| 1 | 生长加速单元 | ∞（受段数限制） | 生长速度 **+1.0/个**（加法）；+1.25×基础耗电/个 |
| 2 | 肥料单元 | 1 | 生长速度 **×1.5**；收割轮数 +0.5；+0.5×基础耗电 |
| 3 | 高级收割单元 | 2 | 收割轮数 **×(1+0.2/个)**；+0.5×基础耗电 |
| 4 | 超频生长加速单元 | 1 | 用 GT 的 `OverclockCalculator` 超频；**与生长加速单元互斥** |

---

## 4. 三种模式

| 模式 | 周期 | 耗电 | 做什么 |
|---|---:|---:|---|
| `MODE_INPUT` | 5 tick | 0 | 从输入总线吞种子 + 底土进内部槽 |
| `MODE_FARM` | **100 tick** | `expectedEUt` | 水 + 肥；按模拟推进生长；产出累积 |
| `MODE_OUTPUT` | 5 tick | 0 | 内部种子/底土退回输出总线 |

**4 个内部槽**：`SLOT_SEED=0`、`SLOT_SUB_SOIL=1`、`SLOT_ENV_CARD_START=2`（+0/+1，随环境强化单元数量解锁）

**关键行为**：
- **底土槽禁止手动插入**——只能由 `MODE_INPUT` 从输入总线吞入
- 机器运行时物品栏全锁
- 掉落是**小数累积制**（`IFDropTable`），跨周期不重置，只在切模式或结算失败时清空

---

## 5. 两个问题的裁决（2026-09-11）

### 问题 1 → **现有机器退化成纯原版作物模拟机；新机器专管 CropQT**

用户原话：「单独开个多方块负责原版作物（反正逻辑就是现在的）；或者目前的多方块保留，你写个新多方块负责 CropQT 的作物」。

审计后确认：现有 `MetaTileentityCropsSimulateMachine` 的 `deployCropRackCrops` / `tickCropRackGrowthAndHarvest` 路径**与新的工业农场功能 100% 重叠**（都是把 CropQT 种子袋放进机器模拟）。

**执行**：
1. 新增 `MetaTileEntityIndustrialFarm`，专管 CropQT 作物（本次 M7 主体）
2. **剥离** `MetaTileentityCropsSimulateMachine` 的 CropQT 路径，退化成纯原版作物模拟机（`deployNormalCrops` / `runNormalEntry` / `getStemFruit` 那条）

> 依据用户既定原则「老代码必须为迁移让路」——不留两套做同一件事的机器。

### 问题 2 → **单个控制器，等级由输入电压决定**

用户原话：「仅一个控制器，用输入电压区分等级」。

**执行**：
- 控制器**只注册一次**（不是四档）
- `农场等级` = 能量仓的最高输入电压对应的 GT 电压档（`MV` ~ `IV`）
- 段数上限 = `农场等级 - MV + 1`（MV 1 段 / HV 2 段 / EV 3 段 / IV 4 段）
- 结构校验里的「等级 ≥ MV」改为「能量仓电压 ≥ MV」

比四档注册干净得多，也更符合 GT 的直觉：**喂什么电压，就是什么等级**。

---

## 6. 已核实的源端数值（照搬清单）

| 项 | 值 |
|---|---|
| `CYCLE_DURATION` | 100 tick |
| `CYCLE_TICK_RATE_SCALAR` | `100/256 = 0.390625` |
| 模拟储水 | `SIMULATED_WATER_STORAGE = 200` |
| 模拟露天 | `SIMULATED_CAN_SEE_SKY = true` |
| 模拟储肥 | 有肥 `200` / 无肥 `0` |
| 种子床容量 | `(7 + 4×tier)²` |
| 每周期耗水/耗肥 | `ceil(容量 × 0.390625)` |
| 基础耗电 | `VP[tier]`（我们用 `V[tier]`） |
| 收割轮数加成 | `tier × 0.2` |
| 营养点 | `5 + (水+9)/10 + (肥+9)/10 + 露天2 + max(湿度加成, 喜好群×14)` |
| 生长速率 | `need = tier×10`；`points×5 ≥ need` → `(6+growth)×(100+盈余)/100`，否则 `max(base×(100-缺口×4)/100, 0)` |
| 掉落 | `getDropChance() × 1.03^gain` 轮；每轮 `(数量 + (gain+1)/100) × 概率` |
| 环境模块 | 28 个 BiomeDictionary.Type，**只影响喜好群计数**，不改温度/湿度 |

---

## 7. 文件清单（开工后）

| 文件 | 动作 |
|---|---|
| `machine/MetaTileEntityIndustrialFarm.java` | 新增（控制器） |
| `block/BlockSeedBed.java` | 新增 |
| `block/BlockIndustrialFarmUnit.java` | 新增（1 方块 + 5 meta） |
| `item/ItemEnvironmentalModule.java` | 新增（29 meta） |
| `api/FarmNutrientModel.java` | 新增（照搬 `getNutrientsPerCycle` + `getGrowthRate` 两个公式） |
| `api/DropTracker.java` | 新增（小数累积掉落表） |
| `DrTechMetaTileEntities` | 改：注册控制器 |
| `DrTechBlocksInit` + `ItemsInit` | 改：注册 3 个方块 + 1 个物品 |
| ~~`jei/multiblock/IndustrialFarmCategory.java`~~ | **不写**（GTQT 自动，见 §2） |
| ~~`machine/MetaTileEntityIndustrialFarmItemStackHandler.java`~~ | **不写**（槽位用 `GTItemStackHandler` 匿名子类即可） |
| ~~`machine/MetaTileEntityIndustrialFarmUI.java`~~ | **不写**（界面走 `createUIFactory`） |
| `MetaTileentityCropsSimulateMachine.java` | **删除**（待问题 1 定论） |
