package com.drppp.drtech.common.drone.filter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DroneFluidFilterSpec {
    public static final int MAX_RULES = 64;
    private final DroneFilterMode mode;
    private final Set<String> fluidNames;

    public DroneFluidFilterSpec(DroneFilterMode mode, List<String> fluidNames) {
        this.mode = mode == null ? DroneFilterMode.WHITELIST : mode;
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (fluidNames != null) {
            for (String name : fluidNames) {
                if (name != null && !name.isEmpty() && name.length() <= 128 && values.size() < MAX_RULES) values.add(name);
            }
        }
        this.fluidNames = Collections.unmodifiableSet(values);
    }

    public DroneFilterMode getMode() { return mode; }
    public Set<String> getFluidNames() { return fluidNames; }

    public boolean matches(@Nullable FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return false;
        if (fluidNames.isEmpty()) return true;
        return mode.apply(fluidNames.contains(stack.getFluid().getName()));
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setString("Mode", mode.name());
        NBTTagList list = new NBTTagList();
        for (String name : fluidNames) list.appendTag(new NBTTagString(name));
        root.setTag("Fluids", list);
        return root;
    }

    public static DroneFluidFilterSpec readFromNbt(@Nullable NBTTagCompound root) {
        if (root == null) return new DroneFluidFilterSpec(DroneFilterMode.WHITELIST, Collections.emptyList());
        NBTTagList list = root.getTagList("Fluids", 8);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < list.tagCount() && names.size() < MAX_RULES; i++) names.add(list.getStringTagAt(i));
        return new DroneFluidFilterSpec(DroneFilterMode.fromName(root.getString("Mode")), names);
    }
}
