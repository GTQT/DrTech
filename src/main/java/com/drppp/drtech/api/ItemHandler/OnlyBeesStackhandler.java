package com.drppp.drtech.api.ItemHandler;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 只接受蜜蜂的容器，用于工业蜂箱的蜂后槽与雄蜂槽。
 *
 * <p>两个槽位各管各的：0 号只收公主与蜂后，1 号只收雄蜂。
 */
public class OnlyBeesStackhandler extends ItemStackHandler {

    /** 蜂后 / 公主槽位。 */
    private static final int SLOT_QUEEN = 0;
    /** 雄蜂槽位。 */
    private static final int SLOT_DRONE = 1;

    public OnlyBeesStackhandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        final EnumBeeType type = BeeManager.beeRoot.getType(stack);
        if (slot == SLOT_QUEEN) {
            return type == EnumBeeType.PRINCESS || type == EnumBeeType.QUEEN;
        }
        if (slot == SLOT_DRONE) {
            return type == EnumBeeType.DRONE;
        }
        return false;
    }
}
