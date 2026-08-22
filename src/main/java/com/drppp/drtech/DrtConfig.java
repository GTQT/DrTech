package com.drppp.drtech;

import net.minecraftforge.common.config.Config;
import com.drppp.drtech.common.drone.entity.DroneFakePlayerIdentity;

@Config(modid = Tags.MODID)
public class DrtConfig {

    @Config.LangKey("drone_fake_player_identity")
    @Config.Comment({"Drone FakePlayer identity: PER_DRONE, OWNER, or SHARED.",
            "OWNER inherits the owner's UUID; unowned drones fall back to PER_DRONE."})
    public static DroneFakePlayerIdentity DroneFakePlayerIdentityStrategy = DroneFakePlayerIdentity.PER_DRONE;

    @Config.LangKey("enable_drone_audit_log")
    @Config.Comment("Write terminal state-changing drone actions to the server log with drone, owner and program ids.")
    public static boolean EnableDroneAuditLog = true;

    @Config.LangKey("enable_drone_combat")
    @Config.Comment("Allow programmable drones to execute attack-entity nodes. Bosses remain protected.")
    public static boolean EnableDroneCombat = true;

    @Config.LangKey("enable_drone_player_attack")
    @Config.Comment("Allow drone attack nodes to target players when drone combat is enabled.")
    public static boolean EnableDronePlayerAttack = false;

    @Config.LangKey("drone_fishing_luck_bonus")
    @Config.Comment({"Additional vanilla fishing luck applied to drone bobbers.",
            "This stacks with Luck of the Sea; 8 strongly favors treasure and suppresses junk."})
    @Config.RangeInt(min = 0, max = 100)
    public static int DroneFishingLuckBonus = 8;

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
