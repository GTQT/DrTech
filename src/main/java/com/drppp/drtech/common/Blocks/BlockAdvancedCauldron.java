package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityAdvancedCauldron;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockAdvancedCauldron extends BlockCauldron {

    public BlockAdvancedCauldron() {
        super();
        this.setHardness(2.0F);
        this.setTranslationKey("advanced_cauldron");
        this.setRegistryName(Tags.MODID,"advanced_cauldron");
        this.setCreativeTab(DrTechMain.DrTechTab);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityAdvancedCauldron();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return ItemsInit.ITEM_BLOCK_ADVANCED_CAULDRON;
    }
}
