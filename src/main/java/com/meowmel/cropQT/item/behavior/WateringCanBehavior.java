package com.meowmel.cropQT.item.behavior;

import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.World;

/**
 * 浇水壶：把壶里的流体喂给作物架。
 *
 * <p>壶本身只是个装了流体能力的 MetaItem（见 {@code FilteredFluidStats}），
 * 本类负责「右键作物架时把流体转成水或肥」。
 *
 * <p>识别哪一种是水、哪一种是肥，走的是 {@link HydrationRegistry} 与
 * {@link FertilizerRegistry} 的登记表——所以第三方加的液体肥料只要登记过，
 * 灌进水壶就能用，不需要额外的物品。
 */
public class WateringCanBehavior implements IItemBehaviour {

    /** 一次用掉多少 mB。 */
    public static final int MB_PER_USE = 500;

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (world.isRemote) {
            return success(held);
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileCropStick)) {
            return tryFill(player, held, world, pos, facing);
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(held);
        if (handler == null) {
            return pass(held);
        }
        // 先模拟抽一份，认出是什么液体再决定要不要真的扣
        FluidStack preview = handler.drain(MB_PER_USE, false);
        if (preview == null || preview.amount <= 0) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "水壶空了。"));
            return fail(held);
        }

        int water = HydrationRegistry.getWater(preview.getFluid()) * preview.amount;
        int fertilizer = FertilizerRegistry.getFertilizer(preview.getFluid()) * preview.amount;
        if (water <= 0 && fertilizer <= 0) {
            player.sendMessage(new TextComponentString(
                    TextFormatting.YELLOW + "壶里的液体浇不了作物。"));
            return fail(held);
        }

        TileCropStick crop = (TileCropStick) tileEntity;
        int added = water > 0 ? crop.addWater(water) : crop.addFertilizer(fertilizer);
        if (added <= 0) {
            player.sendMessage(new TextComponentString(
                    TextFormatting.YELLOW + (water > 0 ? "已经浇透了。" : "已经施够肥了。")));
            return fail(held);
        }

        handler.drain(MB_PER_USE, true);
        player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + (water > 0 ? "浇水 +" : "施肥 +") + added));
        return success(held);
    }

    /** 右键的不是作物架，就当成想装水——走 Forge 的通用「把流体吸进容器」。 */
    private ActionResult<ItemStack> tryFill(EntityPlayer player, ItemStack held, World world,
                                            BlockPos pos, EnumFacing facing) {
        FluidActionResult result = FluidUtil.tryPickUpFluid(held, player, world, pos, facing);
        return result.isSuccess() ? success(result.getResult()) : pass(held);
    }
}
