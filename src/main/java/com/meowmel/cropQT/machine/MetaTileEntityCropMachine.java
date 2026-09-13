package com.meowmel.cropQT.machine;

import com.meowmel.cropQT.api.capability.impl.CropMachineRecipeLogic;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;

/**
 * 作物系统 5 台机器的公共基类。
 *
 * <h2>为什么继承 {@code SimpleMachineMetaTileEntity}</h2>
 * 它的界面是照 RecipeMap 自动规划的（中间「输入 | 进度 | 输出」，右边一列
 * 自动输出开关 / 充电槽 / 设备设置 / 徽标），并且每 5 tick 自动把产物推到
 * 输出面外的容器里。手写这些既难看又容易漏。代价是得给每台配一张
 * {@link RecipeMap} —— 但表是空的无所谓，见 {@code CropRecipeMaps}。
 *
 * <h2>干活逻辑在哪</h2>
 * 在各自的 {@link CropMachineRecipeLogic} 里（继承 {@code RecipeLogicEnergy}），
 * 由 {@link #createLogic(RecipeMap)} 造出来、交给基类当 workable 存着并自动
 * 注册成 MTE trait。所以 {@code CAPABILITY_WORKABLE}、进度同步、存档、电源开关
 * 全都不用我们操心。
 *
 * <p>槽位 / 罐的布局各机器不同，由子类覆写 {@code createXxxHandler}。
 * 界面上装不下的额外控件（管理器的收获/浇水/施肥、提取机的模式）走
 * <b>潜行 + 螺丝刀</b>，见那两台的 {@code onScrewdriverClick}。
 */
public abstract class MetaTileEntityCropMachine extends SimpleMachineMetaTileEntity {

    protected MetaTileEntityCropMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                        ICubeRenderer renderer, int tier) {
        // hasFrontFacing = true：这几台都有正面（贴运行态贴图用）
        // tankScalingFunction 用不上——罐的容量各机器在 createImportFluidHandler 里自己定
        super(metaTileEntityId, recipeMap, renderer, tier, true, GTUtility.defaultTankSizeFunction);
    }

    /**
     * 造本机的干活逻辑。
     *
     * <p>由基类构造器调用，那时子类的字段还没赋值 —— 所以实现里只能传
     * {@code () -> energyContainer} 这种延迟求值的东西，不能读子类自己的字段。
     */
    protected abstract CropMachineRecipeLogic<?> createLogic(RecipeMap<?> recipeMap);

    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return createLogic(recipeMap);
    }

    /** 作物机器泡水不炸——它们本来就是温室里的东西。 */
    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
