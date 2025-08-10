package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static gregtech.api.unification.material.Materials.Water;
import static keqing.gtqtcore.api.unification.GTQTMaterials.SuperheatedSteam;

public class MetaTileEntityDeepGroundPump extends MetaTileEntityBaseWithControl {
    public int Deep = 0;
    public double Temp;
    public double Heat;
    public int thresholdPercentage = 1;
    int water;
    FluidStack WATER_STACK = Water.getFluid(1000);

    public MetaTileEntityDeepGroundPump(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 9, 18, "", this::decrementThreshold)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_MINUS)
                .setTooltipText("增加单位水换热"));
        group.addWidget(new ClickButtonWidget(9, 0, 9, 18, "", this::incrementThreshold)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_PLUS)
                .setTooltipText("减少单位水换热"));
        return group;
    }
    @Override
    public boolean usesMui2() {
        return false;
    }
    private void incrementThreshold(Widget.ClickData clickData) {
        this.thresholdPercentage = MathHelper.clamp(thresholdPercentage + 1, 1, 10);
    }

    private void decrementThreshold(Widget.ClickData clickData) {
        this.thresholdPercentage = MathHelper.clamp(thresholdPercentage - 1, 1, 10);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("water", water);
        data.setInteger("thresholdPercentage", thresholdPercentage);
        data.setDouble("Temp", Temp);
        data.setDouble("Heat", Heat);
        data.setInteger("Deep", Deep);
        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        water = data.getInteger("water");
        thresholdPercentage = data.getInteger("thresholdPercentage");
        Temp = data.getDouble("Temp");
        Heat = data.getDouble("Heat");
        Deep = data.getInteger("Deep");
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("谢谢地球", new Object[0]));
        tooltip.add(I18n.format("提供采矿管道，机器将向下挖掘（最大10000m）"));
        tooltip.add(I18n.format("环境温度将随着深度增加而上升"));
        tooltip.add(I18n.format("交换仓将基于与环境温度的热交换公式增加温度"));
        tooltip.add(I18n.format("提供水时将按比例消耗交换仓温度生产蒸汽废气 ，过热蒸汽，高压蒸汽（按温度区分）"));
        tooltip.add(I18n.format("调节节流阀控制换热效率"));
    }

    @Override
    protected void updateFormedValid() {
        IMultipleTankHandler inputTank = getInputFluidInventory();
        if (water < 100000 && WATER_STACK.isFluidStackIdentical(inputTank.drain(WATER_STACK, false))) {
            inputTank.drain(WATER_STACK, true);
            water += 1;
        }

        ItemStack item;
        int deep = 0;
        if (Deep < 10000) for (int i = 0; i < this.getInputInventory().getSlots(); i++) {
            item = this.getInputInventory().getStackInSlot(i);
            if (item.getItem() == DrMetaItems.PIPIE_1.getMetaItem() && item.getMetadata() == DrMetaItems.PIPIE_1.getMetaValue()) {
                deep += item.getCount();
            } else if (item.getItem() == DrMetaItems.PIPIE_5.getMetaItem() && item.getMetadata() == DrMetaItems.PIPIE_5.getMetaValue()) {
                deep += item.getCount() * 5;
            } else if (item.getItem() == DrMetaItems.PIPIE_10.getMetaItem() && item.getMetadata() == DrMetaItems.PIPIE_10.getMetaValue()) {
                deep += item.getCount() * 10;
            }
        }

        if (Deep != deep) Deep = deep;

        Temp = 20 + Deep * 0.03;//这里表示当前的温度
        if (Heat < Temp) Heat = (Temp - Heat) / Heat;//升温曲线

        if (water < 1000) return;
        water -= thresholdPercentage;
        Heat -= Heat / 10000;

        if (Heat > 5000) {
            GTTransferUtils.addFluidsToFluidHandler(getOutputFluidInventory(), false, Collections.singletonList(SuperheatedSteam.getFluid(((Deep - 5000) / 1000 + 1) * thresholdPercentage)));
        } else if (Heat > 1000) {
            GTTransferUtils.addFluidsToFluidHandler(getOutputFluidInventory(), false, Collections.singletonList(SuperheatedSteam.getFluid(((Deep - 1000) / 1000 + 1) * 8 * thresholdPercentage)));
        } else if (Heat > 500) {
            GTTransferUtils.addFluidsToFluidHandler(getOutputFluidInventory(), false, Collections.singletonList(SuperheatedSteam.getFluid(((Deep - 500) / 100 + 1) * 32 * thresholdPercentage)));
        }


    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("A   A   A BBBB B", "A   A   A    B B", "BBBBBBBBBBBBBB B", "BBBBBBBBB    B B", "BBBBBBBBBBBBBB B", "BBBBBBBBB    B B", "             B B", "             BBB", "             BBB", "             BBB", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ")
                .aisle("              C ", "A   A   A     C ", "BBBBBBBBB     C ", "BCCCCCCCCCCCCCC ", "B C C C B     C ", "BBBBBBBBB     C ", "              A ", "             BAB", "             BAB", "             BAB", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ")
                .aisle("A   A   A BBBB B", "A   A   A    B B", "BBBBBBBBBBBBBB B", "BBBBBBBBB    B B", "BBCBCBCBBBBBBB B", "BBBBBBBBB    B B", "             B B", "             BBB", "             BBB", "             BBB", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ")
                .aisle("                ", "A   A   A       ", "                ", "                ", "  C C C         ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ")
                .aisle("                ", "A   A   A       ", "                ", "                ", "  C C C         ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ")
                .aisle("A   A   A BBBB B", "A   A   A    B B", "BBBBBBBBBBBBBB B", "BBBBBBBBB    B B", "BBCBCBCBBBBBBB B", "BBBBBBBBB    B B", "             B B", "             BBB", "             BBB", "             BBB", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ")
                .aisle("              C ", "A   A   A     C ", "BBBBBBBBB     C ", "BCCCCCCCCCCCCCC ", "B C C C B     C ", "BBBBBBBBB     C ", "              A ", "             BAB", "             BAB", "             BAB", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ", "              A ")
                .aisle("A   A   A BBBB B", "A   A   A    B B", "BBBBBBBBBBBBBB B", "BBBBSBBBB    B B", "BBBBBBBBBBBBBB B", "BBBBBBBBB    B B", "             B B", "             BBB", "             BBB", "             BBB", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ", "                ")
                .where('S', selfPredicate())
                .where(' ', any())
                .where('C', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('A', frames(Materials.Steel))
                .where('B', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                )
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDeepGroundPump(this.metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("drtech.deep_ground_pump.deep", this.Deep));
        textList.add(new TextComponentTranslation("水量：%s", this.water));
        textList.add(new TextComponentTranslation("反应仓温度：%s", this.Heat));
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.emptyList();
    }
}
