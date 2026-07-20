package com.drppp.drtech.wings;

import com.drppp.drtech.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;

/** Optional Baubles bridge with no runtime linkage when Baubles is absent. */
public final class WingsBaublesCompat {
    private static final ResourceLocation CAPABILITY_KEY = new ResourceLocation(Tags.MODID, "baubles_wings");

    private WingsBaublesCompat() {
    }

    public static void init() {
        if (Loader.isModLoaded("baubles")) {
            MinecraftForge.EVENT_BUS.register(new WingsBaublesCompat());
        }
    }

    public static ItemStack findWings(EntityPlayer player) {
        if (!Loader.isModLoaded("baubles")) {
            return ItemStack.EMPTY;
        }
        try {
            Object handler = getHandler(player);
            Method getStack = handler.getClass().getMethod("getStackInSlot", int.class);
            for (int slot : getBodySlots()) {
                ItemStack stack = (ItemStack) getStack.invoke(handler, slot);
                if (stack.getItem() instanceof ItemWings) {
                    return stack;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return ItemStack.EMPTY;
    }

    public static boolean equip(EntityPlayer player, ItemStack stack) {
        if (!Loader.isModLoaded("baubles")) {
            return false;
        }
        try {
            Object handler = getHandler(player);
            Method insert = handler.getClass().getMethod("insertItem", int.class, ItemStack.class, boolean.class);
            for (int slot : getBodySlots()) {
                ItemStack remainder = (ItemStack) insert.invoke(handler, slot, stack, false);
                if (remainder.getCount() < stack.getCount()) {
                    stack.setCount(remainder.getCount());
                    return true;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    @SubscribeEvent
    public void attachBaubleCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof ItemWings) {
            try {
                event.addCapability(CAPABILITY_KEY, createBaubleProvider());
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private static Object getHandler(EntityPlayer player) throws ReflectiveOperationException {
        Class<?> api = Class.forName("baubles.api.BaublesApi");
        return api.getMethod("getBaublesHandler", EntityPlayer.class).invoke(null, player);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static int[] getBodySlots() throws ReflectiveOperationException {
        Class<?> type = Class.forName("baubles.api.BaubleType");
        Object body = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), "BODY");
        return (int[]) type.getMethod("getValidSlots").invoke(body);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ICapabilityProvider createBaubleProvider() throws ReflectiveOperationException {
        Class<?> type = Class.forName("baubles.api.BaubleType");
        Object body = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), "BODY");
        Object bauble = Class.forName("baubles.api.cap.BaubleItem").getConstructor(type).newInstance(body);
        Capability capability = (Capability) Class.forName("baubles.api.cap.BaublesCapabilities")
                .getField("CAPABILITY_ITEM_BAUBLE").get(null);
        return new ICapabilityProvider() {
            @Override
            public boolean hasCapability(Capability<?> requested, net.minecraft.util.EnumFacing facing) {
                return requested == capability;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getCapability(Capability<T> requested, net.minecraft.util.EnumFacing facing) {
                return requested == capability ? (T) bauble : null;
            }
        };
    }
}
