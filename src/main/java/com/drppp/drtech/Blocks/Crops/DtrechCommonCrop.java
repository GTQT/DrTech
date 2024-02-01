package com.drppp.drtech.Blocks.Crops;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class DtrechCommonCrop extends BlockCrops {
    private Item seed;
    private Item crop;
    private int age=0;

    public DtrechCommonCrop(String registryName,Item seed, Item crop) {
        this.setRegistryName(Tags.MODID,"drtech_crop_"+registryName);
        this.setCreativeTab(DrTechMain.Mytab);
        this.seed = seed;
        this.crop = crop;
    }

    public DtrechCommonCrop(String registryName,Item seed, Item crop, int age) {
        this.setRegistryName(Tags.MODID,"drtech_crop_"+registryName);
        this.setCreativeTab(DrTechMain.Mytab);
        this.seed = seed;
        this.crop = crop;
        this.age = age;
    }

    /**
     * @param state
     * @param source
     * @param pos
     * @deprecated call via {@link IBlockState#getBoundingBox(IBlockAccess, BlockPos)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    protected Item getSeed() {
        return this.seed;
    }

    @Override
    protected Item getCrop() {
        return this.crop;
    }

    @Override
    public int getMaxAge() {
        return this.age==0?super.getMaxAge():this.age;
    }
}
