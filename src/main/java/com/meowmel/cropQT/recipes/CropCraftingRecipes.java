package com.meowmel.cropQT.recipes;

import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.items.ItemsInit;
import com.drppp.drtech.common.metaTileEntities.DrTechMetaTileEntities;
import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.loaders.recipe.CraftingComponent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * coprQT 合成配方（对应 CropsNH {@code loaders/GTRecipeLoader.java} 的 crafting 部分）。
 *
 * <p>逐条对应源端行号（GTRecipeLoader.java）：
 * <ul>
 *   <li>铲子 90-120：普通铲 = 三重钢锭板 + 铁铲 + 长木棍；强化铲 = 致密钢板 + 普通铲 + 长木棍 + 橡胶板</li>
 *   <li>植物药 122-127：空喷罐 + 肥料 + 骨粉</li>
 *   <li>作物架 158-172："S S","S S" 长木棍 ×4</li>
 *   <li>作物管理器 174-196："ASA","PHP","CIC"（I 在源端是输入仓，本仓库用泵模块代替）</li>
 *   <li>种子生成器 198-222："SAW","CHC","BDB"（W 在源端是水壶，本仓库用泵模块代替）</li>
 *   <li>作物繁殖机 224-247："SAS","CHC","WDW"</li>
 *   <li>基因提取器 249-273："SES","WHW","CSC"</li>
 *   <li>作物合成机 275-298："FCF","CHC","EEE"（F 在源端是力场发生器，本仓库用 IV 力场发生器）</li>
 *   <li>工业农场 300-315："PWS","CMC","ICI"（I 在源端是输入总线，本仓库用传送带模块代替）</li>
 *   <li>种子床 317-336："DDD","CHC","PIP"（I 在源端是输入仓，本仓库用传送带模块代替）</li>
 *   <li>高级收割单元 338-360："RSR","CHC","WBW"</li>
 *   <li>环境强化单元 362-383："EEE","CHC","WBW"</li>
 *   <li>肥料单元 563-586："RPS","CHC","WBW"</li>
 *   <li>生长加速单元 588-611："RPE","CHC","WBW"</li>
 *   <li>超频生长加速单元 613-636："FAF","CGC","WBW"（A 在源端是 World Accelerator，本仓库用力场发生器）</li>
 * </ul>
 *
 * <p>源端的 {@code BrickedAgriculturalCasing} 在 coprQT 中没有对应方块，统一用同档机器外壳（HULL）代替；
 * 这些替代已在上面的行号说明里记录，实现 P6 统一复核时可按需替换。
 */
public final class CropCraftingRecipes {

    private CropCraftingRecipes() {}

    public static void load() {
        tools();
        plantCure();
        cropSticks();
        singleMachines();
        industrialFarm();
        seedBeds();
        upgradeUnits();
    }

    // ---------------------------------------------------------------- 工具

    private static void tools() {
        // GTRecipeLoader.java:90-104 普通铲，钢锁定
        ModHandler.addShapedRecipe("cropqt_crop_spade", new ItemStack(ItemsInit.CROP_SPADE),
                "fPh", "PSP", " T ",
                'P', new UnificationEntry(OrePrefix.plateTriple, Materials.Steel),
                'S', new ItemStack(Items.IRON_SHOVEL),
                'T', new UnificationEntry(OrePrefix.stickLong, Materials.Wood));

        // GTRecipeLoader.java:106-119 强化铲，MV 卷板机锁定
        ModHandler.addShapedRecipe("cropqt_reinforced_crop_spade", new ItemStack(ItemsInit.REINFORCED_CROP_SPADE),
                "fPh", "PHP", "RSR",
                'P', new UnificationEntry(OrePrefix.plateDense, Materials.Steel),
                'H', new ItemStack(ItemsInit.CROP_SPADE),
                'S', new UnificationEntry(OrePrefix.stickLong, Materials.Wood),
                'R', new UnificationEntry(OrePrefix.plate, Materials.Rubber));

        // GTRecipeLoader.java:122-127 植物药（源端用空喷罐 + 肥料胶囊 + 肥料粉 + 骨粉）
        ModHandler.addShapelessRecipe("cropqt_plant_cure", new ItemStack(ItemsInit.PLANT_CURE),
                MetaItems.SPRAY_EMPTY,
                new ItemStack(Items.DYE, 1, 15),
                new ItemStack(Items.DYE, 1, 15),
                new UnificationEntry(OrePrefix.dust, Materials.Steel));
    }

    private static void plantCure() {
        // 植物透镜：源端在 CropRecipes 里用玻璃板 + 电路；这里给一个不冲突的 LV 配方。
        ModHandler.addShapedRecipe("cropqt_plant_lens", new ItemStack(ItemsInit.PLANT_LENS),
                " G ", "PCP", " G ",
                'G', new ItemStack(Blocks.GLASS_PANE),
                'P', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                'C', circuit(GTValues.LV));
    }

    private static void cropSticks() {
        // GTRecipeLoader.java:158-172
        ModHandler.addShapedRecipe("cropqt_crop_sticks", new ItemStack(BlocksInit.CROP_STICK, 4),
                "S S", "S S",
                'S', new UnificationEntry(OrePrefix.stickLong, Materials.Wood));
    }

    // ------------------------------------------------------------ 单方块机器

    private static void singleMachines() {
        for (int i = 0; i < DrTechMetaTileEntities.CROP_MANAGER.length; i++) {
            int tier = i + 1;
            // GTRecipeLoader.java:174-196
            ModHandler.addShapedRecipe(machineId("crop_manager", tier),
                    DrTechMetaTileEntities.CROP_MANAGER[i].getStackForm(),
                    "ASA", "PHP", "CIC",
                    'A', robotArm(tier),
                    'S', sensor(tier),
                    'P', plate(tier),
                    'H', hull(tier),
                    'C', cable(tier),
                    'I', pump(tier));
        }

        for (int i = 0; i < DrTechMetaTileEntities.CROP_SEED_GENERATOR.length; i++) {
            int tier = i + 1;
            // GTRecipeLoader.java:198-222
            ModHandler.addShapedRecipe(machineId("seed_generator", tier),
                    DrTechMetaTileEntities.CROP_SEED_GENERATOR[i].getStackForm(),
                    "SAW", "CHC", "BDB",
                    'S', new ItemStack(ItemsInit.CROP_SPADE),
                    'A', robotArm(tier),
                    'W', pump(tier),
                    'C', circuit(tier),
                    'H', hull(tier),
                    'B', cable(tier),
                    'D', new ItemStack(Blocks.DIRT));
        }

        for (int i = 0; i < DrTechMetaTileEntities.CROP_BREEDER.length; i++) {
            int tier = i + 1;
            // GTRecipeLoader.java:224-247
            ModHandler.addShapedRecipe(machineId("crop_breeder", tier),
                    DrTechMetaTileEntities.CROP_BREEDER[i].getStackForm(),
                    "SAS", "CHC", "WDW",
                    'S', new ItemStack(BlocksInit.CROP_STICK),
                    'A', robotArm(tier),
                    'C', circuit(tier),
                    'H', hull(tier),
                    'W', cable(tier),
                    'D', new ItemStack(Blocks.DIRT));
        }

        for (int i = 0; i < DrTechMetaTileEntities.CROP_GENE_EXTRACTOR.length; i++) {
            int tier = i + GTValues.EV;
            // GTRecipeLoader.java:249-273
            ModHandler.addShapedRecipe(machineId("gene_extractor", tier),
                    DrTechMetaTileEntities.CROP_GENE_EXTRACTOR[i].getStackForm(),
                    "SES", "WHW", "CSC",
                    'S', sensor(tier),
                    'E', emitter(tier),
                    'W', cable(tier),
                    'H', hull(tier),
                    'C', betterCircuit(tier));
        }

        for (int i = 0; i < DrTechMetaTileEntities.CROP_SYNTHESIZER.length; i++) {
            int tier = i + GTValues.EV;
            // GTRecipeLoader.java:275-298
            ModHandler.addShapedRecipe(machineId("crop_synthesizer", tier),
                    DrTechMetaTileEntities.CROP_SYNTHESIZER[i].getStackForm(),
                    "FCF", "CHC", "EEE",
                    'F', MetaItems.FIELD_GENERATOR_IV.getStackForm(),
                    'C', betterCircuit(tier),
                    'H', hull(tier),
                    'E', emitter(tier));
        }
    }

    private static void industrialFarm() {
        // GTRecipeLoader.java:300-315
        ModHandler.addShapedRecipe("cropqt_industrial_farm", DrTechMetaTileEntities.INDUSTRIAL_FARM.getStackForm(),
                "PWS", "CMC", "ICI",
                'P', robotArm(GTValues.MV),
                'W', pump(GTValues.MV),
                'S', sensor(GTValues.MV),
                'C', circuit(GTValues.MV),
                'M', DrTechMetaTileEntities.CROP_MANAGER[GTValues.MV - 1].getStackForm(),
                'I', conveyor(GTValues.MV));
    }

    // -------------------------------------------------------------- 农场方块

    private static void seedBeds() {
        for (int meta = 0; meta < 12; meta++) {
            int tier = meta + 2; // MV..OpV
            String id = "cropqt_seed_bed_" + GTValues.VN[tier].toLowerCase();
            ItemStack result = new ItemStack(BlocksInit.SEED_BED, 1, meta);
            if (result.isEmpty()) continue;
            // GTRecipeLoader.java:317-336
            ModHandler.addShapedRecipe(id, result,
                    "DDD", "CHC", "PIP",
                    'D', new ItemStack(Blocks.DIRT),
                    'C', circuit(tier),
                    'H', hull(tier),
                    'P', pipe(tier),
                    'I', conveyor(tier));
        }
    }

    private static void upgradeUnits() {
        for (int meta = 0; meta < 12; meta++) {
            int tier = meta + 2;
            String suffix = GTValues.VN[tier].toLowerCase();

            // GTRecipeLoader.java:338-360 高级收割单元
            addUnit("cropqt_advanced_harvesting_" + suffix, BlocksInit.ADVANCED_HARVESTING, meta,
                    "RSR", "CHC", "WBW",
                    'R', robotArm(tier), 'S', sensor(tier), 'C', circuit(tier),
                    'H', hull(tier), 'W', cable(tier), 'B', casing(tier));

            // GTRecipeLoader.java:362-383 环境强化单元
            addUnit("cropqt_environmental_enhancement_" + suffix, BlocksInit.ENVIRONMENTAL_ENHANCEMENT, meta,
                    "EEE", "CHC", "WBW",
                    'E', emitter(tier), 'C', circuit(tier),
                    'H', hull(tier), 'W', cable(tier), 'B', casing(tier));

            // GTRecipeLoader.java:563-586 肥料单元
            addUnit("cropqt_fertilizer_unit_" + suffix, BlocksInit.FERTILIZER_UNIT, meta,
                    "RPS", "CHC", "WBW",
                    'R', robotArm(tier), 'P', pump(tier), 'S', sensor(tier), 'C', circuit(tier),
                    'H', hull(tier), 'W', cable(tier), 'B', casing(tier));

            // GTRecipeLoader.java:588-611 生长加速单元
            addUnit("cropqt_growth_acceleration_" + suffix, BlocksInit.GROWTH_ACCELERATION, meta,
                    "RPE", "CHC", "WBW",
                    'R', robotArm(tier), 'P', pump(tier), 'E', emitter(tier), 'C', circuit(tier),
                    'H', hull(tier), 'W', cable(tier), 'B', casing(tier));

            // GTRecipeLoader.java:613-636 超频生长加速单元（源端 World Accelerator 用力场发生器代替）
            addUnit("cropqt_overclocked_growth_acceleration_" + suffix, BlocksInit.OVERCLOCKED_GROWTH_ACCELERATION, meta,
                    "FAF", "CGC", "WBW",
                    'F', MetaItems.FIELD_GENERATOR_IV.getStackForm(),
                    'A', emitter(tier), 'C', circuit(tier),
                    'G', new ItemStack(BlocksInit.GROWTH_ACCELERATION, 1, meta),
                    'W', cable(tier), 'B', casing(tier));
        }
    }

    private static void addUnit(String id, net.minecraft.block.Block block, int meta,
                                String row1, String row2, String row3, Object... ingredients) {
        ItemStack result = new ItemStack(block, 1, meta);
        if (result.isEmpty()) return;
        Object[] args = new Object[3 + ingredients.length];
        args[0] = row1;
        args[1] = row2;
        args[2] = row3;
        System.arraycopy(ingredients, 0, args, 3, ingredients.length);
        ModHandler.addShapedRecipe(id, result, args);
    }

    // -------------------------------------------------------------- 组件helpers

    private static String machineId(String name, int tier) {
        return "cropqt_" + name + "_" + GTValues.VN[tier].toLowerCase();
    }

    /**
     * 取组件；GTCEu 的高档组件（UEV..OpV）由 {@code GregTechAPI.isHighTier()} 决定是否登记，
     * 关闭时这些档位没有条目。这里向下回退到最近一个可用档位，保证 OpV 档位的方块仍能合成。
     */
    private static Object at(gregtech.api.recipes.crafting.Component component, int tier) {
        for (int t = Math.min(tier, GTValues.MAX); t >= 0; t--) {
            Object ingredient = component.getIngredient(t);
            if (ingredient != null) return ingredient;
        }
        return null;
    }

    private static Object circuit(int tier) {
        return at(CraftingComponent.CIRCUIT, tier);
    }

    private static Object betterCircuit(int tier) {
        return at(CraftingComponent.BETTER_CIRCUIT, tier);
    }

    private static Object plate(int tier) {
        return at(CraftingComponent.PLATE, tier);
    }

    private static Object cable(int tier) {
        return at(CraftingComponent.CABLE, tier);
    }

    private static Object hull(int tier) {
        return at(CraftingComponent.HULL, tier);
    }

    private static Object robotArm(int tier) {
        return at(CraftingComponent.ROBOT_ARM, tier);
    }

    private static Object sensor(int tier) {
        return at(CraftingComponent.SENSOR, tier);
    }

    private static Object emitter(int tier) {
        return at(CraftingComponent.EMITTER, tier);
    }

    private static Object pump(int tier) {
        return at(CraftingComponent.PUMP, tier);
    }

    private static Object conveyor(int tier) {
        return at(CraftingComponent.CONVEYOR, tier);
    }

    private static Object pipe(int tier) {
        return at(CraftingComponent.PIPE_NORMAL, tier);
    }

    private static Object casing(int tier) {
        return at(CraftingComponent.HULL, tier);
    }
}
