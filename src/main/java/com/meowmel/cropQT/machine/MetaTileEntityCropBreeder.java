package com.meowmel.cropQT.machine;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.CropMachineTankCapacity;
import com.meowmel.cropQT.api.CropTooltips;
import com.meowmel.cropQT.api.capability.impl.CropBreederRecipeLogic;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
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
 * 作物育种机：把 2~4 颗已分析的亲本种子杂交成一颗新种子。
 *
 * <p>这里是机器的「壳」—— 槽位、罐、tooltip。界面由 CEU 照
 * {@link CropRecipeMaps#CROP_BREEDER} 自动生成，干活逻辑全在
 * {@link CropBreederRecipeLogic} 里。
 */
public class MetaTileEntityCropBreeder extends MetaTileEntityCropMachine {

    /** 低电压档给 3 个输入槽，HV 及以上给 6 个。 */
    private static final int MIN_INPUT_SLOTS = 3;
    private static final int MAX_INPUT_SLOTS = 6;

    private static final int SLOT_OUTPUT = 0;

    public MetaTileEntityCropBreeder(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, CropRecipeMaps.CROP_BREEDER, Textures.CROP_BREEDER_OVERLAY, tier);
    }

    @Override
    protected CropBreederRecipeLogic createLogic(RecipeMap<?> recipeMap) {
        return new CropBreederRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropBreeder(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(CropTooltips.machineType(getTooltipKey()));
        tooltip.add(I18n.format("drtech_tooltip.cropBreeder.0"));
        tooltip.add(I18n.format("drtech_tooltip.cropBreeder.1", CropTooltips.fluid(144)));
        tooltip.add(I18n.format("drtech_tooltip.cropBreeder.2", CropTooltips.fluid(72)));
        tooltip.add(I18n.format("drtech_tooltip.cropBreeder.3"));
        tooltip.add(I18n.format("drtech_tooltip.cropBreeder.4",
                CropTooltips.number(Math.min(100, 40 + Math.max(0, getTier() - GTValues.LV) * 10))));
        tooltip.add(I18n.format("drtech_tooltip.cropBreeder.5",
                CropTooltips.fluid(CropMachineTankCapacity.forBreeder(getTier()))));
        if (getTier() <= GTValues.MV) {
            tooltip.add(I18n.format("drtech_tooltip.cropBreeder.mv_warn"));
        }
    }

    @Override
    protected String getTooltipKey() {
        return "cropBreeder";
    }

    /**
     * 本档位有几个输入槽。
     *
     * <p>低档只开放前 3 个，但 handler 一律按 6 个建 —— 界面照 handler 的槽位数画，
     * 所以高电压档不用换界面。
     */
    public int getInputSlots() {
        return getTier() < GTValues.HV ? MIN_INPUT_SLOTS : MAX_INPUT_SLOTS;
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        // 只收「已分析」的种子，且只在开放的槽位上
        return new NotifiableItemStackHandler(this, MAX_INPUT_SLOTS, this, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot < getInputSlots()
                        && !ItemCropSeed.getCropId(stack).isEmpty()
                        && ItemCropSeed.getCropStats(stack).isAnalyzed();
            }
        };
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        // 源端只接受浓缩液肥；按 mB 直接扣，不做 potency 折算
        return new FluidTankList(false,
                new NotifiableFluidTank(CropMachineTankCapacity.forBreeder(getTier()), this, false) {
                    @Override
                    public boolean canFillFluidType(FluidStack fluid) {
                        return fluid != null
                                && fluid.getFluid() == DrtechMaterials.EnrichedFertilizer.getFluid();
                    }
                });
    }
}
