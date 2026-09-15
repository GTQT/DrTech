package com.meowmel.cropQT.machine;

import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.capability.impl.SeedGeneratorRecipeLogic;
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
 * 种子生成器：把一份已分析的种子扩繁成两份。
 *
 * <p>这里是机器的「壳」—— 槽位、罐、tooltip。界面由 CEU 照
 * {@link CropRecipeMaps#SEED_GENERATOR} 自动生成，干活逻辑全在
 * {@link SeedGeneratorRecipeLogic} 里。
 */
public class MetaTileEntitySeedGenerator extends MetaTileEntityCropMachine {

    /** 输入槽：待扩繁的种子。 */
    public static final int SLOT_INPUT = 0;
    /** 输出槽：扩繁出来的种子。 */
    public static final int SLOT_OUTPUT = 0;

    /** 肥料罐容量，16 桶。 */
    private static final int TANK_CAPACITY = 16000;

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
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.6"));
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
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
