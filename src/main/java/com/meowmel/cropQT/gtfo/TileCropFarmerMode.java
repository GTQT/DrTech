package com.meowmel.cropQT.gtfo;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.tile.TileCropStick;
import com.drppp.drtech.common.items.ItemsInit;
import gregtechfoodoption.common.machines.farmer.FarmerMode;
import gregtechfoodoption.common.machines.farmer.MetaTileEntityFarmer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static com.meowmel.cropQT.block.BlockCropStick.VANILLA_SEED_MAP;

/**
 * GTFO 收割机对作物架的适配。
 *
 * <p>按 M2 之后的规则，种下去之前要先过<b>土壤</b>这道硬门槛（底土只是软惩罚），
 * 所以这里在动手之前先读一眼土壤组：地面不认这株作物就直接 {@code PASS}，
 * 让收割机留着种子去下一格，而不是把种子在这块地上白扔一次。
 *
 * <p><b>本模式不浇水也不施肥。</b>GTFO 的收割机没有流体仓，给不了水肥；
 * 大田自动化要浇水施肥请用作物监管机（{@code MetaTileEntityCropSupervisor}）。
 * 这里能做的是别把种子种到明显长不出东西的地方。
 */
public class TileCropFarmerMode implements FarmerMode {

    @Override
    public boolean canOperate(IBlockState iBlockState, MetaTileEntityFarmer metaTileEntityFarmer, BlockPos blockPos, World world) {
        var te = world.getTileEntity(blockPos);
        if (te instanceof TileCropStick crop) {
            if (!crop.hasCrop() || !crop.isMature()) {
                return false;
            }
            return crop.getCropType() != null;
        }
        return false;
    }

    @Override
    public void harvest(IBlockState state, World world, BlockPos.MutableBlockPos pos, MetaTileEntityFarmer farmer) {
        var te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            ((TileCropStick) te).harvest();
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockState state, World world, BlockPos.MutableBlockPos pos, MetaTileEntityFarmer farmer) {
        var te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            return ((TileCropStick) te).getHarvestDrops();
        }
        return FarmerMode.super.getDrops(state, world, pos, farmer);
    }

    /**
     * 只认种子袋。
     *
     * <p><b>故意不认原版种子</b>：GTFO 是按<b>物品</b>挑模式的（{@code findSuitableFarmerMode(ItemStack)}），
     * 不看位置。这里要是把小麦种子也认下来，收割机在普通耕地上就会选中本模式，
     * 而本模式的 {@code canPlaceAt} 只对作物架放行，结果是把 GTFO 自己的小麦模式挤掉、
     * 整片田都不再工作。种子袋没有这个问题——GTFO 别的模式不认它。
     *
     * <p>{@code BlockCropStick.VANILLA_SEED_MAP} 那条路留给玩家手搓，不走机器。
     */
    @Override
    public boolean canPlaceItem(ItemStack itemStack) {
        return itemStack.getItem() == ItemsInit.CROP_SEED;
    }

    @Override
    public boolean canPlaceAt(BlockPos.MutableBlockPos operationPos, BlockPos.MutableBlockPos farmerPos, EnumFacing facing, World world) {
        if (world.isRemote) {
            return false;
        }
        var te = world.getTileEntity(operationPos);
        if (te instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) te;
            if (tile.isDoubleCropStick() || tile.hasCrop()) {
                return false;
            }
            // 脚下不是任何已登记的土壤组，种什么都是白种
            if (tile.getSoilType() == null) {
                return false;
            }
        }
        return FarmerMode.super.canPlaceAt(operationPos, farmerPos, facing, world);
    }

    @Override
    public EnumActionResult place(ItemStack stack, World world, BlockPos.MutableBlockPos pos, MetaTileEntityFarmer farmer) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCropStick)) {
            return EnumActionResult.FAIL;
        }
        TileCropStick tile = (TileCropStick) te;
        if (tile.isDoubleCropStick() || tile.hasCrop()) {
            return EnumActionResult.FAIL;
        }

        // 能走到这里的只有种子袋，见 canPlaceItem 的说明
        if (stack.getItem() instanceof ItemCropSeed) {
            String id = ItemCropSeed.getCropId(stack);
            if (id == null || id.isEmpty() || !CropRegistry.exists(id)) {
                return EnumActionResult.FAIL;
            }
            return plant(tile, id, ItemCropSeed.getCropStats(stack));
        }
        return FarmerMode.super.place(stack, world, pos, farmer);
    }

    /**
     * 真正下种。
     *
     * <p>土壤不符合作物要求是<b>这块地</b>的问题，不是种子的问题，所以返回
     * {@code PASS} 而不是 {@code FAIL}——收割机会留着这袋种子去试下一格。
     */
    private static EnumActionResult plant(TileCropStick tile, String cropId, CropStats stats) {
        CropType type = CropRegistry.get(cropId);
        if (type == null) {
            return EnumActionResult.FAIL;
        }
        // 杂草不是作物，别往田里种
        if ("weed".equals(cropId)) {
            return EnumActionResult.PASS;
        }
        ISoilList soil = tile.getSoilType();
        if (soil == null || !tile.isSoilValid(type)) {
            return EnumActionResult.PASS;
        }
        return tile.plantCrop(cropId, stats) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }
}
