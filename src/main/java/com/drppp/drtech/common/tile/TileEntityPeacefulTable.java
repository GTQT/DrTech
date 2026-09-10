package com.drppp.drtech.common.tile;

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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TileEntityPeacefulTable extends TileEntity implements ITickable {

    private static final int WORK_INTERVAL = 200;
    private static final int LOOT_ROLLS = 3;
    private static final String LOOT_TABLE_PREFIX = "entities/";

    private int tick = 0;

    @Override
    public void update() {
        if (world == null || world.isRemote || world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            return;
        }
        if (++tick <= WORK_INTERVAL) {
            return;
        }
        tick = 0;

        IItemHandler handler = getOutputHandler();
        if (handler == null) {
            return;
        }

        Map<ResourceLocation, Integer> lootTables = collectMonsterLootTables();
        if (lootTables.isEmpty()) {
            return;
        }

        Random random = world.rand;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!(stack.getItem() instanceof ItemSword)) {
                continue;
            }
            ResourceLocation lootTable = pickWeighted(lootTables, random);
            if (lootTable != null) {
                collectDrops(lootTable, handler, stack, slot, random);
            }
        }
    }

    @Nullable
    private IItemHandler getOutputHandler() {
        TileEntity outputTile = world.getTileEntity(pos.offset(EnumFacing.UP));
        return outputTile == null
                ? null
                : outputTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    private Map<ResourceLocation, Integer> collectMonsterLootTables() {
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
        return tables;
    }

    /**
     * Weighted random pick without materializing an expanded list.
     * Avoids allocating a list sized by the sum of all spawn weights every tick.
     */
    @Nullable
    private static ResourceLocation pickWeighted(Map<ResourceLocation, Integer> tables, Random random) {
        int totalWeight = 0;
        for (int weight : tables.values()) {
            totalWeight += weight;
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (Map.Entry<ResourceLocation, Integer> entry : tables.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    private ResourceLocation getLootTableForSpawnEntry(Biome.SpawnListEntry spawnEntry) {
        if (spawnEntry == null || spawnEntry.entityClass == null) {
            return null;
        }
        if (!EntityLiving.class.isAssignableFrom(spawnEntry.entityClass)
                || !IMob.class.isAssignableFrom(spawnEntry.entityClass)) {
            return null;
        }

        EntityEntry entityEntry = EntityRegistry.getEntry(spawnEntry.entityClass);
        if (entityEntry == null || entityEntry.getRegistryName() == null) {
            return null;
        }

        ResourceLocation entityId = entityEntry.getRegistryName();
        return new ResourceLocation(entityId.getNamespace(), LOOT_TABLE_PREFIX + entityId.getPath());
    }

    private void collectDrops(ResourceLocation lootTableLocation, IItemHandler handler,
                              ItemStack sword, int slot, Random random) {
        if (!(world instanceof WorldServer)) {
            return;
        }

        LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(lootTableLocation);
        LootContext lootContext = new LootContext.Builder((WorldServer) world).build();
        List<ItemStack> allDrops = new ArrayList<>();

        for (int i = 0; i < LOOT_ROLLS; i++) {
            allDrops.addAll(lootTable.generateLootForPools(random, lootContext));
            if (!damageSword(handler, sword, slot, random)) {
                break;
            }
        }

        if (!allDrops.isEmpty()) {
            GTTransferUtils.addItemsToItemHandler(handler, false, allDrops);
        }
    }

    /**
     * Damage the sword by one point with 50% probability.
     *
     * @return true if the sword stack still occupies its slot, false if it was consumed.
     */
    private boolean damageSword(IItemHandler handler, ItemStack sword, int slot, Random random) {
        if (!random.nextBoolean()) {
            return true;
        }
        sword.setItemDamage(sword.getItemDamage() + 1);
        if (sword.getItemDamage() >= sword.getMaxDamage()) {
            handler.extractItem(slot, 1, false);
            return false;
        }
        return true;
    }
}