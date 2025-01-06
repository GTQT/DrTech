package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityPeacefulTable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockPeacefulTable extends Block {
    public BlockPeacefulTable() {
        super(Material.WOOD);
        this.setResistance(5F);
        this.setRegistryName(Tags.MODID,"peaceful_table");
        this.setCreativeTab(DrTechMain.Mytab);
        this.setTranslationKey(Tags.MODID+".peaceful_table");
    }
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityPeacefulTable();
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}
