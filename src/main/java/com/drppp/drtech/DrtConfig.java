package com.drppp.drtech;

import net.minecraftforge.common.config.Config;
@Config(modid = DrTechMain.MODID)
public class DrtConfig {
    @Config.LangKey("逃生舱迫降")
    @Config.Comment("Player Logged In Event")
    @Config.RequiresMcRestart
    public static boolean onPlayerLoggedInEvent=true;

    @Config.LangKey("怪物袭击")
    @Config.Comment("Mob Horde Event")
    @Config.RequiresMcRestart
    public static boolean MobHordeEvent=true;
}