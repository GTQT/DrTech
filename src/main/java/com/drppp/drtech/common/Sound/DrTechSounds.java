package com.drppp.drtech.common.Sound;

import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class DrTechSounds {
    public static final SoundEvent LIGHTSABER_ON = create("player.lightsaber.on");
    public static final SoundEvent LIGHTSABER_OFF = create("player.lightsaber.off");
    public static final SoundEvent LIGHTSABER_SWING = create("player.lightsaber.swing");
    public static final SoundEvent LIGHTSABER_HIT = create("player.lightsaber.hit");

    private DrTechSounds() {
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(LIGHTSABER_ON, LIGHTSABER_OFF, LIGHTSABER_SWING, LIGHTSABER_HIT);
    }

    private static SoundEvent create(String name) {
        ResourceLocation id = new ResourceLocation(Tags.MODID, name);
        return new SoundEvent(id).setRegistryName(id);
    }
}
