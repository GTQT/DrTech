package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.common.multiblock.mover.MoverSessionManager;
import com.drppp.drtech.common.multiblock.mover.MoverEnergyService;
import com.drppp.drtech.common.multiblock.mover.MoverTargeting;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public final class MultiblockMoverBehavior implements IItemBehaviour {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack mover = player.getHeldItem(hand);
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
            if (player.isSneaking()) {
                MoverSessionManager.INSTANCE.cancel(serverPlayer, mover, true,
                        "drtech.multiblock_mover.cancelled");
            } else if (MoverSessionManager.INSTANCE.hasSession(serverPlayer)) {
                MoverSessionManager.INSTANCE.confirm(serverPlayer, mover,
                        MoverTargeting.resolve(serverPlayer, null, 1.0F));
            } else {
                return pass(mover);
            }
        }
        return success(mover);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ,
                                           EnumHand hand) {
        if (world.isRemote) return EnumActionResult.PASS;
        if (!(player instanceof EntityPlayerMP)) return EnumActionResult.FAIL;

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        ItemStack mover = player.getHeldItem(hand);
        if (player.isSneaking()) {
            MoverSessionManager.INSTANCE.cancel(serverPlayer, mover, true,
                    "drtech.multiblock_mover.cancelled");
            return EnumActionResult.SUCCESS;
        }

        if (MoverSessionManager.INSTANCE.hasSession(serverPlayer)) {
            MoverSessionManager.INSTANCE.confirm(serverPlayer, mover, pos.offset(side));
        } else {
            MoverSessionManager.INSTANCE.select(serverPlayer, mover, pos);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        lines.add(net.minecraft.client.resources.I18n.format(
                "metaitem.multiblock_mover.tooltip.1"));
        lines.add(net.minecraft.client.resources.I18n.format(
                "metaitem.multiblock_mover.tooltip.2"));
        lines.add(net.minecraft.client.resources.I18n.format(
                "metaitem.multiblock_mover.tooltip.air_target",
                DrtConfig.MultiblockMover.airTargetDistance));
        lines.add(net.minecraft.client.resources.I18n.format(
                "metaitem.multiblock_mover.tooltip.3", MoverEnergyService.CAPACITY));
        lines.add(net.minecraft.client.resources.I18n.format(
                "metaitem.multiblock_mover.tooltip.4",
                DrtConfig.MultiblockMover.baseEnergyCost,
                DrtConfig.MultiblockMover.energyPerBlock,
                DrtConfig.MultiblockMover.energyPerTileEntity));
        lines.add(net.minecraft.client.resources.I18n.format(
                DrtConfig.MultiblockMover.enableRotation
                        ? "metaitem.multiblock_mover.tooltip.rotation_enabled"
                        : "metaitem.multiblock_mover.tooltip.rotation_disabled"));
        if (DrtConfig.MultiblockMover.enableRotation) {
            lines.add(net.minecraft.client.resources.I18n.format(
                    "metaitem.multiblock_mover.tooltip.rotation_energy",
                    DrtConfig.MultiblockMover.rotationEnergyPerQuarterTurn));
        }
    }
}
