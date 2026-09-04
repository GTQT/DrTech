package com.drppp.drtech.drone.filter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

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
        return matches(state, null, null);
    }

    public boolean matches(World world, BlockPos pos) {
        if (world == null || pos == null) return false;
        return matches(world.getBlockState(pos), world, pos);
    }

    private boolean matches(IBlockState state, @Nullable World world, @Nullable BlockPos pos) {
        if (state == null) return false;
        if (rules.isEmpty()) return true;
        boolean matched = rules.stream().anyMatch(rule -> rule.matches(state, world, pos));
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
        private final String namespace;
        private final Boolean tileEntity;
        private final Boolean replaceable;
        private final String oreDictionary;
        private final String category;
        private final int metadata;
        private final Map<String, String> stateProperties;

        public Rule(ResourceLocation blockId, int metadata) {
            this(blockId, metadata, Collections.emptyMap());
        }

        public Rule(ResourceLocation blockId, int metadata, @Nullable Map<String, String> stateProperties) {
            this(blockId, "", metadata, stateProperties);
        }

        public Rule(@Nullable ResourceLocation blockId, @Nullable String namespace, int metadata,
                @Nullable Map<String, String> stateProperties) {
            this(blockId, namespace, metadata, stateProperties, null);
        }

        public Rule(@Nullable ResourceLocation blockId, @Nullable String namespace, int metadata,
                @Nullable Map<String, String> stateProperties, @Nullable Boolean tileEntity) {
            this(blockId, namespace, metadata, stateProperties, tileEntity, null);
        }

        public Rule(@Nullable ResourceLocation blockId, @Nullable String namespace, int metadata,
                @Nullable Map<String, String> stateProperties, @Nullable Boolean tileEntity,
                @Nullable Boolean replaceable) {
            this(blockId, namespace, metadata, stateProperties, tileEntity, replaceable, "");
        }

        public Rule(@Nullable ResourceLocation blockId, @Nullable String namespace, int metadata,
                @Nullable Map<String, String> stateProperties, @Nullable Boolean tileEntity,
                @Nullable Boolean replaceable, @Nullable String oreDictionary) {
            this(blockId, namespace, metadata, stateProperties, tileEntity, replaceable, oreDictionary, "");
        }

        public Rule(@Nullable ResourceLocation blockId, @Nullable String namespace, int metadata,
                @Nullable Map<String, String> stateProperties, @Nullable Boolean tileEntity,
                @Nullable Boolean replaceable, @Nullable String oreDictionary, @Nullable String category) {
            this.blockId = blockId;
            this.namespace = limit(namespace == null ? "" : namespace, 64);
            this.tileEntity = tileEntity;
            this.replaceable = replaceable;
            this.oreDictionary = limit(oreDictionary == null ? "" : oreDictionary, 128);
            this.category = normalizeCategory(category);
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
        public String getNamespace() { return namespace; }
        @Nullable public Boolean getTileEntityRequirement() { return tileEntity; }
        @Nullable public Boolean getReplaceableRequirement() { return replaceable; }
        public String getOreDictionary() { return oreDictionary; }
        public String getCategory() { return category; }
        public int getMetadata() { return metadata; }
        public Map<String, String> getStateProperties() { return stateProperties; }

        private boolean matches(IBlockState state, @Nullable World world, @Nullable BlockPos pos) {
            ResourceLocation id = Block.REGISTRY.getNameForObject(state.getBlock());
            if (blockId != null && !blockId.equals(id)) return false;
            if (!namespace.isEmpty() && (id == null || !namespace.equals(id.getNamespace()))) return false;
            if (tileEntity != null && state.getBlock().hasTileEntity(state) != tileEntity) return false;
            if (replaceable != null) {
                if (world == null || pos == null || state.getBlock().isReplaceable(world, pos) != replaceable) return false;
            }
            if (!oreDictionary.isEmpty() || !category.isEmpty()) {
                Item item = Item.getItemFromBlock(state.getBlock());
                if (item == null) return false;
                int meta;
                try {
                    meta = state.getBlock().getMetaFromState(state);
                } catch (RuntimeException ignored) {
                    return false;
                }
                boolean oreMatched = oreDictionary.isEmpty();
                boolean categoryMatched = category.isEmpty();
                for (int oreId : OreDictionary.getOreIDs(new ItemStack(item, 1, meta))) {
                    String oreName = OreDictionary.getOreName(oreId);
                    oreMatched |= oreDictionary.equals(oreName);
                    categoryMatched |= matchesCategory(category, oreName);
                }
                if (!oreMatched || !categoryMatched) return false;
            }
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
            if (blockId != null) tag.setString("Block", blockId.toString());
            if (!namespace.isEmpty()) tag.setString("Namespace", namespace);
            if (tileEntity != null) tag.setBoolean("TileEntity", tileEntity);
            if (tileEntity != null) tag.setBoolean("TileEntitySet", true);
            if (replaceable != null) tag.setBoolean("Replaceable", replaceable);
            if (replaceable != null) tag.setBoolean("ReplaceableSet", true);
            if (!oreDictionary.isEmpty()) tag.setString("Ore", oreDictionary);
            if (!category.isEmpty()) tag.setString("Category", category);
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
                ResourceLocation blockId = tag.hasKey("Block", 8) && !tag.getString("Block").isEmpty()
                        ? new ResourceLocation(tag.getString("Block")) : null;
                String namespace = limit(tag.getString("Namespace"), 64);
                String ore = limit(tag.getString("Ore"), 128);
                String category = normalizeCategory(tag.getString("Category"));
                if (blockId == null && namespace.isEmpty() && ore.isEmpty() && category.isEmpty()) return null;
                return new Rule(blockId, namespace,
                        tag.hasKey("Meta", 99) ? tag.getInteger("Meta") : -1, properties,
                        tag.getBoolean("TileEntitySet") ? tag.getBoolean("TileEntity") : null,
                        tag.getBoolean("ReplaceableSet") ? tag.getBoolean("Replaceable") : null,
                        ore, category);
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

        private static String normalizeCategory(@Nullable String value) {
            if ("ORE".equals(value) || "WOOD".equals(value) || "CROP".equals(value)) return value;
            return "";
        }

        private static boolean matchesCategory(String category, String oreName) {
            String name = oreName == null ? "" : oreName.toLowerCase(java.util.Locale.ROOT);
            if ("ORE".equals(category)) return name.startsWith("ore");
            if ("WOOD".equals(category)) return name.startsWith("log") || name.startsWith("plank")
                    || name.startsWith("wood") || name.startsWith("tree");
            return "CROP".equals(category) && (name.startsWith("crop") || name.startsWith("seed")
                    || name.startsWith("listallseed"));
        }
    }
}
