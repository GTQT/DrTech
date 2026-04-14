package com.drppp.drtech.api.vein;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * ─── 接入指南（在你自己的 Mod 主类中仿照此处操作）───
 *
 * 1. preInit：注册 Capability + 事件监听
 * 2. preInit：向 VeinRegistry 注册矿脉类型
 *
 * 之后在任意 TileEntity 中通过 VeinHelper 查询矿脉即可。
 */

public class VeinSystemInit {

    public static void init() {

        // ① 注册 Capability（必须在 preInit 完成）
        CapabilityManager.INSTANCE.register(
                VeinDataCapability.IVeinDataCapability.class,
                new VeinDataCapability.Storage(),
                VeinDataCapability.Implementation::new
        );
        // ② 注册事件监听（AttachCapabilities + ChunkLoad）
        MinecraftForge.EVENT_BUS.register(new VeinChunkEventHandler());
        // ③ 注册矿脉类型（按游戏设计自由添加）
        registerVeinTypes();
    }

    // ─── 矿脉定义示例 ────────────────────────────────────────────
    private static void registerVeinTypes() {

        // 普通铁矿脉：铁最常见，铜次之，煤偶尔出现
        VeinRegistry.register(new VeinType("iron_vein")
                .addOre("gregtech:ore_monazite_0:6", 50)
                .addOre("gregtech:ore_redstone_0:5", 50)
                .addOre("minecraft:redstone_ore", 50)
                .addOre("minecraft:iron_ore",   60)
                .addOre("minecraft:copper_ore", 30)   // 自定义矿物填自己的注册名
                .addOre("minecraft:coal_ore",   10)
                .addDimension(0)
        );

        // 稀有钻石脉：钻石极少，红石主体，金次之
        VeinRegistry.register(new VeinType("diamond_vein")
                .addOre("gregtech:ore_monazite_0:6", 50)
                .addOre("gregtech:ore_redstone_0:5", 50)
                .addOre("minecraft:redstone_ore", 50)
                .addOre("minecraft:gold_ore",     20)
                .addOre("minecraft:diamond_ore",   8)
                .addOre("minecraft:emerald_ore",   2)
                .setOreTypeRange(2, 3).addDimension(0));              // 只随机出 2~3 种

        // 贫瘠石矿脉：普通石头，偶有燧石
        VeinRegistry.register(new VeinType("stone_vein")
                .addOre("gregtech:ore_monazite_0:6", 50)
                .addOre("gregtech:ore_redstone_0:5", 50)
                .addOre("minecraft:redstone_ore", 50)
                .addOre("minecraft:stone",  80)
                .addOre("minecraft:gravel", 15)
                .addOre("minecraft:flint",   5)
                .setOreTypeRange(2, 2).addDimension(0));              // 固定出 2 种
    }

}
