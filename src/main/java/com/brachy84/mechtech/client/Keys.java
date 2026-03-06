package com.brachy84.mechtech.client;

import gregtech.api.util.input.KeyBind;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.EnumHelper;
import org.lwjgl.input.Keyboard;

public class Keys {

    public final KeyBind TESLA_COIL_MODE_SWITCH;
    public final KeyBind AUTO_FEEDER_MODE_SWITCH;

    public Keys() {
        TESLA_COIL_MODE_SWITCH = EnumHelper.addEnum(KeyBind.class, "TESLA_COIL_MODE_SWITCH", new Class[] {String.class, IKeyConflictContext.class, int.class}, "mechtech.key.tesla_coil.mode_switch", KeyConflictContext.IN_GAME, Keyboard.KEY_APOSTROPHE);
        AUTO_FEEDER_MODE_SWITCH = EnumHelper.addEnum(KeyBind.class, "AUTO_FEEDER_MODE_SWITCH", new Class[] {String.class, IKeyConflictContext.class, int.class}, "mechtech.key.auto_feeder.mode_switch", KeyConflictContext.IN_GAME, Keyboard.KEY_RSHIFT);

    }
}
