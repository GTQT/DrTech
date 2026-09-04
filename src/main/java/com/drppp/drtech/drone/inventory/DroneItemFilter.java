package com.drppp.drtech.drone.inventory;

import com.drppp.drtech.drone.filter.DroneFilterMode;
import com.drppp.drtech.drone.filter.DroneItemFilterSpec;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;

/** Immutable item-id/metadata filter produced by a visual value node. Empty item id means match all. */
public final class DroneItemFilter {

    public static final DroneItemFilter ANY = new DroneItemFilter(null, -1);

    private final ResourceLocation itemId;
    private final int metadata;
    private final DroneItemFilterSpec spec;

    public DroneItemFilter(@Nullable ResourceLocation itemId, int metadata) {
        this.itemId = itemId;
        this.metadata = metadata;
        this.spec = itemId == null ? DroneItemFilterSpec.ANY : new DroneItemFilterSpec(DroneFilterMode.WHITELIST,
                Collections.singletonList(new DroneItemFilterSpec.Rule(itemId, metadata, "", "", false, null)));
    }

    private DroneItemFilter(DroneItemFilterSpec spec) {
        this.spec = spec == null ? DroneItemFilterSpec.ANY : spec;
        DroneItemFilterSpec.Rule first = this.spec.getRules().isEmpty() ? null : this.spec.getRules().get(0);
        this.itemId = first == null ? null : first.getItemId();
        this.metadata = first == null ? -1 : first.getMetadata();
    }

    public static DroneItemFilter fromSpec(DroneItemFilterSpec spec) { return new DroneItemFilter(spec); }

    public static DroneItemFilter fromConfiguration(NBTTagCompound configuration) {
        if (configuration.hasKey("FilterSpec", 10)) {
            return new DroneItemFilter(DroneItemFilterSpec.readFromNbt(configuration.getCompoundTag("FilterSpec")));
        }
        String value = configuration.getString("Item");
        if (value.isEmpty()) return ANY;
        try {
            return new DroneItemFilter(new ResourceLocation(value),
                    configuration.hasKey("Meta") ? configuration.getInteger("Meta") : -1);
        } catch (IllegalArgumentException ignored) {
            return ANY;
        }
    }

    public boolean matches(ItemStack stack) {
        return spec.matches(stack);
    }

    @Nullable
    public ResourceLocation getItemId() {
        return itemId;
    }

    public int getMetadata() {
        return metadata;
    }

    public DroneItemFilterSpec getSpec() { return spec; }
}
