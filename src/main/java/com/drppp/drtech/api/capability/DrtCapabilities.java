package com.drppp.drtech.api.capability;

import com.drppp.drtech.Tags;
import gregtech.api.capability.SimpleCapabilityManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtCapabilities {
    @CapabilityInject(IElytraFlyingProvider.class)
    public static Capability<IElytraFlyingProvider> ELYTRA_FLYING_PROVIDER;
    public static void init() {
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IElytraFlyingProvider.class);
    }
}