package com.drppp.drtech.common.drone.filter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DroneEntityFilterSpec {
    public static final int MAX_RULES = 64;
    private final DroneFilterMode mode;
    private final Set<ResourceLocation> entityIds;

    public DroneEntityFilterSpec(DroneFilterMode mode, List<ResourceLocation> entityIds) {
        this.mode = mode == null ? DroneFilterMode.WHITELIST : mode;
        LinkedHashSet<ResourceLocation> values = new LinkedHashSet<>();
        if (entityIds != null) {
            for (ResourceLocation id : entityIds) if (id != null && values.size() < MAX_RULES) values.add(id);
        }
        this.entityIds = Collections.unmodifiableSet(values);
    }

    public DroneFilterMode getMode() { return mode; }
    public Set<ResourceLocation> getEntityIds() { return entityIds; }

    public boolean matches(Entity entity) {
        if (entity == null) return false;
        if (entityIds.isEmpty()) return true;
        return mode.apply(entityIds.contains(EntityList.getKey(entity)));
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setString("Mode", mode.name());
        NBTTagList list = new NBTTagList();
        for (ResourceLocation id : entityIds) list.appendTag(new NBTTagString(id.toString()));
        root.setTag("Entities", list);
        return root;
    }

    public static DroneEntityFilterSpec readFromNbt(@Nullable NBTTagCompound root) {
        if (root == null) return new DroneEntityFilterSpec(DroneFilterMode.WHITELIST, Collections.emptyList());
        List<ResourceLocation> ids = new ArrayList<>();
        NBTTagList list = root.getTagList("Entities", 8);
        for (int i = 0; i < list.tagCount() && ids.size() < MAX_RULES; i++) {
            try {
                ids.add(new ResourceLocation(list.getStringTagAt(i)));
            } catch (RuntimeException ignored) {
                // Invalid or removed entity id is discarded without invalidating the whole program.
            }
        }
        return new DroneEntityFilterSpec(DroneFilterMode.fromName(root.getString("Mode")), ids);
    }
}
