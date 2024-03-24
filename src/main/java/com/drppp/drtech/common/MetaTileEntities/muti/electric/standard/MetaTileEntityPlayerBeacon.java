package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.WirelessNetwork.WirelessNetworkManager;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
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
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MetaTileEntityPlayerBeacon extends MetaTileEntityBaseWithControl{
    private long energyStore=0;
    private final long maxEnergyStore=100000000;

    private UUID networkUid=null;
    private int tick=0;
    private List<UUID> players = new ArrayList<>();
    public MetaTileEntityPlayerBeacon(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAA", "GGGGG","     ")
                .aisle("ACCCA", "GTTTG","     ")
                .aisle("ACCCA", "GTTTG","  X  ")
                .aisle("ACCCA", "GTTTG","     ")
                .aisle("AASAA", "GGGGG","     ")
                .where('S', selfPredicate())
                .where('X', blocks(Blocks.BEACON))
                .where('T', blocks(Blocks.IRON_BLOCK))
                .where(' ', any())
                .where('C', heatingCoils())
                .where('G', frames(Materials.Steel))
                .where('A',
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)
                                .or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                )
                .build();
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityPlayerBeacon(this.metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentString("绑定网络:"+this.networkUid));
        textList.add(new TextComponentString("网络存储能量:"+WirelessNetworkManager.getUserEU(this.networkUid).toString()));
        textList.add(new TextComponentString("机器存储能量:"+this.energyStore));
        if(players.size()>0)
            for (int i = 0; i < players.size(); i++) {
                textList.add(new TextComponentString("存储玩家:"+this.getWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(players.get(i)).getDisplayNameString()));
            }
    }

    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 9, 9, "", this::decrementThreshold)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_MINUS)
                .setTooltipText("drtech.machine.player_beacon.btn1"));
        group.addWidget(new ClickButtonWidget(9, 0, 9, 9, "", this::incrementThreshold)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_PLUS)
                .setTooltipText("drtech.machine.player_beacon.btn2"));
        return group;
    }
    private void incrementThreshold(Widget.ClickData clickData) {
        //addPlayerUUID();
    }

    private void decrementThreshold(Widget.ClickData clickData) {
        for (int i = 0; i < players.size(); i++) {
            removePlayerUUID(players.get(i));
        }
    }
    @Override
    protected void updateFormedValid() {
        if(!this.getWorld().isRemote)
        {
            if(!this.isActive())
                setActive(true);
            if(this.energyStore<=this.maxEnergyStore/2 && this.networkUid!=null &&this.isWorkingEnabled())
            {

                WirelessNetworkManager.strongCheckOrAddUser(networkUid);
                BigInteger min = DrtechUtils.getBigIntegerMin(BigInteger.valueOf(maxEnergyStore/2), WirelessNetworkManager.getUserEU(networkUid));
                this.energyStore = Math.min(maxEnergyStore,min.longValue()+energyStore);
            }
            if(++tick>20 &&this.isWorkingEnabled())
            {
                tick=0;
                if(this.energyStore>0 && players.size()>0)
                {
                    for (UUID ID:players)
                    {
                        var p = this.getWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(ID);
                       if(p!=null && p.inventory!=null)
                       {
                           IInventory inventoryPlayer = p.inventory;
                           for (int i = 0; i < inventoryPlayer.getSizeInventory(); i++) {
                               ItemStack itemInSlot = inventoryPlayer.getStackInSlot(i);
                               IElectricItem slotElectricItem = itemInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                               if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {
                                   long chargedAmount = slotElectricItem.charge(energyStore,slotElectricItem.getTier(),true,false);
                                   if (chargedAmount > 0L) {
                                       energyStore -= chargedAmount;
                                   }
                               }
                           }
                           if (Loader.isModLoaded("baubles"))
                           {

                               IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(p);

                               for (int i = 0; i < baubles.getSlots(); i++) {
                                   ItemStack itemInSlot = baubles.getStackInSlot(i);
                                   IElectricItem slotElectricItem = itemInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                                   if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {
                                       long chargedAmount = slotElectricItem.charge(energyStore,slotElectricItem.getTier(),true,false);
                                       if (chargedAmount > 0L) {
                                           energyStore -= chargedAmount;
                                       }
                                   }
                               }
                           }
                       }
                    }
                }
            }
        }

    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("storeEnergy",this.energyStore);
        data.setUniqueId("netWorkId",this.networkUid);
        if(players.size()>0)
        {
            NBTTagCompound tag = new NBTTagCompound();
            for (int i = 0; i < players.size(); i++) {
                tag.setUniqueId("player"+i,players.get(i));
            }
            data.setTag("players",tag);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if(data.hasKey("storeEnergy"))
            this.energyStore = data.getLong("storeEnergy");
        if(data.hasKey("netWorkIdMost"))
            this.networkUid =data.getUniqueId("netWorkId");
        if(data.hasKey("players"))
        {
            players.clear();
            NBTTagCompound tag = data.getCompoundTag("players");
            for (int i = 0; i < 999; i++) {
                if(tag.hasKey("player"+i+"Most"))
                {
                    players.add(tag.getUniqueId("player"+i));
                }
                else
                    break;
            }
        }
    }

    @Override
    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack is = player.getHeldItem(EnumHand.MAIN_HAND);
        if(is.getItem()== MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaItem() && is.getMetadata()==MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaValue())
        {
            NBTTagCompound compound = is.getTagCompound();
            if(compound!=null && compound.hasKey("PUUIDMost"))
            {
                UUID id = compound.getUniqueId("PUUID");
                setUUID( id);
            }

        }else if(is.getItem()== MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaItem() && is.getMetadata()==MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaValue() && player.isSneaking())
        {
            addPlayerUUID(player.getUniqueID());
        }
    }
    public void setUUID(UUID uuid) {
        this.networkUid = uuid;
        if(!this.players.contains(uuid))
            this.players.add(uuid);
        this.writeCustomData(1919, (b) -> {
            b.writeUniqueId(this.networkUid);
        });
        this.writeCustomData(1920, (b) -> {
            b.writeUniqueId(uuid);
        });
    }
    public void addPlayerUUID(UUID uuid) {
        if(!this.players.contains(uuid))
            this.players.add(uuid);
        else
            return;
        for (int i = 0; i < players.size(); i++) {
            int finalI = i;
            this.writeCustomData(1920+i, (b) -> {
                b.writeUniqueId(players.get(finalI));
            });
        }
    }
    public void removePlayerUUID(UUID uuid) {
        if(this.players.contains(uuid))
            this.players.remove(uuid);
        for (int i = 0; i < players.size(); i++) {
            int finalI = i;
            this.writeCustomData(1920+i, (b) -> {
                b.writeUniqueId(players.get(finalI));
            });
        }
    }
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1919) {
            this.networkUid = buf.readUniqueId();
        }
        for (int i = 0; i < 10; i++) {
            if(dataId==1920+i)
            {
                int finalI = i;
                UUID uid = buf.readUniqueId();
                if(!this.players.contains(uid))
                    this.players.add(uid);
            }
        }
    }
}
