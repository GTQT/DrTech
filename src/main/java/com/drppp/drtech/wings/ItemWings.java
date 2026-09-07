package com.drppp.drtech.wings;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public final class ItemWings extends Item {
    private final WingType wingType;

    public ItemWings(WingType wingType) {
        this.wingType = wingType;
        setRegistryName(Tags.MODID, wingType.getSerializedName() + "_wings");
        setTranslationKey(Tags.MODID + "." + wingType.getSerializedName() + "_wings");
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
        setMaxDamage(wingType.getDurability());
    }

    public WingType getWingType() {
        return wingType;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.type == EnumEnchantmentType.ALL
                || enchantment.type == EnumEnchantmentType.BREAKABLE
                || enchantment.type == EnumEnchantmentType.WEARABLE;
    }

    @Override
    public boolean getIsRepairable(ItemStack stack, ItemStack ingredient) {
        return ingredient.getItem() == ItemsInit.WING_FAIRY_DUST;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chest.isEmpty()) {
            if (WingsBaublesCompat.equip(player, held.copy())) {
                held.shrink(1);
                player.world.playSound(null, player.posX, player.posY, player.posZ,
                        net.minecraft.init.SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                        SoundCategory.PLAYERS, 1.0F, 1.0F);
                return new ActionResult<>(EnumActionResult.SUCCESS, held);
            }
            return new ActionResult<>(EnumActionResult.FAIL, held);
        }

        ItemStack equipped = held.splitStack(1);
        player.setItemStackToSlot(EntityEquipmentSlot.CHEST, equipped);
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                net.minecraft.init.SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }
}
