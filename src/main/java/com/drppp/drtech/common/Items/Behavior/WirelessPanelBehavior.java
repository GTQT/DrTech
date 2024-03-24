package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.WirelessNetwork.WirelessNetworkManager;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class WirelessPanelBehavior implements IItemBehaviour, ItemUIFactory {

    private ItemStack stack=null;

    public WirelessPanelBehavior() {
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player.isSneaking()) {
            ItemStack item = player.getHeldItem(hand);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("PUUID",player.getUniqueID());
            tag.setString("UserName",player.getDisplayNameString());
            item.setTagCompound(tag);
        }else if(!world.isRemote && !player.isSneaking())
        {
            stack=  player.getHeldItem(hand);
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder playerInventoryHolder, EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        builder.widget((new AdvancedTextWidget(9, 8, this::addDisplayText, 16777215)).setMaxWidthLimit(181));
        return builder.build(playerInventoryHolder, entityPlayer);
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        String name="空";
        UUID uid = null;
        if(stack!=null)
        {
            NBTTagCompound tag = stack.getTagCompound();
            if(tag!=null && tag.hasKey("UserName"))
            {
                name = tag.getString("UserName");
                uid = tag.getUniqueId("PUUID");
            }
        }
        textList.add(new TextComponentString("网络所属:"+name));
        if(uid!=null)
        {
            WirelessNetworkManager.strongCheckOrAddUser(uid);
            textList.add(new TextComponentString("网络存储能量:"+WirelessNetworkManager.getUserEU(uid).toString()));
        }
    }
    @Override
    public void addInformation(@NotNull ItemStack itemStack, List<String> lines) {
        if(itemStack.getItem()== MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaItem() && itemStack.getMetadata()==MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL.getMetaValue())
        {
            lines.add(I18n.format("behavior.data_item.wireless_panel.data.2"));
            lines.add(I18n.format("behavior.data_item.wireless_panel.data.3"));
            NBTTagCompound compound = itemStack.getTagCompound();
            if(compound!=null && compound.hasKey("PUUIDMost"))
            {
                UUID id = compound.getUniqueId("PUUID");
                if(compound.hasKey("UserName"))
                {
                    lines.add(I18n.format("behavior.data_item.wireless_panel.username", compound.getString("UserName")));
                }
                lines.add(I18n.format("behavior.data_item.wireless_panel.data", id.toString()));
            }
        }
    }
}
