package com.meowmel.cropQT.integration;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.api.recipes.machines.RecipeMapScanner;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 让 GT 的<b>扫描仪</b>能分析种子。
 *
 * <h2>为什么不用 mixin</h2>
 * {@code RecipeMapScanner} 本身就给外挂留了口子：
 * <pre>{@code
 * public static void registerCustomScannerLogic(ICustomScannerLogic logic)
 * }</pre>
 * 它的 {@code findRecipe} 在常规查表失败后会依次问每个注册的逻辑，拿到配方就用。
 * GT 自己就有一个一模一样的先例 —— {@code ForestryScannerLogic}（把蜜蜂/树苗/蝴蝶
 * 扫成带 NBT 的已分析个体）。我们做的事和它完全同类：同一袋种子进，
 * {@code stats.analyze()} 过的同一袋出。
 *
 * <p>mixin 进别人的类属于最后手段：上游改一个字段名就炸，而且跟别的 mixin 撞车时
 * 极难查。有正式接口就绝不走那条路。
 *
 * @see RecipeMapScanner#registerCustomScannerLogic
 */
public class CropScannerLogic implements IScannerRecipeMap.ICustomScannerLogic {

    /** 扫一袋种子要多少 EU/t。与手持分析仪单次耗电一致（HV 档）。 */
    private static final int EUT = GTValues.VA[GTValues.HV];
    /** 扫一袋种子要多少 tick。 */
    private static final int DURATION = 200;

    @Override
    @Nullable
    public Recipe createCustomRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                     boolean exactVoltage) {
        // 扫描仪的输入槽只有一个，但接口给的是列表，按列表处理
        for (ItemStack stack : inputs) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemCropSeed)) {
                continue;
            }
            // 已经分析过的就别再扫一遍了 —— 否则玩家会白烧一份电
            CropStats stats = ItemCropSeed.getCropStats(stack);
            if (stats.isAnalyzed()) {
                continue;
            }
            String cropId = ItemCropSeed.getCropId(stack);
            if (cropId.isEmpty() || !CropRegistry.exists(cropId)) {
                continue;
            }

            // getCropStats 每次都是新读出来的对象，就地 analyze 不会影响物品本身
            ItemStack output = ItemCropSeed.createSeedBag(cropId, stats.analyze());
            return RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                    .inputs(stack.copy())
                    .outputs(output)
                    .duration(DURATION).EUt(EUT)
                    .build().getResult();
        }
        return null;
    }

    /**
     * 给 JEI 看的示例。
     *
     * <p>这条配方不在表里（是 {@link #createCustomRecipe} 现造的），所以要手工摆一页，
     * 否则玩家在 JEI 里根本不知道扫描仪能用在这上面。
     */
    @Override
    @Nullable
    public List<Recipe> getRepresentativeRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        addExample(recipes, "wheat");
        addExample(recipes, "iron_leaf");
        return recipes;
    }

    private static void addExample(List<Recipe> recipes, String cropId) {
        CropType crop = CropRegistry.get(cropId);
        if (crop == null) {
            return;
        }
        ItemStack input = ItemCropSeed.createSeedBag(cropId);                 // 三围 1/1/1，未分析
        ItemStack output = ItemCropSeed.createSeedBag(cropId, new CropStats(1, 1, 1).analyze());
        recipes.add(RecipeMaps.SCANNER_RECIPES.recipeBuilder()
                .inputs(input)
                .outputs(output)
                .duration(DURATION).EUt(EUT)
                .build().getResult());
    }
}
