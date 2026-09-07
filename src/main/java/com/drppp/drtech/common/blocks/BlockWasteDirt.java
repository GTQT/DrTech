package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockWasteDirt extends Block {
    public BlockWasteDirt() {
        super(Material.GRASS);
        this.setRegistryName(Tags.MODID,"waste_dirt");
        this.setCreativeTab(DrTechMain.DrTechTab);
        this.setTranslationKey(Tags.MODID+".waste_dirt");
    }
}
