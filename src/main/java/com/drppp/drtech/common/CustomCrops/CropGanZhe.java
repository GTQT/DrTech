package com.drppp.drtech.common.CustomCrops;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public class CropGanZhe extends Block {

    public static final PropertyInteger GANZHE_AGE = PropertyInteger.create("age",0,4);

    public static  final AxisAlignedBB[] COMMON_AABB = new AxisAlignedBB[]
            {
                    new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
                    new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D),
                    new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
                    new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5125D, 1.0D)
            };
    public int CropTire=1;
    public CropGanZhe() {
        super(Material.PLANTS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(this.getAgeProperty(), Integer.valueOf(0)));
        this.setCreativeTab(DrTechMain.Mytab);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setRegistryName(Tags.MODID,"custom_crop_ganzhe");
        this.setTranslationKey(Tags.MODID+".custom.crop.ganzhe");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
       if(!worldIn.isRemote)
       {
           var tile = worldIn.getTileEntity(pos);
           if(tile!=null && tile instanceof DrtCropTileBase)
           {
               DrtCropTileBase base = (DrtCropTileBase)tile;
               base.onRightClick();
           }
       }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    public PropertyInteger getAgeProperty()
    {
        return GANZHE_AGE;
    }
    public void grow(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = this.getAge(state) ;
        if (i > 4)
        {
            i = 4;
        }
        worldIn.setBlockState(pos, this.withAge(i), 2);
    }
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

    }
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return COMMON_AABB[(state.getValue(this.getAgeProperty())).intValue()];
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return  new DrtCropTileBase();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    public int getAge(IBlockState state)
    {
        return (state.getValue(this.getAgeProperty())).intValue();
    }
//    public int getGrowSpeed(IBlockState state)
//    {
//        return (state.getValue(GROW_SPEED)).intValue();
//    }
//    public int getGainRate(IBlockState state)
//    {
//        return (state.getValue(GAIN_RATE)).intValue();
//    }
//    public int getResistance(IBlockState state)
//    {
//        return (state.getValue(RESISTANCE)).intValue();
//    }
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
        return new BlockStateContainer(this, new IProperty[] {GANZHE_AGE});
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
