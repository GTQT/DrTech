package com.drppp.drtech.api.ItemHandler;

import com.drppp.drtech.api.utils.GT_ApiaryUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 只接受工业蜂箱升级的容器。
 *
 * <p>除了"是不是升级"，它还负责升级之间的互斥规则：
 * <ul>
 *     <li>同种升级只能叠在同一个槽位里，且总量不能超过该升级的上限；</li>
 *     <li>加速类升级之间互斥，同一台机器只能装一件；</li>
 *     <li>{@code speed8upgraded} 与增产升级互斥，两者只能选一。</li>
 * </ul>
 *
 * <p>注意 {@link ItemStackHandler#insertItem} 不会调用本方法，
 * 这里的校验只对 GUI 槽位与部分自动化插入生效。
 */
public class OnlyUpgradeStackhandler extends ItemStackHandler {

    public OnlyUpgradeStackhandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!GT_ApiaryUpgrade.isUpgrade(stack)) {
            return false;
        }

        final int maxNumber = GT_ApiaryUpgrade.getUpgrade(stack).getMaxNumber();
        final int sameSlot = findSameUpgradeSlot(stack);
        if (sameSlot >= 0) {
            // 同种升级只认原来那个槽位，且加起来不能超过上限
            return sameSlot == slot && getStackInSlot(sameSlot).getCount() + stack.getCount() <= maxNumber;
        }

        return !hasSpeedUpgrade(stack)
                && !hasConflictingSpeedProduction(stack)
                && stack.getCount() <= maxNumber;
    }

    /** 找到已经装着同种升级的槽位，没有则返回 -1。 */
    private int findSameUpgradeSlot(ItemStack stack) {
        for (int i = 0; i < getSlots(); i++) {
            final ItemStack slotStack = getStackInSlot(i);
            if (!slotStack.isEmpty()
                    && slotStack.getItem() == stack.getItem()
                    && slotStack.getMetadata() == stack.getMetadata()) {
                return i;
            }
        }
        return -1;
    }

    /** 加速类升级互斥：已经有任意一件加速升级时，就不能再装另一件。 */
    private boolean hasSpeedUpgrade(ItemStack incoming) {
        if (!GT_ApiaryUpgrade.isSpeedUpgrade(incoming)) {
            return false;
        }
        for (int i = 0; i < getSlots(); i++) {
            if (GT_ApiaryUpgrade.isSpeedUpgrade(getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code speed8upgraded} 与增产升级互斥。
     *
     * @return 传入的是这两个之一、且另一个已经装在机器上时返回 true
     */
    private boolean hasConflictingSpeedProduction(ItemStack incoming) {
        final int meta = incoming.getMetadata();
        final int conflictingMeta;
        if (meta == GT_ApiaryUpgrade.META_SPEED_MAX) {
            conflictingMeta = GT_ApiaryUpgrade.META_PRODUCTION;
        } else if (meta == GT_ApiaryUpgrade.META_PRODUCTION) {
            conflictingMeta = GT_ApiaryUpgrade.META_SPEED_MAX;
        } else {
            return false;
        }

        for (int i = 0; i < getSlots(); i++) {
            final ItemStack slotStack = getStackInSlot(i);
            if (!slotStack.isEmpty() && slotStack.getMetadata() == conflictingMeta) {
                return true;
            }
        }
        return false;
    }
}
