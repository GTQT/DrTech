package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntitySapBag;
import com.drppp.drtech.Tile.TileEntityTimeTable;
import keqing.gtqtcore.common.items.GTQTMetaItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockTimeTable extends Block {
    public BlockTimeTable() {
        super(Material.ROCK);
        this.setHardness(1);
        this.setResistance(10F);
        this.setSoundType(SoundType.METAL);
        this.setCreativeTab(DrTechMain.Mytab);
        this.setRegistryName(Tags.MODID,"time_table");
        this.setTranslationKey(Tags.MODID+".Time_Table");
    }
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return  new TileEntityTimeTable();
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
        if(!worldIn.isRemote)
        {
            hand = EnumHand.OFF_HAND;
            TileEntityTimeTable tt = (TileEntityTimeTable) worldIn.getTileEntity(pos);
            if(playerIn.getHeldItem(hand).getItem()== GTQTMetaItems.TIME_BOTTLE.getMetaItem() && playerIn.getHeldItem(hand).getMetadata()==GTQTMetaItems.TIME_BOTTLE.getMetaValue())
            {
                if(tt.inventory.getStackInSlot(0).isEmpty())
                {
                    tt.inventory.insertItem(0,playerIn.getHeldItem(hand),false);
                    tt.markDirty();
                    playerIn.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
                    return true;
                }
            }else if(playerIn.isSneaking())
            {
                if(!tt.inventory.getStackInSlot(0).isEmpty())
                {
                    worldIn.spawnEntity(new EntityItem(worldIn,pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,tt.inventory.getStackInSlot(0)));
                    tt.markDirty();
                    tt.inventory.extractItem(0,1,false);
                }
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }


    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if(!worldIn.isRemote)
        {
            TileEntityTimeTable tt = (TileEntityTimeTable) worldIn.getTileEntity(pos);
            if(!tt.inventory.getStackInSlot(0).isEmpty())
                worldIn.spawnEntity(new EntityItem(worldIn,pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,tt.inventory.getStackInSlot(0)));
        }
    }
}
