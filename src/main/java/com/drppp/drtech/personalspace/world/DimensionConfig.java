package com.drppp.drtech.personalspace.world;

import static com.drppp.drtech.personalspace.PersonalSpaceModule.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.common.DimensionManager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.drppp.drtech.personalspace.PersonalSpaceModule;
import com.drppp.drtech.personalspace.proxy.CommonProxy;

public class DimensionConfig {

    private File config;

    private int dimID;
    private boolean allowGenerationChanges = true;
    private boolean generateTrees = false;
    private boolean clouds = true;
    private boolean weather = false;
    private int skyColor = 0xc0d8ff;
    private float starsVisibility = 1F;
    private List<FlatLayerInfo> layers = new ArrayList<>();
    private boolean needsSaving = true;
    private Biome biome = Biomes.PLAINS;
    private DaylightCycle daylightCycle = DaylightCycle.CYCLE;
    private boolean vegetation = false;
    private boolean spawnPassiveMobs = false;
    private boolean spawnMonsters = false;

    private String boundaryBlockA = "minecraft:stonebrick";
    private int boundaryMetaA = 0;
    private String boundaryBlockB = "minecraft:stone_slab";
    private int boundaryMetaB = 0;
    private int boundaryChunkIntervalX = 80;
    private int boundaryChunkIntervalZ = 80;

    private int gapWidth = 8;
    private GapPreset gapPreset = GapPreset.ROAD;
    private String gapBlockA = "minecraft:stonebrick";
    private int gapMetaA = 0;
    private String gapBlockB = "minecraft:stone_slab";
    private int gapMetaB = 0;
    private String gapBlockC = "minecraft:concrete";
    private int gapMetaC = 4;

    private boolean centerEnabled = false;
    private CenterDirection centerDirection = CenterDirection.SE;
    private String centerBlock = "minecraft:concrete";
    private int centerMeta = 14;

    public static final String PRESET_FLAT = "Flat;minecraft:bedrock,3*minecraft:dirt,minecraft:grass";
    public static final String PRESET_MINING = "Mining;4*minecraft:bedrock,58*minecraft:stone,minecraft:dirt,minecraft:grass";

    public static final Pattern PRESET_VALIDATION_PATTERN = Pattern.compile(
            "^(?:[1-9][0-9]*\\*)?[a-zA-Z0-9+_.-]+:[a-zA-Z0-9+_.-]+(?::[0-9]{1,2})?(?:,(?:[1-9][0-9]*\\*)?[a-zA-Z0-9+_.-]+:[a-zA-Z0-9+_.-]+(?::[0-9]{1,2})?)*$");

    public enum DaylightCycle {

        SUN,
        MOON,
        CYCLE;

        public static DaylightCycle fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? CYCLE : values()[ordinal];
        }
    }

    public enum GapPreset {

        ROAD,
        SOLID;

        public static GapPreset fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? ROAD : values()[ordinal];
        }
    }

    public enum CenterDirection {

        SE,
        SW,
        NE,
        NW;

        public static CenterDirection fromOrdinal(int ordinal) {
            return ordinal < 0 || ordinal >= values().length ? SE : values()[ordinal];
        }

        /** 中心标记相对地块中心的 X 偏移（-1 或 0）。 */
        public int getDirOffX() {
            return this == SW || this == NW ? -1 : 0;
        }

        /** 中心标记相对地块中心的 Z 偏移（-1 或 0）。 */
        public int getDirOffZ() {
            return this == NE || this == NW ? -1 : 0;
        }
    }

    public DimensionConfig(int dimID) {
        this.dimID = dimID;
        this.config = new File(
                DimensionManager.getCurrentSaveRootDirectory() + "/" +
                        "personal_world_" + dimID + "/PWConfig.dat");
        if (config.exists()) {
            load();
        }
    }

    private void load() {
        try {
            NBTTagCompound configNBT = CompressedStreamTools.readCompressed(Files.newInputStream(config.toPath()));
            this.skyColor = configNBT.getInteger("sky_color");
            this.starsVisibility = configNBT.hasKey("stars_visibility") ? configNBT.getFloat("stars_visibility") : 1F;
            this.spawnMonsters = configNBT.getBoolean("spawn_monsters");
            this.spawnPassiveMobs = configNBT.getBoolean("spawn_passive_mobs");
            this.generateTrees = configNBT.getBoolean("generate_trees");
            this.daylightCycle = DaylightCycle.fromOrdinal(configNBT.getInteger("daylightcycle"));
            this.clouds = configNBT.getBoolean("clouds");
            this.weather = configNBT.getBoolean("weather");
            if (configNBT.hasKey("biome")) {
                Biome loadedBiome = Biome.REGISTRY.getObject(new ResourceLocation(configNBT.getString("biome")));
                if (loadedBiome != null) {
                    this.biome = loadedBiome;
                }
            }
            this.vegetation = configNBT.getBoolean("vegetation");
            this.allowGenerationChanges = configNBT.hasKey("allow_generation_changes") ?
                    configNBT.getBoolean("allow_generation_changes") :
                    configNBT.getBoolean("allow_configuration_changes");
            if (configNBT.hasKey("blocks") && !configNBT.getString("blocks").isEmpty()) {
                this.layers = LayersFromString(configNBT.getString("blocks"));
            }

            if (configNBT.hasKey("boundary_block_a")) {
                this.boundaryBlockA = configNBT.getString("boundary_block_a");
            }
            this.boundaryMetaA = configNBT.getInteger("boundary_meta_a");
            if (configNBT.hasKey("boundary_block_b")) {
                this.boundaryBlockB = configNBT.getString("boundary_block_b");
            }
            this.boundaryMetaB = configNBT.getInteger("boundary_meta_b");
            this.boundaryChunkIntervalX = configNBT.getInteger("boundary_interval_x");
            this.boundaryChunkIntervalZ = configNBT.getInteger("boundary_interval_z");

            this.gapWidth = configNBT.getInteger("gap_width");
            this.gapPreset = GapPreset.fromOrdinal(configNBT.getInteger("gap_preset"));
            if (configNBT.hasKey("gap_block_a")) {
                this.gapBlockA = configNBT.getString("gap_block_a");
            }
            this.gapMetaA = configNBT.getInteger("gap_meta_a");
            if (configNBT.hasKey("gap_block_b")) {
                this.gapBlockB = configNBT.getString("gap_block_b");
            }
            this.gapMetaB = configNBT.getInteger("gap_meta_b");
            if (configNBT.hasKey("gap_block_c")) {
                this.gapBlockC = configNBT.getString("gap_block_c");
            }
            this.gapMetaC = configNBT.getInteger("gap_meta_c");

            this.centerEnabled = configNBT.getBoolean("center_enabled");
            this.centerDirection = CenterDirection.fromOrdinal(configNBT.getInteger("center_direction"));
            if (configNBT.hasKey("center_block")) {
                this.centerBlock = configNBT.getString("center_block");
            }
            this.centerMeta = configNBT.getInteger("center_meta");
        } catch (IOException e) {
            PersonalSpaceModule.log.error(String.format("Could not load config in %s! Error:", config.getAbsolutePath()));
            throw new RuntimeException(e);
        }
    }

    public DimensionConfig copy() {
        return copy(this.dimID);
    }

    public DimensionConfig copy(int dimID) {
        DimensionConfig copy = new DimensionConfig(dimID);
        copy.copyFrom(this);
        copy.needsSaving = true;
        return copy;
    }

    public void copyFrom(DimensionConfig src) {
        this.needsSaving = true;
        this.dimID = src.dimID;
        this.skyColor = src.skyColor;
        this.starsVisibility = src.starsVisibility;
        this.daylightCycle = src.daylightCycle;
        this.clouds = src.clouds;
        this.weather = src.weather;
        this.generateTrees = src.generateTrees;
        this.layers = new ArrayList<>(src.layers);
        this.allowGenerationChanges = src.allowGenerationChanges;
        this.biome = src.biome;
        this.vegetation = src.vegetation;
        this.spawnMonsters = src.spawnMonsters;
        this.spawnPassiveMobs = src.spawnPassiveMobs;
        this.boundaryBlockA = src.boundaryBlockA;
        this.boundaryMetaA = src.boundaryMetaA;
        this.boundaryBlockB = src.boundaryBlockB;
        this.boundaryMetaB = src.boundaryMetaB;
        this.boundaryChunkIntervalX = src.boundaryChunkIntervalX;
        this.boundaryChunkIntervalZ = src.boundaryChunkIntervalZ;
        this.gapWidth = src.gapWidth;
        this.gapPreset = src.gapPreset;
        this.gapBlockA = src.gapBlockA;
        this.gapMetaA = src.gapMetaA;
        this.gapBlockB = src.gapBlockB;
        this.gapMetaB = src.gapMetaB;
        this.gapBlockC = src.gapBlockC;
        this.gapMetaC = src.gapMetaC;
        this.centerEnabled = src.centerEnabled;
        this.centerDirection = src.centerDirection;
        this.centerBlock = src.centerBlock;
        this.centerMeta = src.centerMeta;
    }

    public void update() {
        if (!config.exists()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                PersonalSpaceModule.log
                        .error(String.format("Could not create config in %s! Error:", config.getAbsolutePath()));
                throw new RuntimeException(e);
            }
        }
        NBTTagCompound configNBT = new NBTTagCompound();
        configNBT.setInteger("sky_color", this.skyColor);
        configNBT.setFloat("stars_visibility", this.starsVisibility);
        configNBT.setBoolean("spawn_monsters", this.spawnMonsters);
        configNBT.setBoolean("spawn_passive_mobs", this.spawnPassiveMobs);
        configNBT.setBoolean("vegetation", this.vegetation);
        configNBT.setBoolean("clouds", this.clouds);
        configNBT.setBoolean("generate_trees", this.generateTrees);
        configNBT.setInteger("daylightcycle", this.daylightCycle.ordinal());
        configNBT.setBoolean("weather", this.weather);
        configNBT.setString("biome", this.biome.getRegistryName().toString());
        configNBT.setBoolean("allow_generation_changes", this.allowGenerationChanges);
        configNBT.setBoolean("allow_configuration_changes", this.allowGenerationChanges);
        if (!layers.isEmpty()) {
            configNBT.setString("blocks", LayersToString(layers));
        }

        configNBT.setString("boundary_block_a", this.boundaryBlockA);
        configNBT.setInteger("boundary_meta_a", this.boundaryMetaA);
        configNBT.setString("boundary_block_b", this.boundaryBlockB);
        configNBT.setInteger("boundary_meta_b", this.boundaryMetaB);
        configNBT.setInteger("boundary_interval_x", this.boundaryChunkIntervalX);
        configNBT.setInteger("boundary_interval_z", this.boundaryChunkIntervalZ);

        configNBT.setInteger("gap_width", this.gapWidth);
        configNBT.setInteger("gap_preset", this.gapPreset.ordinal());
        configNBT.setString("gap_block_a", this.gapBlockA);
        configNBT.setInteger("gap_meta_a", this.gapMetaA);
        configNBT.setString("gap_block_b", this.gapBlockB);
        configNBT.setInteger("gap_meta_b", this.gapMetaB);
        configNBT.setString("gap_block_c", this.gapBlockC);
        configNBT.setInteger("gap_meta_c", this.gapMetaC);

        configNBT.setBoolean("center_enabled", this.centerEnabled);
        configNBT.setInteger("center_direction", this.centerDirection.ordinal());
        configNBT.setString("center_block", this.centerBlock);
        configNBT.setInteger("center_meta", this.centerMeta);
        try {
            CompressedStreamTools.writeCompressed(configNBT, Files.newOutputStream(config.toPath()));
        } catch (IOException e) {
            PersonalSpaceModule.log.error(String.format("Could not save config in %s! Error:", config.getAbsolutePath()));
            throw new RuntimeException(e);
        }
    }

    public static DimensionConfig getConfig(int dimId, boolean isClient) {
        synchronized (CommonProxy.getDimensionConfigs(isClient)) {
            return CommonProxy.getDimensionConfigs(isClient).get(dimId);
        }
    }

    public boolean registerWithDimManager(boolean isClient, boolean saveToWorldConfig) {
        this.config = new File(
                DimensionManager.getCurrentSaveRootDirectory() + "/" +
                        "personal_world_" + dimID + "/PWConfig.dat");
        if (!DimensionManager.isDimensionRegistered(dimID)) {
            if (!isClient && saveToWorldConfig) {
                if (!registerDimensionToFile(dimID)) {
                    log.fatal("Failed to register dimension {} in PWWorlds.dat!", dimID);
                    return false;
                }
            }
            DimensionManager.registerDimension(dimID, dimType);
            PersonalSpaceModule.log.info("DimensionConfig registered for dim {}, client {}", dimID, isClient);
        }
        synchronized (CommonProxy.getDimensionConfigs(isClient)) {
            if (!CommonProxy.getDimensionConfigs(isClient).containsKey(dimID)) {
                CommonProxy.getDimensionConfigs(isClient).put(dimID, this);
            } else {
                CommonProxy.getDimensionConfigs(isClient).get(dimID).copyFrom(this);
            }
        }
        if (!isClient) {
            DimensionManager.initDimension(dimID);
            this.needsSaving = false;
            this.allowGenerationChanges = false;
            this.update();
        }
        return true;
    }

    private synchronized boolean registerDimensionToFile(int dimID) {
        NBTTagCompound nbtTagCompound;
        File file = new File(DimensionManager.getCurrentSaveRootDirectory() + "/PWWorlds.dat");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.fatal("Could not create PWWorlds.dat!", e);
                return false;
            }
            nbtTagCompound = new NBTTagCompound();
        } else {
            try {
                nbtTagCompound = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
            } catch (IOException e) {
                log.fatal("Could not read PWWorlds.dat for dim {}!", dimID, e);
                return false;
            }
        }
        int[] intArray;
        if (nbtTagCompound.hasKey("dimensions")) {
            int[] dimensions = nbtTagCompound.getIntArray("dimensions");
            for (int dimension : dimensions) {
                if (dimension == dimID) {
                    log.fatal("Dimension with ID {} is already registered!", dimID, new Throwable());
                    return false;
                }
            }
            int[] updated = new int[dimensions.length + 1];
            System.arraycopy(dimensions, 0, updated, 0, dimensions.length);
            updated[dimensions.length] = dimID;
            intArray = updated;
            nbtTagCompound.setIntArray("dimensions", intArray);
        } else {
            intArray = new int[] { dimID };
            nbtTagCompound.setIntArray("dimensions", intArray);
        }
        try {
            CompressedStreamTools.writeCompressed(nbtTagCompound, Files.newOutputStream(file.toPath()));
        } catch (IOException e) {
            log.fatal("Could not save PWWorlds.dat for dim {}!", dimID, e);
            return false;
        }
        return true;
    }

    public String LayersToString(List<FlatLayerInfo> flatLayerInfos) {
        if (flatLayerInfos.isEmpty()) {
            return "";
        }
        // The layer list is stored bottom-up: index 0 is the lowest layer (minY = 0).
        // The textual form uses the same order, so a preset reads left-to-right from
        // the bottom of the world to the top. This matches the GTNH preset syntax
        // (e.g. "minecraft:bedrock*4;...;minecraft:grass").
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < flatLayerInfos.size(); i++) {
            sb.append(flatLayerInfos.get(i).toString());
            if (i + 1 < flatLayerInfos.size()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static List<FlatLayerInfo> LayersFromString(String string) {
        ArrayList<FlatLayerInfo> flatLayerInfos = new ArrayList<>();
        if (string == null || string.trim().isEmpty()) {
            return flatLayerInfos;
        }
        int currY = 0;
        String[] stringArray = string.split(",");
        // Read the preset from the bottom layer upwards, consistent with LayersToString.
        for (int i = 0; i < stringArray.length; i++) {
            FlatLayerInfo flatLayerInfo = LayerFromString(stringArray[i]);
            if (flatLayerInfo == null) {
                continue;
            }
            flatLayerInfo.setMinY(currY);
            flatLayerInfos.add(flatLayerInfo);
            currY += flatLayerInfo.getLayerCount();
            if (currY > 255) {
                break;
            }
        }
        return flatLayerInfos;
    }

    public static FlatLayerInfo LayerFromString(String string) {
        int metadata = 0;
        int layers = 1;
        String[] stringArray = string.split("\\*", 2);
        if (stringArray.length == 2) {
            try {
                layers = Integer.parseInt(stringArray[0].trim());
            } catch (NumberFormatException ignored) {
                layers = 1;
            }
            if (layers >= 256 || layers < 0) {
                layers = 1;
            }
            string = stringArray[1];
        }
        String[] stringArray1 = string.split(":");
        if (stringArray1.length < 2) {
            return null;
        }
        if (stringArray1.length >= 3) {
            try {
                metadata = Integer.parseInt(stringArray1[2].trim());
            } catch (NumberFormatException ignored) {
                metadata = 0;
            }
            string = stringArray1[0] + ":" + stringArray1[1];
        }
        Block block = Block.REGISTRY.getObject(new ResourceLocation(string));
        return block == null ? null : new FlatLayerInfo(3, layers, block, metadata);
    }

    public static DimensionConfig readFromPacket(MCDataInput packet) {
        DimensionConfig cfg = new DimensionConfig(packet.readInt());
        cfg.setSkyColor(packet.readInt());
        cfg.setStarVisibility(packet.readFloat());
        cfg.setDaylightCycle(DaylightCycle.fromOrdinal(packet.readInt()));
        cfg.enableClouds(packet.readBoolean());
        cfg.enableWeather(packet.readBoolean());
        cfg.setGeneratingTrees(packet.readBoolean());
        cfg.setGeneratingVegetation(packet.readBoolean());
        Biome packetBiome = Biome.REGISTRY.getObject(new ResourceLocation(packet.readString()));
        if (packetBiome != null) {
            cfg.setBiome(packetBiome);
        }
        cfg.setAllowGenerationChanges(packet.readBoolean());
        cfg.setSpawnMonsters(packet.readBoolean());
        cfg.setSpawnPassiveMobs(packet.readBoolean());
        int layerCount = packet.readVarInt();
        cfg.layers.clear();
        for (int i = 0; i < layerCount; i++) {
            int minY = packet.readInt();
            int currentLayerCount = packet.readVarInt();
            Block block = Block.getBlockById(packet.readVarInt());
            byte meta = packet.readByte();
            if (block == null) {
                log.error("Block was missing");
                continue;
            }
            FlatLayerInfo fli = new FlatLayerInfo(3, currentLayerCount, block, meta);
            fli.setMinY(minY);
            cfg.layers.add(fli);
        }
        cfg.setBoundaryBlockA(packet.readString());
        cfg.setBoundaryMetaA(packet.readVarInt());
        cfg.setBoundaryBlockB(packet.readString());
        cfg.setBoundaryMetaB(packet.readVarInt());
        cfg.setBoundaryChunkIntervalX(packet.readVarInt());
        cfg.setBoundaryChunkIntervalZ(packet.readVarInt());
        cfg.setGapWidth(packet.readVarInt());
        cfg.setGapPreset(GapPreset.fromOrdinal(packet.readVarInt()));
        cfg.setGapBlockA(packet.readString());
        cfg.setGapMetaA(packet.readVarInt());
        cfg.setGapBlockB(packet.readString());
        cfg.setGapMetaB(packet.readVarInt());
        cfg.setGapBlockC(packet.readString());
        cfg.setGapMetaC(packet.readVarInt());
        cfg.setCenterEnabled(packet.readBoolean());
        cfg.setCenterDirection(CenterDirection.fromOrdinal(packet.readVarInt()));
        cfg.setCenterBlock(packet.readString());
        cfg.setCenterMeta(packet.readVarInt());
        return cfg;
    }

    public void writeToPacket(MCDataOutput packet) {
        packet.writeInt(dimID);
        packet.writeInt(skyColor);
        packet.writeFloat(starsVisibility);
        packet.writeInt(daylightCycle.ordinal());
        packet.writeBoolean(clouds);
        packet.writeBoolean(weather);
        packet.writeBoolean(generateTrees);
        packet.writeBoolean(vegetation);
        packet.writeString(biome.getRegistryName().toString());
        packet.writeBoolean(allowGenerationChanges);
        packet.writeBoolean(spawnMonsters);
        packet.writeBoolean(spawnPassiveMobs);
        packet.writeVarInt(layers.size());
        for (FlatLayerInfo fli : layers) {
            packet.writeInt(fli.getMinY());
            packet.writeVarInt(fli.getLayerCount());
            packet.writeVarInt(Block.getIdFromBlock(fli.getLayerMaterial().getBlock()));
            packet.writeByte((byte) fli.getLayerMaterial().getBlock().getMetaFromState(fli.getLayerMaterial()));
        }
        packet.writeString(boundaryBlockA);
        packet.writeVarInt(boundaryMetaA);
        packet.writeString(boundaryBlockB);
        packet.writeVarInt(boundaryMetaB);
        packet.writeVarInt(boundaryChunkIntervalX);
        packet.writeVarInt(boundaryChunkIntervalZ);
        packet.writeVarInt(gapWidth);
        packet.writeVarInt(gapPreset.ordinal());
        packet.writeString(gapBlockA);
        packet.writeVarInt(gapMetaA);
        packet.writeString(gapBlockB);
        packet.writeVarInt(gapMetaB);
        packet.writeString(gapBlockC);
        packet.writeVarInt(gapMetaC);
        packet.writeBoolean(centerEnabled);
        packet.writeVarInt(centerDirection.ordinal());
        packet.writeString(centerBlock);
        packet.writeVarInt(centerMeta);
    }

    public static String extractLayersPart(String fullPreset) {
        if (fullPreset == null) {
            return "";
        }
        int pipe = fullPreset.indexOf('|');
        return pipe >= 0 ? fullPreset.substring(0, pipe) : fullPreset;
    }

    public static boolean hasExtendedSettings(String fullPreset) {
        return fullPreset != null && fullPreset.contains("|");
    }

    public String getFullPresetString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getLayersAsString());
        sb.append("|B,");
        sb.append(encodeBlockSelection(getBoundaryBlockA(), getBoundaryMetaA())).append(',');
        sb.append(encodeBlockSelection(getBoundaryBlockB(), getBoundaryMetaB())).append(',');
        sb.append(getBoundaryChunkIntervalX()).append(',').append(getBoundaryChunkIntervalZ());
        sb.append("|G,");
        sb.append(getGapWidth()).append(',').append(getGapPreset().ordinal()).append(',');
        sb.append(encodeBlockSelection(getGapBlockA(), getGapMetaA())).append(',');
        sb.append(encodeBlockSelection(getGapBlockB(), getGapMetaB())).append(',');
        sb.append(encodeBlockSelection(getGapBlockC(), getGapMetaC()));
        if (isCenterEnabled() && !getCenterBlock().isEmpty()) {
            sb.append("|C,");
            sb.append(1).append(',').append(getCenterDirection().ordinal()).append(',');
            sb.append(encodeBlockSelection(getCenterBlock(), getCenterMeta()));
        }
        return sb.toString();
    }

    public void applyExtendedSettings(String fullPreset) {
        if (!hasExtendedSettings(fullPreset)) {
            return;
        }
        DimensionConfig parsedConfig = copy(getDimID());
        if (applyExtendedSettingsSections(fullPreset.split("\\|"), parsedConfig)) {
            this.boundaryBlockA = parsedConfig.boundaryBlockA;
            this.boundaryMetaA = parsedConfig.boundaryMetaA;
            this.boundaryBlockB = parsedConfig.boundaryBlockB;
            this.boundaryMetaB = parsedConfig.boundaryMetaB;
            this.boundaryChunkIntervalX = parsedConfig.boundaryChunkIntervalX;
            this.boundaryChunkIntervalZ = parsedConfig.boundaryChunkIntervalZ;
            this.gapWidth = parsedConfig.gapWidth;
            this.gapPreset = parsedConfig.gapPreset;
            this.gapBlockA = parsedConfig.gapBlockA;
            this.gapMetaA = parsedConfig.gapMetaA;
            this.gapBlockB = parsedConfig.gapBlockB;
            this.gapMetaB = parsedConfig.gapMetaB;
            this.gapBlockC = parsedConfig.gapBlockC;
            this.gapMetaC = parsedConfig.gapMetaC;
            this.centerEnabled = parsedConfig.centerEnabled;
            this.centerDirection = parsedConfig.centerDirection;
            this.centerBlock = parsedConfig.centerBlock;
            this.centerMeta = parsedConfig.centerMeta;
            this.needsSaving = true;
        }
    }

    private static boolean applyExtendedSettingsSections(String[] sections, DimensionConfig target) {
        for (int i = 1; i < sections.length; i++) {
            String section = sections[i];
            if (section.isEmpty()) {
                continue;
            }
            String[] parts = section.split(",", -1);
            try {
                switch (parts[0]) {
                    case "B":
                        if (parts.length < 5) {
                            return false;
                        }
                        BlockSelection boundaryA = parseBlockSelection(parts[1]);
                        BlockSelection boundaryB = parseBlockSelection(parts[2]);
                        target.setBoundaryBlockA(boundaryA.blockName);
                        target.setBoundaryMetaA(boundaryA.meta);
                        target.setBoundaryBlockB(boundaryB.blockName);
                        target.setBoundaryMetaB(boundaryB.meta);
                        target.setBoundaryChunkIntervalX(Integer.parseInt(parts[3]));
                        target.setBoundaryChunkIntervalZ(Integer.parseInt(parts[4]));
                        break;
                    case "G":
                        if (parts.length < 6) {
                            return false;
                        }
                        BlockSelection gapA = parseBlockSelection(parts[3]);
                        BlockSelection gapB = parseBlockSelection(parts[4]);
                        BlockSelection gapC = parseBlockSelection(parts[5]);
                        target.setGapWidth(Integer.parseInt(parts[1]));
                        target.setGapPreset(GapPreset.fromOrdinal(Integer.parseInt(parts[2])));
                        target.setGapBlockA(gapA.blockName);
                        target.setGapMetaA(gapA.meta);
                        target.setGapBlockB(gapB.blockName);
                        target.setGapMetaB(gapB.meta);
                        target.setGapBlockC(gapC.blockName);
                        target.setGapMetaC(gapC.meta);
                        break;
                    case "C":
                        if (parts.length < 4) {
                            return false;
                        }
                        BlockSelection center = parseBlockSelection(parts[3]);
                        target.setCenterEnabled(Integer.parseInt(parts[1]) != 0);
                        target.setCenterDirection(CenterDirection.fromOrdinal(Integer.parseInt(parts[2])));
                        target.setCenterBlock(center.blockName);
                        target.setCenterMeta(center.meta);
                        break;
                    default:
                        return false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
    }

    private static String encodeBlockSelection(String blockName, int meta) {
        if (blockName == null || blockName.isEmpty()) {
            return "minecraft:air";
        }
        return meta != 0 ? blockName + ":" + meta : blockName;
    }

    private static BlockSelection parseBlockSelection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new BlockSelection("minecraft:air", 0);
        }
        String trimmed = input.trim();
        int lastColon = trimmed.lastIndexOf(':');
        int firstColon = trimmed.indexOf(':');
        if (lastColon > firstColon) {
            try {
                return new BlockSelection(trimmed.substring(0, lastColon),
                        Integer.parseInt(trimmed.substring(lastColon + 1)));
            } catch (NumberFormatException ignored) {
                return new BlockSelection(trimmed, 0);
            }
        }
        return new BlockSelection(trimmed, 0);
    }

    private static class BlockSelection {

        private final String blockName;
        private final int meta;

        private BlockSelection(String blockName, int meta) {
            this.blockName = blockName;
            this.meta = meta;
        }
    }

    public String getLayersAsString() {
        return LayersToString(this.layers);
    }

    public void setLayers(String preset) {
        this.layers = LayersFromString(preset);
        this.needsSaving = true;
    }

    public int getGroundLevel() {
        if (layers.isEmpty()) {
            return 128;
        }
        int y = 0;
        for (FlatLayerInfo info : layers) {
            y += info.getLayerCount();
        }
        return MathHelper.clamp(y, 0, 255);
    }

    public void setDimID(int dimID) {
        this.dimID = dimID;
    }

    public void setSpawnPassiveMobs(boolean spawnPassiveMobs) {
        if (this.spawnPassiveMobs != spawnPassiveMobs) {
            this.needsSaving = true;
            this.spawnPassiveMobs = spawnPassiveMobs;
        }
    }

    public void setSpawnMonsters(boolean spawnMonsters) {
        if (this.spawnMonsters != spawnMonsters) {
            this.needsSaving = true;
            this.spawnMonsters = spawnMonsters;
        }
    }

    public void setGeneratingVegetation(boolean generateVegetation) {
        if (this.vegetation != generateVegetation) {
            this.needsSaving = true;
            this.vegetation = generateVegetation;
        }
    }

    public void setBiome(Biome biome) {
        if (biome != null && this.biome != biome) {
            this.needsSaving = true;
            this.biome = biome;
        }
    }

    public void setStarVisibility(float starVisibility) {
        if (this.starsVisibility != starVisibility) {
            this.needsSaving = true;
            this.starsVisibility = MathHelper.clamp(starVisibility, 0.0f, 1.0f);
        }
    }

    public void setSkyColor(int skyColor) {
        if (this.skyColor != skyColor) {
            this.needsSaving = true;
            this.skyColor = MathHelper.clamp(skyColor, 0, 0xFFFFFF);
        }
    }

    public void enableWeather(boolean enableWeather) {
        if (this.weather != enableWeather) {
            this.needsSaving = true;
            this.weather = enableWeather;
        }
    }

    public void setDaylightCycle(DaylightCycle cycle) {
        if (this.daylightCycle != cycle) {
            this.needsSaving = true;
            this.daylightCycle = cycle;
        }
    }

    public void enableClouds(boolean enableClouds) {
        if (this.clouds != enableClouds) {
            this.needsSaving = true;
            this.clouds = enableClouds;
        }
    }

    public void setGeneratingTrees(boolean generateTrees) {
        if (this.generateTrees != generateTrees) {
            this.needsSaving = true;
            this.generateTrees = generateTrees;
        }
    }

    public void setAllowGenerationChanges(boolean allowGenerationChanges) {
        if (this.allowGenerationChanges != allowGenerationChanges) {
            this.needsSaving = true;
            this.allowGenerationChanges = allowGenerationChanges;
        }
    }

    public void setBoundaryBlockA(String boundaryBlockA) {
        String normalized = normalizeBlockName(boundaryBlockA);
        if (!this.boundaryBlockA.equals(normalized)) {
            this.needsSaving = true;
            this.boundaryBlockA = normalized;
        }
    }

    public void setBoundaryMetaA(int boundaryMetaA) {
        int value = Math.max(boundaryMetaA, 0);
        if (this.boundaryMetaA != value) {
            this.needsSaving = true;
            this.boundaryMetaA = value;
        }
    }

    public void setBoundaryBlockB(String boundaryBlockB) {
        String normalized = normalizeBlockName(boundaryBlockB);
        if (!this.boundaryBlockB.equals(normalized)) {
            this.needsSaving = true;
            this.boundaryBlockB = normalized;
        }
    }

    public void setBoundaryMetaB(int boundaryMetaB) {
        int value = Math.max(boundaryMetaB, 0);
        if (this.boundaryMetaB != value) {
            this.needsSaving = true;
            this.boundaryMetaB = value;
        }
    }

    public void setBoundaryChunkIntervalX(int boundaryChunkIntervalX) {
        int value = MathHelper.clamp(boundaryChunkIntervalX, 0, 512);
        if (this.boundaryChunkIntervalX != value) {
            this.needsSaving = true;
            this.boundaryChunkIntervalX = value;
        }
    }

    public void setBoundaryChunkIntervalZ(int boundaryChunkIntervalZ) {
        int value = MathHelper.clamp(boundaryChunkIntervalZ, 0, 512);
        if (this.boundaryChunkIntervalZ != value) {
            this.needsSaving = true;
            this.boundaryChunkIntervalZ = value;
        }
    }

    public void setGapWidth(int gapWidth) {
        int value = MathHelper.clamp(gapWidth, 0, 31);
        if (this.gapWidth != value) {
            this.needsSaving = true;
            this.gapWidth = value;
        }
    }

    public void setGapPreset(GapPreset gapPreset) {
        if (this.gapPreset != gapPreset) {
            this.needsSaving = true;
            this.gapPreset = gapPreset;
        }
    }

    public void setGapBlockA(String gapBlockA) {
        String normalized = normalizeBlockName(gapBlockA);
        if (!this.gapBlockA.equals(normalized)) {
            this.needsSaving = true;
            this.gapBlockA = normalized;
        }
    }

    public void setGapMetaA(int gapMetaA) {
        int value = Math.max(gapMetaA, 0);
        if (this.gapMetaA != value) {
            this.needsSaving = true;
            this.gapMetaA = value;
        }
    }

    public void setGapBlockB(String gapBlockB) {
        String normalized = normalizeBlockName(gapBlockB);
        if (!this.gapBlockB.equals(normalized)) {
            this.needsSaving = true;
            this.gapBlockB = normalized;
        }
    }

    public void setGapMetaB(int gapMetaB) {
        int value = Math.max(gapMetaB, 0);
        if (this.gapMetaB != value) {
            this.needsSaving = true;
            this.gapMetaB = value;
        }
    }

    public void setGapBlockC(String gapBlockC) {
        String normalized = normalizeBlockName(gapBlockC);
        if (!this.gapBlockC.equals(normalized)) {
            this.needsSaving = true;
            this.gapBlockC = normalized;
        }
    }

    public void setGapMetaC(int gapMetaC) {
        int value = Math.max(gapMetaC, 0);
        if (this.gapMetaC != value) {
            this.needsSaving = true;
            this.gapMetaC = value;
        }
    }

    public void setCenterEnabled(boolean centerEnabled) {
        if (this.centerEnabled != centerEnabled) {
            this.needsSaving = true;
            this.centerEnabled = centerEnabled;
        }
    }

    public void setCenterDirection(CenterDirection centerDirection) {
        if (this.centerDirection != centerDirection) {
            this.needsSaving = true;
            this.centerDirection = centerDirection;
        }
    }

    public void setCenterBlock(String centerBlock) {
        String normalized = normalizeBlockName(centerBlock);
        if (!this.centerBlock.equals(normalized)) {
            this.needsSaving = true;
            this.centerBlock = normalized;
        }
    }

    public void setCenterMeta(int centerMeta) {
        int value = Math.max(centerMeta, 0);
        if (this.centerMeta != value) {
            this.needsSaving = true;
            this.centerMeta = value;
        }
    }

    private static String normalizeBlockName(String blockName) {
        if (blockName == null || blockName.trim().isEmpty()) {
            return "minecraft:air";
        }
        return blockName;
    }

    private static Block parseBlockName(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        BlockSelection selection = parseBlockSelection(value);
        return Block.REGISTRY.getObject(new ResourceLocation(selection.blockName));
    }

    public boolean needsSaving() {
        return needsSaving;
    }

    public boolean generateTrees() {
        return generateTrees;
    }

    public boolean cloudsEnabled() {
        return clouds;
    }

    public boolean weatherEnabled() {
        return weather;
    }

    public int getSkyColor() {
        return skyColor;
    }

    public float getStarsVisibility() {
        return starsVisibility;
    }

    public List<FlatLayerInfo> getLayers() {
        return layers;
    }

    public Biome getBiome() {
        return biome;
    }

    public boolean vegetationEnabled() {
        return vegetation;
    }

    public boolean spawnPassiveMobs() {
        return spawnPassiveMobs;
    }

    public boolean spawnMonsters() {
        return spawnMonsters;
    }

    public boolean allowGenerationChanges() {
        return allowGenerationChanges;
    }

    public DaylightCycle getDaylightCycle() {
        return daylightCycle;
    }

    public int getDimID() {
        return dimID;
    }

    public String getBoundaryBlockA() {
        return boundaryBlockA;
    }

    public int getBoundaryMetaA() {
        return boundaryMetaA;
    }

    public String getBoundaryBlockB() {
        return boundaryBlockB;
    }

    public int getBoundaryMetaB() {
        return boundaryMetaB;
    }

    public int getBoundaryChunkIntervalX() {
        return boundaryChunkIntervalX;
    }

    public int getBoundaryChunkIntervalZ() {
        return boundaryChunkIntervalZ;
    }

    public int getGapWidth() {
        return gapWidth;
    }

    public GapPreset getGapPreset() {
        return gapPreset;
    }

    public String getGapBlockA() {
        return gapBlockA;
    }

    public int getGapMetaA() {
        return gapMetaA;
    }

    public String getGapBlockB() {
        return gapBlockB;
    }

    public int getGapMetaB() {
        return gapMetaB;
    }

    public String getGapBlockC() {
        return gapBlockC;
    }

    public int getGapMetaC() {
        return gapMetaC;
    }

    public boolean isCenterEnabled() {
        return centerEnabled;
    }

    public CenterDirection getCenterDirection() {
        return centerDirection;
    }

    public String getCenterBlock() {
        return centerBlock;
    }

    public int getCenterMeta() {
        return centerMeta;
    }

    public Block getBoundaryBlockAResolved() {
        return parseBlockName(boundaryBlockA);
    }

    public Block getBoundaryBlockBResolved() {
        return parseBlockName(boundaryBlockB);
    }

    public Block getGapBlockAResolved() {
        return parseBlockName(gapBlockA);
    }

    public Block getGapBlockBResolved() {
        return parseBlockName(gapBlockB);
    }

    public Block getGapBlockCResolved() {
        return parseBlockName(gapBlockC);
    }

    public Block getCenterBlockResolved() {
        return parseBlockName(centerBlock);
    }

    public IBlockState stateFromSelection(String blockName, int meta) {
        Block block = parseBlockName(blockName);
        if (block == null) {
            return null;
        }
        int clampedMeta = MathHelper.clamp(meta, 0, 15);
        return clampedMeta == 0 ? block.getDefaultState() : block.getStateFromMeta(clampedMeta);
    }
}
