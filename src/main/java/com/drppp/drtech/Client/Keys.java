package com.drppp.drtech.Client;

import gregtech.api.util.input.KeyBind;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.EnumHelper;
import org.lwjgl.input.Keyboard;

/**
 * 装甲相关键位（GT KeyBind 枚举注入）。
 * 原为 mechtech 的 MechTech.keys 实例字段，随合并改为静态字段；
 * 客户端首次加载此类即完成键位注入。
 */
public class Keys {

    public static final KeyBind AUTO_FEEDER_MODE_SWITCH = EnumHelper.addEnum(
            KeyBind.class, "AUTO_FEEDER_MODE_SWITCH",
            new Class[]{String.class, IKeyConflictContext.class, int.class},
            "mechtech.key.auto_feeder.mode_switch", KeyConflictContext.IN_GAME, Keyboard.KEY_RSHIFT);

    private Keys() {
    }

    /** 仅用于确保客户端在合适时机完成类加载（键位注入） */
    public static void initClient() {
    }
}
