package com.meowmel.cropQT.machine;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.capability.impl.CropGeneExtractorRecipeLogic;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 基因提取器：把一颗已分析的种子拆成 4 个基因球之一。
 *
 * <p>一次只提取一样——用<b>潜行 + 螺丝刀</b>切换要抽的是物种还是某一项属性。
 * 源端用「电路槽选 1~4」，这里换成螺丝刀：这 4 台机器都不走 RecipeMap 的配方逻辑，
 * 为它单独接一套幽灵电路不划算；而 CEU 的标准面板里也没有塞按钮的地方。
 * 普通螺丝刀（不潜行）保留基类行为：切「允许从输出面输入」。
 *
 * <p>这里是机器的「壳」—— 槽位、tooltip。界面由 CEU 照
 * {@link CropRecipeMaps#GENE_EXTRACTOR} 自动生成，干活逻辑全在
 * {@link CropGeneExtractorRecipeLogic} 里。
 */
public class MetaTileEntityCropGeneExtractor extends MetaTileEntityCropMachine {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 0;

    public MetaTileEntityCropGeneExtractor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, CropRecipeMaps.GENE_EXTRACTOR, Textures.GENE_EXTRACTOR_OVERLAY, tier);
    }

    @Override
    protected CropGeneExtractorRecipeLogic createLogic(RecipeMap<?> recipeMap) {
        return new CropGeneExtractorRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropGeneExtractor(metaTileEntityId, getTier());
    }

    /** 本机的逻辑，带完整类型。 */
    private CropGeneExtractorRecipeLogic logic() {
        return (CropGeneExtractorRecipeLogic) workable;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.6"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.7"));
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (player.isSneaking() && !player.getEntityWorld().isRemote) {
            CropGeneExtractorRecipeLogic logic = logic();
            logic.cycleMode();
            player.sendStatusMessage(new TextComponentTranslation(logic.currentModeNameKey()), true);
            return true;
        }
        return super.onScrewdriverClick(player, hand, facing, hitResult);
    }

    // ==================== 槽位 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        // 只收「已分析」的种子
        return new NotifiableItemStackHandler(this, 1, this, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !ItemCropSeed.getCropId(stack).isEmpty() && ItemCropSeed.getCropStats(stack).isAnalyzed();
            }
        };
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }
}
