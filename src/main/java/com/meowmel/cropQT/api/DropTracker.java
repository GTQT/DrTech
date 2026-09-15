package com.meowmel.cropQT.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 小数累积产出表。
 *
 * <p>工业农场每周期的产出是小数——比如「铜纤维草，0.37 个/周期」。不能每周期取整，
 * 否则 0.37 永远进到 0，什么都产不出来。这里把小数攒着，只输出<b>攒够 1 个</b>的部分，
 * 余数留到下一周期继续攒。
 *
 * <p>必须持久化：读档丢掉余数的话，玩家会看到产出莫名其妙地少一截。
 *
 * <p>按「物品 + 元数据 + NBT」判等，<b>不看数量</b>。
 * 不能用 {@link ItemStack#areItemStacksEqual}——它会连 stackSize 一起比（见 {@code isItemStackEqual}），
 * 于是「放不下退回来的 1 个」和「原本 2 个的原型」会被认成两种东西、各攒一份，
 * 同一个物品在表里碎成好几条。原型一律存成 1 个，免得数量再有机会掺和进来。
 *
 * <p>条目数等于这一株作物的掉落种类数，通常个位数，线性查找足够。
 */
public class DropTracker {

    private final List<Entry> entries = new ArrayList<>();

    private static final class Entry {
        final ItemStack prototype;
        double amount;

        Entry(ItemStack prototype, double amount) {
            this.prototype = prototype;
            this.amount = amount;
        }
    }

    /** 同一种东西：物品、元数据、NBT 都一致。数量不参与判定。 */
    private static boolean sameKind(@NotNull ItemStack a, @NotNull ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    /** 累加一个物品的期望产出。 */
    public void add(@NotNull ItemStack stack, double amount) {
        if (stack.isEmpty() || amount == 0.0d) {
            return;
        }
        for (Entry entry : entries) {
            if (sameKind(entry.prototype, stack)) {
                entry.amount += amount;
                return;
            }
        }
        ItemStack prototype = stack.copy();
        prototype.setCount(1);
        entries.add(new Entry(prototype, amount));
    }

    /**
     * 取出所有攒够整数的产出，余数留在表里。
     *
     * @return 可以输出的物品；没有攒够的返回空表
     */
    @NotNull
    public List<ItemStack> drainComplete() {
        List<ItemStack> out = new ArrayList<>();
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            int whole = (int) Math.floor(entry.amount);
            if (whole <= 0) {
                continue;
            }
            entry.amount -= whole;
            ItemStack stack = entry.prototype.copy();
            stack.setCount(whole);
            out.add(stack);
            if (entry.amount <= 0.0d) {
                it.remove();
            }
        }
        return out;
    }

    /**
     * 试算：如果现在取出，会得到什么（不改动状态）。
     *
     * <p>给「产物放不放得下」的判定用。
     */
    @NotNull
    public List<ItemStack> peekComplete() {
        List<ItemStack> out = new ArrayList<>();
        for (Entry entry : entries) {
            int whole = (int) Math.floor(entry.amount);
            if (whole > 0) {
                ItemStack stack = entry.prototype.copy();
                stack.setCount(whole);
                out.add(stack);
            }
        }
        return out;
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    // ==================== 存档 ====================

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagList list = new NBTTagList();
        for (Entry entry : entries) {
            NBTTagCompound tag = new NBTTagCompound();
            entry.prototype.writeToNBT(tag);
            tag.setDouble("cropqt_amount", entry.amount);
            list.appendTag(tag);
        }
        data.setTag("cropqt_drops", list);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        entries.clear();
        NBTTagList list = data.getTagList("cropqt_drops", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            ItemStack stack = new ItemStack(tag);
            if (!stack.isEmpty()) {
                entries.add(new Entry(stack, tag.getDouble("cropqt_amount")));
            }
        }
    }
}
