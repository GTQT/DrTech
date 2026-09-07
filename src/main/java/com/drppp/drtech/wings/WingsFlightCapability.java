package com.drppp.drtech.wings;

import com.drppp.drtech.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class WingsFlightCapability {
    private static final ResourceLocation KEY = new ResourceLocation(Tags.MODID, "wings_flight");

    @CapabilityInject(WingsFlightData.class)
    private static Capability<WingsFlightData> CAPABILITY;

    private WingsFlightCapability() {
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(WingsFlightData.class, new Capability.IStorage<WingsFlightData>() {
            @Override
            public NBTBase writeNBT(Capability<WingsFlightData> capability, WingsFlightData instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<WingsFlightData> capability, WingsFlightData instance, EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }
            }
        }, WingsFlightData::new);
    }

    public static WingsFlightData get(EntityPlayer player) {
        return CAPABILITY == null ? null : player.getCapability(CAPABILITY, null);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(KEY, new Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        WingsFlightData original = get(event.getOriginal());
        WingsFlightData replacement = get(event.getEntityPlayer());
        if (original != null && replacement != null) {
            replacement.copyFrom(original);
        }
    }

    private static final class Provider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
        private final WingsFlightData data = new WingsFlightData();

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            return capability == CAPABILITY ? CAPABILITY.cast(data) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            data.deserializeNBT(nbt);
        }
    }
}
