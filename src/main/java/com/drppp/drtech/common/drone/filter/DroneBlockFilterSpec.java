package com.drppp.drtech.common.drone.filter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resource-id/metadata block filter foundation; BlockState predicates can be added without changing node ports. */
public final class DroneBlockFilterSpec {
    public static final int MAX_RULES = 64;
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

        public Rule(ResourceLocation blockId, int metadata) {
            this.blockId = blockId;
            this.metadata = metadata;
        }

        public ResourceLocation getBlockId() { return blockId; }
        public int getMetadata() { return metadata; }

        private boolean matches(IBlockState state) {
            ResourceLocation id = Block.REGISTRY.getNameForObject(state.getBlock());
            if (!blockId.equals(id)) return false;
            if (metadata < 0) return true;
            try {
                return state.getBlock().getMetaFromState(state) == metadata;
            } catch (RuntimeException ignored) {
                return false;
            }
        }

        private NBTTagCompound writeToNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Block", blockId.toString());
            tag.setInteger("Meta", metadata);
            return tag;
        }

        @Nullable
        private static Rule readFromNbt(NBTTagCompound tag) {
            try {
                return new Rule(new ResourceLocation(tag.getString("Block")),
                        tag.hasKey("Meta", 99) ? tag.getInteger("Meta") : -1);
            } catch (RuntimeException ignored) {
                return null;
            }
        }
    }
}
