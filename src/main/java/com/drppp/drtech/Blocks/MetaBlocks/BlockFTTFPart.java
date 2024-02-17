package com.drppp.drtech.Blocks.MetaBlocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.ITfftData;
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
            tooltip.add(I18n.format("drtech.universal.tooltip.energy_storage_capacity", Long.toString(batteryType.getCapacity()).replaceAll("(\\d)(?=(\\d{3})+$)", "$1,")));
            tooltip.add(I18n.format("drtech.universal.tooltip.energy_eut", batteryType.getEut()));
        }
    }

    public enum BlockYotTankPartType implements IStringSerializable, ITfftData {

        TFFT_PART_TIER_T1(1,1000000L,1),
        TFFT_PART_TIER_T2(2, 4000000L,2),
        TFFT_PART_TIER_T3(3, 16000000L,5),
        TFFT_PART_TIER_T4(4,64000000L,14),
        TFFT_PART_TIER_T5(5, 256000000L,42),
        TFFT_PART_TIER_T6(6, 2048000000L,132),
        TFFT_PART_TIER_T7(7,131072000000L,429),
        TFFT_PART_TIER_T8(8, 8388608000000L,1430),
        TFFT_PART_TIER_T9(9, 536870912000000L,4862),
        TFFT_PART_TIER_T10(10, 1099511627776000000L,0)
        ;

        private final int tier;
        private final long capacity;
        private final int eut;

        BlockYotTankPartType() {
            this.tier = -1;
            this.capacity = 0;
            this.eut=0;
        }

        BlockYotTankPartType(int tier, long capacity,int eut) {
            this.tier = tier;
            this.capacity = capacity;
            this.eut  =eut;
        }
        @Override
        public int getEut()
        {
            return this.eut;
        }
        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

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
