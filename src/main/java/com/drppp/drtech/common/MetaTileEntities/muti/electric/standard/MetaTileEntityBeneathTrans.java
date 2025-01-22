package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.shinoow.beneath.Beneath;
import com.shinoow.beneath.common.network.PacketDispatcher;
import com.shinoow.beneath.common.network.server.TeleportMessage;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqtcore.common.metatileentities.multi.multiblock.standard.MetaTileEntityBaseWithControl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityBeneathTrans extends MetaTileEntityBaseWithControl {

    public MetaTileEntityBeneathTrans(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 238);
        // Display
        builder.image(4, 4, 172, 139, GuiTextures.DISPLAY);
        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);

        builder.widget(new ClickButtonWidget(68, 60, 40, 20, "地心盾构", data -> transport(entityPlayer))
                .setTooltipText("确认传送"));

        builder.bindPlayerInventory(entityPlayer.inventory, 155);
        return builder;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("带上她的眼睛", new Object[0]));
        tooltip.add(I18n.format("使用时需将多方块结构放置在基岩层"));
        tooltip.add(I18n.format("可将玩家传送至深渊世界"));

    }
    private void transport(EntityPlayer entityPlayer) {
        if(this.getPos().getY()>=10)
        {
            entityPlayer.sendStatusMessage(new TextComponentString("设备需接触基岩层！"), true);
            return;
        }
        double xdiff = entityPlayer.getPosition().getX() - this.getPos().getX();
        double zdiff = entityPlayer.getPosition().getZ() - this.getPos().getZ();
        if (xdiff <= 2.0 && xdiff >= -2.0 && zdiff <= 2.0 && zdiff >= -2.0) {
            if (entityPlayer.posY >= (double) this.getPos().getY()-1 && entityPlayer.posY <= (double) (this.getPos().getY() + 1)) {
                if (entityPlayer.dimension != 0 && entityPlayer.dimension != Beneath.dim && !Beneath.dimTeleportation) {
                    entityPlayer.sendStatusMessage(new TextComponentString("设备需放置在主世界"), true);
                } else {
                    PacketDispatcher.sendToServer(new TeleportMessage(this.getPos()));
                }
            } else {
                entityPlayer.sendStatusMessage(new TextComponentString("玩家必须与设备在同一水平高度!"), true);
            }
        } else {
            entityPlayer.sendStatusMessage(new TextComponentString("玩家距离设备过远"), true);
        }
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AA     AA", "AA     AA", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle("AAAAAAAAA", "AA     AA", " B     B ", " B     B ", " B     B ", " B     B ", " B     B ", " B     B ", "         ", "         ", "         ", "         ")
                .aisle(" AAAAAAA ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle(" AAAPAAA ", "   AAA   ", "   AAA   ", "   B B   ", "   B B   ", "   BBB   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   AAA   ", "   AAA   ")
                .aisle(" AAPPPAA ", "   APA   ", "   APA   ", "    P    ", "    P    ", "   BPB   ", "    P    ", "    P    ", "    P    ", "    P    ", "   APA   ", "   APA   ")
                .aisle(" AAAPAAA ", "   AMA   ", "   ASA   ", "   B B   ", "   B B   ", "   BBB   ", "   B B   ", "   B B   ", "   B B   ", "   B B   ", "   AAA   ", "   AAA   ")
                .aisle(" AAAAAAA ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")
                .aisle("AAAAAAAAA", "AA     AA", " B     B ", " B     B ", " B     B ", " B     B ", " B     B ", " B     B ", "         ", "         ", "         ", "         ")
                .aisle("AA     AA", "AA     AA", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ", "         ")

                .where('S', this.selfPredicate())
                .where('M', abilities(MultiblockAbility.MAINTENANCE_HATCH))
                .where('A', states(this.getCasingState()))
                .where('P', states(this.getPipeState()))
                .where('B', this.getFramePredicate()).where('#', any()).build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }
    private IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }
    private TraceabilityPredicate getFramePredicate() {
        return frames(Materials.Steel);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }


    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBeneathTrans(this.metaTileEntityId);
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.emptyList();
    }
}
