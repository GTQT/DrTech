package com.drppp.drtech.drone.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase;
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

    public static final int VERSION = 2;
    public static final int MAX_RULES = 64;
    private static final int MAX_NBT_DEPTH = 8;
    private static final int MAX_NBT_FIELDS = 32;
    private static final int MAX_NBT_LIST = 32;
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
        private final boolean matchNbtPartially;
        private final NBTTagCompound nbt;
        private final int minDurability;
        private final int maxDurability;
        private final int minCount;
        private final int maxCount;

        public Rule(@Nullable ResourceLocation itemId, int metadata, @Nullable String oreDictionary,
                @Nullable String namespace, boolean matchNbt, @Nullable NBTTagCompound nbt) {
            this(itemId, metadata, oreDictionary, namespace, matchNbt, nbt,
                    false, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        }

        public Rule(@Nullable ResourceLocation itemId, int metadata, @Nullable String oreDictionary,
                @Nullable String namespace, boolean matchNbt, boolean matchNbtPartially,
                @Nullable NBTTagCompound nbt, int minDurability, int maxDurability,
                int minCount, int maxCount) {
            this(itemId, metadata, oreDictionary, namespace, matchNbt, nbt, matchNbtPartially,
                    minDurability, maxDurability, minCount, maxCount);
        }

        /**
         * Creates a rule with optional remaining-durability and stack-count bounds.
         * A full integer range disables the corresponding constraint.
         */
        public Rule(@Nullable ResourceLocation itemId, int metadata, @Nullable String oreDictionary,
                @Nullable String namespace, boolean matchNbt, @Nullable NBTTagCompound nbt,
                int minDurability, int maxDurability, int minCount, int maxCount) {
            this(itemId, metadata, oreDictionary, namespace, matchNbt, nbt, false,
                    minDurability, maxDurability, minCount, maxCount);
        }

        private Rule(@Nullable ResourceLocation itemId, int metadata, @Nullable String oreDictionary,
                @Nullable String namespace, boolean matchNbt, @Nullable NBTTagCompound nbt,
                boolean matchNbtPartially, int minDurability, int maxDurability,
                int minCount, int maxCount) {
            this.itemId = itemId;
            this.metadata = metadata;
            this.oreDictionary = bounded(oreDictionary, 128);
            this.namespace = bounded(namespace, 64);
            this.matchNbt = matchNbt;
            this.matchNbtPartially = matchNbt && matchNbtPartially;
            this.nbt = nbt == null ? null : nbt.copy();
            this.minDurability = boundedRangeMin(minDurability, maxDurability);
            this.maxDurability = boundedRangeMax(minDurability, maxDurability);
            this.minCount = Math.max(0, Math.min(Integer.MAX_VALUE, minCount));
            this.maxCount = Math.max(this.minCount, Math.min(Integer.MAX_VALUE, maxCount));
        }

        public ResourceLocation getItemId() { return itemId; }
        public int getMetadata() { return metadata; }
        public String getOreDictionary() { return oreDictionary; }
        public String getNamespace() { return namespace; }
        public boolean isMatchNbt() { return matchNbt; }
        public boolean isMatchNbtPartially() { return matchNbtPartially; }
        @Nullable public NBTTagCompound getNbt() { return nbt == null ? null : nbt.copy(); }
        public int getMinDurability() { return minDurability; }
        public int getMaxDurability() { return maxDurability; }
        public int getMinCount() { return minCount; }
        public int getMaxCount() { return maxCount; }

        public boolean matches(ItemStack stack) {
            ResourceLocation stackId = stack.getItem().getRegistryName();
            if (itemId != null && !itemId.equals(stackId)) return false;
            if (metadata >= 0 && metadata != stack.getMetadata()) return false;
            if (!namespace.isEmpty() && (stackId == null || !namespace.equals(stackId.getNamespace()))) return false;
            int count = stack.getCount();
            if (count < minCount || count > maxCount) return false;
            if (minDurability > 0 || maxDurability < Integer.MAX_VALUE) {
                int durability = stack.getMaxDamage() <= 0 ? 0
                        : Math.max(0, stack.getMaxDamage() - stack.getItemDamage());
                if (durability < minDurability || durability > maxDurability) return false;
            }
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
            if (!matchNbt) return true;
            return matchNbtPartially ? isNbtSubset(nbt, stack.getTagCompound())
                    : Objects.equals(nbt, stack.getTagCompound());
        }

        private static boolean isNbtSubset(@Nullable NBTTagCompound expected, @Nullable NBTTagCompound actual) {
            if (expected == null) return actual == null;
            if (actual == null) return false;
            return isNbtSubset(expected, actual, 0);
        }

        private static boolean isNbtSubset(NBTBase expected, NBTBase actual, int depth) {
            if (depth > MAX_NBT_DEPTH || expected == null || actual == null || expected.getId() != actual.getId()) return false;
            if (expected instanceof NBTTagCompound) {
                NBTTagCompound e = (NBTTagCompound) expected;
                NBTTagCompound a = (NBTTagCompound) actual;
                if (e.getKeySet().size() > MAX_NBT_FIELDS) return false;
                for (String key : e.getKeySet()) {
                    if (!a.hasKey(key)) return false;
                    if (!isNbtSubset(e.getTag(key), a.getTag(key), depth + 1)) return false;
                }
                return true;
            }
            if (expected instanceof NBTTagList) {
                NBTTagList e = (NBTTagList) expected;
                NBTTagList a = (NBTTagList) actual;
                if (e.tagCount() > MAX_NBT_LIST || e.tagCount() > a.tagCount()) return false;
                for (int i = 0; i < e.tagCount(); i++) {
                    if (!isNbtSubset(e.get(i), a.get(i), depth + 1)) return false;
                }
                return true;
            }
            return expected.equals(actual);
        }

        private NBTTagCompound writeToNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            if (itemId != null) tag.setString("Item", itemId.toString());
            tag.setInteger("Meta", metadata);
            if (!oreDictionary.isEmpty()) tag.setString("Ore", oreDictionary);
            if (!namespace.isEmpty()) tag.setString("Namespace", namespace);
            tag.setBoolean("MatchNbt", matchNbt);
            tag.setBoolean("PartialNbt", matchNbtPartially);
            if (nbt != null) tag.setTag("Nbt", nbt.copy());
            if (minDurability > 0) tag.setInteger("MinDurability", minDurability);
            if (maxDurability < Integer.MAX_VALUE) tag.setInteger("MaxDurability", maxDurability);
            if (minCount > 0) tag.setInteger("MinCount", minCount);
            if (maxCount < Integer.MAX_VALUE) tag.setInteger("MaxCount", maxCount);
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
                    tag.getBoolean("PartialNbt"),
                    tag.hasKey("Nbt", 10) ? tag.getCompoundTag("Nbt") : null,
                    tag.hasKey("MinDurability", 3) ? tag.getInteger("MinDurability") : 0,
                    tag.hasKey("MaxDurability", 3) ? tag.getInteger("MaxDurability") : Integer.MAX_VALUE,
                    tag.hasKey("MinCount", 3) ? tag.getInteger("MinCount") : 0,
                    tag.hasKey("MaxCount", 3) ? tag.getInteger("MaxCount") : Integer.MAX_VALUE);
        }

        private static int boundedRangeMin(int min, int max) {
            return Math.max(0, Math.min(Integer.MAX_VALUE, Math.min(min, max)));
        }

        private static int boundedRangeMax(int min, int max) {
            return Math.max(boundedRangeMin(min, max), Math.min(Integer.MAX_VALUE, Math.max(min, max)));
        }

        private static String bounded(@Nullable String value, int maxLength) {
            if (value == null) return "";
            return value.length() <= maxLength ? value : value.substring(0, maxLength);
        }
    }
}
