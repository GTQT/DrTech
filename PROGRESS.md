# DrTech 开发进度跟踪 (PROGRESS.md)

> 维护规则：每完成一个关键阶段更新本文件；每轮任务开始前先读取本文件。

## 项目概况
- **目标**：在 `DrTech-master` 实现《发电聚变堆》设计文档（GTQT 2.0.0 更新计划表内容）
- **构建适配目标**：`libs/gregtech-gtqt-1.12.2-1.9.0.jar`（保留版，旧 API：`StructureDefinition`/`Elements`/`CasingSlot` 体系，无 `BlockPatternTemplate`/`setExactLimit` 等新 API）

### 📌 上下文精简快照（新会话从这里继续，无需重读冗长历史）
- **现状**：16/16 台停用机器已全部适配恢复（标准 API）；`[TEMP-DISABLED]` 全部撤销。**09-05 结构 v2 重做**：聚变堆改为 21×21×11 立式胖环托卡马克（沿用 GA Tokamak 轮廓：圆润、逐层全包、高度 5→11；模块 R/M/W/B/C/N/P 分层，复用现有 3 方块无新增），材质改指向 GT 聚变/金属外壳贴图。**全量 gradle 编译 + jar 均 BUILD SUCCESSFUL**（产物 `build/libs/drtech-1.8.9-dev.jar` 已刷新）。备份在 `src_broken_backup/`（勿删）。
- **待办**：① ~~用户机器全量 gradle 编译~~ **已通过（09-05）** ② 游戏内逐台验证 16 台机器成型 ③ 聚变堆 21×21×11 v2 新结构实测（旧 7×7×5/9×9×5 作废）④ 可选：JEI 结构预览 / TOP 显示
- **产物**：`docs/fusion-reactor.md`（第二节已按代码重绘 21×21×11 v2 y 层图 + 方块数量）、`docs/in-game-test-checklist.md`（实测清单，供用户回报格式）、`发电聚变堆开发总结.docx`（工作区根）、`PROGRESS.md`。Word 文档可用 `gen_fusion_doc.ps1` 重新生成。
- **API 速查**：`CasingSlot.auto(7bool)` 顺序=(muffler,maintenance,energyIn,itemIn,itemOut,fluidIn,fluidOut)；机器本地 helper 顺序=(energyIn,maintenance,itemIn,itemOut,fluidIn,fluidOut,muffler) 需重排。分级外壳用 `tieredCasing(char,group)+withChannel`+`CasingDefinition.fromMap`；结构用 `Elements.block/blocks/frames/any/air/self/blockPredicate`、A 位链 `Elements.chain(...)`/`Elements.abilities(...)`。
- **降级记录**：TeslaTower 固定 12 层（COIL_SEGMENT_REPEAT）；TFFT/YotTank 用 tieredCasing 单档电池（drtech_tfft_battery/drtech_yot_battery）+ aisleRepeated(6)。

### 🔧 沙箱构建结论（重要，勿再重复踩坑）
**普通 workspace-write 沙箱**下无法跑通完整 gradle 产物编译，原因（非代码问题）：
1. 网络不可用 → RFG `downloadFernflower` 离线即抛错；缓存 jar 已在
   `D:\GTQT\DrTech\.gradle-home\caches\retro_futura_gradle\fernflower-cache\1.0.342-3afabd6e….jar`。
   跳过该任务可继续：`-x downloadFernflower`。
2. `enableModernJavaSyntax=true`（Jabel）需 ByteBuddy agent self-attach（Windows 命名管道），
   普通沙箱策略拒绝 → 关 Jabel 又因无人机 GUI 等 ~14 个文件的 `case->` arrow-switch（Java 14+ 语法，
   属既有代码）失败。
3. gradle 用户目录必须用 `D:\GTQT\DrTech\.gradle-home`（默认 `%USERPROFILE%\.gradle` 普通沙箱不可写）。

**✅ 已证实可行路径（2026-09-05）**：用 `danger-full-access` 权限执行
`gradlew compileJava/jar --offline -x downloadFernflower` 即可跑通（Jabel 正常初始化，
BUILD SUCCESSFUL）。普通桌面用户直接 `gradlew compileJava`/`runClient` 不受任何影响。

**受限模式全量类型检查替代法（此前验证，可复用）**：
- 用 `gradlew -I build/cp-print.gradle printMainCp --offline -x downloadFernflower` 导出
  main compileClasspath 到 `build/maincp.txt`（`MAIN_CP_BEGIN/END` 标记行夹着完整 cp）。
- cp 中剔除 `jabel` jar（ServiceLoader 会强制加载导致 javac 启动失败）；**不要用 argfile 的 `-cp`
  传长路径**（javac 解析异常），改设 `$env:CLASSPATH`。
- 源文件清单 + 选项写入 `build/javac-main.args`（-encoding UTF-8 -proc:none -nowarn
  -Xmaxerrs 3000 -d build\javac-check），执行 `javac.exe @argfile`；javac 输出经 cmd 级
  重定向落盘再按 GBK(936) 读取（PowerShell 管道会二次乱码）。
- 只类型检查 `src/main/java`；`src/api/java` 的第三方 stub 需 gradle 专属 classpath，勿并入。
- 脚手架已按用户要求清理（如需可重新生成：cp-print.gradle 只需 6 行 printMainCp init 脚本）。

---

## 一、已完成事项

### 1. 聚变堆核心实现（全量 gradle 编译通过）
- `BlockFusionReactorCasing`：基础外壳 12 变种；`BlockFusionReactorTieredCasing`：分级一 11 变种；
  `BlockFusionReactorTieredCasing2`：分级二 8 变种（VariantBlock ≤16 变种硬限制拆分）
- `FusionCasingStats`（数值表+三枚举 `of()`）、`FusionCasingGroups`（五类分级 CasingRegistration）
- `FusionReactorLogic`：状态机 OFFLINE→MAGNETIZING→FUEL_INJECTING→RF_HEATING→IGNITED→RUNNING
- `MetaTileEntityFusionReactor`：**21×21×11 v2 纯环形托卡马克**（见 `docs/fusion-reactor.md` 第二节，
  已含 y0..y4 五层按代码转置的施工图与备料数量）
- 注册、双语 lang、blockstate ×3、配方、文档
- **2026-09-05：文档结构图与代码 aisles 程序化核对一致**（19 切片×5 行×19 列 = 1805 格；
  各字母计数 R232/B168/C166/W144/P204/N64/M48/Z4/F4/A9/S1/空气405/x356）

### 2. 既有机器适配（→ 1.9.0 API，16/16 恢复）
- 基类 `MetaTileEntityBaseWithControl` + 6 台工业机 + CombProcess
- 简单档 5 台：LargeAlloySmelter/PlayerBeacon/LargeLightningRod/SolarTower/ConcreteBackfiller×2
- 中等档：EnergyTransTower/DronePad/ExtremeExterminationChamber/CropsSimulateMachine/LargeBeeHive/
  InfiniteFluidDrill(+Logic)/AnnihilationGenerator(+Logic)
- 复杂档：TFFT/YotTank+YotHatch/TeslaTower（mechtech 全套）
- 关键映射与降级说明见上"API 速查/降级记录"；注册与配方均已还原（全量 gradle 编译通过佐证）

### 3. 临时测试版（已终结）
- 16 台机器曾移 `src_broken_backup/`（留存备用，勿删），现已全部恢复于 `src/main`；
  注册字段/注册行/配方引用全部还原；`[TEMP-DISABLED]` 清零；
  `DrTechMetaTileEntities` 两处"临时停用"旧注释已于 09-05 清理。
- lang 核对：机器名、`drtech.multiblock.fusion.*` UI 文案、3 方块全部分级变种
  `tile.*.*.name` 双语键齐全（09-05 复核）

### 4. 构建环境修复（历史）
- EnderIO/Endercore → compileOnly；RFG 1.4.0→1.4.2；23 处 var→显式类型；
  VariantBlock 31 变种超限 → 拆 3 方块。

### 5. 当前游戏运行状态
- ✅ 8/30 曾 runClient 可进游戏（drtech 载入正常）
- ✅ **2026-09-05 全量 gradle `compileJava` + `jar` BUILD SUCCESSFUL**（Jabel 正常），
  产物 `build/libs/drtech-1.8.9-dev.jar`（6.18MB）已含 16 台恢复机器 + 聚变堆全部类与资源
- ⏳ 游戏内逐台/聚变堆实测：待按 `docs/in-game-test-checklist.md` 执行

---

## 二、关键决策及理由

| 决策 | 理由 |
|---|---|
| 适配保留的 1.9.0 jar（用户拍板，不用 (1).jar） | 团队无匹配新版 jar；仓库源码超前于任何本机工件 |
| 外壳拆 3 方块（12+11+8 变种） | `VariantBlock` 硬限制 16 变种，31 变种启动崩溃 |
| 聚变堆 21×21×11 v2 纯环形（甜甜圈形，无方形外壳） | 用户反馈"方形机器不像现实装置" |
| 沙箱用 javac17 全量类型检查替代 gradle 产物编译 | RFG Jabel agent attach 与离线下载在沙箱被禁 |
| 结构图以代码 aisles 为准程序化生成 | 手绘 ASCII 与代码漂移，会造成搭建错误 |

---

## 三、已修改/创建的文件清单（09-05 会话后）

**新建**
- `docs/in-game-test-checklist.md`（游戏内实测清单 + 用户回报格式）
- （09-05 晚已清理 build 脚手架/logs、Gregtech.zip、run 旧日志与崩溃报告）

**重写/修改**
- `docs/fusion-reactor.md`：第二节按代码重绘（y0–y4 五层施工图、备料表、仓口说明）；
  第六/七节更新验证状态与待办
- `DrTechMetaTileEntities.java`：清理两处过期 `[临时测试版]` 注释
- `PROGRESS.md`（本文件）

**此前（仍有效）**：聚变堆 6 文件 + blockstate ×3、16 台机器源码、lang ×2、
配方/注册、`src_broken_backup/`（勿删）

---

## 四、当前阻塞点
1. ~~16 台机器适配~~ **全部完成（16/16）**
2. ~~全量 gradle 编译~~ **已通过（2026-09-05，compileJava+jar）**
3. **聚变堆与 16 台机器的游戏内实测结果待反馈**（新 21×21×11 v2 纯环形结构；旧 7×7×5 / 9×9×5 作废）

---

## 五、下一步计划
1. 游戏内启动：`gradlew.bat runClient`（或把 `build/libs/drtech-1.8.9-dev.jar` 放入 mods）
2. 按 `docs/in-game-test-checklist.md` 逐台验证 16 台机器成型
3. 聚变堆 21×21×11 v2 新结构搭建实测（各状态机阶段 + 输出数值核对 + 燃料耗尽回退）
4. 按实测反馈调整数值/结构（常量集中于 `FusionReactorLogic`）
5. 可选：JEI 结构预览页、TOP 聚变堆信息显示

---

## 六、上下文/环境清理建议
- build 脚手架与 run 旧日志已于 09-05 清理；沙箱类型检查方法见上"受限模式替代法"
- 工作区根 `gregtechfoodoption-gtqt-1.12.2-1.9.0.jar` 与 libs 重复，可删
- `.gradle-home/`（1.4GB 构建缓存）沙箱编译必需，勿删
- `src_broken_backup/` 切勿删除
