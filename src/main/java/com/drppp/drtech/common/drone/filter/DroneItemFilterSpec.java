package com.drppp.drtech.common.drone.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Bounded multi-rule item filter used by future selectors and transfer requests. */
public final class DroneItemFilterSpec {

    public static final int VERSION = 1;
    public static final int MAX_RULES = 64;
    public static final DroneItemFilterSpec ANY = new DroneItemFilterSpec(DroneFilterMode.WHITELIST,
            Collections.emptyList());

    private final DroneFilterMode mode;
    private final List<Rule> rules;

    public DroneItemFilterSpec(DroneFilterMode mode, List<Rule> rules) {
        this.mode = mode == null ? DroneFilterMode.WHITELIST : mode;
        if (rules == null || rules.isEmpty()) this.rules = Collections.emptyList();
        else this.rules = Collections.unmodifiableList(new ArrayList<>(rules.subList(0, Math.min(MAX_RULES, rules.size()))));
    }

    public DroneFilterMode getMode() { return mode; }
    public List<Rule> getRules() { return rules; }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (rules.isEmpty()) return true;
        boolean matched = rules.stream().anyMatch(rule -> rule.matches(stack));
        return mode.apply(matched);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setInteger("Version", VERSION);
        root.setString("Mode", mode.name());
        NBTTagList list = new NBTTagList();
        for (Rule rule : rules) list.appendTag(rule.writeToNbt());
        root.setTag("Rules", list);
        return root;
    }

    public static DroneItemFilterSpec readFromNbt(@Nullable NBTTagCompound root) {
        if (root == null) return ANY;
        List<Rule> rules = new ArrayList<>();
        NBTTagList list = root.getTagList("Rules", 10);
        for (int index = 0; index < list.tagCount() && rules.size() < MAX_RULES; index++) {
            Rule rule = Rule.readFromNbt(list.getCompoundTagAt(index));
            if (rule != null) rules.add(rule);
        }
        return new DroneItemFilterSpec(DroneFilterMode.fromName(root.getString("Mode")), rules);
    }

    public static final class Rule {
        private final ResourceLocation itemId;
        private final int metadata;
        private final String oreDictionary;
        private final String namespace;
        private final boolean matchNbt;
        private final NBTTagCompound nbt;

        public Rule(@Nullable ResourceLocation itemId, int metadata, @Nullable String oreDictionary,
                @Nullable String namespace, boolean matchNbt, @Nullable NBTTagCompound nbt) {
            this.itemId = itemId;
            this.metadata = metadata;
            this.oreDictionary = bounded(oreDictionary, 128);
            this.namespace = bounded(namespace, 64);
            this.matchNbt = matchNbt;
            this.nbt = nbt == null ? null : nbt.copy();
        }

        public ResourceLocation getItemId() { return itemId; }
        public int getMetadata() { return metadata; }
        public String getOreDictionary() { return oreDictionary; }
        public String getNamespace() { return namespace; }
        public boolean isMatchNbt() { return matchNbt; }
        @Nullable public NBTTagCompound getNbt() { return nbt == null ? null : nbt.copy(); }

        public boolean matches(ItemStack stack) {
            ResourceLocation stackId = stack.getItem().getRegistryName();
            if (itemId != null && !itemId.equals(stackId)) return false;
            if (metadata >= 0 && metadata != stack.getMetadata()) return false;
            if (!namespace.isEmpty() && (stackId == null || !namespace.equals(stackId.getNamespace()))) return false;
            if (!oreDictionary.isEmpty()) {
                boolean oreMatched = false;
                for (int oreId : OreDictionary.getOreIDs(stack)) {
                    if (oreDictionary.equals(OreDictionary.getOreName(oreId))) {
                        oreMatched = true;
                        break;
                    }
                }
                if (!oreMatched) return false;
            }
            return !matchNbt || Objects.equals(nbt, stack.getTagCompound());
        }

        private NBTTagCompound writeToNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            if (itemId != null) tag.setString("Item", itemId.toString());
            tag.setInteger("Meta", metadata);
            if (!oreDictionary.isEmpty()) tag.setString("Ore", oreDictionary);
            if (!namespace.isEmpty()) tag.setString("Namespace", namespace);
            tag.setBoolean("MatchNbt", matchNbt);
            if (nbt != null) tag.setTag("Nbt", nbt.copy());
            return tag;
        }

        @Nullable
        private static Rule readFromNbt(NBTTagCompound tag) {
            ResourceLocation itemId = null;
            if (tag.hasKey("Item", 8) && !tag.getString("Item").isEmpty()) {
                try {
                    itemId = new ResourceLocation(tag.getString("Item"));
                } catch (RuntimeException ignored) {
                    return null;
                }
            }
            int metadata = tag.hasKey("Meta", 99) ? tag.getInteger("Meta") : -1;
            String ore = bounded(tag.getString("Ore"), 128);
            String namespace = bounded(tag.getString("Namespace"), 64);
            if (itemId == null && ore.isEmpty() && namespace.isEmpty()) return null;
            return new Rule(itemId, metadata, ore, namespace, tag.getBoolean("MatchNbt"),
                    tag.hasKey("Nbt", 10) ? tag.getCompoundTag("Nbt") : null);
        }

        private static String bounded(@Nullable String value, int maxLength) {
            if (value == null) return "";
            return value.length() <= maxLength ? value : value.substring(0, maxLength);
        }
    }
}
