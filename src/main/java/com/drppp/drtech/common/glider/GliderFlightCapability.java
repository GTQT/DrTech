package com.drppp.drtech.common.glider;

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
public final class GliderFlightCapability {
    private static final ResourceLocation KEY = new ResourceLocation(Tags.MODID, "hang_glider");

    @CapabilityInject(GliderFlightData.class)
    private static Capability<GliderFlightData> capability;

    private GliderFlightCapability() {
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(GliderFlightData.class, new Capability.IStorage<GliderFlightData>() {
            @Override
            public NBTBase writeNBT(Capability<GliderFlightData> ignored, GliderFlightData instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<GliderFlightData> ignored, GliderFlightData instance, EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }
            }
        }, GliderFlightData::new);
    }

    public static GliderFlightData get(EntityPlayer player) {
        return capability == null ? null : player.getCapability(capability, null);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(KEY, new Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        GliderFlightData original = get(event.getOriginal());
        GliderFlightData replacement = get(event.getEntityPlayer());
        if (original != null && replacement != null) {
            replacement.copyFrom(original);
        }
    }

    private static final class Provider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
        private final GliderFlightData data = new GliderFlightData();

        @Override
        public boolean hasCapability(Capability<?> requested, EnumFacing side) {
            return requested == capability;
        }

        @Override
        public <T> T getCapability(Capability<T> requested, EnumFacing side) {
            return requested == capability ? capability.cast(data) : null;
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
