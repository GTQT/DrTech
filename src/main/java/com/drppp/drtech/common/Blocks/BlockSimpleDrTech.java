package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockSimpleDrTech extends Block {
    public BlockSimpleDrTech(String name, Material material, SoundType soundType, float hardness, float resistance) {
        super(material);
        setRegistryName(Tags.MODID, name);
        setTranslationKey(Tags.MODID + "." + name);
        setCreativeTab(DrTechMain.DrTechTab);
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(soundType);
    }
}
