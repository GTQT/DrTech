package com.drppp.drtech.api.ItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 可以配置成"只出不进"的容器，用于机器的输出槽。
 *
 * <p>{@code allowInsert} 为 false 时 GUI 槽位与部分自动化插入都会被拒绝；
 * 但 {@link ItemStackHandler#insertItem} 本身不经过 {@link #isItemValid}，
 * 所以机器内部的产出仍然可以直接写进来。
 */
public class InOutItemStackHandler extends ItemStackHandler {

    private final boolean allowInsert;

    public InOutItemStackHandler(int size, boolean allowInsert) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.allowInsert = allowInsert;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.allowInsert;
    }
}
