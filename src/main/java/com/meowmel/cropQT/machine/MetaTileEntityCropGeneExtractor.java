package com.meowmel.cropQT.machine;

import com.meowmel.cropQT.api.CropTooltips;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.capability.impl.CropGeneExtractorRecipeLogic;
import com.meowmel.cropQT.api.recipes.CropRecipeMaps;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 基因提取器：把一颗已分析的种子拆成 4 个基因球之一。
 *
 * <p>抽哪一项由<b>第二个输入槽的编程电路</b>决定（1 物种 / 2 生长 / 3 产量 / 4 抗性），
 * 与源端 {@code MTECropGeneExtractor} 一致；第三个槽放空数据球，抽完消耗。
 *
 * <p>这里是机器的「壳」—— 槽位、tooltip。界面由 CEU 照
 * {@link CropRecipeMaps#GENE_EXTRACTOR} 自动生成，干活逻辑全在
 * {@link CropGeneExtractorRecipeLogic} 里。
 */
public class MetaTileEntityCropGeneExtractor extends MetaTileEntityCropMachine {

    /** 输入槽 0：待提取的已分析种子。 */
    public static final int SLOT_INPUT = 0;
    /** 输入槽 1：空数据球，成功后被消耗。 */
    public static final int SLOT_DATA_ORB = 1;
    /** 输入槽 2：编程电路 1..4，选择抽取项。 */
    public static final int SLOT_CIRCUIT = 2;
    /** 输出槽 0：带内容的基因球。 */
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(CropTooltips.machineType(getTooltipKey()));
        tooltip.add(I18n.format("drtech_tooltip.cropGeneExtractor.0"));
        tooltip.add(I18n.format("drtech_tooltip.cropGeneExtractor.1"));
        tooltip.add(I18n.format("drtech_tooltip.cropGeneExtractor.2"));
    }

    @Override
    protected String getTooltipKey() {
        return "cropGeneExtractor";
    }

    // ==================== 槽位 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 3, this, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (stack.isEmpty()) {
                    return false;
                }
                switch (slot) {
                    case SLOT_INPUT:
                        // 只收「已分析」的种子
                        return !ItemCropSeed.getCropId(stack).isEmpty()
                                && ItemCropSeed.getCropStats(stack).isAnalyzed();
                    case SLOT_DATA_ORB:
                        // 空数据球，或本系统的基因球（可覆写）
                        return MetaItems.TOOL_DATA_ORB.isItemEqual(stack);
                    case SLOT_CIRCUIT:
                        return MetaItems.INTEGRATED_CIRCUIT.isItemEqual(stack);
                    default:
                        return false;
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == SLOT_CIRCUIT ? 1 : super.getSlotLimit(slot);
            }
        };
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    /** 本机用实体电路槽选模式，不需要 CEU 的幽灵电路槽。 */
    @Override
    public boolean hasGhostCircuitInventory() {
        return false;
    }
}
