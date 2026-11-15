package com.meowmel.cropQT.machine;

import com.meowmel.cropQT.api.CropTooltips;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.CropGeneOrb;
import com.meowmel.cropQT.api.CropMachineTankCapacity;
import com.meowmel.cropQT.api.capability.impl.CropSynthesizerRecipeLogic;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 作物合成器：4 个基因球 + UUM → 一颗完整的种子。
 *
 * <p>和提取器配对成闭环：提取器把种子拆成 4 个球，合成器再拼回去。
 *
 * <p>这里是机器的「壳」—— 槽位、罐、tooltip。界面由 CEU 照
 * {@link CropRecipeMaps#CROP_SYNTHESIZER} 自动生成，干活逻辑全在
 * {@link CropSynthesizerRecipeLogic} 里。
 */
public class MetaTileEntityCropSynthesizer extends MetaTileEntityCropMachine {

    /** 输入槽：4 个基因球（槽位无关，靠 NBT 里的 kind 区分）。 */
    public static final int INPUT_SLOT_COUNT = 4;
    private static final int SLOT_OUTPUT = 0;

    public MetaTileEntityCropSynthesizer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, CropRecipeMaps.CROP_SYNTHESIZER, Textures.CROP_SYNTHESIZER_OVERLAY, tier);
    }

    @Override
    protected CropSynthesizerRecipeLogic createLogic(RecipeMap<?> recipeMap) {
        return new CropSynthesizerRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropSynthesizer(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(CropTooltips.machineType(getTooltipKey()));
        tooltip.add(I18n.format("drtech_tooltip.cropSynthesizer.0"));
        tooltip.add(I18n.format("drtech_tooltip.cropSynthesizer.1", CropTooltips.fluid(750)));
        tooltip.add(I18n.format("drtech_tooltip.cropSynthesizer.2", CropTooltips.fluid(100)));
        tooltip.add(I18n.format("drtech_tooltip.cropSynthesizer.3", CropTooltips.number(3)));
    }

    @Override
    protected String getTooltipKey() {
        return "cropSynthesizer";
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, INPUT_SLOT_COUNT, this, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // 只认本系统的基因球（GT 数据球 + cropqt_gene 子标签）
                return CropGeneOrb.isGeneOrb(stack);
            }
        };
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        // 源端只收 UUM
        return new FluidTankList(false,
                new NotifiableFluidTank(CropMachineTankCapacity.forSynthesizer(getTier()), this, false) {
                    @Override
                    public boolean canFillFluidType(FluidStack fluid) {
                        return fluid != null && fluid.getFluid() == Materials.UUMatter.getFluid();
                    }
                });
    }

    /**
     * 源端合成器按 3A 取电；CEU 的默认机器只给 2A，这里单独建一个 3A 的能量容器。
     */
    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = EnergyContainerHandler.receiverContainer(
                this, tierVoltage * 64L, tierVoltage, 3L);
    }
}
