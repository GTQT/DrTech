package com.drppp.futuremc.blocks;

import com.drppp.futuremc.FutureMCMain;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockSimpleDrTech extends Block {
    public BlockSimpleDrTech(String name, Material material, SoundType soundType, float hardness, float resistance) {
        super(material);
        setRegistryName(FutureMCMain.MODID, name);
        setTranslationKey(FutureMCMain.MODID + "." + name);
        setCreativeTab(FutureMCMain.TAB);
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(soundType);
    }
}

