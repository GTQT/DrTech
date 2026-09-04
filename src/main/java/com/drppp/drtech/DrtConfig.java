package com.drppp.drtech;

import net.minecraftforge.common.config.Config;
import com.drppp.drtech.drone.entity.DroneFakePlayerIdentity;

@Config(modid = Tags.MODID)
public class DrtConfig {

    // ========== 全局 ==========
    @Config.Comment("启用调试模式，会在日志中输出更多辅助排查信息")
    @Config.Name("调试模式")
    public static boolean debug = false;

    // ========== 无人机模块 ==========
    @Config.Comment("无人机相关配置")
    @Config.Name("无人机")
    public static DroneConfig drone = new DroneConfig();

    // ========== 机器模块 ==========
    @Config.Comment("机器相关配置（拆解机、核电站、工业蜂箱等）")
    @Config.Name("机器")
    public static MachineConfig machine = new MachineConfig();

    // ========== 模块化装甲模块 ==========
    @Config.Comment("模块化装甲相关配置（槽位数与模块参数）")
    @Config.Name("模块化装甲")
    public static ModularArmorConfig armor = new ModularArmorConfig();

    // --------------------------------------------
    // 无人机配置类
    // --------------------------------------------
    public static class DroneConfig {
        @Config.Comment({
                "无人机假玩家身份策略：",
                "  PER_DRONE  - 每个无人机独立身份",
                "  OWNER      - 继承拥有者的 UUID（无归属时回退为 PER_DRONE）",
                "  SHARED     - 共享身份"
        })
        @Config.Name("假玩家身份策略")
        public DroneFakePlayerIdentity DroneFakePlayerIdentityStrategy = DroneFakePlayerIdentity.PER_DRONE;

        @Config.Comment("是否将无人机终端状态变更（如程序切换）写入服务器日志，包含无人机、拥有者及程序 ID")
        @Config.Name("启用审计日志")
        public boolean EnableDroneAuditLog = true;

        @Config.Comment("是否允许可编程无人机执行攻击实体的指令（Boss 类生物仍受保护）")
        @Config.Name("启用无人机战斗")
        public boolean EnableDroneCombat = true;

        @Config.Comment("当无人机战斗启用时，是否允许无人机攻击玩家")
        @Config.Name("允许攻击玩家")
        public boolean EnableDronePlayerAttack = false;

        @Config.Comment({
                "额外施加于无人机浮标的原版钓鱼幸运值，与「海之眷顾」附魔叠加。",
                "数值 8 时强烈偏向宝藏，并大幅抑制垃圾。取值范围 0 ~ 100"
        })
        @Config.Name("无人机钓鱼幸运加成")
        @Config.RangeInt(min = 0, max = 100)
        public int DroneFishingLuckBonus = 8;
    }

    // --------------------------------------------
    // 机器配置类
    // --------------------------------------------
    public static class MachineConfig {
        @Config.Comment("是否启用拆解机（需要重启游戏生效）")
        @Config.Name("启用拆解机")
        @Config.RequiresMcRestart
        public boolean EnableDisassembly = false;

        @Config.Comment("核爆炸的影响半径（单位：格，需要重启生效）")
        @Config.Name("核爆炸范围")
        @Config.RequiresMcRestart
        public float NuclearExplosionRange = 10;

        @Config.Comment("是否开启工业蜂箱的粒子特效（需要重启生效）")
        @Config.Name("工业蜂箱粒子特效")
        @Config.RequiresMcRestart
        public boolean EnableIndustrialApiaryTx = true;

        @Config.Comment("是否启用更廉价的大型机器合成配方（需要重启生效）")
        @Config.Name("启用便宜大机器")
        @Config.RequiresMcRestart
        public boolean EnableIndustrialMachines = false;
    }

    // --------------------------------------------
    // 模块化装甲配置类
    // --------------------------------------------
    public static class ModularArmorConfig {
        // 原本单独的 Modules 类中的缩放倍数，直接合并到此处
        @Config.Comment("望远镜模块的缩放倍数（默认 5 倍）")
        @Config.Name("望远镜缩放倍数")
        @Config.RangeDouble
        public double binocularZoom = 5;

        @Config.Comment("头盔可安装的模块槽位数（0~12，需要重启生效）")
        @Config.Name("头盔槽位")
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 0, max = 12)
        public int helmetSlots = 3;

        @Config.Comment("胸甲可安装的模块槽位数（0~12，需要重启生效）")
        @Config.Name("胸甲槽位")
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 0, max = 12)
        public int chestPlateSlots = 5;

        @Config.Comment("护腿可安装的模块槽位数（0~12，需要重启生效）")
        @Config.Name("护腿槽位")
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 0, max = 12)
        public int leggingsSlots = 4;

        @Config.Comment("靴子可安装的模块槽位数（0~12，需要重启生效）")
        @Config.Name("靴子槽位")
        @Config.RequiresMcRestart
        @Config.RangeInt(min = 0, max = 12)
        public int bootsSlot = 2;
    }
}