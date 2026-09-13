package com.meowmel.cropQT.machine;

import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.capability.impl.CropSynthesizerRecipeLogic;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
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

    private static final int TANK_CAPACITY = 16000;

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
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.6"));
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, INPUT_SLOT_COUNT, this, false);
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new NotifiableFluidTank(TANK_CAPACITY, this, false));
    }
}
