package com.drppp.drtech.Client.Sound;


import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class SoundManager {

    // DrTech sounds
    public static SoundEvent plasma_charge_sound = new SoundEvent(new ResourceLocation(Tags.MODID, "plasma_charge_sound"));
    public static SoundEvent laser_bullet_shoot = new SoundEvent(new ResourceLocation(Tags.MODID, "laser_bullet_shoot"));
    public static SoundEvent lighter_place = new SoundEvent(new ResourceLocation(Tags.MODID, "lighter_place"));
    public static SoundEvent plasma_launch = new SoundEvent(new ResourceLocation(Tags.MODID, "plasma_launch"));

    // LootGames sounds
    public static SoundEvent golStartGame = new SoundEvent(new ResourceLocation(Tags.MODID, "gol_start_game"));
    public static SoundEvent golSequenceWrong = new SoundEvent(new ResourceLocation(Tags.MODID, "gol_sequence_wrong"));
    public static SoundEvent golSequenceComplete = new SoundEvent(new ResourceLocation(Tags.MODID, "gol_sequence_complete"));
    public static SoundEvent golGameWin = new SoundEvent(new ResourceLocation(Tags.MODID, "gol_gameover_win"));
    public static SoundEvent golGameLose = new SoundEvent(new ResourceLocation(Tags.MODID, "gol_gameover_lose"));
    public static SoundEvent msStartGame = new SoundEvent(new ResourceLocation(Tags.MODID, "ms_start_game"));
    public static SoundEvent msOnEmptyRevealNeighbours = new SoundEvent(new ResourceLocation(Tags.MODID, "ms_empty_revel_neighbours"));
    public static SoundEvent msBombActivated = new SoundEvent(new ResourceLocation(Tags.MODID, "bomb_activated"));
    public static SoundEvent puzzleMasterStrange = new SoundEvent(new ResourceLocation(Tags.MODID, "puzzle_master_strange"));

    @SubscribeEvent
    public static void onSoundEvenrRegistration(RegistryEvent.Register<SoundEvent> event)
    {
        event.getRegistry().register(plasma_charge_sound.setRegistryName("plasma_charge_sound"));
        event.getRegistry().register(laser_bullet_shoot.setRegistryName("laser_bullet_shoot"));
        event.getRegistry().register(lighter_place.setRegistryName("lighter_place"));
        event.getRegistry().register(plasma_launch.setRegistryName("plasma_launch"));

        event.getRegistry().register(golStartGame.setRegistryName("gol_start_game"));
        event.getRegistry().register(golSequenceWrong.setRegistryName("gol_sequence_wrong"));
        event.getRegistry().register(golSequenceComplete.setRegistryName("gol_sequence_complete"));
        event.getRegistry().register(golGameWin.setRegistryName("gol_gameover_win"));
        event.getRegistry().register(golGameLose.setRegistryName("gol_gameover_lose"));
        event.getRegistry().register(msStartGame.setRegistryName("ms_start_game"));
        event.getRegistry().register(msOnEmptyRevealNeighbours.setRegistryName("ms_empty_revel_neighbours"));
        event.getRegistry().register(msBombActivated.setRegistryName("bomb_activated"));
        event.getRegistry().register(puzzleMasterStrange.setRegistryName("puzzle_master_strange"));
    }

}