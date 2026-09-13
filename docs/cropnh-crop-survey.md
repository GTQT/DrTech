# CropNH 作物清单（移植候选）

> 来源：`E:\模组开发\资料库\CropsNH` —— `crops/` 下共 **173 株**
> （不含 `abstracts/` 的基类、`CropWeed`、`CropMigrator`）

**怎么勾**：把你要的行首 `[ ]` 改成 `[x]`，然后告诉我一声。
分几次勾也行，勾一批我移植一批。

### 列的含义

| 列 | 说明 |
|---|---|
| tier | CropNH 的作物等级，决定生长快慢与合成器最低电压 |
| 机育 | `getMachineBreedingRecipeTier()`，育种机需要的电压档 |
| 时长 | `getGrowthDuration()`，一轮成熟的 tick 数，折算成秒 |
| 产物 | `addDrop` 的掉落物与概率 |
| 底土 | `addSubSoilRequirement`，不满足时生长大幅变慢 |
| 贴图 | ✅ = DrTechC 里已有同名生长贴图目录；❌ = 没找到（可能真缺，也可能只是叫法不同），移植前得先从 CropNH 拷 4~6 张生长帧图 |
| 依赖 | 该作物引用的别的模组的物品，没有那个模组就移植不了 |

### 移植的两条路径

1. **材料驱动**（`material/`、`oreberries/`、`stoneilies/`）—— 在
   `CropMaterials.declare(...)` 加一行即可，产物物品、图标颜色、矿物词典全自动推出来。
2. **手写表**（其余）—— 产物不是 GT 材料，得在 `CropRegistry` 的手写表里加一条，
   并自己指定种子图标与产物物品。

> ⚠️ 手写表那条路目前只支持「一个作物一个产物物品、无概率」的简单作物。
> 带概率的、掉多个物品的（比如 `corpseplant` 掉 3 样）需要先扩这套结构。

---

## 已移植（20 株）

这 20 株已经在 `CropMaterials.java` 里，列出来供对照，不用再勾。

> ⚠️ **注意 tier 那一列**：移植时把 CropNH 的 tier 大幅压缩过了
> （金属叶原版是 5~12，我们统一压到 5~8），所以两边的 tier 对不上是正常的，
> 不是统计错误。下面这个表同时列了两边。

| id | CropNH tier | DrTechC tier | 土壤 | 底土 | CropNH 时长 | 产物 |
|---|---|---|---|---|---|---|
| `argentia` | 7 | 5 | 石头 | silver | 70s | `argentiaLeaf`×1 100% |
| `auronia` | 8 | 5 | 石头 | gold | 185s | `auroniaLeaf`×1 100% |
| `bauxia` | 6 | 5 | 石头 | aluminiumBauxite | 60s | `bauxiaLeaf`×1 100% |
| `coppon` | 6 | 5 | 石头 | copper | 60s | `copponFiber`×1 100% |
| `ferrofern` | 6 | 5 | 石头 | — | 140s | `ferrofernLeaf`×1 100% |
| `micadia` | 9 | 5 | 石头 | mica | 90s | `micadiaFlower`×1 100% |
| `oilBerry` | 4 | 5 | 油砂类 | — | 60s | `oilBerry`×2 100% |
| `plumbilia` | 6 | 5 | 石头 | lead | 60s | `plumbiliaLeaf`×1 100% |
| `saltyRoot` | 4 | 5 | 耕地 | — | 80s | `saltyRoot`×1 100% |
| `tine` | 5 | 5 | 石头 | tin | 50s | `tineTwig`×1 100% |
| `galvania` | 6 | 6 | 石头 | zinc | 60s | `galvaniaLeaf`×1 100% |
| `nickelback` | 5 | 6 | 石头 | nickel | 50s | `nickelbackLeaf`×1 100% |
| `scheelinium` | 12 | 6 | 石头 | tungsten | 120s | `scheeliniumLeaf`×1 100% |
| `thiosulfine` | 6 | 6 | 石头 | sulfur | 60s | `thiosulfineFlower`×1 100% |
| `titania` | 9 | 6 | 石头 | titanium | 90s | `titaniaLeaf`×1 100% |
| `iridine` | 12 | 7 | 石头 | iridium | 360s | `iridineFlower`×1 75% |
| `osmianth` | 12 | 7 | 石头 | osmium | 360s | `osmianthFlower`×1 25% |
| `platina` | 11 | 7 | 石头 | platinum | 330s | `platinaLeaf`×1 100% |
| `pyrolusium` | 12 | 8 | 石头 | manganese | 120s | `pyrolusiumLeaf`×1 100% |
| `reactoria` | 12 | 8 | 石头 | uranium | 240s | `reactoriaLeaf`×1 75% + `reactoriaStem`×1 25% |

---

## 概览

| 分类 | 株数 | 已移植 | 移植路径 |
|---|---|---|---|
| `vanilla/` | 5 | 0 | 手写表 |
| `vanilla/flowers/` | 10 | 0 | 手写表 |
| `vanilla/mushrooms/` | 2 | 0 | 手写表 |
| `food/` | 14 | 0 | 手写表 |
| `food/vanilla/` | 6 | 0 | 手写表 |
| `food/natura/` | 6 | 0 | 手写表 |
| `food/bop/` | 3 | 0 | 手写表 |
| `material/` | 56 | 20 | **材料驱动** |
| `oreberries/` | 12 | 0 | **材料驱动**（需要 Tinkers 的 oreBerries 物品） |
| `stoneilies/` | 10 | 0 | **材料驱动**（底土按石种） |
| `stoneilies/modern/` | 3 | 0 | **材料驱动** |
| `stoneilies/etfuturum/` | 2 | 0 | **材料驱动** |
| `mobs/` | 12 | 0 | 手写表 |
| `witchery/` | 9 | 0 | 手写表 |
| `thaumcraft/` | 4 | 0 | 手写表 |
| `biomesoplenty/` | 8 | 0 | 手写表 |
| `natura/nether/` | 1 | 0 | 手写表 |
| `natura/nether/berry/` | 4 | 0 | 手写表 |
| `natura/nether/glowshroom/` | 4 | 0 | 手写表 |
| `twilightforest/` | 1 | 0 | 手写表 |
| `TiC/` | 1 | 0 | 手写表 |
| **合计** | **173** | **20** | |

---

## 原版方块/植物 —— `vanilla/`（5 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `sugarCane` | 2 | LV | 20s | 沙/泥土 | — | `reeds`×2 100% | ✅ | — |
| [ ] | `vine` | 2 | LV | 22s | 耕地 | — | `vine`×2 100% | ✅ | — |
| [ ] | `waterLily` | 2 | LV | 22s | 耕地 | — | `dye`×2:9 200% + `waterlily`×2 800% | ✅ | — |
| [ ] | `cactus` ☠ | 3 | LV | 22s | 沙子 | — | `cactus`×1 100% | ✅ | — |
| [ ] | `netherwart` | 5 | LV | 100s | 灵魂沙 | — | `nether_wart`×1 100% | ❌ | — |

## 原版花 —— `vanilla/flowers/`（10 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `allium` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:13 100% | ✅ | — |
| [ ] | `azureBluet` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:7 100% | ✅ | — |
| [ ] | `blueOrchid` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:12 100% | ✅ | — |
| [ ] | `dandelion` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:11 100% | ✅ | — |
| [ ] | `orangeTulip` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:14 100% | ✅ | — |
| [ ] | `oxeyeDaisy` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:7 100% | ✅ | — |
| [ ] | `pinkTulip` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:9 100% | ✅ | — |
| [ ] | `poppy` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:1 100% | ✅ | — |
| [ ] | `redTulip` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:1 100% | ✅ | — |
| [ ] | `whiteTulip` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:7 100% | ✅ | — |

## 原版蘑菇 —— `vanilla/mushrooms/`（2 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `brownMushroom` | 1 | LV | 20s | 菌丝/泥土/石头 | — | `brown_mushroom`×1 100% | ✅ | — |
| [ ] | `redMushroom` | 1 | LV | 20s | 菌丝/泥土/石头 | — | `red_mushroom`×1 100% | ✅ | — |

## 食物 —— `food/`（14 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `huckleberry` | 2 | LV | 10s | 耕地 | — | `huckleBerry`×1 100% | ✅ | — |
| [ ] | `strawberry` | 2 | LV | 10s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropStr` 100% | ✅ | — |
| [ ] | `chilly` | 4 | LV | 40s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropChi` 100% | ✅ | — |
| [ ] | `cucumber` | 4 | LV | 40s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropCuc` 100% | ✅ | — |
| [ ] | `grape` | 4 | LV | 40s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropGra` 100% | ✅ | — |
| [ ] | `lemon` | 4 | LV | 40s | 泥土/草 | — | `OreDictHelper.getCopiedOreStack("cropLem` 100% | ✅ | PamsHarvestCraft |
| [ ] | `onion` | 4 | LV | 40s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropOni` 100% | ✅ | — |
| [ ] | `sugarBeet` | 4 | LV | 22s | 耕地 | — | `sugarBeet`×1 100% | ❌ | — |
| [ ] | `tea` | 4 | LV | 120s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropTea` 100% | ✅ | — |
| [ ] | `tomato` | 4 | LV | 40s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropTom` 75% + `maxTomato`×1 25% | ✅ | — |
| [ ] | `gaiaWart` | 5 | LV | 50s | 灵魂沙 | — | `gaiaWart`×1 100% | ✅ | — |
| [ ] | `hops` | 5 | LV | 120s | 耕地 | — | `hops`×1 100% | ✅ | — |
| [ ] | `coffee` | 7 | LV | 140s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropCof` 100% | ✅ | — |
| [ ] | `meatrose` | 7 | LV | 210s | 泥土/草 | — | `dye`×1:9 60% + `chicken`×1 10% + `fish`×1 10% + `beef`×1 10% + `porkchop`×1 10% | ✅ | — |

## 食物（原版） —— `food/vanilla/`（6 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `potato` | 1 | LV | 40s | 耕地 | — | `potato`×1 100% | ✅ | — |
| [ ] | `wheat` | 1 | LV | 50s | 耕地 | — | `wheat`×1 100% | ✅ | — |
| [ ] | `carrot` | 2 | LV | 40s | 耕地 | — | `carrot`×1 100% | ✅ | — |
| [ ] | `melon` | 2 | LV | 60s | 耕地 | — | `melon`×4 66.66% + `melon_block`×1 33.33% | ✅ | — |
| [ ] | `pumpkin` | 2 | LV | 50s | 耕地 | — | `pumpkin`×1 100% | ✅ | — |
| [ ] | `cocoa` | 3 | LV | 85s | 耕地 | — | `dye`×1:3 100% | ✅ | — |

## 食物（Natura） —— `food/natura/`（6 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `barley` | 2 | LV | 34s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropBar` 100% | ❌ | — |
| [ ] | `blackberry` | 2 | LV | 10s | 耕地 | — | `Natura:berry`×3 100% | ✅ | Natura |
| [ ] | `blueberry` | 2 | LV | 10s | 耕地 | — | `Natura:berry`×3 100% | ✅ | Natura |
| [ ] | `maloberry` | 2 | LV | 10s | 耕地 | — | `Natura:berry`×3 100% | ✅ | Natura |
| [ ] | `raspberry` | 2 | LV | 10s | 耕地 | — | `Natura:berry`×3 100% | ✅ | Natura |
| [ ] | `saguaroCactus` | 4 | LV | 22s | 沙子 | — | `saguaro` 50% + `saguaroFruit` 50% | ✅ | Natura |

## 食物（BoP） —— `food/bop/`（3 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `bopBerry` | 2 | LV | 10s | 耕地 | — | `bopBerry` 100% | ✅ | BiomesOPlenty |
| [ ] | `turnip` | 2 | LV | 22s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropTur` 100% | ✅ | — |
| [ ] | `wildCarrot` | 2 | LV | 22s | 耕地 | — | `wildCarrot` 100% | ✅ | BiomesOPlenty |

## 材料作物 —— `material/`（56 株）
移植路径：**材料驱动**

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `dayflower` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:6 100% | ✅ | — |
| [ ] | `purpleTulip` | 1 | LV | 30s | 泥土/草 | — | `dye`×1:5 100% | ✅ | — |
| [ ] | `flax` | 2 | LV | 60s | 耕地 | — | `string`×1 100% | ✅ | — |
| [ ] | `indigo` | 2 | LV | 60s | 泥土/草 | — | `indigoBlossom`×1 100% | ✅ | — |
| [ ] | `olivia` | 2 | MV | 20s | 石头 | olivine | Olivine dust 75% + Olivine gem 25% | ✅ | — |
| [ ] | `cotton` 🌑 | 3 | LV | 45s | 耕地 | — | `Natura:barleyFood`×1 100% | ❌ | Natura |
| [ ] | `fertilia` | 3 | MV | 90s | 耕地 | — | Calcite dust 62.5% + Phosphate dust 12.5% + Apatite dust 12.5% + `fertilizer`×1 12.5% | ✅ | — |
| [ ] | `necrobloom` | 3 | LV | 90s | 耕地 | — | `poisonPowder`×1 95% + `dye`×1:5 5% | ✅ | — |
| [ ] | `canola` | 4 | LV | 22s | 耕地 | — | `canolaFlower`×1 100% | ✅ | — |
| [ ] | `glowheat` | 4 | MV | 75s | 下界岩 | glowstone | `glowstone_dust`×1 100% | ✅ | — |
| ~~已移植~~ | `oilBerry` | 4 | MV | 60s | 油砂类 | — | `oilBerry`×2 100% | ✅ | — |
| [ ] | `rubyne` | 4 | MV | 40s | 石头 | ruby | Ruby dust 75% + Ruby gem 25% | ✅ | — |
| ~~已移植~~ | `saltyRoot` | 4 | MV | 80s | 耕地 | — | `saltyRoot`×1 100% | ✅ | — |
| [ ] | `sapphirum` | 4 | MV | 40s | 石头 | sapphire | Sapphire dust 75% + Sapphire gem 25% | ✅ | — |
| [ ] | `stickyCane` | 4 | LV | 10s | 沙/泥土 | — | `ItemList.IC2_Resin.get(1L)` 100% | ✅ | — |
| [ ] | `hemp` | 5 | LV | 40s | 耕地 | — | `hempStem`×1 100% | ✅ | — |
| ~~已移植~~ | `nickelback` | 5 | MV | 50s | 石头 | nickel | `nickelbackLeaf`×1 100% | ✅ | — |
| [ ] | `papyrus` | 5 | LV | 22s | 耕地 | — | `paper`×1 100% | ✅ | — |
| ~~已移植~~ | `tine` | 5 | MV | 50s | 石头 | tin | `tineTwig`×1 100% | ✅ | — |
| ~~已移植~~ | `bauxia` | 6 | MV | 60s | 石头 | aluminiumBauxite | `bauxiaLeaf`×1 100% | ✅ | — |
| [ ] | `bobsYerUncleRanks` | 6 | MV | 150s | 石头 | emerald | `bobsYerUncleBerry`×1 75% + `emerald`×1 25% | ✅ | — |
| [ ] | `cassitine` | 6 | LV | 140s | 石头 | — | Tin dusttiny 100% | ✅ | — |
| ~~已移植~~ | `coppon` | 6 | MV | 60s | 石头 | copper | `copponFiber`×1 100% | ✅ | — |
| ~~已移植~~ | `ferrofern` | 6 | MV | 140s | 石头 | — | `ferrofernLeaf`×1 100% | ✅ | — |
| ~~已移植~~ | `galvania` | 6 | MV | 60s | 石头 | zinc | `galvaniaLeaf`×1 100% | ✅ | — |
| [ ] | `malaxia` | 6 | LV | 140s | 石头 | — | Copper dusttiny 100% | ✅ | — |
| [ ] | `milkWart` | 6 | LV | 120s | 耕地 | — | `milkWart`×1 100% | ✅ | — |
| ~~已移植~~ | `plumbilia` | 6 | MV | 60s | 石头 | lead | `plumbiliaLeaf`×1 100% | ✅ | — |
| [ ] | `plumbshade` | 6 | LV | 140s | 石头 | — | Lead dusttiny 100% | ✅ | — |
| [ ] | `redstraw` 🌑 | 6 | MV | 75s | 耕地 | redstone | `redstone`×1 100% | ✅ | — |
| ~~已移植~~ | `thiosulfine` | 6 | MV | 60s | 石头 | sulfur | `thiosulfineFlower`×1 100% | ✅ | — |
| [ ] | `trollplant` | 6 | EV | 240s | 砖块 | — | Spinel gem 62.5% + Plutonium241 dust 12.5% + `ItemList.IC2_Plantball.get(1)` 12.5% + `ItemList.IC2_Scrap.get(1)` 12.5% | ✅ | — |
| ~~已移植~~ | `argentia` | 7 | MV | 70s | 石头 | silver | `argentiaLeaf`×1 100% | ✅ | — |
| [ ] | `garnydinia` | 7 | LV | 42s | 石头 | garnetGem | GarnetRed dust 22.5% + GarnetYellow dust 22.5% + GarnetRed gem 22.5% + GarnetYellow gem 22.5% + GarnetRed gemExquisite 2.5% + GarnetYellow gemExquisite 2.5% + GarnetRed crushedPurified 2.5% + GarnetYellow crushedPurified 2.5% | ✅ | — |
| [ ] | `lazulia` | 7 | MV | 140s | 石头 | lapis | Lapis dust 66.67% + `dye`×1:4 33.33% | ✅ | — |
| ~~已移植~~ | `auronia` | 8 | MV | 185s | 石头 | gold | `auroniaLeaf`×1 100% | ✅ | — |
| [ ] | `evilOre` | 8 | MV | 80s | 下界岩 | — | NetherQuartz dust 66.66% + CertusQuartz dust 16.67% + `quartz`×1 16.67% | ✅ | — |
| [ ] | `liveroot` | 8 | LV | 240s | 耕地 | — | LiveRoot dust 75% + `ItemList.TF_LiveRoot.get(1)` 75% | ✅ | — |
| [ ] | `silviscus` | 8 | LV | 185s | 石头 | — | Silver dusttiny 100% | ✅ | — |
| [ ] | `withereed` | 8 | MV | 240s | 石头 | coal | Coal dust 66.67% + `coal`×1 33.33% + `skull`×1:1 1.29% + `skull`×1 2.58% | ✅ | — |
| [ ] | `godOfThunder` | 9 | HV | 180s | 石头 | thorium | `thunderLeaf`×1 100% | ✅ | — |
| ~~已移植~~ | `micadia` | 9 | MV | 90s | 石头 | mica | `micadiaFlower`×1 100% | ✅ | — |
| ~~已移植~~ | `titania` | 9 | EV | 90s | 石头 | titanium | `titaniaLeaf`×1 100% | ✅ | — |
| [ ] | `steeleafranks` | 10 | MV | 300s | 石头 | steeleaf | Steeleaf dust 25% + `TwilightForest:item.steeleafIngot`×1 25% | ✅ | TwilightForest |
| ~~已移植~~ | `platina` | 11 | HV | 330s | 石头 | platinum | `platinaLeaf`×1 100% | ✅ | — |
| [ ] | `diareed` | 12 | MV | 360s | 石头 | diamond | `diamond`×1 75% + Diamond dust 25% | ✅ | — |
| ~~已移植~~ | `iridine` | 12 | IV | 360s | 石头 | iridium | `iridineFlower`×1 75% | ✅ | — |
| ~~已移植~~ | `osmianth` | 12 | IV | 360s | 石头 | osmium | `osmianthFlower`×1 25% | ✅ | — |
| ~~已移植~~ | `pyrolusium` | 12 | MV | 120s | 石头 | manganese | `pyrolusiumLeaf`×1 100% | ✅ | — |
| ~~已移植~~ | `reactoria` | 12 | EV | 240s | 石头 | uranium | `reactoriaLeaf`×1 75% + `reactoriaStem`×1 25% | ✅ | — |
| ~~已移植~~ | `scheelinium` | 12 | EV | 120s | 石头 | tungsten | `scheeliniumLeaf`×1 100% | ✅ | — |
| [ ] | `starWart` | 12 | EV | 360s | 石头 | netherStar | `starWart`×1 100% | ✅ | — |
| [ ] | `stargatium` | 12 | IV | 360s | 石头 | naquadah | `stargatiumLeaf`×1 75% + Endstone dust 25% | ✅ | — |
| [ ] | `transformium` | 12 | EV | 360s | 耕地 | — | `uuaBerry`×1 90% + `uumBerry`×1 10% | ✅ | — |
| [ ] | `magicalNightshade` | 13 | HV | 1172s | 耕地 | shadowmetal | `magicEssence`×1 100% | ✅ | — |
| [ ] | `spaceFlower` | 13 | IV | 750s | 耕地 | space | `spaceFlower`×1 100% | ✅ | GalacticraftCore |

## 矿莓 —— `oreberries/`（12 株）
移植路径：**材料驱动**（需要 Tinkers 的 oreBerries 物品）

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `aluminiumOreBerry` 🌑 | 1 | MV | 300s | 石头 | aluminium | `oreBerries` 100% | ❌ | TinkerConstruct |
| [ ] | `arditeOreBerry` 🌑 | 1 | MV | 300s | 石头 | ardite | Ardite nuggets 100% | ❌ | — |
| [ ] | `cobaltOreBerry` 🌑 | 1 | MV | 300s | 石头 | cobalt | Cobalt nuggets 100% | ❌ | — |
| [ ] | `copperOreBerry` 🌑 | 1 | MV | 300s | 石头 | copper | `oreBerries` 100% | ❌ | TinkerConstruct |
| [ ] | `essenceOreBerry` | 1 | MV | 300s | 石头 | skull | `oreBerries` 100% | ❌ | TinkerConstruct |
| [ ] | `goldOreBerry` 🌑 | 1 | MV | 300s | 石头 | gold | `oreBerries` 100% | ❌ | TinkerConstruct |
| [ ] | `ironOreBerry` 🌑 | 1 | MV | 300s | 石头 | iron | `oreBerries` 100% | ❌ | TinkerConstruct |
| [ ] | `tinOreBerry` 🌑 | 4 | MV | 300s | 石头 | tin | `oreBerries` 100% | ❌ | TinkerConstruct |
| [ ] | `thauminiteOreBerry` 🌑 | 7 | MV | 225s | 石头 | thauminite | `OreDictHelper.getCopiedOreStack("nuggetT` 100% | ❌ | — |
| [ ] | `thaumiumOreBerry` 🌑 | 7 | MV | 150s | 石头 | thaumium | Thaumium nuggets 100% | ❌ | — |
| [ ] | `voidOreBerry` 🌑 | 7 | MV | 225s | 石头 | $void | Void nuggets 100% | ❌ | Thaumcraft |
| [ ] | `knightmetalOreBerry` 🌑 | 8 | MV | 275s | 石头 | knightmetal | `TwilightForest:item.armorShards`×4 100% | ❌ | TwilightForest |

## 石百合 —— `stoneilies/`（10 株）
移植路径：**材料驱动**（底土按石种）

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `basaltLily` | 1 | LV | 30s | 石头 | basalt | Basalt dust 100% | ❌ | — |
| [ ] | `blackGraniteLily` | 1 | LV | 30s | 石头 | blackGranite | GraniteBlack dust 100% | ❌ | — |
| [ ] | `clayLily` | 1 | MV | 42s | 石头 | clay | `clay_ball`×1 100% | ❌ | — |
| [ ] | `endStoneLily` | 1 | MV | 42s | 石头 | endStone | Endstone dust 100% | ❌ | — |
| [ ] | `marbleLily` | 1 | LV | 30s | 石头 | marble | Marble dust 100% | ❌ | — |
| [ ] | `netherStoneLily` | 1 | LV | 30s | 石头 | netherrack | Netherrack dust 100% | ❌ | — |
| [ ] | `redGraniteLily` | 1 | LV | 30s | 石头 | redGranite | GraniteRed dust 100% | ❌ | — |
| [ ] | `sandLily` | 1 | LV | 30s | 石头 | sand | `sand`×4 100% | ❌ | — |
| [ ] | `stoneLily` | 1 | LV | 30s | 石头 | stone | Stone dust 100% | ❌ | — |
| [ ] | `soulSandLily` | 2 | HV | 100s | 石头 | soulSand | `soul_sand`×1 25% | ❌ | — |

## 石百合（现代石种） —— `stoneilies/modern/`（3 株）
移植路径：**材料驱动**

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `andesiteLily` | 1 | LV | 30s | 石头 | modernAndesite | `OreDictHelper.getCopiedOreStack("stoneAn` DROPCHANCE + `new ItemStack(efrStone, DROP_COUNT, 5)` DROPCHANCE | ❌ | Botania、Chisel、EtFuturumRequiem |
| [ ] | `dioriteLily` | 1 | LV | 30s | 石头 | modernDiorite | `OreDictHelper.getCopiedOreStack("stoneDi` DROPCHANCE + `new ItemStack(efrStone, DROP_COUNT, 3)` DROPCHANCE | ❌ | Botania、Chisel、EtFuturumRequiem |
| [ ] | `graniteLily` | 1 | LV | 30s | 石头 | modernGranite | `OreDictHelper.getCopiedOreStack("stoneGr` DROPCHANCE + `new ItemStack(efrStone, DROP_COUNT, 1)` DROPCHANCE | ❌ | Botania、Chisel、EtFuturumRequiem |

## 石百合（Et Futurum） —— `stoneilies/etfuturum/`（2 株）
移植路径：**材料驱动**

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `deepslateLily` | 1 | LV | 30s | 石头 | deepslate | `ModUtils.NewHorizonsCoreMod.getStack("De` 100% | ❌ | NewHorizonsCoreMod |
| [ ] | `tuffLily` | 1 | LV | 30s | 石头 | tuff | `ModUtils.NewHorizonsCoreMod.getStack("Tu` 100% | ❌ | NewHorizonsCoreMod |

## 怪物掉落作物 —— `mobs/`（12 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `zomplant` | 3 | LV | 90s | 墓地 | — | `rottenFlesh` 98% + `ModUtils.Thaumcraft.getStack("ItemZombie` 1.5% + `rottenFlesh` 99.5% + `skull`×1:2 0.5% | ✅ | Thaumcraft |
| [ ] | `goldfish` | 4 | LV | 22s | 耕地 | — | `goldfish`×1 60% | ✅ | — |
| [ ] | `inkbloom` | 4 | LV | 30s | 泥土/草 | — | `dye`×1 100% | ✅ | — |
| [ ] | `spidernip` | 4 | LV | 120s | 耕地 | — | `string`×1 66.66% + `spider_eye`×1 16.67% + `web`×1 16.67% | ✅ | — |
| [ ] | `corpseplant` | 5 | LV | 40s | 墓地 | — | `dye`×1:15 62.5% + `rotten_flesh`×1 25% + `bone`×1 12.5% | ✅ | — |
| [ ] | `blazereed` | 6 | MV | 180s | 下界岩 | — | `blaze_powder`×1 75% + `blaze_rod`×1 25% | ✅ | — |
| [ ] | `corium` | 6 | LV | 40s | 耕地 | — | `leather`×1 100% | ✅ | — |
| [ ] | `eggPlant` | 6 | LV | 60s | 耕地 | — | `egg`×1 60% + `feather`×1 30% + `chicken`×1 10% | ✅ | — |
| [ ] | `slimeplant` | 6 | LV | 60s | 泥土/草 | — | `slime_ball`×1 100% | ✅ | — |
| [ ] | `creeperweed` | 7 | LV | 50s | 耕地 | — | `gunpowder`×1 100% | ✅ | — |
| [ ] | `tearstalks` | 8 | LV | 240s | 耕地 | — | `ghast_tear`×1 66.66% | ✅ | — |
| [ ] | `enderbloom` | 10 | HV | 150s | 耕地 | endStone | EnderPearl dust 62.5% + `ender_pearl`×1 37.5% | ✅ | — |

## Witchery —— `witchery/`（9 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `garlic` | 3 | LV | 22s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropGar` 100% | ❌ | — |
| [ ] | `belladonna` | 4 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("itemBel` 100% | ❌ | — |
| [ ] | `glintWeed` | 4 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropGli` 100% | ✅ | — |
| [ ] | `mandrake` | 4 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("itemMan` 100% | ❌ | — |
| [ ] | `snowbell` | 4 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("itemSno` 89% + `snowball`×1 10% + Cryolite dust 2.5% | ❌ | — |
| [ ] | `waterArtichoke` | 4 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropWat` 100% | ❌ | — |
| [ ] | `wolfsbane` | 4 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("itemWol` 100% | ❌ | — |
| [ ] | `emberMoss` | 7 | LV | 55s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropEmb` 100% | ✅ | — |
| [ ] | `spanishMoss` | 7 | LV | 34s | 耕地 | — | `OreDictHelper.getCopiedOreStack("cropSpa` 100% | ✅ | — |

## Thaumcraft —— `thaumcraft/`（4 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `cinderpearl` | 5 | LV | 200s | 泥土/草 | blaze | `cinderpearl` 100% | ✅ | Thaumcraft |
| [ ] | `manaBean` | 5 | LV | 100s | 神秘原木 | mixedCrystalCluster | `getBean(Aspect.AIR)` 16.66% + `getBean(Aspect.FIRE)` 16.66% + `getBean(Aspect.WATER)` 16.66% + `getBean(Aspect.EARTH)` 16.66% + `getBean(Aspect.ORDER)` 16.66% + `getBean(Aspect.ENTROPY)` 16.66% | ✅ | Thaumcraft |
| [ ] | `shimmerleaf` | 5 | LV | 200s | 泥土/草 | quicksilver | `silverleaf` 100% | ✅ | Thaumcraft |
| [ ] | `primordialBerry` | 16 | LV | 18750s | 耕地 | — | `primPerl` 100% | ✅ | Thaumcraft |

## Biomes O' Plenty —— `biomesoplenty/`（8 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `eyebulb` | 1 | LV | 22s | 下界岩 | — | `eyeBulb` 100% | ✅ | BiomesOPlenty |
| [ ] | `bamboo` ☠ | 2 | LV | 12s | 泥土/草 | — | `BiomesOPlenty:bamboo`×2 100% | ✅ | BiomesOPlenty |
| [ ] | `ivy` | 2 | LV | 22s | 耕地 | — | `ivy` 100% | ✅ | BiomesOPlenty |
| [ ] | `floweringVine` | 3 | LV | 34s | 耕地 | — | `flowerVine` 100% | ✅ | BiomesOPlenty |
| [ ] | `glowflower` | 3 | LV | 100s | 泥土/草 | — | `glowflower` 100% | ✅ | BiomesOPlenty |
| [ ] | `glowshroom` | 3 | LV | 30s | 下界菌 | — | `glowshroom` 100% | ✅ | BiomesOPlenty |
| [ ] | `moss` | 4 | LV | 22s | 耕地 | — | `tfMoss` 5% + `moss` 30% + `treeMoss` 65% | ✅ | BiomesOPlenty、TwilightForest |
| [ ] | `glowingCoral` | 5 | LV | 22s | 耕地 | glowstone | `coral` 100% | ✅ | BiomesOPlenty |

## Natura（下界） —— `natura/nether/`（1 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `thornvine` ☠ | 3 | LV | 22s | 下界岩 | — | `thornVines` 100% | ✅ | Natura |

## Natura（下界浆果） —— `natura/nether/berry/`（4 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `blightberry` | 4 | LV | 15s | 下界岩 | — | `blightberry` 100% | ✅ | Natura |
| [ ] | `duskberry` | 4 | LV | 15s | 下界岩 | — | `duskberry` 100% | ✅ | Natura |
| [ ] | `skyberry` | 4 | LV | 15s | 下界岩 | — | `skyberry` 100% | ✅ | Natura |
| [ ] | `stingberry` | 4 | LV | 15s | 下界岩 | — | `stingberry` 100% | ✅ | Natura |

## Natura（发光菇） —— `natura/nether/glowshroom/`（4 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `blueGlowshroom` | 3 | LV | 30s | 下界菌 | — | `glowshroom` 100% | ✅ | Natura |
| [ ] | `greenGlowshroom` | 3 | LV | 30s | 下界菌 | — | `glowshroom` 100% | ✅ | Natura |
| — | `name` ⚠️CropNH 里未注册 | 3 | LV | 30s | 下界菌 | — | `glowshroom` 100% | — | Natura |
| [ ] | `purpleGlowshroom` | 3 | LV | 30s | 下界菌 | — | `glowshroom` 100% | ✅ | Natura |

## Twilight Forest —— `twilightforest/`（1 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `torchberry` 🌑 | 2 | LV | 8s | 耕地 | — | `torchberries` 100% | ✅ | TwilightForest |

## Tinkers Construct —— `TiC/`（1 株）
移植路径：手写表

| 勾 | id | tier | 机育 | 时长 | 土壤 | 底土 | 产物 | 贴图 | 依赖 |
|---|---|---|---|---|---|---|---|---|---|
| [ ] | `bonsaiSlimy` | 1 | LV | 60s | 粘液泥土 | — | `slime.sapling` 30% + `slime.gel ×4` 60% | ✅ | TinkerConstruct |

---

## 图例

- ☠ = 作物会伤害踩上去的实体（`getEntityDamage`）
- 🌑 = 需要低光照（`MaxLightLevelGrowthRequirement`），只能种在暗处
- 贴图列 ❌ = DrTechC 里没有对应目录，移植前得先从 CropNH 拷贴图（或另做）