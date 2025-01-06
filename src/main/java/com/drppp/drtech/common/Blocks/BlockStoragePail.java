package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityStoragePail;
import com.drppp.drtech.api.ItemHandler.TileEntityUIFactory;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockStoragePail extends Block {
    private int level;
    public BlockStoragePail(String name,int level) {
        super(Material.WOOD);
        this.setResistance(25F);
        this.setHardness(5f);
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityStoragePail && !worldIn.isRemote) {
            TileEntityStoragePail storagePail = (TileEntityStoragePail)tile;
            TileEntityUIFactory.INSTANCE.openUI( storagePail, (EntityPlayerMP) playerIn);

            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn == null)
            return;
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityStoragePail) {

            final TileEntityStoragePail spail = (TileEntityStoragePail) tileEntity;

            worldIn.updateComparatorOutputLevel(pos, this);
            final ItemStack droppedStack = new ItemStack(this, 1, 0);
            final NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setTag("PailContent",spail.inventory.serializeNBT());
            droppedStack.setTagCompound(nbtTagCompound);
            worldIn.spawnEntity( new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, droppedStack));
        }
        super.breakBlock(worldIn,pos,state);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return super.damageDropped(state);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if(worldIn==null)
            return;
        if(stack.hasTagCompound())
        {
            NBTTagCompound tag =  stack.getTagCompound();
            if(tag.hasKey("PailContent"))
            {
                final TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity instanceof TileEntityStoragePail)
                {
                    final TileEntityStoragePail spail = (TileEntityStoragePail) tileEntity;
                    spail.inventory.deserializeNBT(tag.getCompoundTag("PailContent"));
                }
            }
        }
    }
}
