package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.WirelessNetwork.WirelessNetworkManager;
import com.drppp.drtech.api.capability.impl.EnergyContainerWireless;
import com.drppp.drtech.common.Items.MetaItems.MetaItems1;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class MetaTileEntityWirelessEnergyHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {
    private final int amperage;
    private final boolean isExport;
    private final EnergyContainerWireless energyContainer;
    private UUID ownerUuid=null;
    
    public MetaTileEntityWirelessEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage,boolean isExport) {
        super(metaTileEntityId, tier);
        this.isExport = isExport;
        this.amperage = amperage;
        energyContainer = new EnergyContainerWireless(this,isExport, GTValues.V[tier],this.amperage);

    }
    
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityWirelessEnergyHatch(this.metaTileEntityId,this.getTier(),this.amperage,this.isExport);
    }
    
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        builder.widget((new AdvancedTextWidget(9, 8, this::addDisplayText, 16777215)).setMaxWidthLimit(181));
        return builder.build(this.getHolder(), entityPlayer);
    }
    protected void addDisplayText(List<ITextComponent> textList) {
        if(this.ownerUuid!=null)
        {
            WirelessNetworkManager.strongCheckOrAddUser(this.ownerUuid);
            textList.add(new TextComponentString(this.ownerUuid.toString()));
            textList.add(new TextComponentString("网络存储能量:"+WirelessNetworkManager.getUserEU(this.ownerUuid).toString()));
        }
    }
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            getOverlay().renderSided(getFrontFacing(),renderState,translation,pipeline);
        }

    }
    private SimpleOverlayRenderer getOverlay() {
        if (amperage == 2) {
            return Textures.MULTIPART_WIRELESS_ENERGY;
        } else if (amperage == 4) {
            return Textures.MULTIPART_WIRELESS_ENERGY_4x;
        } else if (amperage == 16) {
            return Textures.MULTIPART_WIRELESS_ENERGY_16x;
        } else if (amperage == 64) {
            return Textures.MULTIPART_WIRELESS_ENERGY_64x;
        } else if (amperage == 256) {
            return Textures.MULTIPART_WIRELESS_ENERGY_256x;
        } else if (amperage == 1024) {
            return Textures.MULTIPART_WIRELESS_ENERGY_1024x;
        } else if (amperage == 4096) {
            return Textures.MULTIPART_WIRELESS_ENERGY_4096x;
        } else if (amperage == 16384) {
            return Textures.MULTIPART_WIRELESS_ENERGY_16384x;
        } else if (amperage == 65536) {
            return Textures.MULTIPART_WIRELESS_ENERGY_65536x;
        } else if (amperage == 262144) {
            return Textures.MULTIPART_WIRELESS_ENERGY_262144x;
        } else if (amperage == 1048576) {
            return Textures.MULTIPART_WIRELESS_ENERGY_1048576x;
        }
        else return Textures.MULTIPART_WIRELESS_ENERGY;

    }
    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack is = playerIn.getHeldItem(EnumHand.MAIN_HAND);
        if(is.getItem()== MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaItem() && is.getMetadata()==MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaValue())
        {
            NBTTagCompound compound = is.getTagCompound();
            if(compound!=null && compound.hasKey("PUUIDMost"))
            {
                UUID id = compound.getUniqueId("PUUID");
                setUUID( id);
            }

        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return isExport ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }
    
    @Override
    public void registerAbilities(List<IEnergyContainer> list) {
        list.add(energyContainer);
    }
    public void setUUID(UUID uuid) {
        this.ownerUuid = uuid;
        this.energyContainer.ownerUuid= uuid;
        this.writeCustomData(1919, (b) -> {
            b.writeUniqueId(this.ownerUuid);
        });
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1919) {
            this.energyContainer.ownerUuid= this.ownerUuid = buf.readUniqueId();
        }

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if(this.ownerUuid!=null)
            data.setUniqueId("OwnerUUID",this.ownerUuid);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("OwnerUUIDMost"))
        {
            this.ownerUuid = data.getUniqueId("OwnerUUID");
            this.energyContainer.ownerUuid = data.getUniqueId("OwnerUUID");
        }
    }

    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format(this.isExport ? "drtech.machine.wireless.export.tooltip" : "drtech.machine.wireless.import.tooltip", new Object[0]));
    }
}
