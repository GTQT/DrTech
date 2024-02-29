package com.drppp.drtech;

import com.drppp.drtech.Materials.DrtechMaterials;
import gregtech.api.unification.material.event.MaterialEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechEventHandler {
public static int ctrlflag = 0;
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(MaterialEvent event) {
        DrtechMaterials.init();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keybinds.CTRL.isKeyDown()) {
            ctrlflag = 1;
        }else
        ctrlflag = 0;
    }
    @SideOnly(Side.CLIENT)
    public static class Keybinds {

        public static final KeyBinding CTRL = new KeyBinding("key.ctrl", Keyboard.KEY_LCONTROL, "key.categories.drtech");

        public static void registerKeybinds() {
            ClientRegistry.registerKeyBinding(CTRL);
        }
    }
}
