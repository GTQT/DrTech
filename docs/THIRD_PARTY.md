# 第三方代码与资源声明

本项目（DrTech / coprQT）的作物系统在「机器、设备、机制」层面复刻自 CropsNH，
部分类为直接照抄后改包名/适配 GTCEu，部分为按源端公式重写。按许可证要求在此声明。

---

## CropsNH

- 项目：CropsNH（GT: New Horizons 作物系统）
- 来源：https://github.com/GTNewHorizons/CropsNH
- 许可证：**LGPL-3.0**（源仓库根目录 `LICENSE.txt`）
- 本项目许可证：GPL-3.0（根目录 `LICENSE`）。LGPL-3.0 允许并入 GPL-3.0 项目；
  被并入的源文件仍受 LGPL-3.0 约束，分发时需保留本声明与源出处。

### 直接对应关系（机器/机制）

| 本项目文件 | CropsNH 源文件 |
| --- | --- |
| `api/VoltageMap.java` | `api/VoltageIndex` 用法（索引映射） |
| `api/CropMachineTankCapacity.java` | `MTEBasicMachine.getCapacityForTier` |
| `api/potency/*`、`api/registries/{Potency,Fertilizer,Hydration,WeedEX}Registry.java` | `api/IPotencyData`、`loaders/PotencyLoader` |
| `api/CropConstants.java` | `TileEntityCropSticks`、`MTECropManager`、`MTEIndustrialFarm` 等常量 |
| `tile/TileCropStick.java`（P1 重写） | `tileentity/TileEntityCropSticks.java` |
| `machine/MetaTileEntityCropManager.java`（P2） | `tileentity/singleblock/MTECropManager.java` |
| `machine/MetaTileEntitySeedGenerator.java` | `tileentity/singleblock/MTESeedGenerator.java` |
| `machine/MetaTileEntityCropBreeder.java` | `tileentity/singleblock/MTECropBreeder.java` |
| `machine/MetaTileEntityCropGeneExtractor.java` | `tileentity/singleblock/MTECropGeneExtractor.java` |
| `machine/MetaTileEntityCropSynthesizer.java` | `tileentity/singleblock/MTECropSynthesizer.java` |
| `machine/MetaTileEntityIndustrialFarm.java`（P4） | `tileentity/multi/MTEIndustrialFarm.java` |
| `block/BlockSeedBed.java`、`block/upgrade/*` | `blocks/BlockSeedBed.java`、`Block*Unit.java` |
| `item/ItemPlantLens.java`、`ItemCropSpade.java`、`ItemPlantCure.java`、`ItemWeedEx.java`（P5） | `items/tools/*` |
| `api/mutation/*`（扩展字段） | `farming/mutation/CropMutation.java`、`api/ICropMutation` |

### 资源

- `src/main/resources/assets/drtech/textures/blocks/crops/**` 等作物贴图，
  按方案第 5.10 节从 CropsNH 的 `assets/cropsnh/textures/**` 复制/回填，
  目录结构与署名随源项目保留。
- 语言文件只取工具/机器名等必要键，不整份复制。

---

## GT5-Unofficial / GTCEu

- 本项目基于 GTCEu（GTQT 分支），机器基类、配方系统、MUI2 等来自 GTCEu/GT5U。
- GT5U 仅作为数值参考（`MTEBasicMachine.getCapacityForTier`、`GTValues.VP`），
  未直接复制其代码。

---

## 维护约定

- 新增/修改与 CropsNH 对应的文件时，在类注释里写明源文件路径。
- 源端数值改动需同步更新 `api/CropConstants.java` 与本表。
