package com.meowmel.cropQT.item.behavior;

import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * 施肥器：右键作物架施肥，每次消耗一点耐久。
 *
 * <p><b>耐久不走原版 {@code setMaxDamage}</b>——MetaItem 的耐久统一存 NBT，
 * 由 {@link IItemDurabilityManager} 负责渲染耐久条，做法照 GT 的 {@code CatalystBehavior}。
 *
 * <p>用完就没了：这是一次性工具，不做补充。
 */
public class FertilizerApplicatorBehavior implements IItemBehaviour, IItemDurabilityManager,
        IItemMaxStackSizeProvider {

    /** 一次施肥补多少肥。 */
    public static final int POTENCY_PER_USE = 100;

    /** 一共能用多少次。 */
    public static final int MAX_USES = 64;

    /** 耐久所在 NBT 子标签，跟 GT 自己的零件统计分开，互不干扰。 */
    private static final String NBT_TAG = "cropqt.FertilizerApplicator";
    private static final String NBT_DAMAGE = "Damage";

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (world.isRemote) {
            return success(held);
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileCropStick)) {
            return pass(held);
        }
        TileCropStick crop = (TileCropStick) tileEntity;

        if (getRemainingUses(held) <= 0) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "施肥器已经空了。"));
            return fail(held);
        }

        int added = crop.addFertilizer(POTENCY_PER_USE);
        if (added <= 0) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "已经施够肥了。"));
            return fail(held);
        }

        setDamage(held, getDamage(held) + 1);
        player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "施肥 +" + added + TextFormatting.GRAY
                        + String.format("（%.0f%%，还剩 %d 次）",
                        crop.getFertilizerRatio() * 100, getRemainingUses(held))));
        return success(held);
    }

    // ==================== 耐久 ====================

    /** 还剩几次可用。 */
    public static int getRemainingUses(ItemStack stack) {
        return Math.max(0, MAX_USES - getDamage(stack));
    }

    private static int getDamage(ItemStack stack) {
        NBTTagCompound tag = stack.getSubCompound(NBT_TAG);
        return tag == null ? 0 : tag.getInteger(NBT_DAMAGE);
    }

    private static void setDamage(ItemStack stack, int damage) {
        stack.getOrCreateSubCompound(NBT_TAG).setInteger(NBT_DAMAGE, Math.min(MAX_USES, Math.max(0, damage)));
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return GTUtility.calculateDurabilityFromDamageTaken(getDamage(itemStack), MAX_USES);
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        lines.add(I18n.format("metaitem.tool.tooltip.durability", getRemainingUses(stack), MAX_USES));
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        return 1;
    }
}
