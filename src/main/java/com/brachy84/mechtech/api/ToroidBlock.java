package com.brachy84.mechtech.api;

import com.brachy84.mechtech.integration.crafttweaker.IToroidBlock;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Material;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ToroidBlock implements IToroidBlock {

    private static final ToroidBlock NULL = new ToroidBlock("NULL", Blocks.AIR.getDefaultState());

    private static final Map<String, ToroidBlock> TORUS_BLOCK_MAP = new HashMap<>();

    @Nullable
    public static ToroidBlock get(IBlockState state) {
        for (ToroidBlock block : TORUS_BLOCK_MAP.values()) {
            if (block.state.getBlock() == state.getBlock() && block.state.getBlock().getMetaFromState(block.state) == state.getBlock().getMetaFromState(state))
                return block;
        }
        return null;
    }

    @Nullable
    public static ToroidBlock get(String name) {
        return TORUS_BLOCK_MAP.get(name);
    }

    public static void remove(String name) {
        if (!TORUS_BLOCK_MAP.containsKey(name)) {
            CraftTweakerAPI.logError("Could not find ToroidBlock with name " + name);
            return;
        }
        TORUS_BLOCK_MAP.remove(name);
    }

    public static Collection<ToroidBlock> getAll() {
        return Collections.unmodifiableCollection(TORUS_BLOCK_MAP.values());
    }

    public static Map<String, ToroidBlock> getRegistryMap() {
        return Collections.unmodifiableMap(TORUS_BLOCK_MAP);
    }

    private final IBlockState state;
    public float dmgModifier = 0;
    public float rangeModifier = 0;
    public float ampsPerBlock = 0;
    private final String name;

    public ToroidBlock(String name, IBlockState state) {
        this.state = state;
        this.name = name;
    }

    public static ToroidBlock create(String name, crafttweaker.api.block.IBlockState state) {
        if (state == null) {
            CraftTweakerAPI.logError("Can't create ToroidBlock of null BlockState");
            return NULL;
        }
        return create(name, CraftTweakerMC.getBlockState(state));
    }

    public static ToroidBlock create(String name, IBlockState state) {
        return new ToroidBlock(name, state);
    }

    public static ToroidBlock create(String name, IBlock block) {
        if (block == null) {
            CraftTweakerAPI.logError("Can't create ToroidBlock of null block");
            return NULL;
        }
        return create(name, CraftTweakerMC.getBlock(block));
    }

    public static ToroidBlock create(String name, Block block) {
        return new ToroidBlock(name, block.getDefaultState());
    }

    public static ToroidBlock create(Material material) {
        if (material == null) {
            CraftTweakerAPI.logError("Can't create ToroidBlock of null material");
            return NULL;
        }
        return create(material.toString(), material);
    }

    public static ToroidBlock create(String name, Material material) {
        if (material == null) {
            CraftTweakerAPI.logError("Can't create ToroidBlock of null material");
            return NULL;
        }
        BlockCompressed block = MetaBlocks.COMPRESSED.get(material);
        if (block == null) {
            CraftTweakerAPI.logError("Can't find block for ToroidBlock for material " + material.toString());
            return NULL;
        }
        return new ToroidBlock(name, block.getBlock(material));
    }

    @Override
    public void setDmgModifierCT(float dmgModifier) {
        this.dmgModifier = dmgModifier;
    }

    @Override
    public void setRangeModifierCT(float rangeModifier) {
        this.rangeModifier = rangeModifier;
    }

    @Override
    public void setAmpsPerBlockCT(float ampsPerBlock) {
        this.ampsPerBlock = ampsPerBlock;
    }

    @Override
    public ToroidBlock register() {
        if (state.getBlock() == Blocks.AIR) {
            GTLog.logger.error("Can not register TorusBlock with AIR block state");
            return this;
        }
        if (TORUS_BLOCK_MAP.containsKey(name)) {
            CraftTweakerAPI.logError("Can't register ToroidBlock '" + name + "' as it already exists");
        } else {
            TORUS_BLOCK_MAP.put(name, this);
        }
        return this;
    }

    @Override
    public float getDmgModifier() {
        return dmgModifier;
    }

    @Override
    public float getRangeModifier() {
        return rangeModifier;
    }

    @Override
    public float getAmpsPerBlock() {
        return ampsPerBlock;
    }

    @Override
    public String getName() {
        return name;
    }

    public ItemStack getItemForm() {
        return new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
    }

    public static TraceabilityPredicate traceabilityPredicate() {
        Supplier<BlockInfo[]> candidates = () -> {
            BlockInfo[] info = new BlockInfo[TORUS_BLOCK_MAP.size()];
            int i = 0;
            for (ToroidBlock block : TORUS_BLOCK_MAP.values()) {
                info[i] = new BlockInfo(block.state);
                i++;
            }
            return info;
        };
        return new TraceabilityPredicate(blockWorldState -> {
            ToroidBlock toroidBlock = get(blockWorldState.getBlockState());
            if (toroidBlock == null) {
                return false;
            }
            PatternMatchContext matchContext = blockWorldState.getMatchContext();
            matchContext.increment(toroidBlock.name, 1);
            return true;
        }, candidates).addTooltips();
    }
}
