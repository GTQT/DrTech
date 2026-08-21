package com.drppp.drtech.Client.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Network.mover.RotateMoverPreviewPacket;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.UUID;

public final class MoverRotationKeyHandler {
    private static final KeyBinding ROTATE = new KeyBinding(
            "key.drtech.multiblock_mover.rotate", Keyboard.KEY_R, "key.categories.drtech");
    private static boolean initialized;

    private MoverRotationKeyHandler() {
    }

    public static synchronized void init() {
        if (initialized) return;
        ClientRegistry.registerKeyBinding(ROTATE);
        MinecraftForge.EVENT_BUS.register(new MoverRotationKeyHandler());
        initialized = true;
        DrTechMain.LOGGER.info("Registered multiblock mover rotation key: {}",
                ROTATE.getKeyDescription());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (ROTATE.isPressed()) {
            Minecraft minecraft = Minecraft.getMinecraft();
            UUID session = MultiblockMoverPreviewRenderer.getSessionId();
            if (session == null || minecraft.player == null || !isHoldingMover(minecraft)) continue;
            int direction = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? -1 : 1;
            DrTechMain.LOGGER.info("Sending multiblock mover rotation request for session {}; direction={}",
                    session, direction);
            SyncInit.NETWORK.sendToServer(new RotateMoverPreviewPacket(session, direction));
        }
    }

    private static boolean isHoldingMover(Minecraft minecraft) {
        if (DrMetaItems.MULTIBLOCK_MOVER == null) return false;
        ItemStack main = minecraft.player.getHeldItemMainhand();
        ItemStack off = minecraft.player.getHeldItemOffhand();
        return DrMetaItems.MULTIBLOCK_MOVER.isItemEqual(main)
                || DrMetaItems.MULTIBLOCK_MOVER.isItemEqual(off);
    }
}
