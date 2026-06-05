package com.drppp.drtech.lootgames.api.util;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameUtils {

    public static void spawnLootChest(World world, BlockPos centralPos, int offsetX, int offsetZ, SpawnChestInfo chestInfo) {
        if (world.isRemote) {
            return;
        }

        EnumFacing facing;
        if (offsetX == 0 && offsetZ == -1) facing = EnumFacing.SOUTH;
        else if (offsetX == 0 && offsetZ == 1) facing = EnumFacing.NORTH;
        else if (offsetX == 1 && offsetZ == 0) facing = EnumFacing.WEST;
        else facing = EnumFacing.EAST;

        IBlockState chest = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, facing);
        BlockPos placePos = centralPos.add(offsetX, 0, offsetZ);
        world.setBlockState(placePos, chest);

        TileEntityChest teChest = (TileEntityChest) Objects.requireNonNull(world.getTileEntity(placePos));
        teChest.setLootTable(chestInfo.getLootTableRL(), 0);
        teChest.fillWithLoot(null);

        List<Integer> notEmptyIndexes = new ArrayList<>();
        for (int i = 0; i < teChest.getSizeInventory(); i++) {
            if (teChest.getStackInSlot(i) != ItemStack.EMPTY) {
                notEmptyIndexes.add(i);
            }
        }

        if (notEmptyIndexes.isEmpty()) {
            ItemStack stack = new ItemStack(Blocks.STONE);
            try {
                stack.setTagCompound(JsonToNBT.getTagFromJson(
                        String.format("{display:{Name:\"The Sorry Stone\",Lore:[\"Loot table not configured properly.\",\"Please report that LootList [%s] is broken.\"]}}", chestInfo.getLootTableRL())));
            } catch (NBTException e) {
                e.printStackTrace();
            }
            teChest.setInventorySlotContents(0, stack);
            return;
        }

        int minItems = chestInfo.getMinItems();
        int maxItems = chestInfo.getMaxItems();

        if (minItems != -1 || maxItems != -1) {
            Random rand = FMLCommonHandler.instance().getMinecraftServerInstance() != null
                    ? world.rand : new Random();
            int min = minItems == -1 ? 0 : minItems;
            int extra = (maxItems == -1 ? notEmptyIndexes.size() : maxItems) - minItems;
            int itemCount = extra < 1 ? min : min + rand.nextInt(extra);

            if (itemCount < notEmptyIndexes.size()) {
                int[] itemsRemain = new int[itemCount];
                for (int i = 0; i < itemsRemain.length; i++) {
                    int itemRemainArrIndex = rand.nextInt(notEmptyIndexes.size());
                    int itemRemainChestIndex = notEmptyIndexes.get(itemRemainArrIndex);
                    notEmptyIndexes.remove(itemRemainArrIndex);
                    itemsRemain[i] = itemRemainChestIndex;
                }

                for (int i = 0; i < teChest.getSizeInventory(); i++) {
                    boolean toDelete = true;
                    for (int i1 : itemsRemain) {
                        if (i == i1) {
                            toDelete = false;
                            break;
                        }
                    }
                    if (toDelete) {
                        teChest.removeStackFromSlot(i);
                    }
                }
            }
        }
    }

    public static class SpawnChestInfo {
        private final ResourceLocation lootTableRL;
        private final int minItems;
        private final int maxItems;

        public SpawnChestInfo(ResourceLocation lootTableRL, int minItems, int maxItems) {
            this.lootTableRL = lootTableRL;
            this.minItems = minItems;
            this.maxItems = maxItems;
        }

        public ResourceLocation getLootTableRL() {
            return lootTableRL;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public int getMinItems() {
            return minItems;
        }
    }
}
