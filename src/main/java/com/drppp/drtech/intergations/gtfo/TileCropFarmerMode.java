package com.drppp.drtech.intergations.gtfo;

import com.drppp.drtech.Tile.TileCropStick;
import com.drppp.drtech.api.crop.CropRegistry;
import com.drppp.drtech.api.crop.CropStats;
import com.drppp.drtech.api.crop.CropType;
import com.drppp.drtech.common.Items.ItemCropSeed;
import com.drppp.drtech.common.Items.ItemsInit;
import gregtechfoodoption.machines.farmer.FarmerMode;
import gregtechfoodoption.machines.farmer.MetaTileEntityFarmer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static com.drppp.drtech.common.Blocks.Crops.BlockCropStick.VANILLA_SEED_MAP;

public class TileCropFarmerMode implements FarmerMode {
    @Override
    public boolean canOperate(IBlockState iBlockState, MetaTileEntityFarmer metaTileEntityFarmer, BlockPos blockPos, World world) {
        var te = world.getTileEntity(blockPos);
        if(te!=null && te instanceof TileCropStick)
        {
            TileCropStick crop = (TileCropStick)te;
            if(!crop.hasCrop() ||  !crop.isMature())
                return false;
            CropType type = crop.getCropType();
            if (type == null) return false;
            return true;
        }
        return false;
    }

    @Override
    public void harvest(IBlockState state, World world, BlockPos.MutableBlockPos pos, MetaTileEntityFarmer farmer) {
        var te = world.getTileEntity(pos);
        if(te!=null && te instanceof TileCropStick)
        {
            TileCropStick crop = (TileCropStick)te;
            crop.harvest();
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockState state, World world, BlockPos.MutableBlockPos pos, MetaTileEntityFarmer farmer) {
        var te = world.getTileEntity(pos);
        if(te!=null && te instanceof TileCropStick)
        {
            TileCropStick crop = (TileCropStick)te;
           var list = crop.getHarvestDrops();
           return list;
        }
        return FarmerMode.super.getDrops(state, world, pos, farmer);
    }

    @Override
    public boolean canPlaceItem(ItemStack itemStack) {
        return itemStack.getItem()== ItemsInit.CROP_SEED;
    }

    @Override
    public boolean canPlaceAt(BlockPos.MutableBlockPos operationPos, BlockPos.MutableBlockPos farmerPos, EnumFacing facing, World world)
    {
        if(world.isRemote) return false;
       var te =  world.getTileEntity(operationPos);
        if(te!=null && te instanceof TileCropStick)
        {
            TileCropStick tile = (TileCropStick)te;
            if (tile.isDoubleCropStick()) {
                return false;
            }
            if (tile.hasCrop()) {
                return false;
            }

        }
        return FarmerMode.super.canPlaceAt(operationPos, farmerPos, facing, world);
    }

    @Override
    public EnumActionResult place(ItemStack stack, World world, BlockPos.MutableBlockPos pos, MetaTileEntityFarmer farmer)
    {
        if (world.isRemote) return EnumActionResult.PASS;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCropStick)) return EnumActionResult.FAIL;
        TileCropStick tile = (TileCropStick) te;
        ItemStack held = stack;

        if (!held.isEmpty() && held.getItem() instanceof ItemCropSeed) {
            String id = ItemCropSeed.getCropId(held);
            if (tile.isDoubleCropStick()) {
                return EnumActionResult.FAIL;
            }
            if (tile.hasCrop()) {
                return EnumActionResult.FAIL;
            }
            if (id == null || id.isEmpty() || !CropRegistry.exists(id)) {
                return EnumActionResult.FAIL;
            }
            tile.plantCrop(id, ItemCropSeed.getCropStats(held));
            return EnumActionResult.SUCCESS;
        }
        if (!held.isEmpty()) {
            String id = VANILLA_SEED_MAP.get(held.getItem());
            if (id != null)
            {
                tile.plantCrop(id, new CropStats(1, 1, 1));
            }
            return EnumActionResult.SUCCESS;
        }
        return FarmerMode.super.place(stack, world, pos, farmer);
    }
}
