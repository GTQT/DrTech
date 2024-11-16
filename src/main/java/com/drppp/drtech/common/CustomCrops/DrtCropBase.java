package com.drppp.drtech.common.CustomCrops;

import com.drppp.drtech.DrTechMain;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public  class DrtCropBase extends Block   {
    public   PropertyInteger AGE ;
    private   AxisAlignedBB[] CROPS_AABB ;
    public int CropTire;

    public DrtCropBase(PropertyInteger AGE, AxisAlignedBB[] AABB,int tire) {
        super(Material.PLANTS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(this.getAgeProperty(), Integer.valueOf(0)));
        this.setCreativeTab(DrTechMain.Mytab);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.disableStats();
        this.AGE = AGE;
        this.CROPS_AABB = AABB;
        this.CropTire = tire;
    }

    public PropertyInteger getAgeProperty()
    {
        return AGE;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return  null;
    }
    public int getAge(IBlockState state)
    {
        return (state.getValue(this.getAgeProperty())).intValue();
    }

    public IBlockState withAge(int age)
    {
        return this.getDefaultState().withProperty(this.getAgeProperty(), Integer.valueOf(age));
    }
    public IBlockState getStateFromMeta(int meta)
    {
        return this.withAge(meta);
    }

    public int getMetaFromState(IBlockState state)
    {
        return this.getAge(state);
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {AGE});
    }
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }


    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
}
