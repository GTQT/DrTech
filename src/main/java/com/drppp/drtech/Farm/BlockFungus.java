package com.drppp.drtech.Farm;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntitySapBag;
import com.drppp.drtech.Tile.TileEntityStoragePail;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockFungus extends Block {
    public BlockFungus() {
        super(Material.PLANTS);
        this.setTickRandomly(true);
        this.setCreativeTab((CreativeTabs)null);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.disableStats();
        this.setRegistryName(Tags.MODID,"fungus");
        this.setTranslationKey(Tags.MODID+".Fungus");
        this.createBlockState();
    }

    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return  new TileFungus();
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return Block.NULL_AABB;
    }
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn == null)
            return  false;
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileFungus) {
            final TileFungus fungus = (TileFungus) tileEntity;

            if(fungus.canHarvest())
            {

                worldIn.updateComparatorOutputLevel(pos, this);
                var list = fungus.getDropList();
                for (var s:list)
                {
                    worldIn.spawnEntity( new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, s));
                }
                fungus.tire=1;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn == null)
            return;
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileFungus) {
            final TileFungus fungus = (TileFungus) tileEntity;
            worldIn.updateComparatorOutputLevel(pos, this);
            var list = fungus.getDropList();
            for (var s:list)
            {
                worldIn.spawnEntity( new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, s));
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }
    // 重写随机刻方法
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileFungus) {
            final TileFungus fungus = (TileFungus) tileEntity;
            fungus.updateTick();
        }
    }

    // 重写该方法以确保随机刻事件能够被调度
    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        this.updateTick(world, pos, state, random);
    }
    // 设置方块的随机刻速率
    @Override
    public int tickRate(World world) {
        return 1; // 这里返回的数值是刻的间隔时间，数值越小，调用updateTick的频率越高
    }
}
