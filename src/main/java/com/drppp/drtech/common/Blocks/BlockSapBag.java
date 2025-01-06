package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntitySapBag;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class BlockSapBag extends Block {
    public BlockSapBag() {
        super(Material.WOOD);
        this.setResistance(25F);
        this.setHardness(5f);
        this.setRegistryName(Tags.MODID,"sap_bag");
        this.setCreativeTab(DrTechMain.Mytab);
        this.setTranslationKey(Tags.MODID+".SapBag");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        double pixelUnit = 1.0D / 16.0D;
        double edgeSpace = 5.5D * pixelUnit;
        double min = edgeSpace;
        double max = 16.0D * pixelUnit - edgeSpace;
        return new AxisAlignedBB(min, min, min, max, max, max);
    }

    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return  new TileEntitySapBag();
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

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof TileEntitySapBag) {
                TileEntitySapBag sapBag = (TileEntitySapBag) tileentity;
                dropInventoryItems(worldIn, pos, sapBag.inventory);
                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof TileEntitySapBag) {
                TileEntitySapBag sapBag = (TileEntitySapBag) tileentity;
                dropInventoryItems(world, pos, sapBag.inventory);
            }
        }
        super.breakBlock(world, pos, state);
    }
    private void dropInventoryItems(World world, BlockPos pos, ItemStackHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (!itemstack.isEmpty()) {
                net.minecraft.inventory.InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
