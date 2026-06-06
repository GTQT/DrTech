package com.drppp.drtech.lootgames.minigame.minesweeper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.lootgames.api.minigame.ILootGameFactory;
import com.drppp.drtech.lootgames.api.minigame.LootGame;
import com.drppp.drtech.lootgames.api.util.GameUtils;
import com.drppp.drtech.lootgames.loot.ModLootTables;
import com.drppp.drtech.lootgames.api.util.NBTUtils;
import com.drppp.drtech.lootgames.api.util.Pos2i;
import com.drppp.drtech.lootgames.block.BlockDungeonLamp;
import com.drppp.drtech.lootgames.minigame.minesweeper.MSBoard.MSField;
import com.drppp.drtech.lootgames.minigame.minesweeper.block.BlockMSActivator;
import com.drppp.drtech.lootgames.minigame.minesweeper.task.TaskMSCreateExplosion;
import com.drppp.drtech.lootgames.registry.ModBlocks;
import com.drppp.drtech.Client.Sound.SoundManager;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMineSweeper extends LootGame {

    @SideOnly(Side.CLIENT)
    public boolean cIsGenerated;
    @SideOnly(Side.CLIENT)
    public int detonationTimeInTicks;

    private int currentLevel = 1;
    private MSBoard board;
    private Stage stage;
    private int ticks;
    private int attemptCount = 0;
    private boolean playRevealNeighboursSound = true;
    private final Random rand = new Random();

    // Config
    private static final int detonationTime = 60;
    private static final int maxAttempts = 3;

    public GameMineSweeper(int boardSize, int bombCount) {
        board = new MSBoard(boardSize, bombCount);
        stage = Stage.WAITING;
        setOnWin(this::onGameWon);
        setOnLose(this::onGameLost);
    }

    public static Pos2i convertToGamePos(BlockPos masterPos, BlockPos subordinatePos) {
        BlockPos temp = subordinatePos.subtract(masterPos);
        return new Pos2i(temp.getX(), temp.getZ());
    }

    @Override
    public void onTick() {
        super.onTick();
        if (isServerSide()) {
            if (stage == Stage.DETONATING) {
                if (ticks >= detonationTime) {
                    onDetonateTimePassed();
                }
                ticks++;
            } else if (stage == Stage.EXPLODING) {
                ticks--;
                if (ticks <= 0) {
                    // Send message to nearby players
                    BlockPos center = getCentralGamePos();
                    DrTechMain.LOGGER.info("New attempt available for minesweeper at {}", center);

                    updateStage(Stage.WAITING);
                    board.resetBoard();
                    sendUpdatePacket("reset_flag_counter", new NBTTagCompound());
                    saveDataAndSendToClient();
                }
            }
        } else {
            if (stage == Stage.DETONATING) {
                ticks++;
            }
        }
    }

    @Override
    protected BlockPos getRoomFloorPos() { return getMasterPos(); }

    @Override
    protected BlockPos getCentralRoomPos() { return getCentralGamePos(); }

    public void onFieldClicked(Pos2i clickedPos, boolean sneaking) {
        getWorld().playSound(null, convertToBlockPos(clickedPos), SoundEvents.BLOCK_NOTE_HAT, SoundCategory.MASTER, 0.6F, 0.8F);
        if (!board.isGenerated()) {
            generateBoard(clickedPos);
        } else {
            playRevealNeighboursSound = true;
            if (sneaking) {
                if (board.isHidden(clickedPos)) {
                    swapFieldMark(clickedPos);
                } else {
                    revealAllNeighbours(clickedPos, false);
                }
            } else {
                if (board.getMark(clickedPos) == MSField.NO_MARK) {
                    revealField(clickedPos);
                }
            }
        }
    }

    public boolean isBoardGenerated() { return board.isGenerated(); }
    public int getBoardSize() { return board.size(); }

    public void generateBoard(Pos2i clickedPos) {
        board.generate(clickedPos);
        sendUpdatePacket("gen_board", writeNBTForClient());
        if (board.getType(clickedPos) == MSField.EMPTY) {
            revealAllNeighbours(clickedPos, true);
        }
        saveDataAndSendToClient();
    }

    public void revealField(Pos2i pos) {
        if (stage == Stage.WAITING && board.isHidden(pos)) {
            board.reveal(pos);
            int type = board.getType(pos);

            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("x", pos.getX());
            c.setInteger("y", pos.getY());
            c.setInteger("type", type);
            c.setBoolean("hidden", false);
            c.setInteger("mark", MSField.NO_MARK);
            sendUpdatePacket("field_changed", c);

            if (type == MSField.EMPTY) {
                if (playRevealNeighboursSound) {
                    getWorld().playSound(null, convertToBlockPos(pos), SoundManager.msOnEmptyRevealNeighbours, SoundCategory.MASTER, 0.6F, 1.0F);
                    playRevealNeighboursSound = false;
                }
                revealAllNeighbours(pos, true);
            } else if (type == MSField.BOMB) {
                getWorld().playSound(null, convertToBlockPos(pos), SoundManager.msBombActivated, SoundCategory.MASTER, 0.6F, 1.0F);
                onBombTriggered();
            }

            saveData();
            if (board.checkWin()) {
                onLevelSuccessfullyFinished();
            }
        }
    }

    public void swapFieldMark(Pos2i pos) {
        if (stage == Stage.WAITING && board.isHidden(pos)) {
            board.swapMark(pos);
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("x", pos.getX());
            c.setInteger("y", pos.getY());
            c.setInteger("type", MSField.EMPTY);
            c.setBoolean("hidden", true);
            c.setInteger("mark", board.getMark(pos));
            sendUpdatePacket("field_changed", c);
            saveData();
            if (board.checkWin()) {
                onLevelSuccessfullyFinished();
            }
        }
    }

    private void revealAllNeighbours(Pos2i mainPos, boolean revealMarked) {
        if (!revealMarked) {
            if (board.isHidden(mainPos)) {
                return;
            }
            int bombsAround = board.getType(mainPos);
            if (bombsAround < 1) return;

            int marked = 0;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    Pos2i pos = mainPos.add(x, y);
                    if (board.hasFieldOn(pos) && board.getMark(pos) == MSField.FLAG) {
                        marked++;
                    }
                }
            }
            if (marked != bombsAround) return;
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                Pos2i pos = mainPos.add(x, y);
                if (board.hasFieldOn(pos) && board.isHidden(pos)) {
                    if (revealMarked || board.getMark(pos) != MSField.FLAG) {
                        revealField(pos);
                    }
                }
            }
        }
    }

    private void onBombTriggered() {
        updateStage(Stage.DETONATING);
        board.forEach((x, y) -> {
            if (board.isBomb(x, y)) {
                board.reveal(x, y);
            }
        });
        saveDataAndSendToClient();
        attemptCount++;
    }

    private void onDetonateTimePassed() {
        if (attemptCount < maxAttempts) {
            int longestDetTime = detonateBoard(currentLevel + 3, false);
            updateStage(Stage.EXPLODING);
            ticks = longestDetTime + 40;
        } else {
            if (currentLevel > 1) {
                winGame();
            } else {
                loseGame();
            }
        }
    }

    private void onLevelSuccessfullyFinished() {
        if (currentLevel < 4) {
            sendUpdatePacket("spawn_particles_level_beat", null);
            getWorld().playSound(null, getCentralGamePos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.75F, 1.0F);

            masterTileEntity.destroyGameBlocks();
            BlockMSActivator.generateGameStructure(getWorld(), getCentralGamePos(), currentLevel + 1);

            int size;
            int bombCount;
            switch (currentLevel + 1) {
                case 2: size = 9; bombCount = 12; break;
                case 3: size = 11; bombCount = 20; break;
                case 4: size = 13; bombCount = 30; break;
                default: size = 7; bombCount = 6; break;
            }

            BlockPos startPos = getCentralGamePos().add(-size / 2, 0, -size / 2);
            masterTileEntity.validate();
            getWorld().setTileEntity(startPos, masterTileEntity);

            board.resetBoard(size, bombCount);
            currentLevel++;
            saveDataAndSendToClient();
        } else {
            currentLevel++;
            winGame();
        }
    }

    private void onGameWon() {
        genLootChests();
    }

    private void genLootChests() {
        if (currentLevel < 2) return;

        BlockPos central = getCentralGamePos();
        if (currentLevel >= 2) {
            GameUtils.fillLootChest(getWorld(), central, -2, 0, ModLootTables.MS_LEVEL2);
        }
        if (currentLevel >= 3) {
            GameUtils.fillLootChest(getWorld(), central, 2, 0, ModLootTables.MS_LEVEL3);
        }
        if (currentLevel >= 4) {
            GameUtils.fillLootChest(getWorld(), central, 0, -2, ModLootTables.MS_LEVEL4);
        }
        if (currentLevel >= 5) {
            GameUtils.fillLootChest(getWorld(), central, 0, 2, ModLootTables.MS_LEVEL5);
        }
    }

    private void onGameLost() {
        BlockPos expPos = getCentralGamePos();
        getWorld().createExplosion(null, expPos.getX(), expPos.getY() + 1.5, expPos.getZ(), 9, true);
    }

    private int detonateBoard(int strength, boolean damageTerrain) {
        if (!board.isGenerated()) return 0;
        AtomicInteger longestDetTime = new AtomicInteger();
        board.forEach(pos2i -> {
            if (board.getType(pos2i) == MSField.BOMB) {
                int detTime = rand.nextInt(45);
                if (longestDetTime.get() < detTime) {
                    longestDetTime.set(detTime);
                }
                serverTaskPostponer.addTask(
                        new TaskMSCreateExplosion(getMasterPos(), convertToBlockPos(pos2i), strength, damageTerrain),
                        detTime);
            }
        });
        return longestDetTime.get();
    }

    private void updateStage(Stage stageTo) {
        stage = stageTo;
        ticks = 0;
        if (isServerSide()) {
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("stage", stage.ordinal());
            if (stageTo == Stage.DETONATING) {
                c.setInteger("detonation_time", detonationTime);
            }
            sendUpdatePacket("stageUpdate", c);
            saveData();
        }
    }

    @Override
    public void onUpdatePacket(String key, NBTTagCompound compoundIn) {
        switch (key) {
            case "stageUpdate":
                Stage newStage = Stage.values()[compoundIn.getInteger("stage")];
                updateStage(newStage);
                if (newStage == Stage.DETONATING) {
                    detonationTimeInTicks = compoundIn.getInteger("detonation_time");
                }
                break;
            case "gen_board":
                readNBTFromClient(compoundIn);
                break;
            case "field_changed":
                Pos2i pos = new Pos2i(compoundIn.getInteger("x"), compoundIn.getInteger("y"));
                board.setField(pos, compoundIn.getInteger("type"), compoundIn.getBoolean("hidden"), compoundIn.getInteger("mark"));
                break;
            case "spawn_particles_level_beat":
                for (int x = 0; x < getBoardSize() + 1; x++) {
                    for (int z = 0; z < getBoardSize() + 1; z++) {
                        getWorld().spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                                getMasterPos().getX() + x, getCentralGamePos().getY() + 1.1F,
                                getMasterPos().getZ() + z, 0.0, 0.2, 0.0);
                    }
                }
                break;
            case "reset_flag_counter":
                board.cFlaggedFields = 0;
                break;
        }
    }

    public BlockPos convertToBlockPos(int x, int y) {
        return masterTileEntity.getPos().add(x, 0, y);
    }

    public BlockPos convertToBlockPos(Pos2i pos) {
        return convertToBlockPos(pos.getX(), pos.getY());
    }

    public BlockPos getCentralGamePos() {
        return masterTileEntity.getPos().add(getBoardSize() / 2, 0, getBoardSize() / 2);
    }

    public MSBoard getBoard() { return board; }
    public int getTicks() { return ticks; }
    public Stage getStage() { return stage; }

    @Override
    public NBTTagCompound writeNBTForSaving() {
        NBTTagCompound gameCompound = super.writeNBTForSaving();
        if (isBoardGenerated()) {
            gameCompound.setTag("board", board.writeNBTForSaving());
        }
        gameCompound.setInteger("attempt_count", attemptCount);
        return gameCompound;
    }

    @Override
    public void readNBTFromSave(NBTTagCompound compound) {
        super.readNBTFromSave(compound);
        if (compound.hasKey("board")) {
            NBTTagCompound boardTag = compound.getCompoundTag("board");
            MSField[][] boardArr = NBTUtils.readTwoDimArrFromNBT(boardTag, MSField.class, () -> new MSField(MSField.EMPTY, true, MSField.NO_MARK));
            fillNullFields(boardArr);
            board.setBoard(boardArr);
        }
        attemptCount = compound.getInteger("attempt_count");
    }

    @Override
    public NBTTagCompound writeNBTForClient() {
        NBTTagCompound compound = super.writeNBTForClient();
        compound.setBoolean("is_generated", isBoardGenerated());
        if (isBoardGenerated()) {
            compound.setTag("board", board.writeNBTForClient());
        }
        return compound;
    }

    @Override
    public void readNBTFromClient(NBTTagCompound compound) {
        cIsGenerated = compound.getBoolean("is_generated");
        super.readNBTFromClient(compound);
        if (compound.hasKey("board")) {
            NBTTagCompound boardTag = compound.getCompoundTag("board");
            MSField[][] boardArr = NBTUtils.readTwoDimArrFromNBT(boardTag, MSField.class, compoundIn ->
                    new MSField(compoundIn.hasKey("type") ? compoundIn.getInteger("type") : MSField.EMPTY, compoundIn.getBoolean("hidden"), compoundIn.getInteger("mark")));
            fillNullFields(boardArr);
            board.setBoard(boardArr);
            board.updateFlaggedFields_c();
        }
    }

    @Override
    public void writeCommonNBT(NBTTagCompound compound) {
        super.writeCommonNBT(compound);
        compound.setInteger("bomb_count", board.getBombCount());
        compound.setInteger("board_size", board.size());
        compound.setInteger("stage", stage.ordinal());
        compound.setInteger("ticks", ticks);
        compound.setInteger("current_level", currentLevel);
    }

    @Override
    public void readCommonNBT(NBTTagCompound compound) {
        super.readCommonNBT(compound);
        board.setBombCount(compound.getInteger("bomb_count"));
        board.setSize(compound.getInteger("board_size"));
        stage = Stage.values()[compound.getInteger("stage")];
        ticks = compound.getInteger("ticks");
        currentLevel = compound.getInteger("current_level");
    }

    private static void fillNullFields(MSField[][] boardArr) {
        for (int i = 0; i < boardArr.length; i++) {
            for (int j = 0; j < boardArr[i].length; j++) {
                if (boardArr[i][j] == null) {
                    boardArr[i][j] = new MSField(MSField.EMPTY, true, MSField.NO_MARK);
                }
            }
        }
    }

    public enum Stage {
        WAITING, DETONATING, EXPLODING
    }

    public static class Factory implements ILootGameFactory {
        @Override
        public void genOnPuzzleMasterClick(World world, BlockPos puzzleMasterPos, BlockPos bottomPos, BlockPos topPos) {
            int masterTeOffset = 3;
            BlockPos floorCenterPos = puzzleMasterPos.add(0, -masterTeOffset + 1, 0);
            world.setBlockState(floorCenterPos, ModBlocks.MS_ACTIVATOR.getDefaultState());
        }
    }
}
