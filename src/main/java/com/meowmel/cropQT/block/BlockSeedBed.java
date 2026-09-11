package com.meowmel.cropQT.block;

import com.drppp.drtech.DrTechMain;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * 工业农场的种子床。
 *
 * <p>农场每一段中间那一行都是它——代表"这一层能种多少作物的地"。
 *
 * <p>只有一种：农场等级由<b>控制器的输入电压</b>决定，组件不再按 tier 分档，
 * 所以一个 meta 就够（源端按 MV~UXV 分了 12 档，我们不需要）。
 */
public class BlockSeedBed extends VariantBlock<BlockSeedBed.SeedBedType> {

    public BlockSeedBed() {
        super(Material.IRON);
        setTranslationKey("drtech.seed_bed");
        setHardness(3.0f);
        setResistance(6.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(SeedBedType.SEED_BED));
        setRegistryName("seed_bed");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
                                    @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum SeedBedType implements IStringSerializable {
        SEED_BED("seed_bed");

        private final String name;

        SeedBedType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }
    }
}
