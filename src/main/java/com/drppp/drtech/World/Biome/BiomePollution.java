package com.drppp.drtech.World.Biome;

import com.drppp.drtech.common.Entity.moster.EntityUTiGolem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class BiomePollution extends Biome {
    public BiomePollution() {
        super(new Biome.BiomeProperties("Pollution_Biome").setWaterColor(0x3DFF00));
        // 清除所有自然生成的生物列表
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();

        // 添加你希望在该自定义维度中生成的怪物
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityZombie.class, 100, 1, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 100, 1, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityCreeper.class, 100, 1, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWitherSkeleton.class, 100, 1, 1));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityUTiGolem.class, 10, 1, 2));

    }

    @Override
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return new WorldGenTrees(false);
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {

    }
}
