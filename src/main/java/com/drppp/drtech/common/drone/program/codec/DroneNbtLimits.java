package com.drppp.drtech.common.drone.program.codec;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nullable;

/** Recursive limits for untrusted node configuration NBT received from cards or editor packets. */
public final class DroneNbtLimits {
    public static final int MAX_DEPTH = 8;
    public static final int MAX_TOTAL_TAGS = 2_048;
    public static final int MAX_COMPOUND_KEYS = 64;
    public static final int MAX_LIST_LENGTH = 64;
    public static final int MAX_STRING_LENGTH = 512;
    public static final int MAX_BYTE_ARRAY_LENGTH = 4_096;
    public static final int MAX_INT_ARRAY_LENGTH = 1_024;

    private DroneNbtLimits() {}

    public static boolean isWithinLimits(@Nullable NBTBase root) { return violation(root) == null; }

    @Nullable
    public static String violation(@Nullable NBTBase root) {
        if (root == null) return null;
        Counter counter = new Counter();
        return visit(root, 0, counter);
    }

    @Nullable
    private static String visit(NBTBase tag, int depth, Counter counter) {
        if (depth > MAX_DEPTH) return "nbt_too_deep";
        if (++counter.total > MAX_TOTAL_TAGS) return "nbt_too_many_tags";
        if (tag instanceof NBTTagCompound compound) {
            if (compound.getKeySet().size() > MAX_COMPOUND_KEYS) return "nbt_too_many_keys";
            for (String key : compound.getKeySet()) {
                if (key.length() > 64) return "nbt_key_too_long";
                String violation = visit(compound.getTag(key), depth + 1, counter);
                if (violation != null) return violation;
            }
        } else if (tag instanceof NBTTagList list) {
            if (list.tagCount() > MAX_LIST_LENGTH) return "nbt_list_too_long";
            for (int index = 0; index < list.tagCount(); index++) {
                String violation = visit(list.get(index), depth + 1, counter);
                if (violation != null) return violation;
            }
        } else if (tag instanceof NBTTagString string && string.getString().length() > MAX_STRING_LENGTH) {
            return "nbt_string_too_long";
        } else if (tag instanceof NBTTagByteArray bytes && bytes.getByteArray().length > MAX_BYTE_ARRAY_LENGTH) {
            return "nbt_byte_array_too_long";
        } else if (tag instanceof NBTTagIntArray integers && integers.getIntArray().length > MAX_INT_ARRAY_LENGTH) {
            return "nbt_int_array_too_long";
        }
        return null;
    }

    private static final class Counter { private int total; }
}
