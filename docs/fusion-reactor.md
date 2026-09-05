# 发电聚变堆（Fusion Reactor）

> 对应 GTQT 2.0.0 更新计划表《发电聚变堆》设计文档。
> 实现位置：`com.drppp.drtech.common.MetaTileEntities.muti.electric.generator`
> 适配 API：`libs/gregtech-gtqt-1.12.2-1.9.0.jar`（StructureDefinition / Elements / tieredCasing 体系）

## 一、设计概述

纯发电聚变堆，**不需要聚变配方**，直接消耗氘/氚流体输出电力。

核心公式（文档第六章）：

```
每 tick 输出 = 第一壁上限(EU/t) × 冷却剂倍率 + 中子捕获加成
中子捕获加成 = 基础输出 × 0.1 × 中子捕获效率
燃料消耗     = 基础消耗(输出每 1M EU/t 消耗 1 mB/t) × 氚增殖包层系数
建磁需求     = 启动前一次性累计消耗 EU（由超导磁体档位决定，100G~50G EU）
```

## 二、结构布局（21 × 21 × 11，立式胖环托卡马克 v2：整体圆润 + 环形外壳全包）

> **代码为准**：`MetaTileEntityFusionReactor.buildStructureDefinition()` 共 21 个 `.aisle` 切片（z=0 前 → z=20 后），每切片 11 个字符串（y0 底 → y10 顶），每行 21 列（x0..20）。造型沿用仓库 GA Tokamak 蓝图（圆润胖环、管壁逐层包满），轮廓外与中心空洞仍用 `Elements.any()`（字符 x）——不需要放置方块。

**字母与方块（复用现有 3 方块，无新增）**：
- `R` 辐射屏蔽外壳 = 最外壁/外壳层（391）
- `M` 超导磁体（分级 1–4，175）｜`N` 中子捕获（分级 1–5，68，中层环 y5）
- `B` 氚增殖包层（分级 1–5，80，上层环 y9）｜`C` 冷却剂回路（分级 1–5，80，下层环 y1）
- `W` 第一壁（分级 1–5，400，主管壁层 y2-8）｜`P` 等离子约束腔（固定，18，中心）
- `S` 控制器（唯一：z20-y4-x10）｜`A` 仓口（9：z8-12 左侧外壁 x0-2，y2-8）
- `x` = 任意（轮廓外/中心，不建）。总结构 21×21×11=4851 格；实建方块 1222：R391+M175+W400+B80+C80+N68+P18+A9+S1。

### 层图（每层：行=z 0..20，列=x 0..20）
```
y=0
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxxxxxxxxxxxxxxx
 2 xxxxxxxxxxxxxxxxxxxxx
 3 xxxxxxxxxxxxxxxxxxxxx
 4 xxxxxxxxxxMxxxxxxxxxx
 5 xxxxxMxxxxMxxxxMxxxxx
 6 xxxxxxMxxxxxxxMxxxxxx
 7 xxxxxxxxxxxxxxxxxxxxx
 8 xxxxxxxxxxxxxxxxxxxxx
 9 xxxxxxxxxxxxxxxxxxxxx
10 xxxxMMxxxxxxxxxMMxxxx
11 xxxxxxxxxxxxxxxxxxxxx
12 xxxxxxxxxxxxxxxxxxxxx
13 xxxxxxxxxxxxxxxxxxxxx
14 xxxxxxMxxxxxxxMxxxxxx
15 xxxxxMxxxxMxxxxMxxxxx
16 xxxxxxxxxxMxxxxxxxxxx
17 xxxxxxxxxxxxxxxxxxxxx
18 xxxxxxxxxxxxxxxxxxxxx
19 xxxxxxxxxxxxxxxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=1
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxxxxxxxxxxxxxxx
 2 xxxxxxxxxxxxxxxxxxxxx
 3 xxxxxxxRRRMRRRxxxxxxx
 4 xxxxMRRCCCCCCCRRMxxxx
 5 xxxxRCCCCCCCCCCCRxxxx
 6 xxxxRCCCxxMxxCCCRxxxx
 7 xxxRCCCMxxxxxMCCCRxxx
 8 xxxRCCxxxxxxxxxCCRxxx
 9 xxxRCCxxxxxxxxxCCRxxx
10 xxxMCCMxxxPxxxMCCMxxx
11 xxxRCCxxxxxxxxxCCRxxx
12 xxxRCCxxxxxxxxxCCRxxx
13 xxxRCCCMxxxxxMCCCRxxx
14 xxxxRCCCxxMxxCCCRxxxx
15 xxxxRCCCCCCCCCCCRxxxx
16 xxxxMRRCCCCCCCRRMxxxx
17 xxxxxxxRRRMRRRxxxxxxx
18 xxxxxxxxxxxxxxxxxxxxx
19 xxxxxxxxxxxxxxxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=2
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxxxxxxxxxxxxxxx
 2 xxxxxxxRRRMRRRxxxxxxx
 3 xxxxxRRWWWWWWWRRxxxxx
 4 xxxxMWWxxxxxxxWWMxxxx
 5 xxxRWxxxxxxxxxxxWRxxx
 6 xxxRWxxxWWWWWxxxWRxxx
 7 xxRWxxxWxxxxxWxxxWRxx
 8 xxRWxxWxMxxxMxWxxWRxx
 9 xxAWxxWxxxPxxxWxxWRxx
10 xxMWxxWxxPMPxxWxxWMxx
11 xxRWxxWxxxPxxxWxxWRxx
12 xxRWxxWxMxxxMxWxxWRxx
13 xxRWxxxWxxxxxWxxxWRxx
14 xxxRWxxxWWWWWxxxWRxxx
15 xxxRWxxxxxxxxxxxWRxxx
16 xxxxMWWxxxxxxxWWMxxxx
17 xxxxxRRWWWWWWWRRxxxxx
18 xxxxxxxRRRMRRRxxxxxxx
19 xxxxxxxxxxxxxxxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=3
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxRRRMRRRxxxxxxx
 2 xxxxxRRWWWWWWWRRxxxxx
 3 xxxMRWWxxxxxxxWWRMxxx
 4 xxxRWxxxxxxxxxxxWRxxx
 5 xxRWxxxxxxxxxxxxxWRxx
 6 xxRWxxxxxxxxxxxxxWRxx
 7 xRWxxxxxWWWWWxxxxxWRx
 8 xRWxxxxWMxxxMWxxxxWRx
 9 xAWxxxxWxPMPxWxxxxWRx
10 xMWxxxxWxMMMxWxxxxWMx
11 xRWxxxxWxPMPxWxxxxWRx
12 xRWxxxxWMxxxMWxxxxWRx
13 xRWxxxxxWWWWWxxxxxWRx
14 xxRWxxxxxxxxxxxxxWRxx
15 xxRWxxxxxxxxxxxxxWRxx
16 xxxRWxxxxxxxxxxxWRxxx
17 xxxMRWWxxxxxxxWWRMxxx
18 xxxxxRRWWWWWWWRRxxxxx
19 xxxxxxxRRRMRRRxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=4
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxRRRMRRRxxxxxxx
 2 xxxxxRRWWWWWWWRRxxxxx
 3 xxxMRWWxxxxxxxWWRMxxx
 4 xxxRWxxxxxxxxxxxWRxxx
 5 xxRWxxxxxxxxxxxxxWRxx
 6 xxRWxxxxxxxxxxxxxWRxx
 7 xRWxxxxxxWWWxxxxxxWRx
 8 xRWxxxxxWxxxWxxxxxWRx
 9 xAWxxxxWxMMMxWxxxxWRx
10 xMWxxxxWxMMMxWxxxxWMx
11 xRWxxxxWxMMMxWxxxxWRx
12 xRWxxxxxWxxxWxxxxxWRx
13 xRWxxxxxxWWWxxxxxxWRx
14 xxRWxxxxxxxxxxxxxWRxx
15 xxRWxxxxxxxxxxxxxWRxx
16 xxxRWxxxxxxxxxxxWRxxx
17 xxxMRWWxxxxxxxWWRMxxx
18 xxxxxRRWWWWWWWRRxxxxx
19 xxxxxxxRRRMRRRxxxxxxx
20 xxxxxxxxxxSxxxxxxxxxx
```
```
y=5
 0 xxxxxxxRRRMRRRxxxxxxx
 1 xxxxxRRNNNNNNNRRxxxxx
 2 xxxxRNNxxxxxxxNNRxxxx
 3 xxxMNxxxxxxxxxxxNMxxx
 4 xxRNxxxxxxxxxxxxxNRxx
 5 xRNxxxxxxxxxxxxxxxNRx
 6 xRNxxxxxxxxxxxxxxxNRx
 7 RNxxxxxxxNNNxxxxxxxNR
 8 RNxxxxxxNxxxNxxxxxxNR
 9 ANxxxxxNxMMMxNxxxxxNR
10 MNxxxxxNxMMMxNxxxxxNM
11 RNxxxxxNxMMMxNxxxxxNR
12 RNxxxxxxNxxxNxxxxxxNR
13 RNxxxxxxxNNNxxxxxxxNR
14 xRNxxxxxxxxxxxxxxxNRx
15 xRNxxxxxxxxxxxxxxxNRx
16 xxRNxxxxxxxxxxxxxNRxx
17 xxxMNxxxxxxxxxxxNMxxx
18 xxxxRNNxxxxxxxNNRxxxx
19 xxxxxRRNNNNNNNRRxxxxx
20 xxxxxxxRRRMRRRxxxxxxx
```
```
y=6
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxRRRMRRRxxxxxxx
 2 xxxxxRRWWWWWWWRRxxxxx
 3 xxxMRWWxxxxxxxWWRMxxx
 4 xxxRWxxxxxxxxxxxWRxxx
 5 xxRWxxxxxxxxxxxxxWRxx
 6 xxRWxxxxxxxxxxxxxWRxx
 7 xRWxxxxxxWWWxxxxxxWRx
 8 xRWxxxxxWxxxWxxxxxWRx
 9 xAWxxxxWxMMMxWxxxxWRx
10 xMWxxxxWxMMMxWxxxxWMx
11 xRWxxxxWxMMMxWxxxxWRx
12 xRWxxxxxWxxxWxxxxxWRx
13 xRWxxxxxxWWWxxxxxxWRx
14 xxRWxxxxxxxxxxxxxWRxx
15 xxRWxxxxxxxxxxxxxWRxx
16 xxxRWxxxxxxxxxxxWRxxx
17 xxxMRWWxxxxxxxWWRMxxx
18 xxxxxRRWWWWWWWRRxxxxx
19 xxxxxxxRRRMRRRxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=7
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxRRRMRRRxxxxxxx
 2 xxxxxRRWWWWWWWRRxxxxx
 3 xxxMRWWxxxxxxxWWRMxxx
 4 xxxRWxxxxxxxxxxxWRxxx
 5 xxRWxxxxxxxxxxxxxWRxx
 6 xxRWxxxxxxxxxxxxxWRxx
 7 xRWxxxxxWWWWWxxxxxWRx
 8 xRWxxxxWMxxxMWxxxxWRx
 9 xAWxxxxWxPMPxWxxxxWRx
10 xMWxxxxWxMMMxWxxxxWMx
11 xRWxxxxWxPMPxWxxxxWRx
12 xAWxxxxWMxxxMWxxxxWRx
13 xRWxxxxxWWWWWxxxxxWRx
14 xxRWxxxxxxxxxxxxxWRxx
15 xxRWxxxxxxxxxxxxxWRxx
16 xxxRWxxxxxxxxxxxWRxxx
17 xxxMRWWxxxxxxxWWRMxxx
18 xxxxxRRWWWWWWWRRxxxxx
19 xxxxxxxRRRMRRRxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=8
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxxxxxxxxxxxxxxx
 2 xxxxxxxRRRMRRRxxxxxxx
 3 xxxxxRRWWWWWWWRRxxxxx
 4 xxxxMWWxxxxxxxWWMxxxx
 5 xxxRWxxxxxxxxxxxWRxxx
 6 xxxRWxxxWWWWWxxxWRxxx
 7 xxRWxxxWxxxxxWxxxWRxx
 8 xxAWxxWxMxxxMxWxxWRxx
 9 xxRWxxWxxxPxxxWxxWRxx
10 xxMWxxWxxPMPxxWxxWMxx
11 xxAWxxWxxxPxxxWxxWRxx
12 xxRWxxWxMxxxMxWxxWRxx
13 xxRWxxxWxxxxxWxxxWRxx
14 xxxRWxxxWWWWWxxxWRxxx
15 xxxRWxxxxxxxxxxxWRxxx
16 xxxxMWWxxxxxxxWWMxxxx
17 xxxxxRRWWWWWWWRRxxxxx
18 xxxxxxxRRRMRRRxxxxxxx
19 xxxxxxxxxxxxxxxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=9
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxxxxxxxxxxxxxxx
 2 xxxxxxxxxxxxxxxxxxxxx
 3 xxxxxxxRRRMRRRxxxxxxx
 4 xxxxMRRBBBBBBBRRMxxxx
 5 xxxxRBBBBBBBBBBBRxxxx
 6 xxxxRBBBxxMxxBBBRxxxx
 7 xxxRBBBMxxxxxMBBBRxxx
 8 xxxRBBxxxxxxxxxBBRxxx
 9 xxxRBBxxxxxxxxxBBRxxx
10 xxxMBBMxxxPxxxMBBMxxx
11 xxxRBBxxxxxxxxxBBRxxx
12 xxxRBBxxxxxxxxxBBRxxx
13 xxxRBBBMxxxxxMBBBRxxx
14 xxxxRBBBxxMxxBBBRxxxx
15 xxxxRBBBBBBBBBBBRxxxx
16 xxxxMRRBBBBBBBRRMxxxx
17 xxxxxxxRRRMRRRxxxxxxx
18 xxxxxxxxxxxxxxxxxxxxx
19 xxxxxxxxxxxxxxxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```
```
y=10
 0 xxxxxxxxxxxxxxxxxxxxx
 1 xxxxxxxxxxxxxxxxxxxxx
 2 xxxxxxxxxxxxxxxxxxxxx
 3 xxxxxxxxxxxxxxxxxxxxx
 4 xxxxxxxxxxMxxxxxxxxxx
 5 xxxxxMxxxxMxxxxMxxxxx
 6 xxxxxxMxxxxxxxMxxxxxx
 7 xxxxxxxxxxxxxxxxxxxxx
 8 xxxxxxxxxxxxxxxxxxxxx
 9 xxxxxxxxxxxxxxxxxxxxx
10 xxxxMMxxxxxxxxxMMxxxx
11 xxxxxxxxxxxxxxxxxxxxx
12 xxxxxxxxxxxxxxxxxxxxx
13 xxxxxxxxxxxxxxxxxxxxx
14 xxxxxxMxxxxxxxMxxxxxx
15 xxxxxMxxxxMxxxxMxxxxx
16 xxxxxxxxxxMxxxxxxxxxx
17 xxxxxxxxxxxxxxxxxxxxx
18 xxxxxxxxxxxxxxxxxxxxx
19 xxxxxxxxxxxxxxxxxxxxx
20 xxxxxxxxxxxxxxxxxxxxx
```

> 搭建提示：按层图 y0→y10 自下而上放置；同一种分级字母（B/C/W/N/M）整圈尽量同档位。S 放 z20-y4-x10；9 个 A 位放仓口（维护仓恰 1、输入能量/输入流体等），未放仓口的 A 位用辐射屏蔽外壳 R 补齐。

## 三、模块与档位（升级方向）

| 档位 | 第一壁 上限 | 冷却剂 倍率 | 中子捕获 效率 | 氚增殖 DT消耗 | 超导磁体 建磁 |
|---|---|---|---|---|---|
| 1 | 4M EU/t | 1.0x | 45% | 90% | 100G EU |
| 2 | 6M | 1.4x | 60% | 75% | 80G |
| 3 | 8M | 1.8x | 75% | 65% | 65G |
| 4 | 12M | 2.2x | 90% | 55% | 50G |
| 5 | 16M | 2.5x | 110% | 40% | — |

满配最大输出：`16M × 2.5 + 16M×0.1×1.10 = 41,760,000 EU/t`（41.76M EU/t）。

方块实现（`VariantBlock` 单方块 ≤16 变种限制 → 拆 3 个方块）：
- `BlockFusionReactorCasing`：基础 12 变种（P/R/F/Z 等固定 + 各模块 1 档）
- `BlockFusionReactorTieredCasing`：11 变种（第一壁2-5 / 冷却2-5 / 磁体2-4）
- `BlockFusionReactorTieredCasing2`：8 变种（中子2-5 / 氚2-5）

档位数值与枚举映射见 `FusionCasingStats`（`firstWall/coolant/neutronCapture/tritiumBreeding/magnet`
及三枚举 `of()` 重载）；分组注册见 `FusionCasingGroups`。

## 四、启动流程（FusionReactorLogic 状态机）

```
OFFLINE → MAGNETIZING(建磁,≤16M EU/t,磁体1档约5分钟) → FUEL_INJECTING(注入DT,100 tick)
       → RF_HEATING(耗≤8M EU/t 加热,满功率约25秒) → IGNITED(α自加热,RF需求降75%,约5秒)
       → RUNNING(自持燃烧,输出电力+持续消耗DT)
运行中燃料耗尽 → 退回 FUEL_INJECTING
```

- 建磁总量 = 磁体档位 `getMagnetizeEU()`（100G→50G EU）；充电速率上限 16M EU/t。
- RF 加热速率随输入功率与第一壁档位提升（满功率 ~500 tick 点火）。
- IGNITED 阶段维持功率 = 8M × 0.25 = 2M EU/t，升温 1000/ tick 直至 200k 满温 → RUNNING。
- 燃料：GT `Deuterium` + `Tritium`（氘+氚，输入流体仓）；RUNNING 消耗 = 核心输出每 1M 对应
  (1 mB × 氚增殖系数)/t 的氘 **与** 氚各一份。
- 输出：能量通过输出能量仓（0–4 个，A 位）导出；UI 显示状态/输出/温度/建磁进度。

## 五、配方（CraftingReceipe.fusion 等，注册已恢复）

- 12 种基础外壳：聚变零件物品 + GT 材料
- 升级配方：低档外壳 + 对应材料 → 高档外壳（跨 3 个方块）
- 控制器：8× 辐射屏蔽外壳 + DT 燃料注入器
- 具体条目以 `CraftingReceipe`/`MachineReceipe` 中已恢复的代码为准。

## 六、验证状态

- ✅ 核心文件 javac / **全量源码类型检查**通过（659+ 个 main 源文件以 gradle 精确
  classpath 全量 javac17：patchedMc+api+injectedTags+main 输出 + 全部依赖 jar，exit 0）
- ✅ **2026-09-05 真实全量 gradle 编译通过**：`compileJava` + `jar` 均 BUILD SUCCESSFUL
  （Jabel 正常初始化）；产物 `build/libs/drtech-1.8.9-dev.jar` 已刷新
- ✅ 16/16 台机器适配恢复（注册/配方已还原，`[TEMP-DISABLED]` 清零）；双语 lang 键复核齐全
- ✅ 结构字符串 ↔ 本文档 y 层图程序化核对一致；方块数/总格数校验通过
- ⏳ 游戏内联机验证（21×21×11 v2 新结构；旧 7×7×5 / 9×9×5 作废；步骤见 `docs/in-game-test-checklist.md`）

## 七、待办

1. 游戏内启动 `gradlew runClient`（或直接使用新 jar），按 `docs/in-game-test-checklist.md`
   搭建 21×21×11 v2 环形结构并实测（含 16 台机器逐台成型）
2. 数值平衡微调（建磁速率/加热时间/燃料倍率见 `FusionReactorLogic` 常量）
3. 可选：JEI 结构预览页、TOP 聚变堆信息显示
