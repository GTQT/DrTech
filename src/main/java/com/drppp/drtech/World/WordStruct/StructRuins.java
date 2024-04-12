package com.drppp.drtech.World.WordStruct;


import com.drppp.drtech.api.Utils.Pair;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.XSTR;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.items.CapabilityItemHandler;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.drppp.drtech.World.WordStruct.StructUtil.machines;
import static net.minecraft.world.storage.loot.LootTableList.CHESTS_SPAWN_BONUS_CHEST;
import static net.minecraft.world.storage.loot.LootTableList.CHESTS_VILLAGE_BLACKSMITH;

public abstract class StructRuins extends WorldGenerator {

    @SuppressWarnings("unchecked")
    protected Pair<Block, Integer>[][] ToBuildWith = new Pair[4][0];


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void setFloorBlocks(int[] metas, Block... blocks) {
        this.ToBuildWith[0] = new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            this.ToBuildWith[0][i] = new Pair<>(blocks[i % blocks.length], metas[i]);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setWallBlocks(int[] metas, Block... blocks) {
        this.ToBuildWith[1] = new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            this.ToBuildWith[1][i] = new Pair<>(blocks[i % blocks.length], metas[i]);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setRoofBlocks(int[] metas, Block... blocks) {
        this.ToBuildWith[2] = new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            this.ToBuildWith[2][i] = new Pair<>(blocks[i % blocks.length], metas[i]);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setMiscBlocks(int[] metas, Block... blocks) {
        this.ToBuildWith[3] = new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            this.ToBuildWith[3][i] = new Pair<>(blocks[i % blocks.length], metas[i]);
        }
    }

    int[] statBlocks = new int[4];

    protected void setRandomBlockWAirChance(World worldObj, BlockPos pos, Random rand, int airchance,
                                            Pair<Block, Integer>... blocks) {
        if (rand.nextInt(100) > airchance)
            this.setRandomBlock(worldObj, pos, rand, blocks);
        else this.setBlock(worldObj, pos, Blocks.AIR, 0);
    }

    protected void setRandomBlock(World worldObj, BlockPos pos, Random rand, Pair<Block, Integer>... blocks) {
        Block toSet = blocks[rand.nextInt(blocks.length)].getKey();
        int meta = blocks[rand.nextInt(blocks.length)].getValue();
        this.setBlock(worldObj, pos, toSet, meta);
    }

    protected void setBlock(World worldObj, BlockPos pos, Block block, int meta) {
        IBlockState state = block.getStateFromMeta(meta);
        worldObj.setBlockState(pos, state);

    }


    public void setGTMachineBlock(World worldObj,BlockPos pos, int tire) {
        int meta = machines.get(tire).get(new Random().nextInt(machines.get(tire).size()));
        setBlock(worldObj,pos, Block.getBlockFromName("gregtech:machine"),meta);
    }


    protected void setGTCablekWChance(World worldObj, BlockPos pos, Random rand, int airchance, int meta) {
        if (rand.nextInt(100) > airchance) {
            this.setBlock(worldObj, pos, MetaBlocks.CABLES.get("gregtech")[7],meta);
            //MetaTileEntityHolder tile = MetaBlocks.CABLES.get("gregtech")[7];
           // worldObj.setTileEntity(pos,);
        } else this.setBlock(worldObj, pos, Blocks.AIR, 0);
    }


    public static class RuinsBase extends StructRuins {

        private static final String owner = "Ancient Cultures";

        @Override
        public boolean generate(World worldObj, Random rand1, BlockPos pos) {

            for (int i = 0; i < rand1.nextInt(144); i++) {
                rand1.nextLong();
            }

            Random rand = new XSTR(rand1.nextLong());
            SecureRandom secureRandom = new SecureRandom();

            if (worldObj.getBlockState(pos).getBlock() == Blocks.AIR) {
                while (worldObj.getBlockState(pos).getBlock() == Blocks.AIR) {
                    pos = pos.offset(EnumFacing.DOWN);
                }
            }

            this.setFloorBlocks(new int[]{0, 0, 0}, Blocks.BRICK_BLOCK, Blocks.DOUBLE_STONE_SLAB, Blocks.STONEBRICK);
            this.setWallBlocks(new int[]{0, 1, 2, 1, 1}, Blocks.STONEBRICK);
            this.setRoofBlocks(new int[]{9}, Blocks.LOG);
            this.setMiscBlocks(new int[]{1}, Blocks.LOG);
            this.statBlocks = new int[]{rand.nextInt(this.ToBuildWith[0].length)};
            int colored = rand.nextInt(15);
            int tier = secureRandom.nextInt(GTValues.EV);
            boolean useColor = rand.nextBoolean();
            byte set = 0;
            byte toSet = (byte) (rand.nextInt(GTValues.EV - tier) + 1);
            int cablemeta = StructUtil.cableid.get(tier);
            byte treeinaRow = 0;
            boolean lastset = rand.nextBoolean();
            for (int dx = -6; dx <= 6; dx++) {
                for (int dy = 0; dy <= 8; dy++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        this.setBlock(worldObj, pos.add(dx, dy, dz), Blocks.AIR, 0);

                        if (dy == 0) {
                            Pair<Block, Integer> floor = this.ToBuildWith[0][this.statBlocks[0]];
                            this.setBlock(worldObj, pos.add(dx, 0, dz), floor.getKey(), floor.getValue());
                        } else if (dy > 0 && dy < 4) {
                            if (Math.abs(dx) == 5 && Math.abs(dz) == 5) {
                                this.setRandomBlockWAirChance(
                                        worldObj,
                                        new BlockPos(pos.add(dx, dy, dz)),
                                        rand,
                                        5,
                                        this.ToBuildWith[3][0]);
                            } else if (dx == 0 && dz == -5 && (dy == 1 || dy == 2)) {
                                if (dy == 1) {
                                    this.setBlock(worldObj, new BlockPos(pos.add(dx, 1, -5)), Blocks.IRON_DOOR, 1);
                                }
                                if (dy == 2) {
                                    this.setBlock(worldObj, new BlockPos(pos.add(dx, 2, dz)), Blocks.IRON_DOOR, 8);
                                }
                            } else if (Math.abs(dx) == 5 && Math.abs(dz) < 5 || Math.abs(dz) == 5 && Math.abs(dx) < 5) {
                                this.setRandomBlockWAirChance(
                                        worldObj,
                                        new BlockPos(pos.add(dx, dy, dz))
                                        ,
                                        rand,
                                        25,
                                        this.ToBuildWith[1]);
                                if (dy == 2 && rand.nextInt(100) < 12) {
                                    if (useColor) {
                                        this.setRandomBlockWAirChance(
                                                worldObj,
                                                new BlockPos(pos.add(dx, 2, dz)),
                                                rand,
                                                25,
                                                new Pair<>(Blocks.STAINED_GLASS_PANE, colored));
                                    }
                                } else {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, dy, dz)),
                                            rand,
                                            25,
                                            new Pair<>(Blocks.GLASS_PANE, 0));
                                }
                            }

                            if (dy == 3 && Math.abs(dx) == 6) {
                                this.setRandomBlockWAirChance(
                                        worldObj,
                                        new BlockPos(pos.add(dx, 3, dz)),
                                        rand,
                                        25,
                                        this.ToBuildWith[2]);
                            }

                            if (dy == 1) {
                                if (dx == 3 && dz == -3) {
                                    this.setBlock(worldObj, new BlockPos(pos.add(3, 1, dz)), Blocks.CRAFTING_TABLE, 0);
                                }
                                if (dx == -3 && (dz == -3 || dz == -2)) {
                                    this.setBlock(worldObj, new BlockPos(pos.add(-3, dy, dz)), Blocks.CHEST, 5);
                                    TileEntityChest chest = (TileEntityChest) worldObj.getTileEntity(new BlockPos(pos.add(dx, dy, dz)));
                                    if (chest != null) {
                                        //箱子填充物品
                                        List<ItemStack> loots = new ArrayList<>();
                                        for (int i = 0; i < 8; i++) {
                                            var s = machines.get(tier).get(rand.nextInt(machines.get(tier).size()));
                                            loots.add(new ItemStack(Item.getByNameOrId("gregtech:machine") ,1,s));
                                        }
                                        GTTransferUtils.addItemsToItemHandler(chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null),false,loots);
                                        chest.setLootTable(CHESTS_VILLAGE_BLACKSMITH, worldObj.rand.nextLong());

                                    }
                                }

                                if (dx == 4 && dz == 4) {
                                    this.setGTMachineBlock(worldObj, new BlockPos(pos.add(dx, dy, dz)),tier);
                                } else if (dx == 3 && dz == 4) {
                                    if (tier > 0) {

                                        this.setGTMachineBlock(worldObj, new BlockPos(pos.add(dx, dy, dz)),tier);
                                    } else {
                                         this.setGTCablekWChance(worldObj, new BlockPos(pos.add(dx, dy, dz)), rand, 33, cablemeta);
                                    }
                                } else if (dx < 3 && dx > -5 && dz == 4) {
                                    this.setGTCablekWChance(worldObj, new BlockPos(pos.add(dx, dy, dz)), rand, 33, cablemeta);
                                } else if (dx < 3 && dx > -5 && dz == 3 && set < toSet) {
                                    if (!lastset || treeinaRow > 2) {
                                        this.setGTMachineBlock(worldObj, new BlockPos(pos.add(dx, dy, dz)),tier);

                                        set++;
                                        treeinaRow = 0;
                                        lastset = true;
                                    } else {
                                        lastset = rand.nextBoolean();
                                        if (lastset) treeinaRow++;
                                    }
                                }
                            }
                        } else switch (dy) {
                            case 4:
                                if (Math.abs(dx) == 5) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, 4, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[2]);
                                    break;
                                }
                                if (Math.abs(dz) == 5 && Math.abs(dx) < 5) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, dy, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[1]);
                                }
                                break;
                            case 5:
                                if (Math.abs(dx) == 4) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, 5, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[2]);
                                    break;
                                }
                                if (Math.abs(dz) == 5 && Math.abs(dx) < 4) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, dy, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[1]);
                                }
                                break;
                            case 6:
                                if (Math.abs(dx) == 3) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, 6, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[2]);
                                    break;
                                }
                                if (Math.abs(dz) == 5 && Math.abs(dx) < 3) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, dy, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[1]);
                                }
                                break;
                            case 7:
                                if (Math.abs(dx) == 2) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, 7, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[2]);
                                    break;
                                }
                                if (Math.abs(dz) == 5 && Math.abs(dx) < 2) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, dy, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[1]);
                                }
                                break;
                            case 8:
                                if (Math.abs(dx) == 1 || Math.abs(dx) == 0) {
                                    this.setRandomBlockWAirChance(
                                            worldObj,
                                            new BlockPos(pos.add(dx, 8, dz)),
                                            rand,
                                            25,
                                            this.ToBuildWith[2]);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            tosetloop:
            while (set < toSet) {
                int dy = 1;
                int dz = 3;
                for (int dx = 2; dx > -5; dx--) {
                    if (set >= toSet) {
                        break tosetloop;
                    }
                    if (!lastset || treeinaRow > 2 && worldObj.getTileEntity(new BlockPos(pos.add(dx, dy, dz))) == null) {
                        this.setGTMachineBlock(worldObj, new BlockPos(pos.add(dx, dy, dz)),tier);

                        set++;
                        treeinaRow = 0;
                        lastset = true;
                    } else {
                        lastset = rand.nextBoolean();
                        if (lastset) {
                            treeinaRow++;
                        }
                    }
                }
            }
            return true;
        }
    }
}

