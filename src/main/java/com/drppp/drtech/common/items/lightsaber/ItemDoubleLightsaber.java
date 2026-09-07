package com.drppp.drtech.common.Items.lightsaber;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityThrownLightsaber;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Sound.DrTechSounds;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemDoubleLightsaber extends Item {
    public static final int MIN_ATTACK_DAMAGE = 60;
    public static final int MAX_ATTACK_DAMAGE = 80;
    public static final double ATTACK_DAMAGE = MIN_ATTACK_DAMAGE - 1.0D;
    public static final double ATTACK_RANGE = 4.0D;
    private static final String ACTIVE_TAG = "active";
    private static final String UPPER_TAG = "UpperLightsaber";
    private static final String LOWER_TAG = "LowerLightsaber";

    public ItemDoubleLightsaber() {
        setRegistryName(Tags.MODID, "double_lightsaber");
        setTranslationKey(Tags.MODID + ".double_lightsaber");
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
        setMaxDamage(0);
        setFull3D();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            ItemStack mauler = new ItemStack(ItemsInit.getLightsaber(LightsaberHilt.MAULER));
            items.add(create(mauler, mauler));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                ItemStack thrownStack = stack.copy();
                thrownStack.setCount(1);
                setActive(thrownStack, true);
                world.spawnEntity(new EntityThrownLightsaber(world, player, thrownStack, hand));
                player.setHeldItem(hand, ItemStack.EMPTY);
                player.getCooldownTracker().setCooldown(this, 10);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        boolean active = !isActive(stack);
        setActive(stack, active);
        if (!world.isRemote) {
            world.playSound(null, player.posX, player.posY, player.posZ,
                    active ? DrTechSounds.LIGHTSABER_ON : DrTechSounds.LIGHTSABER_OFF,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, net.minecraft.entity.Entity entity) {
        return !isActive(stack);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return true;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (isActive(stack) && !entityLiving.world.isRemote) {
            entityLiving.world.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ,
                    DrTechSounds.LIGHTSABER_SWING, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return false;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EntityEquipmentSlot.MAINHAND && isActive(stack)) {
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier",
                            ATTACK_DAMAGE, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4D, 0));
        }
        return modifiers;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(I18n.format(isActive(stack)
                ? "tooltip.drtech.lightsaber.active" : "tooltip.drtech.lightsaber.inactive"));
        tooltip.add(I18n.format("tooltip.drtech.lightsaber.damage",
                MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE));
        tooltip.add(I18n.format("tooltip.drtech.double_lightsaber.upper", getUpper(stack).getDisplayName()));
        tooltip.add(I18n.format("tooltip.drtech.double_lightsaber.lower", getLower(stack).getDisplayName()));
        tooltip.add(I18n.format("tooltip.drtech.lightsaber.controls"));
    }

    public static ItemStack create(ItemStack upper, ItemStack lower) {
        ItemStack result = new ItemStack(ItemsInit.DOUBLE_LIGHTSABER);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(UPPER_TAG, normalizeBlade(upper).writeToNBT(new NBTTagCompound()));
        tag.setTag(LOWER_TAG, normalizeBlade(lower).writeToNBT(new NBTTagCompound()));
        result.setTagCompound(tag);
        return result;
    }

    public static ItemStack getUpper(ItemStack stack) {
        return getBlade(stack, UPPER_TAG);
    }

    public static ItemStack getLower(ItemStack stack) {
        return getBlade(stack, LOWER_TAG);
    }

    public static boolean isActive(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean(ACTIVE_TAG);
    }

    public static void setActive(ItemStack stack, boolean active) {
        if (!stack.isEmpty()) {
            getOrCreateTag(stack).setBoolean(ACTIVE_TAG, active);
        }
    }

    public static float rollAttackDamage(Random random) {
        return MIN_ATTACK_DAMAGE + random.nextInt(MAX_ATTACK_DAMAGE - MIN_ATTACK_DAMAGE + 1);
    }

    private static ItemStack getBlade(ItemStack stack, String key) {
        if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(key, 10)) {
            ItemStack blade = new ItemStack(stack.getTagCompound().getCompoundTag(key));
            if (blade.getItem() instanceof ItemLightsaber) {
                return blade;
            }
        }
        return new ItemStack(ItemsInit.getLightsaber(LightsaberHilt.MAULER));
    }

    private static ItemStack normalizeBlade(ItemStack stack) {
        ItemStack result = stack.getItem() instanceof ItemLightsaber
                ? stack.copy() : new ItemStack(ItemsInit.getLightsaber(LightsaberHilt.MAULER));
        result.setCount(1);
        ItemLightsaber.setActive(result, false);
        return result;
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }
}
