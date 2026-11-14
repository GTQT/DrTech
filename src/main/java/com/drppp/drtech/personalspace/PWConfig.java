package com.drppp.drtech.personalspace;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.drppp.drtech.personalspace.world.DimensionConfig;

public class PWConfig {

    public static class Values {

        public static String[] allowedBlocks;

        public static String[] allowedBoundaryBlocks;

        public static String[] allowedGapBlocks;

        public static String[] allowedCenterBlocks;

        public static String[] allowedBiomes;

        public static String[] presets;
    }

    public static String[] allowedBlocks = {
            "minecraft:stone",
            "minecraft:dirt",
            "minecraft:grass",
            "minecraft:cobblestone",
            "minecraft:stonebrick",
            "minecraft:stone_slab",
            "minecraft:stained_hardened_clay",
            "minecraft:stained_hardened_clay:1",
            "minecraft:stained_hardened_clay:2",
            "minecraft:stained_hardened_clay:3",
            "minecraft:stained_hardened_clay:4",
            "minecraft:stained_hardened_clay:5",
            "minecraft:stained_hardened_clay:6",
            "minecraft:stained_hardened_clay:7",
            "minecraft:stained_hardened_clay:8",
            "minecraft:stained_hardened_clay:9",
            "minecraft:stained_hardened_clay:10",
            "minecraft:stained_hardened_clay:11",
            "minecraft:stained_hardened_clay:12",
            "minecraft:stained_hardened_clay:13",
            "minecraft:stained_hardened_clay:14",
            "minecraft:stained_hardened_clay:15",
            "minecraft:hardened_clay",
            "minecraft:concrete",
            "minecraft:concrete:1",
            "minecraft:concrete:2",
            "minecraft:concrete:3",
            "minecraft:concrete:4",
            "minecraft:concrete:5",
            "minecraft:concrete:6",
            "minecraft:concrete:7",
            "minecraft:concrete:8",
            "minecraft:concrete:9",
            "minecraft:concrete:10",
            "minecraft:concrete:11",
            "minecraft:concrete:12",
            "minecraft:concrete:13",
            "minecraft:concrete:14",
            "minecraft:concrete:15",
            "minecraft:bedrock"
    };

    public static String[] allowedBoundaryBlocks = {
            "minecraft:air",
            "minecraft:stonebrick",
            "minecraft:stone_slab",
            "minecraft:stained_hardened_clay",
            "minecraft:stained_hardened_clay:1",
            "minecraft:stained_hardened_clay:2",
            "minecraft:stained_hardened_clay:3",
            "minecraft:stained_hardened_clay:4",
            "minecraft:stained_hardened_clay:5",
            "minecraft:stained_hardened_clay:6",
            "minecraft:stained_hardened_clay:7",
            "minecraft:stained_hardened_clay:8",
            "minecraft:stained_hardened_clay:9",
            "minecraft:stained_hardened_clay:10",
            "minecraft:stained_hardened_clay:11",
            "minecraft:stained_hardened_clay:12",
            "minecraft:stained_hardened_clay:13",
            "minecraft:stained_hardened_clay:14",
            "minecraft:stained_hardened_clay:15",
            "minecraft:hardened_clay",
            "minecraft:concrete",
            "minecraft:concrete:1",
            "minecraft:concrete:2",
            "minecraft:concrete:3",
            "minecraft:concrete:4",
            "minecraft:concrete:5",
            "minecraft:concrete:6",
            "minecraft:concrete:7",
            "minecraft:concrete:8",
            "minecraft:concrete:9",
            "minecraft:concrete:10",
            "minecraft:concrete:11",
            "minecraft:concrete:12",
            "minecraft:concrete:13",
            "minecraft:concrete:14",
            "minecraft:concrete:15"
    };

    public static String[] allowedGapBlocks = allowedBoundaryBlocks;

    public static String[] allowedCenterBlocks = allowedBoundaryBlocks;

    public static String[] allowedBiomes = {
            "minecraft:plains",
            "minecraft:void",
            "minecraft:river",
            "minecraft:desert"
    };

    public static String[] presets = new String[] {
            DimensionConfig.PRESET_FLAT, DimensionConfig.PRESET_MINING
    };

    public static void load(java.io.File configDir) {
        Configuration cfg = new Configuration(new java.io.File(configDir, "drtech_personalspace.cfg"));
        cfg.load();
        allowedBlocks = cfg.getStringList("allowedBlocks", "general", allowedBlocks,
                "Blocks allowed to be used in a flat world.");
        allowedBoundaryBlocks = cfg.getStringList("allowedBoundaryBlocks", "general", allowedBoundaryBlocks,
                "Blocks allowed to be used for grid boundaries.");
        allowedGapBlocks = cfg.getStringList("allowedGapBlocks", "general", allowedGapBlocks,
                "Blocks allowed to be used for grid gaps/roads.");
        allowedCenterBlocks = cfg.getStringList("allowedCenterBlocks", "general", allowedCenterBlocks,
                "Blocks allowed to be used for grid center markers.");
        allowedBiomes = cfg.getStringList("allowedBiomes", "general", allowedBiomes,
                "Biomes allowed to be used in a personal world.");
        presets = cfg.getStringList("presets", "general", presets, "Default presets used for generating a world.");
        if (cfg.hasChanged()) {
            cfg.save();
        }
        Values.allowedBlocks = allowedBlocks;
        Values.allowedBoundaryBlocks = allowedBoundaryBlocks;
        Values.allowedGapBlocks = allowedGapBlocks;
        Values.allowedCenterBlocks = allowedCenterBlocks;
        Values.allowedBiomes = allowedBiomes;
        Values.presets = presets;
    }

    public static Object2ObjectOpenHashMap<String, String> getPresets() {
        Object2ObjectOpenHashMap<String, String> map = new Object2ObjectOpenHashMap<>();
        for (String string : Values.presets) {
            String[] stringArray = string.split(";", 2);
            if (stringArray.length != 2) {
                PersonalSpaceModule.log.error("Preset {} is invalid!", string);
                continue;
            }
            String presetName = stringArray[0];
            String preset = stringArray[1];
            String layers = DimensionConfig.extractLayersPart(preset);
            if (!layers.isEmpty() && !DimensionConfig.PRESET_VALIDATION_PATTERN.matcher(layers).matches()) {
                PersonalSpaceModule.log.error("Preset {} is invalid!", string);
                continue;
            }
            map.put(presetName, preset);
        }
        return map;
    }

    public static List<IBlockState> getAllowedBlocks() {
        return parseAllowedBlockStates(Values.allowedBlocks);
    }

    public static List<IBlockState> getAllowedBoundaryBlocks() {
        return parseAllowedBlockStates(Values.allowedBoundaryBlocks);
    }

    public static List<IBlockState> getAllowedGapBlocks() {
        return parseAllowedBlockStates(Values.allowedGapBlocks);
    }

    public static List<IBlockState> getAllowedCenterBlocks() {
        return parseAllowedBlockStates(Values.allowedCenterBlocks);
    }

    private static List<IBlockState> parseAllowedBlockStates(String[] blockStrings) {
        List<IBlockState> allowedBlockStates = new ArrayList<>();
        if (blockStrings == null) {
            return allowedBlockStates;
        }
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String raw : blockStrings) {
            if (raw == null) {
                continue;
            }
            String string = raw.trim();
            if (string.isEmpty()) {
                continue;
            }
            String blockName = string;
            String metaSpec = null;
            int lastColon = string.lastIndexOf(':');
            if (lastColon > 0) {
                String candidate = string.substring(lastColon + 1);
                if (isMetaSpec(candidate)) {
                    blockName = string.substring(0, lastColon);
                    metaSpec = candidate;
                }
            }
            Block block = Block.REGISTRY.getObject(new ResourceLocation(blockName));
            if (block == null) {
                continue;
            }
            for (int meta : parseMetaSpec(metaSpec)) {
                if (!seen.add(blockName + ':' + meta)) {
                    continue;
                }
                IBlockState state;
                try {
                    state = block.getStateFromMeta(meta);
                } catch (RuntimeException e) {
                    continue;
                }
                if (state != null) {
                    allowedBlockStates.add(state);
                }
            }
        }
        return allowedBlockStates;
    }

    /**
     * A meta spec may be a single value, a range ("0-15") or a comma list ("0,4,8-15").
     * Anything else is treated as part of the block name so plain "modid:name" entries still work.
     */
    private static boolean isMetaSpec(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '!' && c != '-' && c != ',' && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }

    private static List<Integer> parseMetaSpec(String spec) {
        List<Integer> metas = new ArrayList<>();
        if (spec == null || spec.isEmpty()) {
            metas.add(0);
            return metas;
        }
        for (String part : spec.split(",")) {
            if (part.isEmpty()) {
                continue;
            }
            int dash = part.indexOf('-', 1);
            if (dash > 0) {
                Integer from = parseIntSafe(part.substring(0, dash));
                Integer to = parseIntSafe(part.substring(dash + 1));
                if (from == null || to == null) {
                    continue;
                }
                int low = Math.max(0, Math.min(from, to));
                int high = Math.min(15, Math.max(from, to));
                for (int meta = low; meta <= high; meta++) {
                    metas.add(meta);
                }
            } else {
                Integer meta = parseIntSafe(part);
                if (meta != null && meta >= 0 && meta <= 15) {
                    metas.add(meta);
                }
            }
        }
        if (metas.isEmpty()) {
            metas.add(0);
        }
        return metas;
    }

    private static Integer parseIntSafe(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<Biome> getAllowedBiomes() {
        List<Biome> allowedBiomes = new ArrayList<>();
        for (String string : Values.allowedBiomes) {
            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(string));
            if (biome == null) {
                continue;
            }
            allowedBiomes.add(biome);
        }
        return allowedBiomes;
    }
}
