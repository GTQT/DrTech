package com.drppp.drtech.lootgames.minigame.gameoflight;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.lootgames.api.tileentity.TileEntityGameMaster;
import com.drppp.drtech.lootgames.api.util.DirectionOctagonal;
import com.drppp.drtech.lootgames.block.BlockDungeonLamp;
import com.drppp.drtech.lootgames.packets.CMessageGOLFeedback;
import com.drppp.drtech.lootgames.packets.NetworkHandler;
import com.drppp.drtech.lootgames.packets.SMessageGOLDrawStuff;
import com.drppp.drtech.lootgames.packets.SMessageGOLParticle;
import com.drppp.drtech.lootgames.registry.ModBlocks;
import com.drppp.drtech.Client.Sound.SoundManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TileEntityGOLMaster extends TileEntityGameMaster<GameOfLight> {
    public static final int MAX_TICKS_EXPANDING = 20;
    private static final int TICKS_PAUSE_BETWEEN_SYMBOLS = 12;
    private static final int TICKS_PAUSE_BETWEEN_STAGES = 25;
    public static int ticksPerShowSymbols = 24;

    // Config values (simplified - inline)
    private static final int startDigitAmount = 3;
    private static final int maxAttempts = 2;
    private static final int timeout = 30;
    private static final int expandFieldAtStage = 3;

    private int currentRound = -1;
    private int gameLevel = 1;
    private int ticks = 0;
    private int symbolIndex = 0;
    private boolean particleSent = false;
    private boolean pauseBeforeShowing = true;
    private List<ClickInfo> symbolsEnteredByPlayer;
    private int currentTimeout = 0;

    private GameStage gameStage;
    private List<Integer> symbolSequence;
    private List<Integer> maxLevelBeatList;
    private List<DrawInfo> stuffToDraw;

    private boolean feedbackPacketReceived;

    private final Random rand = new Random();

    public TileEntityGOLMaster() {
        super(new GameOfLight());
        gameStage = GameStage.NOT_CONSTRUCTED;
    }

    @Override
    public void update() {
        if (gameStage == GameStage.NOT_CONSTRUCTED) return;

        if (gameStage == GameStage.UNDER_EXPANDING) {
            if (ticks > MAX_TICKS_EXPANDING) {
                if (!world.isRemote) {
                    updateGameStage(GameStage.WAITING_FOR_START);
                }
            } else {
                ticks++;
            }
        }

        if (gameStage == GameStage.SHOWING_SEQUENCE) {
            if (pauseBeforeShowing) {
                if (ticks > TICKS_PAUSE_BETWEEN_STAGES) {
                    ticks = 0;
                    pauseBeforeShowing = false;
                } else {
                    ticks++;
                }
            } else {
                if (world.isRemote) {
                    if ((symbolIndex == symbolSequence.size() - 1 && ticks > ticksPerShowSymbols) || (ticks > ticksPerShowSymbols + TICKS_PAUSE_BETWEEN_SYMBOLS)) {
                        ticks = 0;
                        symbolIndex++;
                        particleSent = false;
                    } else {
                        ticks++;
                    }

                    if (isShowingSymbols() && !particleSent) {
                        playFeedbackSound(getCurrentSymbolPosOffset());
                        spawnFeedbackParticles(EnumParticleTypes.SPELL, getPos().add(getCurrentSymbolPosOffset().getOffsetX(), 0, getCurrentSymbolPosOffset().getOffsetZ()));
                        particleSent = true;
                    }
                } else {
                    if (feedbackPacketReceived) {
                        feedbackPacketReceived = false;
                        updateGameStage(GameStage.WAITING_FOR_PLAYER_SEQUENCE);
                    }
                }
            }
        }

        if (!world.isRemote && gameStage == GameStage.WAITING_FOR_PLAYER_SEQUENCE) {
            if (currentTimeout >= timeout * 20) {
                updateGameStage(GameStage.WAITING_FOR_START);
            }
            currentTimeout++;
        }

        if (world.isRemote && symbolsEnteredByPlayer != null) {
            symbolsEnteredByPlayer.removeIf(clickInfo -> System.currentTimeMillis() - clickInfo.msClickedTime > 800);
        }

        if (world.isRemote && stuffToDraw != null && !stuffToDraw.isEmpty()) {
            stuffToDraw.removeIf(stuff -> System.currentTimeMillis() - stuff.msClickedTime > 800);
        }
    }

    private void generateSubordinates(EntityPlayer player) {
        world.playSound(null, pos, SoundManager.golStartGame, SoundCategory.BLOCKS, 0.75F, 1.0F);

        for (DirectionOctagonal value : DirectionOctagonal.values()) {
            IBlockState state = ModBlocks.GOL_SUBORDINATE.getDefaultState().withProperty(BlockGOLSubordinate.OFFSET, value);
            world.setBlockState(pos.add(value.getOffsetX(), 0, value.getOffsetZ()), state);
        }

        player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.start"));
        updateGameStage(GameStage.UNDER_EXPANDING);
    }

    public void onBlockClickedByPlayer(EntityPlayer player) {
        if (gameStage == GameStage.NOT_CONSTRUCTED) {
            generateSubordinates(player);
            return;
        }
        if (gameStage == GameStage.WAITING_FOR_START) {
            startGame(player);
            return;
        }
        if (gameStage == GameStage.WAITING_FOR_PLAYER_SEQUENCE) {
            player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.rules"));
            return;
        }
        player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.not_ready"));
    }

    public void onSubordinateClickedByPlayer(DirectionOctagonal subordinateOffset, EntityPlayer player) {
        if (gameStage == GameStage.WAITING_FOR_PLAYER_SEQUENCE) {
            if (world.isRemote) {
                symbolsEnteredByPlayer.add(new ClickInfo(System.currentTimeMillis(), subordinateOffset));
                playFeedbackSound(subordinateOffset);
            } else {
                currentTimeout = 0;
                checkPlayerReply(subordinateOffset, player);
            }
            return;
        }
        if (!world.isRemote) {
            player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.not_ready"));
        }
    }

    private void checkPlayerReply(DirectionOctagonal subordinateOffset, EntityPlayer player) {
        if (DirectionOctagonal.byIndex(symbolSequence.get(symbolIndex)) == subordinateOffset) {
            if (symbolIndex == symbolSequence.size() - 1) {
                currentRound++;

                if (currentRound >= 1) {
                    gameLevel = 4;
                    onGameEnded(player);
                    return;
                }

                NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 15);

                gameLevel++;
                onGameLevelChanged();

                NetworkHandler.INSTANCE.sendToAllAround(new SMessageGOLDrawStuff(getPos(), EnumDrawStuff.SEQUENCE_ACCEPTED.ordinal()), point);
                world.playSound(null, getPos(), SoundManager.golSequenceComplete, SoundCategory.BLOCKS, 0.75F, 1.0F);

                generateSequence(symbolSequence.size() + 1);
                updateGameStage(GameStage.SHOWING_SEQUENCE);
            }
            symbolIndex++;
        } else {
            world.playSound(null, pos, SoundManager.golSequenceWrong, SoundCategory.BLOCKS, 0.75F, 1.0F);
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 15);
            NetworkHandler.INSTANCE.sendToAllAround(new SMessageGOLDrawStuff(getPos(), EnumDrawStuff.SEQUENCE_DENIED.ordinal()), point);

            player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.wrong_block"));

            maxLevelBeatList.add(gameLevel - 1);
            onGameEnded(player);
        }
    }

    private void onGameLevelChanged() {
        ticksPerShowSymbols = Math.max(8, 24 - (gameLevel - 1) * 4);
    }

    private void generateSequence(int size) {
        symbolSequence = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            addRandSymbolToSequence();
        }
    }

    private void addRandSymbolToSequence() {
        if (symbolSequence == null) {
            symbolSequence = new ArrayList<>();
        }
        if (gameLevel >= expandFieldAtStage) {
            symbolSequence.add(rand.nextInt(8));
        } else {
            symbolSequence.add(rand.nextInt(4) * 2);
        }
    }

    private void startGame(EntityPlayer player) {
        player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.rules"));
        currentRound = 0;
        gameLevel = 1;
        onGameLevelChanged();
        generateSequence(startDigitAmount);
        updateGameStage(GameStage.SHOWING_SEQUENCE);
    }

    @SideOnly(Side.CLIENT)
    private void playFeedbackSound(DirectionOctagonal offset) {
        float pitch = 0.5F + offset.getIndex() * 0.1F;
        world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_NOTE_HARP, SoundCategory.BLOCKS, 3.0F, pitch, false);
    }

    public void onClientThingsDone() {
        feedbackPacketReceived = true;
    }

    @Override
    protected void readCommonNBT(NBTTagCompound compound) {
        super.readCommonNBT(compound);
        ticks = compound.getInteger("ticks");
        gameStage = GameStage.values()[compound.getInteger("game_stage")];
        onStageSetting(gameStage);
        currentTimeout = compound.getInteger("timeout");
        currentRound = compound.getInteger("current_round");
        gameLevel = compound.getInteger("game_level");
        if (compound.hasKey("symbol_sequence")) {
            symbolSequence = Arrays.stream(compound.getIntArray("symbol_sequence")).boxed().collect(Collectors.toList());
        }
        if (compound.hasKey("max_level_beat")) {
            maxLevelBeatList = Arrays.stream(compound.getIntArray("max_level_beat")).boxed().collect(Collectors.toList());
        }
    }

    @Override
    protected NBTTagCompound writeCommonNBT(NBTTagCompound compound) {
        compound = super.writeCommonNBT(compound);
        compound.setInteger("ticks", ticks);
        compound.setInteger("game_stage", gameStage.ordinal());
        compound.setInteger("game_level", gameLevel);
        compound.setInteger("current_round", currentRound);
        compound.setInteger("timeout", currentTimeout);
        if (symbolSequence != null) {
            compound.setIntArray("symbol_sequence", symbolSequence.stream().mapToInt(i -> i).toArray());
        }
        if (maxLevelBeatList != null) {
            compound.setIntArray("max_level_beat", maxLevelBeatList.stream().mapToInt(i -> i).toArray());
        }
        return compound;
    }

    private void onGameEnded(EntityPlayer player) {
        if (isLastStagePassed() ||
                (maxLevelBeatList != null && maxLevelBeatList.size() == maxAttempts + 1 && getBestLevelReached() > 0)) {
            onGameWon(player);
        } else if (maxLevelBeatList == null || maxLevelBeatList.size() < maxAttempts + 1) {
            world.playSound(null, getPos(), SoundManager.golStartGame, SoundCategory.BLOCKS, 0.75F, 1.0F);
            startGame(player);
        } else {
            onGameLost(player);
        }
    }

    private void onGameWon(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            // Advancement trigger skipped as per user request
        }

        if (maxLevelBeatList != null && maxLevelBeatList.size() == maxAttempts + 1 && getBestLevelReached() > 0) {
            gameLevel -= 1;
        }

        player.sendMessage(new TextComponentTranslation("msg.lootgames.win"));
        world.playSound(null, getPos(), SoundManager.golGameWin, SoundCategory.BLOCKS, 0.75F, 1.0F);

        int bestLevelReached = getBestLevelReached();
        if (bestLevelReached != -1) {
            player.sendMessage(new TextComponentTranslation("msg.lootgames.gol_master.reward_level_reached", gameLevel, isLastStagePassed() ? 4 : bestLevelReached));
        }

        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                world.setBlockToAir(pos.add(x, 0, z));
            }
        }
        destroyStructure();
        genLootChests(player);
    }

    private void onGameLost(EntityPlayer player) {
        world.playSound(null, getPos(), SoundManager.golGameLose, SoundCategory.BLOCKS, 0.75F, 1.0F);
        player.sendMessage(new TextComponentTranslation("msg.lootgames.lose"));
        destroyStructure();

        List<String> failEffects = new ArrayList<>();
        failEffects.add("explode");
        failEffects.add("lava");
        failEffects.add("zombies");

        if (!failEffects.isEmpty())
            executeFailEvent(failEffects.get(rand.nextInt(failEffects.size())));
    }

    private void executeFailEvent(String pEffect) {
        if (pEffect.equalsIgnoreCase("explode")) {
            world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 6, true);
        } else if (pEffect.equalsIgnoreCase("lava")) {
            for (int x = -5; x <= 5; x++)
                for (int z = -5; z <= 5; z++)
                    for (int y = 1; y < 3; y++)
                        world.setBlockState(pos.add(x, y, z), Blocks.LAVA.getDefaultState());
        } else if (pEffect.equalsIgnoreCase("zombies")) {
            for (int i = 0; i < 10; i++) {
                EntityZombie zombie = new EntityZombie(world);
                zombie.setLocationAndAngles(pos.getX() + rand.nextFloat() * 2, pos.getY() + 1, pos.getZ() + rand.nextFloat() * 2,
                        MathHelper.wrapDegrees(rand.nextFloat() * 360.0F), 0.0F);
                zombie.rotationYawHead = zombie.rotationYaw;
                zombie.renderYawOffset = zombie.rotationYaw;
                world.spawnEntity(zombie);
                zombie.playLivingSound();
            }
        }
    }

    private void destroyStructure() {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                if (x == 0 || z == 0) {
                    world.setBlockToAir(pos.add(x, 0, z));
                } else {
                    world.setBlockState(pos.add(x, 0, z), ModBlocks.DUNGEON_LAMP.getDefaultState().withProperty(BlockDungeonLamp.BROKEN, false));
                }
            }
        }
    }

    @Override
    public void destroyGameBlocks() {
        // Cleanup handled by destroyStructure
    }

    private boolean isLastStagePassed() {
        return gameLevel == 4 && currentRound >= 1;
    }

    private void genLootChests(EntityPlayer player) {
        int bestLevelReached = getBestLevelReached();
        if (bestLevelReached == -1 || isLastStagePassed()) {
            bestLevelReached = 4;
        }
        if (bestLevelReached < 1) return;

        // Simplified: spawn chests at corners
        if (bestLevelReached >= 1) {
            world.setBlockState(pos.add(-2, 0, 0), Blocks.CHEST.getDefaultState());
        }
        if (bestLevelReached >= 2) {
            world.setBlockState(pos.add(2, 0, 0), Blocks.CHEST.getDefaultState());
        }
        if (bestLevelReached >= 3) {
            world.setBlockState(pos.add(0, 0, -2), Blocks.CHEST.getDefaultState());
        }
        if (bestLevelReached >= 4) {
            world.setBlockState(pos.add(0, 0, 2), Blocks.CHEST.getDefaultState());
        }
    }

    private void updateGameStage(GameStage gameStage) {
        this.gameStage = gameStage;
        onStageSetting(gameStage);
        setBlockToUpdateAndSave();
    }

    private int getBestLevelReached() {
        if (maxLevelBeatList == null || maxLevelBeatList.isEmpty()) return -1;
        int i = -1;
        for (Integer attempt : maxLevelBeatList) {
            if (attempt > i) i = attempt;
        }
        return i;
    }

    private void spawnFeedbackParticles(EnumParticleTypes particle, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            world.spawnParticle(particle, pos.getX() + rand.nextFloat(), pos.getY() + 0.5F + rand.nextFloat(),
                    pos.getZ() + rand.nextFloat(), rand.nextGaussian() * 0.02D, 0.02D, rand.nextGaussian() * 0.02D);
        }
    }

    @NotNull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-2, 0, -2), getPos().add(2, 1, 2));
    }

    public GameStage getGameStage() { return gameStage; }

    public boolean isShowingSymbols() { return symbolIndex < symbolSequence.size(); }

    public DirectionOctagonal getCurrentSymbolPosOffset() { return DirectionOctagonal.byIndex(symbolSequence.get(symbolIndex)); }

    public int getTicks() { return ticks; }

    public List<ClickInfo> getSymbolsEnteredByPlayer() { return symbolsEnteredByPlayer; }

    @SideOnly(Side.CLIENT)
    public List<DrawInfo> getStuffToDraw() { return stuffToDraw; }

    @SideOnly(Side.CLIENT)
    public void addStuffToDraw(DrawInfo stuff) {
        if (stuffToDraw == null) stuffToDraw = new ArrayList<>();
        stuffToDraw.add(stuff);
    }

    public boolean isFeedbackPacketReceived() { return feedbackPacketReceived; }
    public boolean isOnPause() { return pauseBeforeShowing; }

    private void onStageSetting(GameStage stage) {
        ticks = 0;
        if (stage == GameStage.SHOWING_SEQUENCE) {
            feedbackPacketReceived = false;
            symbolIndex = 0;
            particleSent = false;
            pauseBeforeShowing = true;
        }
        if (stage == GameStage.WAITING_FOR_PLAYER_SEQUENCE) {
            currentTimeout = 0;
            symbolIndex = 0;
            if (maxLevelBeatList == null) maxLevelBeatList = new ArrayList<>();
            if (hasWorld() && world.isRemote) {
                symbolsEnteredByPlayer = new LinkedList<>();
            }
        }
    }

    public enum GameStage {
        NOT_CONSTRUCTED, UNDER_EXPANDING, WAITING_FOR_START, SHOWING_SEQUENCE, WAITING_FOR_PLAYER_SEQUENCE
    }

    public enum EnumDrawStuff {
        SEQUENCE_ACCEPTED, SEQUENCE_DENIED, SHOWING_SEQUENCE
    }

    public static class DrawInfo {
        private final long msClickedTime;
        private final EnumDrawStuff stuff;

        public DrawInfo(long msClickedTime, EnumDrawStuff stuff) {
            this.msClickedTime = msClickedTime;
            this.stuff = stuff;
        }

        public EnumDrawStuff getStuff() { return stuff; }
    }

    public static class ClickInfo {
        private final long msClickedTime;
        private final DirectionOctagonal offset;

        private ClickInfo(long msClickedTime, DirectionOctagonal offset) {
            this.msClickedTime = msClickedTime;
            this.offset = offset;
        }

        public DirectionOctagonal getOffset() { return offset; }
    }
}
