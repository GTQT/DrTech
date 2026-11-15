package com.meowmel.cropQT.item.tool;

import com.drppp.drtech.DrTechMain;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

/**
 * 作物铲（源端 {@code ItemSpadeNH} / {@code ItemSpade} / {@code ItemReinforcedSpade}）。
 *
 * <p>两种用途：
 * <ul>
 *   <li>右键作物架：拔草；对「已成熟但环境不达标、收不了」的作物，铲下来并返还种子。</li>
 *   <li>右键地面：草方块/泥土/菌丝 → 耕地；耕地 → 泥土（源端 TILLABLE_BLOCKS）。</li>
 * </ul>
 *
 * <p>强化铲会多掉种子：源端为 {@code resist/10 + (rand(10) < resist%10 ? 1 : 0)}。
 */
public class ItemCropSpade extends Item {

    private final boolean reinforced;

    public ItemCropSpade(String registryName, boolean reinforced) {
        this.reinforced = reinforced;
        setTranslationKey("drtech." + registryName);
        setRegistryName("drtech", registryName);
        setMaxStackSize(1);
        setMaxDamage(0);
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                reinforced ? "cropqt_tooltip.item.reinforcedSpade" : "cropqt_tooltip.item.spade"));
    }

    public boolean isReinforced() {
        return reinforced;
    }

    /** 源端先判断能不能铲：双层杂交架、未成熟作物都不动。 */
    public boolean onCropStickUse(World world, BlockPos pos, TileCropStick tile,
                                  EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            return true;
        }
        if (tile.isDoubleCropStick()) {
            return false;
        }
        if (tile.isWeedPlant()) {
            if (!tile.isMature()) {
                return false;
            }
            List<ItemStack> drops = tile.getHarvestDrops();
            tile.destroyCrop();
            dropAll(world, pos, drops);
            world.playSound(null, pos, SoundType.PLANT.getBreakSound(), SoundCategory.BLOCKS,
                    0.8f, 0.9f);
            return true;
        }
        if (!tile.hasCrop()) {
            return false;
        }
        // 能够正常收获时不抢交互，让空手收获走原来的路径
        if (!tile.isMature() || tile.canHarvest() || !tile.areGrowthRequirementsMet()) {
            return false;
        }
        List<ItemStack> drops = tile.getHarvestDrops();
        addSeedDrops(world, pos, tile.getCropId(), tile.getStats());
        tile.destroyCrop();
        dropAll(world, pos, drops);
        world.playSound(null, pos, SoundType.PLANT.getBreakSound(), SoundCategory.BLOCKS,
                0.8f, 0.9f);
        return true;
    }

    /** 源端 ItemSpade 掉 1 个种子；强化铲按抗性掉 0~4 个（resist/10 + 10% 余数概率）。 */
    private void addSeedDrops(World world, BlockPos pos, String cropId, CropStats stats) {
        if (cropId == null || cropId.isEmpty()) {
            return;
        }
        int count = 1;
        if (reinforced && stats != null) {
            int resistance = Math.max(0, Math.min(31, stats.getResistance()));
            count = resistance / 10;
            if (world.rand.nextInt(10) < resistance % 10) {
                count++;
            }
        }
        ItemStack seed = ItemCropSeed.createSeedBag(cropId, stats);
        if (seed.isEmpty()) {
            return;
        }
        seed.setCount(Math.max(1, count));
        Block.spawnAsEntity(world, pos, seed);
    }

    private static void dropAll(World world, BlockPos pos, List<ItemStack> drops) {
        if (drops == null) {
            return;
        }
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.spawnAsEntity(world, pos, drop);
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }
        int hook = ForgeEventFactory.onHoeUse(stack, player, world, pos);
        if (hook != 0) {
            return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block != Blocks.GRASS && block != Blocks.DIRT && block != Blocks.MYCELIUM) {
            return EnumActionResult.PASS;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, Blocks.FARMLAND.getDefaultState(), 11);
            world.playSound(null, pos, SoundType.GROUND.getStepSound(), SoundCategory.BLOCKS,
                    (SoundType.GROUND.getVolume() + 1.0F) / 2.0F,
                    SoundType.GROUND.getPitch() * 0.8F);
        }
        return EnumActionResult.SUCCESS;
    }
}
