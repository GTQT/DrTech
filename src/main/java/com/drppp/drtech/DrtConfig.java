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
    public static boolean MobHordeEvent=false;

    @Config.LangKey("僵尸袭击")
    @Config.Comment("Zombie Horde Event")
    @Config.RequiresMcRestart
    public static boolean ZombieHordeEvent=false;

    @Config.LangKey("骷髅袭击")
    @Config.Comment("Skeleton Horde Event")
    @Config.RequiresMcRestart
    public static boolean SkeletonMobHordeEvent=false;

    @Config.LangKey("爬行者袭击")
    @Config.Comment("Creeper Horde Event")
    @Config.RequiresMcRestart
    public static boolean CreeperMobHordeEvent=false;

    @Config.LangKey("enable_disassembly")
    @Config.Comment("开启拆解机")
    @Config.RequiresMcRestart
    public static boolean EnableDisassembly=false;
    @Config.LangKey("nuclear_explosion_range")
    @Config.Comment("核电爆炸范围")
    @Config.RequiresMcRestart
    public static float EXPLOSION_RANGE=10;

}