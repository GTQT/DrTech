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
import java.util.Random;

public class ItemLightsaber extends Item {
    public static final int MIN_ATTACK_DAMAGE = 40;
    public static final int MAX_ATTACK_DAMAGE = 60;
    public static final float THROWN_DAMAGE_MULTIPLIER = 1.5F;
    public static final double ATTACK_DAMAGE = MIN_ATTACK_DAMAGE - 1.0D;
    private static final double ATTACK_SPEED = -2.4D;
    private static final String ACTIVE_TAG = "active";
    private static final String BLADE_COLOR_TAG = "BladeColor";
    private static final String PARTS_TAG = "HiltParts";
    private static final String FOCUSING_CRYSTALS_TAG = "FocusingCrystals";
    private final LightsaberHilt baseHilt;
    private final LightsaberColor defaultColor;

    public ItemLightsaber(String registryName, LightsaberHilt baseHilt) {
        this.baseHilt = baseHilt;
        this.defaultColor = baseHilt.getDefaultColor();
        setRegistryName(Tags.MODID, registryName);
        setTranslationKey(Tags.MODID + ".lightsaber." + baseHilt.getSerializedName());
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
        setMaxDamage(0);
        setFull3D();
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey();
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
        tooltip.add(I18n.format("tooltip.drtech.lightsaber.damage", MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE));
        for (LightsaberPartType type : LightsaberPartType.values()) {
            tooltip.add(I18n.format("tooltip.drtech.lightsaber.part." + type.getTextureName(), getPart(stack, type).getDisplayName()));
        }
        int focusingCrystals = getFocusingCrystalMask(stack);
        if (focusingCrystals != 0) {
            for (FocusingCrystal crystal : FocusingCrystal.values()) {
                if ((focusingCrystals & crystal.getMask()) != 0) {
                    tooltip.add(I18n.format("tooltip.drtech.lightsaber.focusing_crystal",
                            I18n.format("item.drtech.focusing_crystal." + crystal.getSerializedName() + ".name")));
                }
            }
        }
        tooltip.add(I18n.format("tooltip.drtech.lightsaber.controls"));
    }

    @Override
    public void getSubItems(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }

    public static LightsaberColor getColor(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(BLADE_COLOR_TAG)) {
            return LightsaberColor.byMetadata(stack.getTagCompound().getInteger(BLADE_COLOR_TAG));
        }
        if (!stack.isEmpty() && stack.getItem() instanceof ItemLightsaber) {
            ItemLightsaber item = (ItemLightsaber) stack.getItem();
            if (item.baseHilt == LightsaberHilt.GRAFLEX && stack.getMetadata() > 0) {
                return LightsaberColor.byMetadata(stack.getMetadata());
            }
            return item.defaultColor;
        }
        return LightsaberColor.DEEP_BLUE;
    }

    public static ItemStack create(LightsaberColor color, LightsaberHilt emitter, LightsaberHilt switchSection,
                                   LightsaberHilt body, LightsaberHilt pommel, int focusingCrystals) {
        ItemStack stack = new ItemStack(com.drppp.drtech.common.Items.ItemsInit.getLightsaber(emitter));
        getOrCreateTag(stack).setInteger(BLADE_COLOR_TAG, color.getMetadata());
        setParts(stack, emitter, switchSection, body, pommel);
        setFocusingCrystalMask(stack, focusingCrystals);
        return stack;
    }

    public static LightsaberHilt getPart(ItemStack stack, LightsaberPartType type) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            int[] parts = stack.getTagCompound().getIntArray(PARTS_TAG);
            if (parts.length == LightsaberPartType.values().length) {
                return LightsaberHilt.byMetadata(parts[type.ordinal()]);
            }
        }
        return !stack.isEmpty() && stack.getItem() instanceof ItemLightsaber
                ? ((ItemLightsaber) stack.getItem()).baseHilt : LightsaberHilt.GRAFLEX;
    }

    public LightsaberHilt getBaseHilt() {
        return baseHilt;
    }

    public LightsaberColor getDefaultColor() {
        return defaultColor;
    }

    public static void setParts(ItemStack stack, LightsaberHilt emitter, LightsaberHilt switchSection,
                                LightsaberHilt body, LightsaberHilt pommel) {
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setIntArray(PARTS_TAG, new int[] {
                emitter.getMetadata(), switchSection.getMetadata(), body.getMetadata(), pommel.getMetadata()
        });
    }

    public static int getFocusingCrystalMask(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound()
                ? stack.getTagCompound().getInteger(FOCUSING_CRYSTALS_TAG) : 0;
    }

    public static void setFocusingCrystalMask(ItemStack stack, int focusingCrystals) {
        getOrCreateTag(stack).setInteger(FOCUSING_CRYSTALS_TAG, focusingCrystals);
    }

    public static boolean isActive(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().getBoolean(ACTIVE_TAG);
    }

    public static void setActive(ItemStack stack, boolean active) {
        if (stack.isEmpty()) {
            return;
        }
        getOrCreateTag(stack).setBoolean(ACTIVE_TAG, active);
    }

    public static float rollAttackDamage(Random random) {
        return MIN_ATTACK_DAMAGE + random.nextInt(MAX_ATTACK_DAMAGE - MIN_ATTACK_DAMAGE + 1);
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
