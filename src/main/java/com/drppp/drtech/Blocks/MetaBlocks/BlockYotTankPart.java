package com.drppp.drtech.Blocks.MetaBlocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import gregtech.api.GTValues;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.multiblock.IBatteryData;
import gregtech.common.blocks.BlockBatteryPart;
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
        if (batteryType.getCapacity() != 0) {
            tooltip.add(I18n.format("drtech.universal.tooltip.energy_storage_capacity", batteryType.getCapacity()));
        }
    }

    public enum BlockYotTankPartType implements IStringSerializable, IBatteryData {

        YOT_PART_TIER_T1(1,1000000L),
        YOT_PART_TIER_T2(2, 100000000L),
        YOT_PART_TIER_T3(3, 10000000000L),
        YOT_PART_TIER_T4(4,1000000000000L),
        YOT_PART_TIER_T5(5, 100000000000000L),
        YOT_PART_TIER_T6(6, 10000000000000000L),
        YOT_PART_TIER_T7(7,1000000000000000000L),
        YOT_PART_TIER_T8(8, Long.MAX_VALUE),
        ;

        private final int tier;
        private final long capacity;

        BlockYotTankPartType() {
            this.tier = -1;
            this.capacity = 0;
        }

        BlockYotTankPartType(int tier, long capacity) {
            this.tier = tier;
            this.capacity = capacity;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        // must be separately named because of reobf issue
        @NotNull
        @Override
        public String getBatteryName() {
            return name().toLowerCase();
        }

        @NotNull
        @Override
        public String getName() {
            return getBatteryName();
        }
    }
}
