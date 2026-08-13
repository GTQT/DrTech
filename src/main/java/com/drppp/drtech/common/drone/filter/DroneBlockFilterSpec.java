package com.drppp.drtech.common.drone.filter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;

/** Resource-id/metadata block filter foundation; BlockState predicates can be added without changing node ports. */
public final class DroneBlockFilterSpec {
    public static final int MAX_RULES = 64;
    public static final int MAX_STATE_PROPERTIES = 32;
    public static final DroneBlockFilterSpec ANY = new DroneBlockFilterSpec(DroneFilterMode.WHITELIST,
            Collections.emptyList());
    private final DroneFilterMode mode;
    private final List<Rule> rules;

    public DroneBlockFilterSpec(DroneFilterMode mode, List<Rule> rules) {
        this.mode = mode == null ? DroneFilterMode.WHITELIST : mode;
        this.rules = rules == null ? Collections.emptyList() : Collections.unmodifiableList(
                new ArrayList<>(rules.subList(0, Math.min(MAX_RULES, rules.size()))));
    }

    public DroneFilterMode getMode() { return mode; }
    public List<Rule> getRules() { return rules; }

    public boolean matches(IBlockState state) {
        if (state == null) return false;
        if (rules.isEmpty()) return true;
        boolean matched = rules.stream().anyMatch(rule -> rule.matches(state));
        return mode.apply(matched);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setString("Mode", mode.name());
        NBTTagList list = new NBTTagList();
        for (Rule rule : rules) list.appendTag(rule.writeToNbt());
        root.setTag("Rules", list);
        return root;
    }

    public static DroneBlockFilterSpec readFromNbt(@Nullable NBTTagCompound root) {
        if (root == null) return new DroneBlockFilterSpec(DroneFilterMode.WHITELIST, Collections.emptyList());
        List<Rule> rules = new ArrayList<>();
        NBTTagList list = root.getTagList("Rules", 10);
        for (int i = 0; i < list.tagCount() && rules.size() < MAX_RULES; i++) {
            Rule rule = Rule.readFromNbt(list.getCompoundTagAt(i));
            if (rule != null) rules.add(rule);
        }
        return new DroneBlockFilterSpec(DroneFilterMode.fromName(root.getString("Mode")), rules);
    }

    public static final class Rule {
        private final ResourceLocation blockId;
        private final int metadata;
        private final Map<String, String> stateProperties;

        public Rule(ResourceLocation blockId, int metadata) {
            this(blockId, metadata, Collections.emptyMap());
        }

        public Rule(ResourceLocation blockId, int metadata, @Nullable Map<String, String> stateProperties) {
            this.blockId = blockId;
            this.metadata = metadata;
            LinkedHashMap<String, String> bounded = new LinkedHashMap<>();
            if (stateProperties != null) {
                stateProperties.entrySet().stream()
                        .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .limit(MAX_STATE_PROPERTIES)
                        .forEach(entry -> bounded.put(limit(entry.getKey(), 64), limit(entry.getValue(), 64)));
            }
            this.stateProperties = Collections.unmodifiableMap(bounded);
        }

        public ResourceLocation getBlockId() { return blockId; }
        public int getMetadata() { return metadata; }
        public Map<String, String> getStateProperties() { return stateProperties; }

        private boolean matches(IBlockState state) {
            ResourceLocation id = Block.REGISTRY.getNameForObject(state.getBlock());
            if (!blockId.equals(id)) return false;
            if (metadata >= 0) {
                try {
                    if (state.getBlock().getMetaFromState(state) != metadata) return false;
                } catch (RuntimeException ignored) {
                    return false;
                }
            }
            for (Map.Entry<String, String> expected : stateProperties.entrySet()) {
                boolean found = false;
                for (Map.Entry<IProperty<?>, Comparable<?>> actual : state.getProperties().entrySet()) {
                    if (expected.getKey().equals(actual.getKey().getName())
                            && expected.getValue().equals(propertyValueName(actual.getKey(), actual.getValue()))) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        }

        private NBTTagCompound writeToNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Block", blockId.toString());
            tag.setInteger("Meta", metadata);
            if (!stateProperties.isEmpty()) {
                NBTTagCompound properties = new NBTTagCompound();
                for (Map.Entry<String, String> entry : stateProperties.entrySet()) {
                    properties.setString(entry.getKey(), entry.getValue());
                }
                tag.setTag("State", properties);
            }
            return tag;
        }

        @Nullable
        private static Rule readFromNbt(NBTTagCompound tag) {
            try {
                Map<String, String> properties = new LinkedHashMap<>();
                if (tag.hasKey("State", 10)) {
                    NBTTagCompound state = tag.getCompoundTag("State");
                    state.getKeySet().stream().sorted().limit(MAX_STATE_PROPERTIES)
                            .forEach(key -> properties.put(limit(key, 64), limit(state.getString(key), 64)));
                }
                return new Rule(new ResourceLocation(tag.getString("Block")),
                        tag.hasKey("Meta", 99) ? tag.getInteger("Meta") : -1, properties);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static String propertyValueName(IProperty property, Comparable value) {
            return property.getName(value);
        }

        private static String limit(String value, int maxLength) {
            return value.length() <= maxLength ? value : value.substring(0, maxLength);
        }
    }
}
