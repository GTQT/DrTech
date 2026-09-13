package com.meowmel.cropQT.api.recipes;

import com.cleanroommc.modularui.widgets.ProgressWidget.Direction;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

/**
 * 四台「有进有出」的作物机器的 RecipeMap。
 *
 * <p><b>作物管理器不在这里</b> —— 它不是配方机器（没有输入槽、没有配方表），
 * 硬塞一张空表只为了蹭自动界面是拿机器的形状迁就框架。它照
 * {@code MetaTileEntityUniversalCollector} 自包含，界面手绘。
 *
 * <h2>这些表是空的，而且不会往里面加配方</h2>
 * 四台机器的「配方」是运行时动态算的（育种查变异表、合成读基因球 NBT），
 * 塞不进 RecipeMap 的配料树。干活逻辑在各自的 {@code XxxRecipeLogic} 里，
 * 走 {@code shouldSearchForRecipes()} → {@code updateRecipeProgress()} 这条自定义通道，
 * 永不查表。
 *
 * <h2>那为什么还要建它们</h2>
 * 因为 {@code SimpleMachineMetaTileEntity} 的整套界面（槽位布局、进度条、
 * 右边那列自动输出/充电槽/设置按钮）都要从 {@code RecipeMap.getRecipeMapUI()} 拿。
 * 有了表，界面白拿，不用手写。
 *
 * <p>空表<b>不会在 JEI 留空页面</b> —— JEI 遍历的是 {@code getRecipesByCategory()}，
 * 而那个 map 只在真正 {@code addRecipe} 时才填。所以这几张表在 JEI 里完全隐形，
 * 也不需要写 {@code gtrecipe.category.*} 的 lang。
 *
 * <h2>声明的数量只影响一件事</h2>
 * 面板高度。{@code RecipeMapUI.PanelBuilder} 用 {@code maxInputs} 等判断
 * 「要不要多留 9px」。真正的槽位/罐数由各机器覆写的 {@code createXxxHandler} 决定
 * —— {@code setInputs(...)} 只读 handler 的 {@code getSlots()}/{@code getTanks()}。
 *
 * <p>{@link RecipeMap} 构造即注册，不需要额外登记。
 */
public final class CropRecipeMaps {

    /** 种子生成器：1 进 1 出 + 1 个肥料罐。 */
    public static final RecipeMap<SimpleRecipeBuilder> SEED_GENERATOR = build(
            "crop_seed_generator", 1, 1, 1, 0);

    /** 育种机：最多 6 个亲本槽（低档只用前 3 个）+ 1 个输出 + 1 个液肥罐。 */
    public static final RecipeMap<SimpleRecipeBuilder> CROP_BREEDER = build(
            "crop_breeder", 6, 1, 1, 0);

    /** 基因提取机：1 进 1 出，没有罐。 */
    public static final RecipeMap<SimpleRecipeBuilder> GENE_EXTRACTOR = build(
            "gene_extractor", 1, 1, 0, 0);

    /** 作物合成器：4 个基因球槽 + 1 个输出 + 1 个 UUM 罐。 */
    public static final RecipeMap<SimpleRecipeBuilder> CROP_SYNTHESIZER = build(
            "crop_synthesizer", 4, 1, 1, 0);

    private CropRecipeMaps() {}

    /**
     * 造一张空的机器表。
     *
     * <p>必须走 {@code uiBuilder(...)} —— 它才会把 {@code usesMui2} 置真。
     * 只调 {@code usesMui2()} 不够：{@code RecipeMapBuilder} 那边还要求
     * {@code mapUIBuilder} 非空，否则会悄悄退回 MUI1 的老界面。
     */
    private static RecipeMap<SimpleRecipeBuilder> build(String name,
                                                        int itemIn, int itemOut,
                                                        int fluidIn, int fluidOut) {
        return new RecipeMapBuilder<>(name, new SimpleRecipeBuilder())
                .itemInputs(itemIn)
                .itemOutputs(itemOut)
                .fluidInputs(fluidIn)
                .fluidOutputs(fluidOut)
                .uiBuilder(b -> b.progressBar(GTGuiTextures.PROGRESS_BAR_ARROW, Direction.RIGHT))
                .build();
    }
}
