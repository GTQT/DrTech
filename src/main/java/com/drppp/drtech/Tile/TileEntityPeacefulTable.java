package com.drppp.drtech.Tile;

import gregtech.api.util.GTTransferUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TileEntityPeacefulTable extends TileEntity implements ITickable {

    private static final int WORK_INTERVAL = 200;
    private static final int LOOT_ROLLS = 3;

    private int tick = 0;

    @Override
    public void update() {
        if (world == null || world.isRemote || world.getDifficulty() != EnumDifficulty.PEACEFUL || tick++ <= WORK_INTERVAL) {
            return;
        }
        tick = 0;

        TileEntity outputTile = world.getTileEntity(pos.offset(EnumFacing.UP));
        if (outputTile == null || !outputTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            return;
        }

        IItemHandler handler = outputTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler == null) {
            return;
        }

        List<ResourceLocation> lootTables = getCurrentDimensionMonsterLootTables();
        if (lootTables.isEmpty()) {
            return;
        }

        Random random = world.rand;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!(stack.getItem() instanceof ItemSword)) {
                continue;
            }

            ResourceLocation lootTable = lootTables.get(random.nextInt(lootTables.size()));
            collectDrops(lootTable, handler, stack, i, random);
        }
    }

    private List<ResourceLocation> getCurrentDimensionMonsterLootTables() {
        Map<ResourceLocation, Integer> tables = new LinkedHashMap<>();
        for (Biome biome : world.getBiomeProvider().getBiomesToSpawnIn()) {
            for (Biome.SpawnListEntry spawnEntry : biome.getSpawnableList(EnumCreatureType.MONSTER)) {
                ResourceLocation lootTable = getLootTableForSpawnEntry(spawnEntry);
                if (lootTable == null) {
                    continue;
                }
                tables.merge(lootTable, Math.max(1, spawnEntry.itemWeight), Math::max);
            }
        }

        List<ResourceLocation> weightedTables = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Integer> entry : tables.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                weightedTables.add(entry.getKey());
            }
        }
        return weightedTables;
    }

    private ResourceLocation getLootTableForSpawnEntry(Biome.SpawnListEntry spawnEntry) {
        if (spawnEntry == null || spawnEntry.entityClass == null) {
            return null;
        }
        if (!EntityLiving.class.isAssignableFrom(spawnEntry.entityClass) || !IMob.class.isAssignableFrom(spawnEntry.entityClass)) {
            return null;
        }

        EntityEntry entityEntry = EntityRegistry.getEntry(spawnEntry.entityClass);
        if (entityEntry == null || entityEntry.getRegistryName() == null) {
            return null;
        }

        ResourceLocation entityId = entityEntry.getRegistryName();
        return new ResourceLocation(entityId.getNamespace(), "entities/" + entityId.getPath());
    }

    private void collectDrops(ResourceLocation lootTableLocation, IItemHandler handler, ItemStack sword, int slot, Random random) {
        if (!(world instanceof WorldServer)) {
            return;
        }

        LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(lootTableLocation);
        LootContext lootContext = new LootContext.Builder((WorldServer) world).build();
        List<ItemStack> allDrops = new ArrayList<>();

        for (int i = 0; i < LOOT_ROLLS; i++) {
            allDrops.addAll(lootTable.generateLootForPools(random, lootContext));
            damageSword(handler, sword, slot, random);
        }

        if (!allDrops.isEmpty()) {
            GTTransferUtils.addItemsToItemHandler(handler, false, allDrops);
        }
    }

    private void damageSword(IItemHandler handler, ItemStack sword, int slot, Random random) {
        if (random.nextBoolean()) {
            sword.setItemDamage(sword.getItemDamage() + 1);
            if (sword.getItemDamage() >= sword.getMaxDamage()) {
                handler.extractItem(slot, 1, false);
            }
        }
    }
}
