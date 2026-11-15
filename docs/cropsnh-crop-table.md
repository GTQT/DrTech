# CropsNH 作物表（静态提取 · 本体作物，供勾选做测试用例）

> 数据来源：`E:\模组开发\资料库\CropsNH`（LGPL-3.0），`crops/**` 类实现 + `loaders/CropLoader.java` 注册链。
> 提取方式：静态解析（未编译、未启动游戏），默认值按 `NHCropCard` / `CropCard` 继承链回填。
> 共 **181** 条注册：**121 条本体作物**（可接入）+ **60 条联动作物**（标注“联动”，只作参考，不接入、不勾选）。

## 怎么用

1. 勾选只针对“范围=本体”的行；联动行保留在表里仅供查数，不会写进项目。
2. 在“勾选”列把 `[ ]` 改成 `[x]`（或直接回复编号，如 `R012 R044 R101`）。
3. 勾选后我把这些作物写成 cropQT 的测试用例（只补 `CropType`/注册数据，不改机器代码）。
4. 推荐测试集已全部避开联动作物，覆盖土壤/子土壤/光照/电压/种子床/渲染/杂草等机制分支。

## 列说明与默认值

| 列 | 含义 | 默认值 |
| --- | --- | --- |
| 范围 | 本体 = 可独立接入；联动 = 依赖其他模组，不接入 | — |
| Tier | 作物 tier（生长难度档，非电压） | 1 |
| 时长 | `getGrowthDuration()`，单位 tick | `600 × tier` |
| 阶段 | 可见生长阶段 min~max | 1~4 |
| 渲染 | `PlantRenderShape` | `Hash` |
| 土壤 | 主土壤组（`CropsNHSoilTypes`） | `farmland` |
| 子土壤 | 必须放在下方的方块组（`addSubSoilRequirement`） | 无 |
| 机器 | 杂交机器配方最低电压 | `LV` |
| 种子床 | 工业农场种子床最低电压 | 不限 |
| 掉落 | `addDrop` 条目数（类内+注册链） | — |
| 备注 | 生长需求 / 喜好群系 / 伤害 / 替代种子 / 催化剂 / NEI | — |

土壤组：`farmland` 耕地、`sand` 沙、`soulsand` 灵魂沙、`dirtGrass` 土/草、`mycelium` 菌丝、
`end` 末地石、`stone` 石、`netherrack` 地狱岩、`brick` 砖、`thaumLogs` 神秘原木、
`graveyard` 墓地、`slimy` 粘液、`gravel` 砾石、`oilSands` 油砂；复合组：`mushroom`、
`netherMushroom`、`sugarcane`、`slimyDirt`、`oil`。

## 推荐测试集（20 条，全部本体作物）

> **状态：已实现（2026-11-14）**。这 20 条已写进 cropQT：`wheat`/`carrot`/`cactus`/`weed` 复用既有 id
> 对齐数值，其余 16 条以 NH id 新增；缺料产物以 GTQT 材料替代。改动清单与替代表见
> `docs/coprqt-implementation-progress.md` 的「P7 测试作物落地」。
>
> **第二批（2026-11-14）已实现 18 条**：原版食物 `potato`/`melon`/`pumpkin`/`cocoa`（改数值）、
> 盆景 `bonsaiSpruce`/`bonsaiBirch`/`bonsaiJungle`/`bonsaiAcacia`/`bonsaiDarkOak`、原版花 9 种
> （`dandelion` 改数值 + 8 条新增）。`bonsaiRubber` 依赖 IC2 树苗/原木，按“不做联动”跳过。
>
> **第三批（2026-11-14）已实现 26 条**：矿石莓 7 条（`tin`/`aluminium`/`copper`/`gold`/`iron`/
> `cobalt` 的 TiC 莓子换成 GT 粒，`essence` 换成经验瓶）、石中百合 9 条（`stone` 第一批已做）、
> 怪物系 10 条（`zomplant`/`enderbloom` 第一批已做）。`ardite`/`thaumium`/`void`/`thauminite`/
> `knightmetal` 需要 TiC/神秘时代/暮色森林的金属，本模组没有对应材料，跳过；
> `goldfish` 的自定义金鱼食物用生鱼代替。

| 勾选 | 编号 | ID | 机制覆盖 |
| --- | --- | --- | --- |
| [x] | R155 | `wheat` | 基线：tier1 / 1000t / 0~7 阶段 / 光照≥9 / 替代种子 |
| [x] | R150 | `carrot` | 食物通用：tier2 / 800t / 替代种子 |
| [x] | R169 | `sugarCane` | X 渲染 + `sugarcane` 复合土壤（沙+土） |
| [x] | R168 | `cactus` | `sand` 土壤 + 实体伤害 1.0 + 阶段 1~3 |
| [x] | R170 | `netherwart` | `soulsand` 土壤 + 阶段 0~2 |
| [x] | R156 | `redMushroom` | `mushroom` 复合土壤（石+土+菌丝） |
| [x] | R160 | `blueOrchid` | 花类：X 渲染 + `flower` 种子形状 + `dirtGrass` |
| [x] | R140 | `stone` | 石百合：`stone` 土壤 + 子土壤=stone |
| [x] | R080 | `olivia` | MV 机器 / 子土壤=橄榄石 / 2 掉落 / 2 催化剂 |
| [x] | R096 | `starWart` | 高压：tier12 / 机器EV / 种子床EV / MachineOnly / 子土壤=下界之星 |
| [x] | R095 | `spaceFlower` | tier13 / 机器IV / 种子床IV / 条件子土壤=space / 附魔种子 |
| [x] | R089 | `redstraw` | 光照窗口 Min5~Max12 + 子土壤=红石 |
| [x] | R073 | `magicalNightshade` | 非整时长 23436t / tier13 / HV 机器+种子床 |
| [x] | R067 | `godOfThunder` | HV 机器+HV 种子床 / 子土壤=钍 / MachineOnly |
| [x] | R079 | `oilBerry` | `oil` 四组分复合土壤（沙+砾石+灵魂沙+油砂） |
| [x] | R134 | `zomplant` | `graveyard` 土壤（怪物系） |
| [x] | R104 | `trollplant` | `brick` 土壤 + 4 掉落 + 3 催化剂 |
| [x] | R003 | `bonsaiOak` | 盆景：1600t / 3 掉落 / 喜好群系 / 替代种子 |
| [x] | R001 | `weed` | 杂草机制（生成/扩散/清除） |
| [x] | R128 | `enderbloom` | 末地系：子土壤=endStone |

## 全量作物表

### 盆景树（7）

> 原版树木；`bonsaiSlimy` 已移入匠魂组

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R003 | 本体 | `bonsaiOak` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 3 | 喜好:PLAINS/FOREST/HILLS; 替代种子×1 |
| [x] | R004 | 本体 | `bonsaiSpruce` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 2 | 喜好:FOREST/CONIFEROUS/MOUNTAIN; 替代种子×1 |
| [x] | R005 | 本体 | `bonsaiBirch` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 2 | 喜好:FOREST/HILLS; 替代种子×1 |
| [x] | R006 | 本体 | `bonsaiJungle` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 2 | 喜好:HOT/JUNGLE/WET/DENSE; 替代种子×1 |
| [x] | R007 | 本体 | `bonsaiAcacia` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 2 | 喜好:HOT/SPARSE/SAVANNA/PLAINS; 替代种子×1 |
| [x] | R008 | 本体 | `bonsaiDarkOak` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 3 | 喜好:DENSE/SPOOKY/FOREST; 替代种子×1 |
| [ ] | R009 | 本体 | `bonsaiRubber` | 1 | 1600 | 1~3 | X | dirtGrass | — | LV | — | 3 | 喜好:CONIFEROUS/FOREST; 替代种子×1; 联动:IC2（树苗/原木/树脂），跳过 |

### BoP（8）

> 联动 · 不接入（需 BiomesOPlenty）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R014 | 联动 | `eyebulb` | 1 | 450 | 1~3 | Hash | netherrack | — | LV | — | 1 | 替代种子×1; 联动:BoP |
| [ ] | R012 | 联动 | `bamboo` | 2 | 250 | 1~3 | Hash | dirtGrass | — | LV | — | 1 | 伤害1.0; 替代种子×1; 联动:BoP |
| [ ] | R019 | 联动 | `ivy` | 2 | 450 | 1~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1; 联动:BoP |
| [ ] | R015 | 联动 | `floweringVine` | 3 | 675 | 1~3 | Hash | farmland | — | LV | — | 1 | 替代种子×1; 联动:BoP |
| [ ] | R016 | 联动 | `glowflower` | 3 | 2000 | 1~3 | Hash | dirtGrass | — | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:BoP |
| [ ] | R018 | 联动 | `glowshroom` | 3 | 600 | 1~5 | X | netherMushroom | — | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:BoP |
| [ ] | R011 | 联动 | `moss` | 4 | 450 | 1~4 | Hash | farmland | — | LV | — | 3 | 替代种子×3 |
| [ ] | R017 | 联动 | `glowingCoral` | 5 | 450 | 1~4 | Hash | farmland | glowstone | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:BoP |

### Natura·地狱菇（3）

> 联动 · 不接入（需 Natura）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R023 | 联动 | `blueGlowshroom` | 3 | 600 | 1~2 | Hash | netherMushroom | — | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:Natura |
| [ ] | R025 | 联动 | `greenGlowshroom` | 3 | 600 | 1~2 | Hash | netherMushroom | — | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:Natura |
| [ ] | R026 | 联动 | `purpleGlowshroom` | 3 | 600 | 1~2 | Hash | netherMushroom | — | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:Natura |

### Natura·地狱莓（4）

> 联动 · 不接入（需 Natura）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R022 | 联动 | `blightberry` | 4 | 300 | 1~3 | Hash | netherrack | — | LV | — | 1 | 替代种子×1; 联动:Natura |
| [ ] | R024 | 联动 | `duskberry` | 4 | 300 | 1~3 | Hash | netherrack | — | LV | — | 1 | 替代种子×1; 联动:Natura |
| [ ] | R027 | 联动 | `skyberry` | 4 | 300 | 1~3 | Hash | netherrack | — | LV | — | 1 | 替代种子×1; 联动:Natura |
| [ ] | R028 | 联动 | `stingberry` | 4 | 300 | 1~3 | Hash | netherrack | — | LV | — | 1 | 替代种子×1; 联动:Natura |

### Natura·地狱（1）

> 联动 · 不接入（需 Natura）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R029 | 联动 | `thornvine` | 3 | 450 | 1~3 | Hash | netherrack | — | LV | — | 1 | 伤害1.0; 替代种子×1; 联动:Natura |

### 食物·BoP（3）

> 联动 · 不接入（需 BiomesOPlenty）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R013 | 联动 | `bopBerry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1; 联动:BoP |
| [ ] | R020 | 联动 | `turnip` | 2 | 450 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2; 联动:BoP |
| [ ] | R021 | 联动 | `wildCarrot` | 2 | 450 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1; 联动:BoP |

### 食物·Natura（6）

> 联动 · 不接入（需 Natura）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R030 | 联动 | `barley` | 2 | 675 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1; 联动:Natura |
| [ ] | R031 | 联动 | `blackberry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2; 联动:Natura |
| [ ] | R032 | 联动 | `blueberry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2; 联动:Natura |
| [ ] | R034 | 联动 | `raspberry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2; 联动:Natura |
| [ ] | R035 | 联动 | `maloberry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2; 联动:Natura |
| [ ] | R036 | 联动 | `saguaroCactus` | 4 | 450 | 1~3 | Hash | sand | — | LV | — | 2 | 需求:MinLightLevel(9); 替代种子×2; 联动:Natura |

### 食物·原版（6）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R153 | 本体 | `potato` | 1 | 800 | 0~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [x] | R155 | 本体 | `wheat` | 1 | 1000 | 0~7 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [x] | R150 | 本体 | `carrot` | 2 | 800 | 0~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [x] | R152 | 本体 | `melon` | 2 | 1200 | 1~4 | Hash | farmland | — | LV | — | 2 | 需求:MinLightLevel(9); 替代种子×1 |
| [x] | R154 | 本体 | `pumpkin` | 2 | 1000 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [x] | R151 | 本体 | `cocoa` | 3 | 1700 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |

### 食物（14）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R043 | 本体 | `huckleberry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [ ] | R047 | 本体 | `strawberry` | 2 | 200 | 1~3 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2 |
| [ ] | R037 | 本体 | `chilly` | 4 | 800 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×3 |
| [ ] | R039 | 本体 | `cucumber` | 4 | 800 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2 |
| [ ] | R041 | 本体 | `grape` | 4 | 800 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2 |
| [ ] | R044 | 本体 | `lemon` | 4 | 800 | 1~4 | X | dirtGrass | — | LV | — | 1 | 替代种子×2 |
| [ ] | R046 | 本体 | `onion` | 4 | 800 | 1~4 | X | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2 |
| [ ] | R048 | 本体 | `sugarBeet` | 4 | 450 | 1~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1; 催化剂×1 |
| [ ] | R049 | 本体 | `tea` | 4 | 2400 | 1~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1 |
| [ ] | R050 | 本体 | `tomato` | 4 | 800 | 1~4 | Hash | farmland | — | LV | — | 2 | 需求:MinLightLevel(9); 替代种子×2 |
| [ ] | R040 | 本体 | `GaiaWart` | 5 | 1000 | 1~3 | Hash | soulsand | — | LV | — | 1 | 替代种子×1; 催化剂×1 |
| [ ] | R042 | 本体 | `hops` | 5 | 2400 | 1~6 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [ ] | R038 | 本体 | `coffee` | 7 | 2800 | 1~4 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1 |
| [ ] | R045 | 本体 | `meatrose` | 7 | 4200 | 1~4 | Hash | dirtGrass | — | LV | — | 5 | 需求:MinLightLevel(9) |

### 材料（56）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R058 | 本体 | `dayflower` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 1 |  |
| [ ] | R063 | 本体 | `flax` | 2 | 1200 | 1~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1 |
| [ ] | R069 | 本体 | `indigo` | 2 | 1200 | 1~4 | Hash | dirtGrass | — | LV | — | 1 | 替代种子×1 |
| [x] | R080 | 本体 | `olivia` | 2 | 400 | 1~4 | Hash | stone | olivine | MV | — | 2 | 催化剂×2 |
| [ ] | R086 | 本体 | `purpleTulip` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 1 |  |
| [ ] | R033 | 联动 | `cotton` | 3 | 900 | 1~5 | X | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×2; 联动:Natura |
| [ ] | R062 | 本体 | `fertilia` | 3 | 1800 | 1~4 | Hash | farmland | — | MV | — | 4 | 催化剂×1 |
| [ ] | R077 | 本体 | `necrobloom` | 3 | 1800 | 1~4 | Hash | farmland | — | LV | — | 2 |  |
| [ ] | R055 | 本体 | `canola` | 4 | 900 | 1~4 | Hash | farmland | — | LV | — | 1 |  |
| [ ] | R066 | 本体 | `glowheat` | 4 | 1500 | 1~7 | Hash | netherrack | glowstone | MV | — | 1 | 催化剂×1 |
| [ ] | R091 | 本体 | `saltyRoot` | 4 | 1600 | 1~3 | Hash | farmland | — | MV | — | 1 | 催化剂×5 |
| [ ] | R092 | 本体 | `sapphirum` | 4 | 800 | 1~4 | Hash | stone | sapphire | MV | — | 2 | 催化剂×2 |
| [ ] | R099 | 本体 | `stickyCane` | 4 | 200 | 1~4 | X | sugarcane | — | LV | — | 1 | 催化剂×1 |
| [ ] | R068 | 本体 | `hemp` | 5 | 800 | 1~5 | Hash | farmland | — | LV | — | 1 |  |
| [ ] | R078 | 本体 | `nickelback` | 5 | 1000 | 1~4 | Hash | stone | nickel | MV | — | 1 | 催化剂×1 |
| [ ] | R082 | 本体 | `papyrus` | 5 | 450 | 1~3 | X | farmland | — | LV | — | 1 |  |
| [ ] | R101 | 本体 | `tine` | 5 | 1000 | 1~4 | Hash | stone | tin | MV | — | 1 | 催化剂×3 |
| [ ] | R053 | 本体 | `bauxia` | 6 | 1200 | 1~4 | Hash | stone | aluminiumBauxite | MV | — | 1 | 催化剂×2 |
| [ ] | R054 | 本体 | `bobsYerUncleRanks` | 6 | 3000 | 1~4 | Hash | stone | emerald | MV | — | 2 | 催化剂×2 |
| [ ] | R056 | 本体 | `cassitine` | 6 | 2800 | 1~4 | Hash | stone | — | LV | — | 1 | 催化剂×1 |
| [ ] | R057 | 本体 | `coppon` | 6 | 1200 | 1~3 | Hash | stone | copper | MV | — | 1 | 催化剂×1 |
| [ ] | R061 | 本体 | `ferrofern` | 6 | 2800 | 1~4 | Hash | stone | — | MV | — | 1 | 催化剂×1 |
| [ ] | R064 | 本体 | `galvania` | 6 | 1200 | 1~4 | Hash | stone | zinc | MV | — | 1 | 催化剂×1 |
| [ ] | R074 | 本体 | `malaxia` | 6 | 2800 | 1~4 | Hash | stone | — | LV | — | 1 | 催化剂×1 |
| [ ] | R076 | 本体 | `milkWart` | 6 | 2400 | 1~4 | Hash | farmland | — | LV | — | 1 |  |
| [ ] | R084 | 本体 | `plumbilia` | 6 | 1200 | 1~4 | Hash | stone | lead | MV | — | 1 | 催化剂×1 |
| [ ] | R085 | 本体 | `plumbshade` | 6 | 2800 | 1~4 | Hash | stone | — | LV | — | 1 | 催化剂×1 |
| [x] | R089 | 本体 | `redstraw` | 6 | 1500 | 1~7 | Hash | farmland | redstone | MV | — | 1 | 需求:MinLightLevel(5)/MaxLightLevel(12); 催化剂×1 |
| [ ] | R100 | 本体 | `thiosulfine` | 6 | 1200 | 1~4 | Hash | stone | sulfur | MV | — | 1 | 催化剂×1 |
| [x] | R104 | 本体 | `trollplant` | 6 | 4800 | 1~8 | Hash | brick | — | EV | — | 4 | 催化剂×3 |
| [ ] | R051 | 本体 | `argentia` | 7 | 1400 | 1~4 | Hash | stone | silver | MV | — | 1 | 催化剂×1 |
| [ ] | R065 | 本体 | `garnydinia` | 7 | 850 | 1~4 | X | stone | garnetGem | LV | — | 8 | 催化剂×5 |
| [ ] | R071 | 本体 | `lazulia` | 7 | 2800 | 1~4 | Hash | stone | lapis | MV | — | 2 | 催化剂×2 |
| [ ] | R052 | 本体 | `auronia` | 8 | 3700 | 1~4 | Hash | stone | gold | MV | — | 1 | 催化剂×1 |
| [ ] | R060 | 本体 | `evilOre` | 8 | 1600 | 1~4 | Hash | netherrack | — | MV | — | 3 | 催化剂×4 |
| [ ] | R072 | 联动 | `liveroot` | 8 | 4800 | 1~4 | Hash | farmland | — | LV | — | 2 | 催化剂×2; 联动:TF |
| [x] | R079 | 本体 | `oilBerry` | 8 | 1200 | 1~4 | Hash | oil | — | MV | — | 1 |  |
| [ ] | R090 | 本体 | `rubyne` | 8 | 1200 | 1~4 | X | farmland | ruby | MV | — | 2 | 催化剂×2 |
| [ ] | R094 | 本体 | `silviscus` | 8 | 3700 | 1~4 | Hash | stone | — | LV | — | 1 | 催化剂×1 |
| [ ] | R105 | 本体 | `withereed` | 8 | 4800 | 1~4 | X | stone | coal | MV | — | 4 | 催化剂×3 |
| [x] | R067 | 本体 | `godOfThunder` | 9 | 3600 | 1~4 | Hash | stone | thorium | HV | HV | 1 | 需求:MachineOnly; 催化剂×1 |
| [ ] | R075 | 本体 | `micadia` | 9 | 1800 | 1~4 | Hash | stone | mica | MV | — | 1 | 催化剂×1 |
| [ ] | R102 | 本体 | `titania` | 9 | 1800 | 1~4 | Hash | stone | titanium | EV | EV | 1 | 需求:MachineOnly; 催化剂×3 |
| [ ] | R098 | 联动 | `steeleafranks` | 10 | 6000 | 1~4 | Hash | stone | steeleaf | MV | — | 2 | 催化剂×2; 联动:TF |
| [ ] | R083 | 本体 | `platina` | 11 | 6600 | 1~4 | Hash | stone | platinum | HV | HV | 1 | 需求:MachineOnly; 催化剂×1 |
| [ ] | R059 | 本体 | `diareed` | 12 | 10800 | 1~4 | X | stone | diamond | MV | — | 1 | 催化剂×2 |
| [ ] | R070 | 本体 | `iridine` | 12 | 7200 | 1~4 | X | stone | iridium | IV | IV | 1 | 需求:MachineOnly; 催化剂×1 |
| [ ] | R081 | 本体 | `osmianth` | 12 | 7200 | 1~4 | X | stone | osmium | IV | IV | 1 | 需求:MachineOnly; 催化剂×1 |
| [ ] | R087 | 本体 | `pyrolusium` | 12 | 2400 | 1~4 | Hash | stone | manganese | MV | — | 1 | 催化剂×1 |
| [ ] | R088 | 本体 | `reactoria` | 12 | 4800 | 1~4 | Hash | stone | uranium | EV | EV | 2 | 需求:MachineOnly; 催化剂×3 |
| [ ] | R093 | 本体 | `scheelinium` | 12 | 2400 | 1~4 | Hash | stone | tungsten | EV | EV | 1 | 需求:MachineOnly; 催化剂×3 |
| [x] | R096 | 本体 | `starWart` | 12 | 7200 | 1~4 | Hash | stone | netherStar | EV | EV | 1 | 需求:MachineOnly; 催化剂×3 |
| [ ] | R097 | 本体 | `stargatium` | 12 | 7200 | 1~4 | Hash | stone | naquadah | IV | IV | 2 | 需求:MachineOnly; 催化剂×4 |
| [ ] | R103 | 本体 | `transformium` | 12 | 7200 | 1~4 | Hash | farmland | — | EV | EV | 2 | 需求:MachineOnly |
| [x] | R073 | 本体 | `magicalNightshade` | 13 | 23436 | 1~4 | Hash | farmland | shadowmetal | HV | HV | 1 | 需求:MachineOnly; 替代种子×1; 催化剂×1 |
| [x] | R095 | 本体 | `spaceFlower` | 13 | 15000 | 1~4 | X | farmland | space | IV | IV | 1 | 需求:MachineOnly |

### 矿石莓（12）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R111 | 本体 | `tin` | 4 | 6000 | 1~4 | Hash | stone | tin | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 替代种子×1; 催化剂×1; 联动:TiC→GT 粒复刻 |
| [x] | R106 | 本体 | `aluminium` | 5 | 6000 | 1~4 | Hash | stone | aluminium | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 替代种子×1; 催化剂×1; 联动:TiC→GT 粒复刻 |
| [x] | R107 | 本体 | `copper` | 5 | 6000 | 1~4 | Hash | stone | copper | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 替代种子×1; 催化剂×1; 联动:TiC→GT 粒复刻 |
| [x] | R108 | 本体 | `essence` | 5 | 6000 | 1~4 | Hash | stone | skull | MV | — | 1 | 伤害1.0; 替代种子×1; 联动:TiC→经验瓶复刻 |
| [x] | R109 | 本体 | `gold` | 5 | 6000 | 1~4 | Hash | stone | gold | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 替代种子×1; 催化剂×1; 联动:TiC→GT 粒复刻 |
| [x] | R110 | 本体 | `iron` | 5 | 6000 | 1~4 | Hash | stone | iron | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 替代种子×1; 催化剂×1; 联动:TiC→GT 粒复刻 |
| [ ] | R112 | 联动 | `ardite` | 5 | 6000 | 1~4 | Hash | stone | ardite | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 催化剂×1; 联动:TiC；本模组无对应材料，跳过 |
| [x] | R113 | 本体 | `cobalt` | 5 | 6000 | 1~4 | Hash | stone | cobalt | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 催化剂×1; 联动:TiC→GT 粒复刻 |
| [ ] | R114 | 联动 | `thaumium` | 7 | 3000 | 1~4 | Hash | stone | thaumium | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 催化剂×1; 联动:Thaumcraft；本模组无对应材料，跳过 |
| [ ] | R115 | 联动 | `void` | 7 | 4500 | 1~4 | Hash | stone | — | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 替代种子×1; 催化剂×1; 联动:Thaumcraft；本模组无对应材料，跳过 |
| [ ] | R116 | 联动 | `thauminite` | 7 | 4500 | 1~4 | Hash | stone | thauminite | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 催化剂×1; 联动:ThaumicBases；本模组无对应材料，跳过 |
| [ ] | R117 | 联动 | `knightmetal` | 8 | 5500 | 1~4 | Hash | stone | knightmetal | MV | — | 1 | 需求:MaxLightLevel(10); 伤害1.0; 催化剂×1; 联动:TF；本模组无对应材料，跳过 |

### 神秘时代（4）

> 联动 · 不接入（需 Thaumcraft）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R118 | 联动 | `cinderpearl` | 5 | 4000 | 1~3 | Hash | dirtGrass | blaze | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:Thaumcraft |
| [ ] | R119 | 联动 | `shimmerleaf` | 5 | 4000 | 1~3 | Hash | dirtGrass | quicksilver | LV | — | 1 | 替代种子×1; 催化剂×1; 联动:Thaumcraft |
| [ ] | R120 | 联动 | `manaBean` | 5 | 2000 | 1~3 | X | thaumLogs | mixedCrystalCluster | LV | — | 6 | 催化剂×6; 联动:Thaumcraft |
| [ ] | R121 | 联动 | `primordialBerry` | 16 | 375000 | 1~4 | X | farmland | — | LV | — | 1 | 替代种子×1; 联动:Thaumcraft |

### 暮色森林（1）

> 联动 · 不接入（需 Twilight Forest）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R122 | 联动 | `torchberry` | 2 | 150 | 1~3 | X | farmland | — | LV | — | 1 | 需求:MaxLightLevel(10); 替代种子×1; 联动:TF |

### 怪物（12）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R134 | 本体 | `zomplant` | 3 | 1800 | 1~4 | X | graveyard | — | LV | — | 4 |  |
| [x] | R129 | 本体 | `goldfish` | 4 | 600 | 1~3 | Hash | farmland | — | LV | — | 1 |  |
| [x] | R130 | 本体 | `inkbloom` | 4 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 1 |  |
| [x] | R132 | 本体 | `spidernip` | 4 | 2400 | 1~4 | Hash | farmland | — | LV | — | 3 |  |
| [x] | R125 | 本体 | `corpseplant` | 5 | 800 | 1~4 | Hash | graveyard | — | LV | — | 3 |  |
| [x] | R123 | 本体 | `blazereed` | 6 | 3600 | 1~4 | X | netherrack | — | MV | — | 2 | 催化剂×2 |
| [x] | R124 | 本体 | `corium` | 6 | 800 | 1~4 | Hash | farmland | — | LV | — | 1 |  |
| [x] | R127 | 本体 | `eggPlant` | 6 | 1200 | 1~3 | Hash | farmland | — | LV | — | 3 | 需求:MinLightLevel(9); 替代种子×1 |
| [x] | R131 | 本体 | `slimeplant` | 6 | 1200 | 1~4 | Hash | dirtGrass | — | LV | — | 1 | 催化剂×1 |
| [x] | R126 | 本体 | `creeperweed` | 7 | 1000 | 1~4 | Hash | farmland | — | LV | — | 1 |  |
| [x] | R133 | 本体 | `tearstalks` | 8 | 4800 | 1~4 | Hash | farmland | — | LV | — | 1 | 催化剂×1 |
| [x] | R128 | 本体 | `enderbloom` | 10 | 3000 | 1~4 | Hash | farmland | endStone | HV | — | 2 | 催化剂×2 |

### 石百合（10）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R140 | 本体 | `stone` | 1 | 600 | 1~4 | X | stone | stone | LV | — | 1 |  |
| [x] | R141 | 本体 | `sand` | 1 | 600 | 1~4 | X | stone | sand | LV | — | 1 |  |
| [x] | R142 | 本体 | `clay` | 1 | 850 | 1~4 | X | stone | clay | MV | — | 1 | 催化剂×2 |
| [x] | R143 | 本体 | `netherStone` | 1 | 600 | 1~4 | X | stone | netherrack | LV | — | 1 |  |
| [x] | R145 | 本体 | `endStone` | 1 | 850 | 1~4 | X | stone | endStone | MV | — | 1 | 催化剂×1 |
| [x] | R146 | 本体 | `redGranite` | 1 | 600 | 1~4 | X | stone | redGranite | LV | — | 1 |  |
| [x] | R147 | 本体 | `blackGranite` | 1 | 600 | 1~4 | X | stone | blackGranite | LV | — | 1 |  |
| [x] | R148 | 本体 | `basalt` | 1 | 600 | 1~4 | X | stone | basalt | LV | — | 1 |  |
| [x] | R149 | 本体 | `marble` | 1 | 600 | 1~4 | X | stone | marble | LV | — | 1 |  |
| [x] | R144 | 本体 | `soulSand` | 2 | 2000 | 1~4 | X | stone | soulSand | HV | — | 1 | 催化剂×1 |

### 石百合·Modern（3）

> 联动 · 不接入（需 Botania / Chisel / EtFuturum 的石块）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R135 | 联动 | `andesite` | 1 | 600 | 1~4 | X | stone | modernAndesite | LV | — | 2 |  |
| [ ] | R136 | 联动 | `diorite` | 1 | 600 | 1~4 | X | stone | modernDiorite | LV | — | 2 |  |
| [ ] | R137 | 联动 | `granite` | 1 | 600 | 1~4 | X | stone | modernGranite | LV | — | 2 |  |

### 石百合·EtFuturum（2）

> 联动 · 不接入（需 EtFuturum + NewHorizonsCoreMod）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R138 | 联动 | `deepslate` | 1 | 600 | 1~4 | X | stone | deepslate | LV | — | 1 |  |
| [ ] | R139 | 联动 | `tuff` | 1 | 600 | 1~4 | X | stone | tuff | LV | — | 1 |  |

### 原版·花（10）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R158 | 本体 | `dandelion` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R159 | 本体 | `poppy` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R160 | 本体 | `blueOrchid` | 2 | 600 | 1~4 | X | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R161 | 本体 | `allium` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R162 | 本体 | `azureBluet` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R163 | 本体 | `redTulip` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R164 | 本体 | `orangeTulip` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R165 | 本体 | `whiteTulip` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R166 | 本体 | `pinkTulip` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |
| [x] | R167 | 本体 | `oxeyeDaisy` | 2 | 600 | 1~4 | Hash | dirtGrass | — | LV | — | 2 | 替代种子×1 |

### 原版·蘑菇（2）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R156 | 本体 | `redMushroom` | 1 | 400 | 1~4 | Hash | mushroom | — | LV | — | 1 | 需求:MaxLightLevel(9); 替代种子×1 |
| [ ] | R157 | 本体 | `brownMushroom` | 1 | 400 | 1~4 | Hash | mushroom | — | LV | — | 1 | 需求:MaxLightLevel(9); 替代种子×1 |

### 原版（5）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R169 | 本体 | `sugarCane` | 2 | 400 | 1~3 | X | sugarcane | — | LV | — | 1 | 替代种子×1 |
| [ ] | R171 | 本体 | `vine` | 2 | 450 | 1~3 | Hash | farmland | — | LV | — | 1 | 替代种子×1 |
| [ ] | R172 | 本体 | `waterLily` | 2 | 450 | 1~3 | Hash | farmland | — | LV | — | 2 | 替代种子×1 |
| [x] | R168 | 本体 | `cactus` | 3 | 450 | 1~3 | Hash | sand | — | LV | — | 1 | 伤害1.0; 替代种子×1 |
| [x] | R170 | 本体 | `netherwart` | 5 | 2000 | 0~2 | Hash | soulsand | — | LV | — | 1 | 替代种子×1 |

### 巫术（9）

> 联动 · 不接入（需 Witchery）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R175 | 联动 | `garlic` | 3 | 450 | 0~5 | Hash | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1; 联动:Witchery |
| [ ] | R173 | 联动 | `belladonna` | 4 | 1100 | 0~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1; 联动:Witchery |
| [ ] | R176 | 联动 | `glintWeed` | 4 | 1100 | 1~4 | X | farmland | — | LV | — | 1 | 替代种子×1; 联动:Witchery |
| [ ] | R177 | 联动 | `mandrake` | 4 | 1100 | 0~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1; 联动:Witchery |
| [ ] | R178 | 联动 | `snowbell` | 4 | 1100 | 0~4 | X | farmland | — | LV | — | 3 | 替代种子×1; 联动:Witchery |
| [ ] | R180 | 联动 | `waterArtichoke` | 4 | 1100 | 0~4 | X | farmland | — | LV | — | 1 | 需求:MinLightLevel(9); 替代种子×1; 联动:Witchery |
| [ ] | R181 | 联动 | `wolfsbane` | 4 | 1100 | 0~7 | X | farmland | — | LV | — | 1 | 替代种子×1; 联动:Witchery |
| [ ] | R174 | 联动 | `emberMoss` | 7 | 1100 | 1~4 | X | farmland | — | LV | — | 1 | 替代种子×1; 联动:Witchery |
| [ ] | R179 | 联动 | `spanishMoss` | 7 | 675 | 1~4 | Hash | farmland | — | LV | — | 1 | 替代种子×1; 联动:Witchery |

### 匠魂（1）

> 联动 · 不接入（需 TinkerConstruct）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [ ] | R010 | 联动 | `bonsaiSlimy` | 1 | 1600 | 1~3 | X | slimyDirt | — | LV | — | 2 | 喜好:WET/SWAMP; 替代种子×1; 催化剂×1; 联动:TiC |

### 特殊（2）

| 勾选 | 编号 | 范围 | ID | Tier | 时长 | 阶段 | 渲染 | 土壤 | 子土壤 | 机器 | 种子床 | 掉落 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [x] | R001 | 本体 | `weed` | 1 | 600 | 1~4 | Hash | farmland | — | LV | — | 0 | NEI隐藏 |
| [ ] | R002 | 本体 | `migrator` | 1 | 1 | 1~1 | Hash | farmland | — | LV | — | 0 | 需求:MigratedCrop; NEI隐藏 |

## 附：不接入的联动作物（并集 60 条，仅备查）

> 判定规则（满足其一即联动）：① 整组属于其他模组的联动作物（45 条）；② 注册调用被 `ModUtils.<模组>.isModLoaded()` 包裹（54 条）。两表有重叠。

### A. 整组联动（45 条）

- **BoP**：`moss`、`bamboo`、`eyebulb`、`floweringVine`、`glowflower`、`glowingCoral`、`glowshroom`、`ivy`
- **Natura·地狱菇**：`blueGlowshroom`、`greenGlowshroom`、`purpleGlowshroom`
- **Natura·地狱莓**：`blightberry`、`duskberry`、`skyberry`、`stingberry`
- **Natura·地狱**：`thornvine`
- **食物·BoP**：`bopBerry`、`turnip`、`wildCarrot`
- **食物·Natura**：`barley`、`blackberry`、`blueberry`、`raspberry`、`maloberry`、`saguaroCactus`
- **神秘时代**：`cinderpearl`、`shimmerleaf`、`manaBean`、`primordialBerry`
- **暮色森林**：`torchberry`
- **石百合·Modern**：`andesite`、`diorite`、`granite`
- **石百合·EtFuturum**：`deepslate`、`tuff`
- **巫术**：`belladonna`、`emberMoss`、`garlic`、`glintWeed`、`mandrake`、`snowbell`、`spanishMoss`、`waterArtichoke`、`wolfsbane`
- **匠魂**：`bonsaiSlimy`

### B. 注册被模组条件包裹（54 条）

- **BoP**：`bamboo`、`bopBerry`、`eyebulb`、`floweringVine`、`glowflower`、`glowingCoral`、`glowshroom`、`ivy`、`turnip`、`wildCarrot`
- **Natura**：`blightberry`、`blueGlowshroom`、`duskberry`、`greenGlowshroom`、`purpleGlowshroom`、`skyberry`、`stingberry`、`thornvine`、`barley`、`blackberry`、`blueberry`、`cotton`、`raspberry`、`maloberry`、`saguaroCactus`
- **Thaumcraft**：`thaumium`、`void`、`cinderpearl`、`shimmerleaf`、`manaBean`、`primordialBerry`
- **ThaumicBases**：`thauminite`
- **TiC**：`bonsaiSlimy`、`aluminium`、`copper`、`essence`、`gold`、`iron`、`tin`、`ardite`、`cobalt`
- **TF**：`liveroot`、`steeleafranks`、`knightmetal`、`torchberry`
- **Witchery**：`belladonna`、`emberMoss`、`garlic`、`glintWeed`、`mandrake`、`snowbell`、`spanishMoss`、`waterArtichoke`、`wolfsbane`

## 附：按机制挑选的索引（仅本体）

- **子土壤**：`aluminiumBauxite`, `basalt`, `blackGranite`, `clay`, `coal`, `copper`, `diamond`, `emerald`, `endStone`, `garnetGem`, `glowstone`, `gold`, `iridium`, `lapis`, `lead`, `manganese`, `marble`, `mica`, `naquadah`, `netherStar`, `netherrack`, `nickel`, `olivine`, `osmium`, `platinum`, `redGranite`, `redstone`, `ruby`, `sand`, `sapphire`, `shadowmetal`, `silver`, `soulSand`, `space`, `stone`, `sulfur`, `thorium`, `tin`, `titanium`, `tungsten`, `uranium`, `zinc`
- **非 farmland 土壤**：brick、dirtGrass、graveyard、mushroom、netherrack、oil、sand、soulsand、stone、sugarcane
- **机器 > LV**：argentia、auronia、bauxia、bobsYerUncleRanks、coppon、diareed、evilOre、ferrofern、fertilia、galvania、glowheat、godOfThunder、iridine、lazulia、magicalNightshade、micadia、nickelback、oilBerry、olivia、osmianth、platina、plumbilia、pyrolusium、reactoria、redstraw、rubyne、saltyRoot、sapphirum、scheelinium、spaceFlower、starWart、stargatium、thiosulfine、tine、titania、transformium、trollplant、withereed、blazereed、enderbloom、clay、soulSand、endStone
- **种子床受限**：godOfThunder、iridine、magicalNightshade、osmianth、platina、reactoria、scheelinium、spaceFlower、starWart、stargatium、titania、transformium
- **X 渲染**：bonsaiOak、bonsaiSpruce、bonsaiBirch、bonsaiJungle、bonsaiAcacia、bonsaiDarkOak、bonsaiRubber、lemon、onion、diareed、garnydinia、iridine、osmianth、papyrus、rubyne、spaceFlower、stickyCane、withereed、blazereed、zomplant、stone、sand、clay、netherStone、soulSand、endStone、redGranite、blackGranite、basalt、marble、blueOrchid、sugarCane
- **有实体伤害**：cactus
- **NEI 隐藏**：weed、migrator
