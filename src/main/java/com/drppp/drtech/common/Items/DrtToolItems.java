package com.drppp.drtech.common.Items;

import com.drppp.drtech.Tags;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolOreDict;
import static gregtech.common.items.ToolItems.register;

public class DrtToolItems {

    public static IGTTool ONCE_WRENCH;
    public static IGTTool ONCE_HARD_HAMMER;
    public static IGTTool ONCE_WIRE_CUTTER;
    public static void init()
    {
        ONCE_WRENCH = register(ItemGTTool.Builder.of(Tags.MODID, "once_wrench")
                .oreDict(ToolOreDict.toolWrench)
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(1).baseDurability(0).durabilityMultiplier(0))
                .secondaryOreDicts("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH));
        ONCE_HARD_HAMMER = register(ItemGTTool.Builder.of(Tags.MODID, "once_hammer")
                .oreDict(ToolOreDict.toolWrench)
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(1).baseDurability(0).durabilityMultiplier(0))
                .secondaryOreDicts("craftingToolHardHammer")
                .toolClasses(ToolClasses.HARD_HAMMER));
        ONCE_WIRE_CUTTER = register(ItemGTTool.Builder.of(Tags.MODID, "once_wire_cutter")
                .oreDict(ToolOreDict.toolWrench)
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(1).baseDurability(0).durabilityMultiplier(0))
                .secondaryOreDicts("craftingToolWireCutter")
                .toolClasses(ToolClasses.WIRE_CUTTER));

    }

}
