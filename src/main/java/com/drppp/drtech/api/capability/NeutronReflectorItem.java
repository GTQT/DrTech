package com.drppp.drtech.api.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeutronReflectorItem implements INeutronReflector, ICapabilityProvider {
    protected final ItemStack itemStack;
    private int maxDurability;

    public NeutronReflectorItem() {
        this.itemStack = ItemStack.EMPTY;
    }
    public NeutronReflectorItem(ItemStack itemStack, int maxDurability) {
        this.itemStack = itemStack;
        this.maxDurability = maxDurability;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability==null)
            return false;
        return capability==DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR? (T) this : null;
    }

    @Override
    public int getDurability() {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null)
            return this.getMaxDurability();
        if(tagCompound.hasKey("Durability"))
            return tagCompound.getInteger("Durability");
        return this.getMaxDurability();
    }

    @Override
    public int getMaxDurability() {
        return this.maxDurability;
    }

    @Override
    public void setDurability(int now) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setInteger("Durability", now);
    }
}
