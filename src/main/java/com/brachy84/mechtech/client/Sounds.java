package com.brachy84.mechtech.client;

import com.brachy84.mechtech.MechTech;
import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class Sounds {
    public static SoundEvent TESLA_ZAP;

    public static void registerSounds() {
        GTLog.logger.info("Register sounds");
        TESLA_ZAP = registerSound("player.tesla_coil");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(MechTech.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
