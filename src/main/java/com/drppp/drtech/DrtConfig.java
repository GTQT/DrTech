package com.drppp.drtech;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID)
public class DrtConfig {

<<<<<<< Updated upstream
=======
    @Config.LangKey("enable_rocket_ditch")
    @Config.Comment("开启火箭迫降")
    @Config.RequiresMcRestart
    public static boolean EnableRocketDitch = false;
>>>>>>> Stashed changes
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
    @Config.LangKey("mill_exchange_rate")
    @Config.Comment("水车推力转换效率")
    @Config.RequiresMcRestart
    public static double MillExchangeRate = 10.0d;
    @Config.LangKey("water_mill_max_ru")
    @Config.Comment("木质水车和轴承最大承受RU")
    @Config.RequiresMcRestart
    public static int MaxRu = 192;
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