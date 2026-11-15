package com.meowmel.cropQT.recipes;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.drppp.drtech.common.metaTileEntities.DrTechMetaTileEntities;
import com.meowmel.cropQT.api.CropGeneOrb;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.MetaItems;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 四台单方块机器的「伪配方」装载（对应 CropsNH 的
 * {@code SeedGeneratorFakeRecipeLoader} / {@code CropBreederFakeRecipeLoader} /
 * {@code CropGeneExtractorFakeRecipeLoader} / {@code CropSynthesizerFakeRecipeLoader}）。
 *
 * <p>这些配方只用于 JEI 展示。四台机器的 {@code XxxRecipeLogic} 覆写了
 * {@code shouldSearchForRecipes()} 并恒返回 {@code false}，运行时永远不会查这些表，
 * 所以功能与配方展示互不影响。
 *
 * <p>种子统一用 {@code CropStats(1,1,1,true)}（已分析、三项属性均为 1），
 * 与源端 {@code DEFAULT_ANALYZED} 等价。所有配方都带完整 NBT，JEI 里能显示具体属性。
 */
public final class CropFakeRecipes {

    private CropFakeRecipes() {}

    /** 源端的 DEFAULT_ANALYZED：已分析、growth/gain/resistance = 1。 */
    private static final CropStats DEFAULT_STATS = new CropStats(1, 1, 1, true);

    public static void load() {
        CropRegistry.ensureRegistered();

        seedGenerator();
        breeder();
        geneExtractor();
        synthesizer();

        // JEI 分类图标用机器本体
        CropRecipeMaps.SEED_GENERATOR.getPrimaryRecipeCategory()
                .jeiIcon(DrTechMetaTileEntities.CROP_SEED_GENERATOR[0].getStackForm());
        CropRecipeMaps.CROP_BREEDER.getPrimaryRecipeCategory()
                .jeiIcon(DrTechMetaTileEntities.CROP_BREEDER[0].getStackForm());
        CropRecipeMaps.GENE_EXTRACTOR.getPrimaryRecipeCategory()
                .jeiIcon(DrTechMetaTileEntities.CROP_GENE_EXTRACTOR[0].getStackForm());
        CropRecipeMaps.CROP_SYNTHESIZER.getPrimaryRecipeCategory()
                .jeiIcon(DrTechMetaTileEntities.CROP_SYNTHESIZER[0].getStackForm());
    }

    /**
     * 种子生成器：已分析种子 + 浓缩液肥 → 1 份同类种子（源端 stackSize=1）。
     *
     * <p>源端 {@code SeedGeneratorFakeRecipeLoader}：duration 160、EUT 30、
     * 液肥 {@code max(1,(gr+ga+re)*36)}，并按 {@code getDuplicationCatalysts()} 分出催化剂配方。
     */
    private static void seedGenerator() {
        for (CropType crop : CropRegistry.getAll().values()) {
            if (crop.isHiddenFromNEI()) continue;
            if (crop.getCrossingThreshold() < 0) continue; // 只能合成，不能增殖

            ItemStack seed = seed(crop.getId());
            ItemStack result = seed.copy();
            int fertilizer = Math.max(1, 3 * 36); // (1+1+1)*36，源端 STATS_FERTILIZER_PER_STAT

            CropRecipeMaps.SEED_GENERATOR.recipeBuilder()
                    .EUt(30)
                    .duration(160)
                    .inputs(seed)
                    .fluidInputs(DrtechMaterials.EnrichedFertilizer.getFluid(fertilizer))
                    .outputs(result)
                    .buildAndRegister();

            for (ItemStack catalyst : crop.getDuplicationCatalysts()) {
                if (catalyst.isEmpty()) continue;
                CropRecipeMaps.SEED_GENERATOR.recipeBuilder()
                        .EUt(30)
                        .duration(160)
                        .inputs(seed(crop.getId()), catalyst)
                        .fluidInputs(DrtechMaterials.EnrichedFertilizer.getFluid(fertilizer))
                        .outputs(result.copy())
                        .buildAndRegister();
            }
        }
    }

    /**
     * 育种机：亲本种子（2~4）+ 浓缩液肥 → 子代种子。
     *
     * <p>源端 {@code MTECropBreeder} 的 EU/时长来自 mutation，液肥
     * {@code max(1,tier*144)+max(1,(gr+ga+re)*72)}。
     */
    private static void breeder() {
        for (CropMutation mutation : MutationRegistry.getAllMutations()) {
            CropType resultCrop = CropRegistry.get(mutation.getResult());
            if (resultCrop == null) continue;

            List<ItemStack> parents = new ArrayList<>();
            boolean valid = true;
            for (String parentId : mutation.getParents()) {
                if (!CropRegistry.exists(parentId)) {
                    valid = false;
                    break;
                }
                parents.add(seed(parentId));
            }
            if (!valid || parents.size() < CropMutation.MIN_PARENTS) continue;

            int tier = resultCrop.getTier();
            int fertilizer = Math.max(1, tier * 144) + Math.max(1, 3 * 72);

            CropRecipeMaps.CROP_BREEDER.recipeBuilder()
                    .EUt(mutation.getBreedingMachineRecipeEUt())
                    .duration(mutation.getBreedingMachineRecipeDuration())
                    .inputs(parents.toArray(new ItemStack[0]))
                    .fluidInputs(DrtechMaterials.EnrichedFertilizer.getFluid(fertilizer))
                    .outputs(seed(mutation.getResult()))
                    .buildAndRegister();
        }
    }

    /**
     * 基因提取机：已分析种子 + 电路 1..4 → 基因球（消耗种子与空数据球）。
     *
     * <p>源端 circuit 1=物种、2=growth、3=gain、4=resistance；
     * 时长 6000（物种）/3000（属性），EU 取作物机器档位。
     */
    private static void geneExtractor() {
        for (CropType crop : CropRegistry.getAll().values()) {
            if (crop.isHiddenFromNEI()) continue;

            int eut = (int) com.meowmel.cropQT.api.VoltageMap.eu(
                    com.meowmel.cropQT.api.VoltageMap.clamp(crop.getMachineBreedingRecipeTier(),
                            com.meowmel.cropQT.api.VoltageMap.GENE_MACHINE_MIN_TIER,
                            com.meowmel.cropQT.api.VoltageMap.GENE_MACHINE_MAX_TIER));

            // 电路 1：物种
            CropRecipeMaps.GENE_EXTRACTOR.recipeBuilder()
                    .EUt(eut)
                    .duration(6000)
                    .inputs(seed(crop.getId()), MetaItems.TOOL_DATA_ORB.getStackForm())
                    .circuitMeta(1)
                    .outputs(CropGeneOrb.create(CropGeneOrb.Kind.SPECIES, crop.getId()))
                    .buildAndRegister();

            // 电路 2/3/4：growth/gain/resistance
            addStatOrbRecipe(crop, 2, CropGeneOrb.Kind.GROWTH, eut);
            addStatOrbRecipe(crop, 3, CropGeneOrb.Kind.GAIN, eut);
            addStatOrbRecipe(crop, 4, CropGeneOrb.Kind.RESISTANCE, eut);
        }
    }

    private static void addStatOrbRecipe(CropType crop, int circuit, CropGeneOrb.Kind kind, int eut) {
        CropRecipeMaps.GENE_EXTRACTOR.recipeBuilder()
                .EUt(eut)
                .duration(3000)
                .inputs(seed(crop.getId()), MetaItems.TOOL_DATA_ORB.getStackForm())
                .circuitMeta(circuit)
                .outputs(CropGeneOrb.createStat(kind, 1))
                .buildAndRegister();
    }

    /**
     * 作物合成机：四颗基因球 + UUM → 已分析种子。
     *
     * <p>源端 UUM = {@code cc.tier*750 + (gr+ga+re)*100}，
     * 时长 {@code 6000 + 6000*ln(tier)/ln(16)}，3A。
     */
    private static void synthesizer() {
        for (CropType crop : CropRegistry.getAll().values()) {
            if (crop.isHiddenFromNEI()) continue;
            if (crop.getCrossingThreshold() < 0) continue;

            int tier = Math.max(1, crop.getTier());
            int uum = tier * 750 + 3 * 100;
            int duration = (int) Math.round(6000 + 6000 * Math.log(tier) / Math.log(16));
            int eut = (int) com.meowmel.cropQT.api.VoltageMap.eu(
                    Math.min(com.meowmel.cropQT.api.VoltageMap.GENE_MACHINE_MAX_TIER,
                            Math.max(com.meowmel.cropQT.api.VoltageMap.GENE_MACHINE_MIN_TIER, tier))) * 3;

            CropRecipeMaps.CROP_SYNTHESIZER.recipeBuilder()
                    .EUt(eut)
                    .duration(duration)
                    .inputs(
                            CropGeneOrb.create(CropGeneOrb.Kind.SPECIES, crop.getId()),
                            CropGeneOrb.createStat(CropGeneOrb.Kind.GROWTH, 1),
                            CropGeneOrb.createStat(CropGeneOrb.Kind.GAIN, 1),
                            CropGeneOrb.createStat(CropGeneOrb.Kind.RESISTANCE, 1))
                    .fluidInputs(Materials.UUMatter.getFluid(uum))
                    .outputs(seed(crop.getId()))
                    .buildAndRegister();
        }
    }

    private static ItemStack seed(String cropId) {
        return GTUtility.copy(1, ItemCropSeed.createSeedBag(cropId, DEFAULT_STATS));
    }
}
