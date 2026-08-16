package com.drppp.drtech.common.drone.hardware;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/** Stable ResourceLocation/level persistence with transparent support for the legacy slot NBT. */
public final class DroneUpgradeDataCodec {

    public static final int CURRENT_VERSION = 2;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;

    private DroneUpgradeDataCodec() {}

    public static NBTTagCompound write(IItemHandler upgrades) {
        Map<DroneUpgradeType, Integer> levels = new EnumMap<>(DroneUpgradeType.class);
        if (upgrades != null) {
            for (int slot = 0; slot < upgrades.getSlots(); slot++) {
                DroneUpgradeType type = ItemDroneUpgradeModule.getType(upgrades.getStackInSlot(slot));
                if (type != null) levels.put(type, 1);
            }
        }
        return writeLevels(levels);
    }

    public static NBTTagCompound migrate(@Nullable NBTTagCompound source) {
        if (source == null) return writeLevels(new EnumMap<>(DroneUpgradeType.class));
        Map<DroneUpgradeType, Integer> levels = source.getInteger("Version") == CURRENT_VERSION
                && source.hasKey("Entries", TAG_LIST) ? readStableLevels(source) : readLegacyLevels(source);
        return writeLevels(levels);
    }

    public static void readInto(@Nullable NBTTagCompound source, ItemStackHandler target) {
        if (target == null) return;
        for (int slot = 0; slot < target.getSlots(); slot++) target.setStackInSlot(slot, ItemStack.EMPTY);
        Map<DroneUpgradeType, Integer> levels = readStableLevels(migrate(source));
        for (Map.Entry<DroneUpgradeType, Integer> entry : levels.entrySet()) {
            DroneUpgradeType type = entry.getKey();
            int slot = type.getMetadata();
            if (slot < target.getSlots() && entry.getValue() > 0) {
                target.setStackInSlot(slot, new ItemStack(ItemsInit.DRONE_UPGRADE_MODULE, 1, type.getMetadata()));
            }
        }
    }

    public static int getLevel(@Nullable NBTTagCompound source, DroneUpgradeType type) {
        Integer level = readStableLevels(migrate(source)).get(type);
        return level == null ? 0 : level;
    }

    private static NBTTagCompound writeLevels(Map<DroneUpgradeType, Integer> levels) {
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("Version", CURRENT_VERSION);
        NBTTagList entries = new NBTTagList();
        for (DroneUpgradeType type : DroneUpgradeType.values()) {
            int level = Math.max(0, Math.min(1, levels.getOrDefault(type, 0)));
            if (level <= 0) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Id", type.getId().toString());
            entry.setInteger("Level", level);
            entries.appendTag(entry);
        }
        result.setTag("Entries", entries);
        return result;
    }

    private static Map<DroneUpgradeType, Integer> readStableLevels(NBTTagCompound source) {
        Map<DroneUpgradeType, Integer> levels = new EnumMap<>(DroneUpgradeType.class);
        NBTTagList entries = source.getTagList("Entries", TAG_COMPOUND);
        for (int index = 0; index < entries.tagCount(); index++) {
            NBTTagCompound entry = entries.getCompoundTagAt(index);
            DroneUpgradeType type;
            try {
                type = DroneUpgradeType.fromId(new ResourceLocation(entry.getString("Id")));
            } catch (RuntimeException ignored) {
                type = null;
            }
            if (type != null) levels.put(type, Math.max(0, Math.min(1, entry.getInteger("Level"))));
        }
        return levels;
    }

    private static Map<DroneUpgradeType, Integer> readLegacyLevels(NBTTagCompound source) {
        Map<DroneUpgradeType, Integer> levels = new EnumMap<>(DroneUpgradeType.class);
        NBTTagList items = source.getTagList("Items", TAG_COMPOUND);
        for (int index = 0; index < items.tagCount(); index++) {
            NBTTagCompound item = items.getCompoundTagAt(index);
            boolean hasMetadata = item.hasKey("Damage", 99);
            boolean hasSlot = item.hasKey("Slot", 99);
            int metadata = hasMetadata ? item.getInteger("Damage") : -1;
            int slot = hasSlot ? item.getInteger("Slot") & 255 : -1;
            DroneUpgradeType type = hasMetadata && metadata >= 0 && metadata < DroneUpgradeType.values().length
                    ? DroneUpgradeType.fromMetadata(metadata)
                    : hasSlot && slot >= 0 && slot < DroneUpgradeType.values().length
                            ? DroneUpgradeType.fromMetadata(slot) : null;
            if (type != null) levels.put(type, 1);
        }
        return levels;
    }
}
