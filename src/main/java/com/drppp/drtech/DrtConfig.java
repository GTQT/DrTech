package com.drppp.drtech;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID)
public class DrtConfig {

    @Config.LangKey("enable_rocket_ditch")
    @Config.Comment("开启火箭迫降")
    @Config.RequiresMcRestart
    public static boolean EnableRocketDitch = true;
    @Config.LangKey("enable_disassembly")
    @Config.Comment("开启拆解机")
    @Config.RequiresMcRestart
    public static boolean EnableDisassembly = false;
    @Config.LangKey("nuclear_explosion_range")
    @Config.Comment("核电爆炸范围")
    @Config.RequiresMcRestart
    public static float NuclearExplosionRange = 10;
    @Config.LangKey("enable_industrial_Apiary_Tx")
    @Config.Comment("开启工业蜂箱粒子特效")
    @Config.RequiresMcRestart
    public static boolean EnableIndustrialApiaryTx = true;
    @Config.LangKey("enable_industrial_machines")
    @Config.Comment("启用更便宜的大机器")
    @Config.RequiresMcRestart
    public static boolean EnableIndustrialMachines = false;

    @Config.Comment("Config options for DrTech")
    public static MachineSwitch MachineSwitch = new MachineSwitch();

    public static class MachineSwitch {
        @Config.LangKey("enable_disassembly")
        @Config.Comment("开启拆解机")
        @Config.RequiresMcRestart
        public static boolean EnableDisassembly = DrtConfig.EnableDisassembly;
        @Config.LangKey("nuclear_explosion_range")
        @Config.Comment("核电爆炸范围")
        @Config.RequiresMcRestart
        public static float NuclearExplosionRange = DrtConfig.NuclearExplosionRange;
        @Config.LangKey("enable_industrial_Apiary_Tx")
        @Config.Comment("开启工业蜂箱粒子特效")
        @Config.RequiresMcRestart
        public static boolean EnableIndustrialApiaryTx = DrtConfig.EnableIndustrialApiaryTx;
        @Config.LangKey("enable_industrial_machines")
        @Config.Comment("启用更便宜的大机器")
        @Config.RequiresMcRestart
        public static boolean EnableIndustrialMachines = DrtConfig.EnableIndustrialMachines;
    }
}