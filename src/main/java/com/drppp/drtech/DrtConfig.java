package com.drppp.drtech;

import net.minecraftforge.common.config.Config;
@Config(modid = DrTechMain.MODID)
public class DrtConfig {
    @Config.LangKey("Player Logged In Event")
    @Config.Comment("逃生舱迫降")
    @Config.RequiresMcRestart
    public static boolean onPlayerLoggedInEvent=true;

    @Config.LangKey("Player Logged At TheBetweenLand")
    @Config.Comment("交错出生；注意，默认交错出生的开关在这里调整而不是在交错的配置文件调整")
    @Config.RequiresMcRestart
    public static boolean onPlayerLoggedAtTheBetweenLand=false;

    @Config.LangKey("Mob Horde Event")
    @Config.Comment("怪物袭击")
    @Config.RequiresMcRestart
    public static boolean MobHordeEvent=false;

    @Config.LangKey("Zombie Horde Event")
    @Config.Comment("僵尸袭击")
    @Config.RequiresMcRestart
    public static boolean ZombieHordeEvent=false;

    @Config.LangKey("Skeleton Horde Event")
    @Config.Comment("骷髅袭击")
    @Config.RequiresMcRestart
    public static boolean SkeletonMobHordeEvent=false;

    @Config.LangKey("Creeper Horde Event")
    @Config.Comment("爬行者袭击")
    @Config.RequiresMcRestart
    public static boolean CreeperMobHordeEvent=false;

    @Config.LangKey("enable_disassembly")
    @Config.Comment("开启拆解机")
    @Config.RequiresMcRestart
    public static boolean EnableDisassembly=false;
    @Config.LangKey("nuclear_explosion_range")
    @Config.Comment("核电爆炸范围")
    @Config.RequiresMcRestart
    public static float NuclearExplosionRange=10;

}