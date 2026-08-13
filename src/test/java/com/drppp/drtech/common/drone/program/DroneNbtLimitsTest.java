package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.codec.DroneNbtLimits;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DroneNbtLimitsTest {

    @Test
    void acceptsNormalNestedFilterConfiguration() {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound filter = new NBTTagCompound();
        NBTTagList rules = new NBTTagList();
        NBTTagCompound rule = new NBTTagCompound();
        rule.setString("Item", "minecraft:stone");
        rules.appendTag(rule);
        filter.setTag("Rules", rules);
        root.setTag("FilterSpec", filter);

        assertNull(DroneNbtLimits.violation(root));
    }

    @Test
    void rejectsDeepOversizedOrLongUntrustedPayloads() {
        NBTTagCompound deep = new NBTTagCompound();
        NBTTagCompound cursor = deep;
        for (int i = 0; i < DroneNbtLimits.MAX_DEPTH + 2; i++) {
            NBTTagCompound child = new NBTTagCompound();
            cursor.setTag("Child", child);
            cursor = child;
        }
        assertEquals("nbt_too_deep", DroneNbtLimits.violation(deep));

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < DroneNbtLimits.MAX_LIST_LENGTH + 1; i++) list.appendTag(new NBTTagString("x"));
        assertEquals("nbt_list_too_long", DroneNbtLimits.violation(list));

        assertEquals("nbt_string_too_long", DroneNbtLimits.violation(
                new NBTTagString(new String(new char[DroneNbtLimits.MAX_STRING_LENGTH + 1]).replace('\0', 'x'))));
    }
}
