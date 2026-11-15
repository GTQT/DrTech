package com.meowmel.cropQT.machine;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.CropMachineTankCapacity;
import com.meowmel.cropQT.api.CropTooltips;
import com.meowmel.cropQT.api.capability.impl.SeedGeneratorRecipeLogic;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
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
 * 种子生成器：把一份已分析的种子扩繁成一份。
 *
 * <p>源端 {@code MTESeedGenerator} 是 2 输入 / 1 输出：输入槽 0 放种子，
 * 输入槽 1 放可选催化剂（作物声明的 {@code duplicationCatalysts}）。
 * 罐里只接受浓缩液肥，容量按 {@link CropMachineTankCapacity#forSeedGenerator(int)}
 * （IV 以下为普通机器容量的 1/10）。
 */
public class MetaTileEntitySeedGenerator extends MetaTileEntityCropMachine {

    /** 输入槽：待扩繁的种子。 */
    public static final int SLOT_INPUT = 0;
    /** 输入槽：可选的扩繁催化剂。 */
    public static final int SLOT_CATALYST = 1;
    /** 输出槽：扩繁出来的种子。 */
    public static final int SLOT_OUTPUT = 0;

    public MetaTileEntitySeedGenerator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, CropRecipeMaps.SEED_GENERATOR, Textures.SEED_GENERATOR_OVERLAY, tier);
    }

    @Override
    protected SeedGeneratorRecipeLogic createLogic(RecipeMap<?> recipeMap) {
        return new SeedGeneratorRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntitySeedGenerator(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(CropTooltips.machineType(getTooltipKey()));
        tooltip.add(I18n.format("drtech_tooltip.seedGenerator.0"));
        tooltip.add(I18n.format("drtech_tooltip.seedGenerator.1", CropTooltips.fluid(36)));
        tooltip.add(I18n.format("drtech_tooltip.seedGenerator.2"));
        tooltip.add(I18n.format("drtech_tooltip.seedGenerator.3",
                CropTooltips.fluid(CropMachineTankCapacity.forSeedGenerator(getTier()))));
        if (getTier() <= GTValues.MV) {
            tooltip.add(I18n.format("drtech_tooltip.seedGenerator.mv_warn"));
        }
    }

    @Override
    protected String getTooltipKey() {
        return "seedGenerator";
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, false);
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new NotifiableFluidTank(
                CropMachineTankCapacity.forSeedGenerator(getTier()), this, false) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return fluid != null && fluid.getFluid() == DrtechMaterials.EnrichedFertilizer.getFluid();
            }
        });
    }
}
