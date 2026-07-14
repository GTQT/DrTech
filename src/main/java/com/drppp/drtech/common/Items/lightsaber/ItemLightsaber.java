package com.drppp.drtech.common.Items.lightsaber;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityThrownLightsaber;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLightsaber extends Item {
    public static final double ATTACK_DAMAGE = 8.0D;
    private static final double ATTACK_SPEED = -2.4D;
    private static final String ACTIVE_TAG = "active";

    public ItemLightsaber() {
        setRegistryName(Tags.MODID, "lightsaber");
        setTranslationKey(Tags.MODID + ".lightsaber");
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        setFull3D();
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + getColor(stack).getSerializedName();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (!world.isRemote) {
                ItemStack thrownStack = stack.copy();
                thrownStack.setCount(1);
                setActive(thrownStack, true);

                EntityThrownLightsaber thrown = new EntityThrownLightsaber(world, player, thrownStack, hand);
                world.spawnEntity(thrown);
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
        if (isActive(stack)) {
            attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
                    DrTechSounds.LIGHTSABER_HIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

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
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND && isActive(stack)) {
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", ATTACK_DAMAGE, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", ATTACK_SPEED, 0));
        }

        return modifiers;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        tooltip.add(I18n.format(isActive(stack) ? "tooltip.drtech.lightsaber.active" : "tooltip.drtech.lightsaber.inactive"));
        tooltip.add(I18n.format("tooltip.drtech.lightsaber.controls"));
    }

    @Override
    public void getSubItems(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (LightsaberColor color : LightsaberColor.values()) {
                items.add(new ItemStack(this, 1, color.getMetadata()));
            }
        }
    }

    public static LightsaberColor getColor(ItemStack stack) {
        return LightsaberColor.byMetadata(stack.getMetadata());
    }

    public static boolean isActive(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean(ACTIVE_TAG);
    }

    public static void setActive(ItemStack stack, boolean active) {
        if (stack.isEmpty()) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setBoolean(ACTIVE_TAG, active);
    }
}
