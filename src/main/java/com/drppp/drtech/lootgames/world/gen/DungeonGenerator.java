package com.drppp.drtech.lootgames.world.gen;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.lootgames.block.BlockDungeonBricks;
import com.drppp.drtech.lootgames.block.BlockDungeonLamp;
import com.drppp.drtech.lootgames.registry.ModBlocks;

import java.util.Random;

public class DungeonGenerator {
    public static final int PUZZLEROOM_CENTER_TO_BORDER = 10;
    public static final int PUZZLEROOM_HEIGHT = 8;
    public static final int PUZZLEROOM_SURFACE_DISTANCE = 2;
    public static final int PUZZLEROOM_MASTER_TE_OFFSET = 3;

    private World world;
    private int centerX;
    private int centerZ;
    private int surfaceLevel = -1;
    private int dungeonTop = -1;
    private int dungeonBottom = -1;

    private static final Material[] INVALID_MATERIALS = {
            Material.WOOD, Material.WATER, Material.CACTUS,
            Material.SNOW, Material.GRASS, Material.LEAVES, Material.PLANTS, Material.AIR, Material.LAVA, Material.PORTAL
    };

    /**
     * Converts shielded floor blocks back to regular floor.
     * Used when a game starts to make the playfield interactive.
     */
    public static void resetUnbreakablePlayfield(World world, BlockPos floorPos) {
        if (!resetUnbreakableFieldsStartingFrom(world, floorPos)) {
            resetUnbreakableFieldsStartingFrom(world, floorPos.add(1, 0, 0));
            resetUnbreakableFieldsStartingFrom(world, floorPos.add(-1, 0, 0));
            resetUnbreakableFieldsStartingFrom(world, floorPos.add(0, 0, 1));
            resetUnbreakableFieldsStartingFrom(world, floorPos.add(0, 0, -1));
        }
    }

    private static boolean resetUnbreakableFieldsStartingFrom(World world, BlockPos blockPos) {
        IBlockState state = world.getBlockState(blockPos);
        if (state.getBlock() == ModBlocks.DUNGEON_BRICKS
                && state.getValue(BlockDungeonBricks.VARIANT) == BlockDungeonBricks.EnumType.DUNGEON_FLOOR_SHIELDED) {
            world.setBlockState(blockPos, state.withProperty(BlockDungeonBricks.VARIANT, BlockDungeonBricks.EnumType.DUNGEON_FLOOR));

            resetUnbreakableFieldsStartingFrom(world, blockPos.add(1, 0, 0));
            resetUnbreakableFieldsStartingFrom(world, blockPos.add(-1, 0, 0));
            resetUnbreakableFieldsStartingFrom(world, blockPos.add(0, 0, 1));
            resetUnbreakableFieldsStartingFrom(world, blockPos.add(0, 0, -1));

            return true;
        }
        return false;
    }

    /**
     * Check if a block is valid ground (not water, plants, air, lava, etc.)
     */
    private boolean isValidSurfaceBlock(IBlockState block) {
        for (Material m : INVALID_MATERIALS) {
            if (block.getMaterial() == m) return false;
        }
        return true;
    }

    /**
     * Scans the entire dungeon area from y=128 downward to find a continuous solid surface.
     */
    private boolean setSurfaceLevel(int x, int z) {
        boolean yLevelCheckPassed = false;

        for (int surfaceY = 128; surfaceY > 20; surfaceY--) {
            yLevelCheckPassed = true;

            for (int axisX = -PUZZLEROOM_CENTER_TO_BORDER; axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
                for (int axisZ = -PUZZLEROOM_CENTER_TO_BORDER; axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                    IBlockState state = world.getBlockState(new BlockPos(axisX + x, surfaceY, axisZ + z));
                    if (!isValidSurfaceBlock(state)) {
                        yLevelCheckPassed = false;
                        break;
                    }
                }
                if (!yLevelCheckPassed) break;
            }

            if (yLevelCheckPassed) {
                surfaceLevel = surfaceY;
                dungeonTop = surfaceLevel - PUZZLEROOM_SURFACE_DISTANCE;
                dungeonBottom = surfaceLevel - (PUZZLEROOM_HEIGHT + PUZZLEROOM_SURFACE_DISTANCE);
                break;
            }
        }

        return yLevelCheckPassed;
    }

    public boolean generateDungeon(World world, int x, int z) {
        return doGenDungeon(world, x, z);
    }

    private boolean doGenDungeon(World world, int x, int z) {
        this.world = world;
        centerX = x;
        centerZ = z;

        if (!setSurfaceLevel(x, z)) {
            DrTechMain.LOGGER.debug("Can't spawn PuzzleDungeon. Y level not found at X={} Z={}", x, z);
            return false;
        }

        if (!checkForFreeSpace()) {
            DrTechMain.LOGGER.debug("PuzzleDungeon not spawned at X/Z {} {} - space not suitable", x, z);
            return false;
        }

        Random rand = world.rand;

        // Build the dungeon room
        for (int axisX = -PUZZLEROOM_CENTER_TO_BORDER; axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
            for (int axisZ = -PUZZLEROOM_CENTER_TO_BORDER; axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                for (int axisY = dungeonBottom; axisY <= dungeonTop; axisY++) {
                    BlockPos pos = new BlockPos(axisX + centerX, axisY, axisZ + centerZ);

                    // Clear space first
                    placeBlock(pos, Blocks.AIR.getDefaultState());

                    // Static center block with master TE
                    if (axisX == 0 && axisZ == 0 && axisY == dungeonBottom + PUZZLEROOM_MASTER_TE_OFFSET) {
                        placeBlock(pos, ModBlocks.PUZZLE_MASTER.getDefaultState());
                    }
                    // Bottom layer (floor)
                    else if (axisY == dungeonBottom) {
                        boolean cracked = rand.nextInt(100) < 10;
                        int meta = cracked
                                ? BlockDungeonBricks.EnumType.DUNGEON_FLOOR_CRACKED.getMeta()
                                : BlockDungeonBricks.EnumType.DUNGEON_FLOOR.getMeta();
                        placeBlock(pos, ModBlocks.DUNGEON_BRICKS.getStateFromMeta(meta));
                    }
                    // Top layer (ceiling)
                    else if (axisY == dungeonTop) {
                        boolean cracked = rand.nextInt(100) < 10;
                        int meta = cracked
                                ? BlockDungeonBricks.EnumType.DUNGEON_CEILING_CRACKED.getMeta()
                                : BlockDungeonBricks.EnumType.DUNGEON_CEILING.getMeta();
                        placeBlock(pos, ModBlocks.DUNGEON_BRICKS.getStateFromMeta(meta));
                    }
                    // Playfield placeholder (shielded, so player doesn't stand inside blocks)
                    else if (axisY == dungeonBottom + 1) {
                        placeBlock(pos, ModBlocks.DUNGEON_BRICKS.getStateFromMeta(
                                BlockDungeonBricks.EnumType.DUNGEON_FLOOR_SHIELDED.getMeta()));
                    }
                    // Walls + lamps
                    else if (axisX == -PUZZLEROOM_CENTER_TO_BORDER || axisX == PUZZLEROOM_CENTER_TO_BORDER
                            || axisZ == -PUZZLEROOM_CENTER_TO_BORDER || axisZ == PUZZLEROOM_CENTER_TO_BORDER) {
                        int midY = dungeonTop - (int) Math.floor(PUZZLEROOM_HEIGHT / 2F);
                        if (axisY == midY) {
                            boolean broken = rand.nextInt(100) < 10;
                            placeBlock(pos, ModBlocks.DUNGEON_LAMP.getDefaultState()
                                    .withProperty(BlockDungeonLamp.BROKEN, broken));
                        } else {
                            boolean cracked = rand.nextInt(100) < 10;
                            int meta = cracked
                                    ? BlockDungeonBricks.EnumType.DUNGEON_WALL_CRACKED.getMeta()
                                    : BlockDungeonBricks.EnumType.DUNGEON_WALL.getMeta();
                            placeBlock(pos, ModBlocks.DUNGEON_BRICKS.getStateFromMeta(meta));
                        }
                    }
                }
            }
        }

        DrTechMain.LOGGER.info("PuzzleDungeon spawned at {} {} {} in Dimension {}",
                centerX, dungeonBottom, centerZ, world.provider.getDimension());

        // === Generate entrance staircase ===
        IBlockState entrance;
        BlockPos pos;
        int axisZ = PUZZLEROOM_CENTER_TO_BORDER;
        int axisY = dungeonBottom + 2;
        do {
            axisZ++;

            for (int stairX = centerX; stairX <= centerX; stairX++) {
                for (int stairY = dungeonBottom + 2; stairY <= dungeonBottom + 4; stairY++) {
                    BlockPos blockPos = new BlockPos(stairX, stairY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + centerZ);
                    IBlockState currentState = this.world.getBlockState(blockPos);
                    if (currentState.getMaterial().isSolid()) {
                        placeBlock(blockPos, Blocks.AIR.getDefaultState());
                    } else {
                        break;
                    }
                }
            }

            pos = new BlockPos(centerX, axisY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + centerZ);
            entrance = this.world.getBlockState(pos);

            if ((axisZ - PUZZLEROOM_CENTER_TO_BORDER) >= 15) break;
        } while (entrance != Blocks.AIR || !this.world.canBlockSeeSky(
                new BlockPos(centerX, axisY + (axisZ - PUZZLEROOM_CENTER_TO_BORDER), axisZ + centerZ)));

        return true;
    }

    /**
     * Checks the entire dungeon volume for TileEntities that would block generation.
     */
    private boolean checkForFreeSpace() {
        for (int axisX = -PUZZLEROOM_CENTER_TO_BORDER; axisX <= PUZZLEROOM_CENTER_TO_BORDER; axisX++) {
            for (int axisZ = -PUZZLEROOM_CENTER_TO_BORDER; axisZ <= PUZZLEROOM_CENTER_TO_BORDER; axisZ++) {
                for (int axisY = dungeonBottom; axisY <= dungeonTop; axisY++) {
                    BlockPos pos = new BlockPos(axisX + centerX, axisY, axisZ + centerZ);
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null) {
                        DrTechMain.LOGGER.debug("Block at {} {} {} has a TileEntity. Skipping dungeon gen.",
                                axisX + centerX, axisY, axisZ + centerZ);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeBlock(BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 3);
    }
}
