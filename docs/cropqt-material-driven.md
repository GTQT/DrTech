# CropQT 重构大纲：IMaterialProperty 驱动

> 状态：**设计已定案**（2026-09-11），待实现。
> 前置：M1–M8 已完成。本文档描述**推倒重来**：删掉现有 137 个手写作物。

---

## 1. 一句话

材料只声明**一个 `IMaterialProperty`**（写清种子材质、作物材质、tier、土壤、阶段、渲染图），
**flag 由后台自动补**，系统扫一遍材料表就长出作物。

---

## 2. 种子与产物走两条不同的路

| | 种子 | 产物 |
|---|---|---|
| 是什么 | **我们的单一 NBT 物品** `ItemCropSeed` | **GT 材质物品** |
| 怎么造 | 不变，还是那一个物品 | `OreDictUnifier.get(OrePrefix("<作物材质>"), material)` |
| 图标 | `.seedIcon` 指定的材质模型 | 材质系统自动 |
| 颜色 | 材料的 `getMaterialRGB()` | 材质系统自动 |
| 需要 flag 吗 | **不需要** | **需要**（GT 的 `OrePrefix` 谓词只能看 flag） |
| 需要 OrePrefix 吗 | **不需要** | 需要，每种作物材质一个 |

```
              材料作者只写这一件事
                      │
                      ▼
        material.setProperty(CROP, CropQTProperty.builder()
                .seedIcon(CropQTMaterialIconType.oreberry)   ← 只定 NBT 种子的模型
                .cropIcon(CropQTMaterialIconType.leaf)       ← 定 GT 产物的材质
                .tier(5).soil(SoilTypes.stone).stages(5,5,30).render("ferrofern")
                .build())
                      │
                      ▼  后台（PostMaterialEvent）自动补 flag
        material.addFlags(flagFor(cropIcon))
                      │
                      ├─→ GT 的 OrePrefix 谓词看到 flag → 生成产物物品
                      └─→ CropMaterialScanner 看到 property → 生成 CropType
```

**为什么不会爆量**：准入门槛是 `CropQTProperty` 这个显式属性，
没声明的材料一律不进 —— 跟 `PropertyKey.DUST` 一样，是「主动选择加入」。

---

## 3. 三个组成部分

### 3.1 `CropQTProperty`（材料作者唯一要写的东西）

```java
public class CropQTProperty implements IMaterialProperty {

    private final MaterialIconType seedIcon;   // 种子材质，8 选 1 —— 只给 NBT 种子物品选模型
    private final MaterialIconType cropIcon;   // 作物材质，10 选 1 —— 给 GT 产物物品
    private final int tier;                    // 作物 tier
    private final ISoilList soil;              // 土壤组
    private final int maxGrowthStage;          // 生长阶段总数
    private final int harvestStage;            // 可收获的阶段
    private final int stageRequirement;        // 每阶段需要的进度
    private final String renderTexture;        // textures/blocks/crop/<这个>/ 目录名

    @Override
    public void verifyProperty(MaterialProperties properties) {
        // 作物要有粉尘形态，不然产物没地方挂
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
```

配套 `PropertyKey<CropQTProperty> CROP = new PropertyKey<>("crop", CropQTProperty.class);`
（`PropertyKey` 构造函数是 public，别的模组可以直接 new ✓）

### 3.2 种子材质（8 种，资源已有）

| 材质类型名 | 资源 |
|---|---|
| `bonsai` / `botania` / `flower` / `grain` / `magic` / `oreberry` / `spore` / `vanilla` | `material_sets/dull/<名>.json` + `<名>{1,2}.png` |

用途：`ItemCropSeed` 的 `SeedMeshDefinition` 读作物 → 读 property 的 `seedIcon`
→ 返回 `gregtech:material_sets/dull/<形状>` 模型；`SeedColorHandler` 返回材料的 RGB。
**可以直接退役 `crop_seed_<形状>.json` 那 8 个中间模型。**

### 3.3 作物材质（10 种，需新建）

| 材质类型名 | 归入的现有产物图（`textures/items/metaitems/crops/`） |
|---|---|
| `leaf` | `*_leaf` 12 张 + `pyrolusium_leaf.*` 7 张彩蛋 |
| `flower` | `*_flower` 7 张 + `hops` |
| `berry` | `*_berry` 4 张 |
| `stem` | `hemp_stem`、`reactoria_stem` |
| `wart` | `milk_wart`、`star_wart` |
| `fiber` | `coppon_fiber` |
| `twig` | `tine_twig` |
| `root` | `salty_root` |
| `blossom` | `indigo_blossom` |
| `essence` | `magic_essence` |

> 40 张图刚好分完。⚠️ 与种子组的 `flower` 撞名，见 §6-2（会崩游戏）。

---

## 4. 生成出来的东西长什么样

以 **Iron** 声明 `seedIcon(oreberry)` + `cropIcon(leaf)` + 参数 为例：

| 项 | 从哪来 | 结果 |
|---|---|---|
| 作物 id | `材料名 + "_" + 作物材质` | `iron_leaf` |
| 显示名 | lang `cropqt.crop.iron_leaf.name` | 「铁叶草」（我们写） |
| 种子 | `ItemCropSeed`（NBT 记作物 id） | 图标 = `material_sets/dull/oreberry`，染成铁色 |
| 产物 | `OreDictUnifier.get(OrePrefix("leaf"), Iron)` | GT 材质物品，图标同样染成铁色 |
| tier / 土壤 / 阶段 / 渲染图 | `CropQTProperty` | 声明什么就是什么 |
| 底土要求 | 按材料推 | `SubSoilRequirements.iron` |

**核心收益**：种子和产物的图标都是**灰度底图 × 材料色**自动染的 ——
「每材料一张专属图」变成「每类型一张灰度图」，40 张压到 10 类而不丢辨识度。
而且种子叫「铁种子」、图标是铁色的种子形状，「名字对得上外型」自然成立。

---

## 5. 材料注册时序（**决定代码挂在哪**）

`GregTech CoreModule` 的实际顺序：

```
MaterialRegistryEvent                建注册表
  unfreezeRegistries()               Phase = OPEN
  Materials.register()               GTCEu 自带材料
  post(MaterialEvent)                ★ 所有模组的材料在这里注册
  closeRegistries()                  Phase = CLOSED（只能改，不能新增材料）
  post(PostMaterialEvent)            ★★ 全表已齐 —— 遍历 + 补 flag 的唯一时机
  Materials.postMaterialLoad()
  freezeRegistries()                 Phase = FROZEN（之后 addFlags 直接抛异常）
  OreDictUnifier.init()              ★ 按 flag 生成产物物品
  MetaBlocks.init()
```

三条结论：

1. **补 flag 的唯一时机是 `PostMaterialEvent`**。晚了 `Material.addFlags` 抛
   `IllegalStateException("Cannot add flag to material when registry is frozen!")`。
2. **`OreDictUnifier.init()` 在冻结之后**，所以 PostMaterialEvent 里补的 flag
   能被矿物词典生成看到 → 产物物品正常产出 ✓
3. 现有的 `CropQTOrePrefix.init()`（`MetaItems.addOrePrefix`）**要留在 `MaterialEvent`**，不能挪。

→ `DrtechEventHandler` 新增 `registerPostMaterials(PostMaterialEvent)`。

---

## 6. 三个必须先处理的坑

### 6-1 `grain` 贴图名不一致（已确认的 bug）

图标类型叫 `grain`（单数），`grain.json` 引用 `grain1/grain2`，
但实际文件叫 **`grains1.png` / `grains2.png`（复数）**。其他 7 个形状都是单数且一致。
→ 只有这一个形状会渲染成缺失贴图。改文件名即可。

### 6-2 两组都有 `flower` → **崩游戏**

```java
// MaterialIconType 构造函数里的硬校验
Preconditions.checkArgument(!ICON_TYPES.containsKey(this.name),
        "MaterialIconType " + this.name + " already registered!");
```

种子组一个 `flower`、作物组又一个 `flower` → 第二次注册抛异常。
两组都是静态字段，**类初始化就炸，服务器起不来**。

**好消息：只有 `flower` 一个撞名。** 两组对照：

```
种子组  bonsai  botania  flower  grain  magic  oreberry  spore  vanilla
作物组  leaf    flower   berry   stem   wart   fiber     twig   root  blossom  essence
                ^^^^^^ 只有这一个重
```

所以**只需给一个名字加前缀**，不是整组：

- **改种子组的** → `seed_flower`（改 1 个类型名 + 它的 json/贴图，`flower1/2.png` 也要跟着改，但**作物组也要用这批图**，所以贴图得留两份或让两边共用一个）
- **改作物组的** → `crop_flower`（同理）

> 更省事的办法：**两组共用同一个 `flower` 类型**。种子的 shape 和作物的产物 shape
> 本来就可以长得一样，共用一套资源没有任何问题，还省一张图。推荐这个。

### 6-3 `CropQTOrePrefix` 只留作物那 10 个

现在 8 个前缀跟**种子**形状同名，而种子不走材质系统了 → **这 8 个删掉**，
换成 10 个作物前缀（`leaf` / `flower` / `berry` / …），
谓词 `mat -> mat.hasFlag(GENERATE_LEAF_X)`，产物 = `OreDictUnifier.get(该前缀, 材料)`。

> ⚠️ 这样"双 FLAG"实际退化成**单组 flag（作物外型 10 个）**——
> 种子那组 flag 随着「种子不走材质系统」一起失去用途。见 §7 决策 5。

---

## 7. 已定的决策（2026-09-11）

| # | 决策 |
|---|---|
| 1 | 作物材质**按后缀归类**（10 类），不做每材料一张 |
| 2 | 作物 id = **材料名 + 后缀**（`iron_leaf`） |
| 3 | 原版作物**留一份小的手写表** |
| 4 | 种子物品**只改显示名**（「种子袋」→「种子」），注册名不动 |
| 5 | flag **由后台根据 property 自动补** |
| 6 | 准入 = `CropQTProperty`；tier/土壤/阶段数/渲染图都写在 property 里 |
| 7 | **种子不走 GT 材料系统** —— `.seedIcon` 只负责给 NBT 种子物品指定材质 |

> 决策 5 原本写的是「双 FLAG」，但决策 7 定了种子不走材质系统之后，
> **种子那组 flag 没有用途了，实际只需要作物外型这一组（10 个）**。
> 若你要保留「双 FLAG」的字面结构，可以留一组空 flag 作占位，但没有功能。

---

## 8. 分步计划

| 步骤 | 内容 |
|---|---|
| **M9-1** | 定 10 个作物 `MaterialIconType`；从 40 张图里每类挑代表，**转灰度**、按明暗拆成 `<类型>1.png`/`<类型>2.png`，落到 `assets/gregtech/textures/items/material_sets/dull/`；补 10 个 `<类型>.json` |
| **M9-2** | 修既有不一致：`grains1/2.png` → `grain1/2.png`（§6-1）；种子材质类型名加前缀防撞（§6-2） |
| **M9-3** | `CropQTOrePrefix` 删掉 8 个种子前缀，换成 10 个作物前缀（§6-3） |
| **M9-4** | 新增 `CropQTProperty` + `PropertyKey.CROP` + Builder |
| **M9-5** | `DrtechEventHandler` 加 `PostMaterialEvent` 钩子：遍历材料 → 有 `CROP` 属性就补作物 flag |
| **M9-6** | 写 `CropMaterialScanner`：扫全表 → 有 `CROP` 属性 → 生成并注册 `CropType` |
| **M9-7** | `SeedMeshDefinition` 改读 property 的 `seedIcon`（指向材质模型）；`SeedColorHandler` 改用材料 RGB；退役 `crop_seed_<形状>.json` |
| **M9-8** | 退役 `MetaCrops` 的 40 个 metaitem（产物改由 `OreDictUnifier` 生成） |
| **M9-9** | **删掉现有 137 个作物**；留一份小的手写原版作物表（小麦/甘蔗/蒲公英…十几个） |
| **M9-10** | 验证：作物数量、图标染色、掉落、JEI、工业农场、TESR 渲染 |

---

## 9. 保留不动的部分

M1–M8 的成果继续用，只是数据源从手写表换成生成器：

- 土壤 / 底土体系（`SoilRegistry` / `SubSoilRequirement`）
- 水肥体系（`HydrationRegistry` / `FertilizerRegistry`）
- 确定性杂交（`MutationRegistry` / `MutationPool`）
- 5 台机器 + 工业农场
- `CropType` / `CropStats` / `TileCropStick` / `CropStickTESR` 的运行时逻辑

---

## 10. 实现记录（2026-09-11 完成）

### 落地情况

| 步骤 | 结果 |
|---|---|
| M9-1 作物材质资源 | ✅ 9 类 × 2 层 = 18 张灰度图（从 40 张产物图里每类挑一张代表转换）；10 个模型（`flower` 与种子共用） |
| M9-2 修 `grain` 贴图名 | ✅ `grains{1,2}.png` → `grain{1,2}.png` |
| M9-3 前缀与 flag | ✅ 由 `CropMaterialType` 枚举统一持有，删掉 `CropQTFlags` / `CropQTOrePrefix` |
| M9-4 `CropProperty` | ✅ 新增，含 tier / 土壤 / 阶段 / 渲染图 / 种子材质 / 显式底土 |
| M9-5 补 flag | ✅ `PostMaterialEvent` 里 `CropMaterialScanner.applyFlags()` |
| M9-6 生成作物 | ✅ 20 种材料 → 20 株作物，跳过 0 |
| M9-7 种子 | ✅ `SeedMeshDefinition` 走 `seedIcon`；显示名「种子袋」→「种子」；删除 8 个中间模型 |
| M9-8 退役产物 | ✅ `MetaCrops` 删除；40 个产物 metaitem 与模型删除（**原图 40 张保留**，见下） |
| M9-9 删旧作物 | ✅ 137 株 → 20 株生成 + 18 株手写原版表 |
| M9-10 验证 | ✅ 服务端 `Done (7.567s)`，无 cropqt 报错 |

### 实现中的偏差与发现

| # | 事项 | 说明 |
|---|---|---|
| 1 | **扫描必须分两趟** | `OreDictUnifier.get(前缀, 材料)` 读的是 `OreDictUnifier.init()` 建的索引，而那个在 **`freezeRegistries()` 之后**才跑。所以 `PostMaterialEvent` 只能补 flag（晚了会抛「registry is frozen」），建作物必须推到 FML init。第一版一趟做完，20 株全被跳过（产物物品取不出来） |
| 2 | `essence` 名字被占 | GT 已声明 `MaterialIconType.essence`（无人使用、无贴图），所以我们这类的叫 `magic_essence` |
| 3 | `flower` 两组共用 | 种子组和作物组都要一个 `flower`，而 `MaterialIconType` 重名会抛异常。共用同一个类型对象，零改名零新增资源 |
| 4 | **生长渲染图必须显式指定** | TESR 按作物 id 找 `textures/blocks/crop/<id>/`。新 id 是 `iron_leaf`，目录却叫 `ferrofern`（源端命名）→ 20 株全渲染成缺失贴图。已在声明表里逐条补上目录名 |
| 5 | `CrossBreedingRegistry` 删除 | 117 条杂交配方全部引用已删的作物 id，留着只会让人以为能用 |
| 6 | `MutationPools` 重写 | 成员换成新 id；JEI 页会跳过认不出的成员，所以看到的就是实际有效的 |

### 已知的遗留

| 项 | 说明 |
|---|---|
| **只声明了 20 种材料** | 这是初始清单，不是全部。要加作物就在 `CropMaterials.declare()` 里加一行 —— 参数格式照抄现有条目 |
| **40 张源图保留** | `assets/drtech/textures/items/metaitems/crops/*.png` 已无代码引用，但它们是我生成灰度材质时的**唯一彩色原图**，删了就没法再生成。要清掉请说一声 |
| **孤儿物品** | `ItemXpBerry` / `ItemSoarXpBerry` 原来是 `xp_berry` 作物的产物，那株作物已删，现在没有获取途径 |
| **未在游戏里看过** | 种子图标染色、作物材质染色、JEI 三个页签、TESR 渲染 —— 都只验证了「不报错」，没验证「好看」 |

### 上线后修的第一批问题（2026-09-12）

客户端实测暴露出来的，日志逐条对得上：

| # | 问题 | 根因 | 修法 |
|---|---|---|---|
| 1 | **20 株材料作物的种子形状全一样** | `CropMaterials` 里写了 `DEFAULT_SEED = oreberry` 给所有声明共用 | 形状改由**外型**决定（`CropMaterialType.getDefaultSeedIcon()`）：叶/根→`vanilla`、浆果→`oreberry`、花→`flower`、秆与纤维→`grain`、疣与菌→`spore`、枝→`bonsai`、花苞→`botania`、精华→`magic`。八种正好分完。材料仍可在属性里覆盖 |
| 2 | **18 株原版作物还是种子袋** | 当初按「行为不变」处理，没给 `seedIcon` | 逐株补上形状 + 颜色（材质底图是灰度的，不染色出来是灰的） |
| 3 | **`uranium_238_leaf` 显示成生 key** | GT 的材料名是 `uranium_238`（带下划线），我 lang 里写的是 `uranium238_leaf` | 改 lang key。**教训**：作物 id 由 `material.getName()` 拼出来，别凭材料常量名猜 |
| 4 | **`environmental_module` 是紫黑块** | M7 漏了 item 模型注册 | 补一张灰度电路卡 + 模型 + 按 meta 染色的颜色处理器（29 个 meta 共用一张图） |

> 诊断方式：往 `SeedMeshDefinition` 里塞了条临时日志，把 `cropId → 查表结果 → 用的模型`
> 打出来。一次客户端启动就定位完了 —— 比在 Forge 的模型加载链里刨源码快得多。
> 日志已撤。

**顺带确认的一个隐患**（未修，不影响当前功能）：作物注册（FML init）比模型注册
（`ModelRegistryEvent`）晚约 19 秒，所以烘焙期 `CropRegistry` 是空的。
种子模型是运行期动态查的所以没事，但「烘焙期看到的世界和运行期不一样」这类问题
迟早会咬人，值得单独理一次。
