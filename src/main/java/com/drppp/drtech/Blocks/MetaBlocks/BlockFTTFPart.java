package com.drppp.drtech.Blocks.MetaBlocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.multiblock.IBatteryData;
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

public class BlockFTTFPart extends VariantBlock<BlockFTTFPart.BlockYotTankPartType> {

    public BlockFTTFPart() {
        super(Material.IRON);
        setTranslationKey("tfft_part");
        setRegistryName(Tags.MODID,"tfft_part");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 3); // Diamond level, can be mined by a steel wrench or better
        setDefaultState(getState(BlockYotTankPartType.TFFT_PART_TIER_T1));
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

        TFFT_PART_TIER_T1(1,1000000L),
        TFFT_PART_TIER_T2(2, 4000000L),
        TFFT_PART_TIER_T3(3, 16000000L),
        TFFT_PART_TIER_T4(4,64000000L),
        TFFT_PART_TIER_T5(5, 256000000L),
        TFFT_PART_TIER_T6(6, 2048000000L),
        TFFT_PART_TIER_T7(7,131072000000L),
        TFFT_PART_TIER_T8(8, 8388608000000L),
        TFFT_PART_TIER_T9(9, 536870912000000L),
        TFFT_PART_TIER_T10(10, 1099511627776000000L)
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
