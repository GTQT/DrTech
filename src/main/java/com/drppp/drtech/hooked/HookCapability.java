package com.drppp.drtech.hooked;

import com.drppp.drtech.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class HookCapability {
    public static final ResourceLocation RESOURCE = new ResourceLocation(Tags.MODID, "hooked_player");

    @CapabilityInject(HooksCap.class)
    public static Capability<HooksCap> CAPABILITY = null;

    private HookCapability() {
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(HooksCap.class, new Capability.IStorage<HooksCap>() {
            @Override
            public NBTBase writeNBT(Capability<HooksCap> capability, HooksCap instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<HooksCap> capability, HooksCap instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }, HooksCap::new);
        MinecraftForge.EVENT_BUS.register(new HookCapability());
    }

    public static HooksCap get(EntityPlayer player) {
        return player.getCapability(CAPABILITY, null);
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(RESOURCE, new HooksCapProvider());
        }
    }
}
