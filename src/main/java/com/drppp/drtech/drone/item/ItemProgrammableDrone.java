package com.drppp.drtech.drone.item;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.drone.hardware.DroneChassisTier;
import com.drppp.drtech.drone.hardware.DroneHardwareStats;
import com.drppp.drtech.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.drone.hardware.DroneUpgradeType;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/** Charged, recalled form of a drone. Deployment is added with the entity runtime slice. */
public final class ItemProgrammableDrone extends Item {

    public static final int DEFAULT_TIER = GTValues.HV;
    public static final long DEFAULT_CAPACITY = 4_000_000L;

    public ItemProgrammableDrone() {
        setRegistryName(Tags.MODID, "programmable_drone");
        setTranslationKey(Tags.MODID + ".programmable_drone");
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(1);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        DroneChassisTier chassis = DroneItemData.getChassis(stack);
        return new ElectricItem(stack, chassis.getBaseCapacity(), chassis.getVoltageTier(), true, false) {
            @Override
            public long getMaxCharge() {
                ItemStackHandler upgrades = new ItemStackHandler(DroneHardwareStats.UPGRADE_SLOTS);
                DroneUpgradeDataCodec.readInto(DroneItemData.getUpgrades(stack), upgrades);
                return DroneHardwareStats.capacity(chassis, upgrades);
            }
        };
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "."
                + DroneItemData.getChassis(stack).name().toLowerCase();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (DroneChassisTier chassis : DroneChassisTier.values()) {
            items.add(new ItemStack(this, 1, chassis.getMetadata()));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!world.isRemote) {
            UUID ownerId = DroneItemData.getOwnerId(held);
            if (ownerId != null && !ownerId.equals(player.getUniqueID())) {
                return new ActionResult<>(EnumActionResult.FAIL, held);
            }
            DroneItemData.migrateInPlace(held, player.getUniqueID());
            EntityProgrammableDrone drone = new EntityProgrammableDrone(world);
            Vec3d eyes = player.getPositionEyes(1.0F);
            Vec3d look = player.getLookVec();
            drone.setPosition(eyes.x + look.x * 1.5D, eyes.y - 0.35D + look.y * 1.5D,
                    eyes.z + look.z * 1.5D);
            drone.rotationYaw = player.rotationYaw;
            drone.initializeFromItem(held, player.getUniqueID());
            if (!world.spawnEntity(drone)) {
                return new ActionResult<>(EnumActionResult.FAIL, held);
            }
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            tooltip.add(I18n.format("drtech.drone.tooltip.eu", TextFormattingUtil.formatNumbers(electricItem.getCharge()),
                    TextFormattingUtil.formatNumbers(electricItem.getMaxCharge()), GTValues.VNF[electricItem.getTier()]));
        }
        DroneChassisTier chassis = DroneItemData.getChassis(stack);
        ItemStackHandler upgrades = new ItemStackHandler(DroneHardwareStats.UPGRADE_SLOTS);
        DroneUpgradeDataCodec.readInto(DroneItemData.getUpgrades(stack), upgrades);
        tooltip.add(I18n.format("drtech.drone.tooltip.hardware", chassis.name(),
                DroneHardwareStats.cargoSlots(chassis, upgrades),
                DroneHardwareStats.wirelessRange(chassis, upgrades)));
        tooltip.add(I18n.format("drtech.drone.tooltip.defense",
                (int) chassis.getMaxHealth(), (int) chassis.getArmor(),
                String.format(java.util.Locale.ROOT, "%.2f", DroneHardwareStats.movementSpeed(chassis, upgrades))));
        for (DroneUpgradeType type : DroneUpgradeType.values()) {
            if (DroneHardwareStats.hasUpgrade(upgrades, type)) {
                tooltip.add(I18n.format("drtech.drone.tooltip.upgrade_installed",
                        I18n.format("item.drtech.drone_upgrade_module." + type.getSerializedName() + ".name")));
            }
        }
        tooltip.add(I18n.format("drtech.drone.tooltip.controls"));
    }
}
