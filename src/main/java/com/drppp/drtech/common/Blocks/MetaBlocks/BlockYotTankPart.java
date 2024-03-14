package com.drppp.drtech.common.Blocks.MetaBlocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.IStoreData;
import com.drppp.drtech.Tags;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;

public class BlockYotTankPart extends VariantBlock<BlockYotTankPart.BlockYotTankPartType> {

    public BlockYotTankPart() {
        super(Material.IRON);
        setTranslationKey("yot_tank_part");
        setRegistryName(Tags.MODID,"yot_tank_part");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 3); // Diamond level, can be mined by a steel wrench or better
        setDefaultState(getState(BlockYotTankPartType.YOT_PART_TIER_T1));
        setCreativeTab(DrTechMain.Mytab);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType placementType) {
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);

        BlockYotTankPartType batteryType = getState(stack);
        if (batteryType.getCapacity().compareTo(BigInteger.ZERO)==1) {
            tooltip.add(I18n.format("drtech.universal.tooltip.energy_storage_capacity", batteryType.getCapacity().toString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1,")));
        }
    }

    public enum BlockYotTankPartType implements IStringSerializable, IStoreData {

        YOT_PART_TIER_T1(1,"1000000"),
        YOT_PART_TIER_T2(2, "100000000"),
        YOT_PART_TIER_T3(3, "10000000000"),
        YOT_PART_TIER_T4(4,"1000000000000"),
        YOT_PART_TIER_T5(5, "100000000000000"),
        YOT_PART_TIER_T6(6, "10000000000000000"),
        YOT_PART_TIER_T7(7,"1000000000000000000"),
        YOT_PART_TIER_T8(8, "100000000000000000000"),
        YOT_PART_TIER_T9(9, "10000000000000000000000"),
        YOT_PART_TIER_T10(10, "1000000000000000000000000")
        ;

        private final int tier;
        private final BigInteger capacity;

        BlockYotTankPartType() {
            this.tier = -1;
            this.capacity = new BigInteger("0");
        }

        BlockYotTankPartType(int tier, String capacity) {
            this.tier = tier;
            this.capacity = new BigInteger(capacity);
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public BigInteger getCapacity() {
            return capacity;
        }

        // must be separately named because of reobf issue
        @NotNull
        @Override
        public String getStoreName() {
            return name().toLowerCase();
        }

        @NotNull
        @Override
        public String getName() {
            return getStoreName();
        }
    }
}
