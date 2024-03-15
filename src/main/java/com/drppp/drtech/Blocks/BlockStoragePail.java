package com.drppp.drtech.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityPeacefulTable;
import com.drppp.drtech.Tile.TileEntityStoragePail;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockStoragePail extends Block {
    private int level;
    public BlockStoragePail(String name,int level) {
        super(Material.WOOD);
        this.setResistance(5F);
        this.setRegistryName(Tags.MODID,"storage_"+name);
        this.setCreativeTab(DrTechMain.Mytab);
        this.setTranslationKey(Tags.MODID+".storage_"+name);
        this.level = level;
    }
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityStoragePail();
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        super.onBlockClicked(worldIn, pos, playerIn);
    }

}
