package com.drppp.drtech.api.vein;


import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 监听 Chunk 生命周期事件：
 * <ol>
 *   <li>{@link AttachCapabilitiesEvent} — 将 VeinData Capability 附加到每个 Chunk。</li>
 *   <li>{@link ChunkEvent.Load}         — Chunk 加载时触发矿脉生成判定（仅服务端，仅一次）。</li>
 * </ol>
 *
 * <p>注册方式（在主 Mod 类的构造函数或 preInit 中）：
 * <pre>
 *   MinecraftForge.EVENT_BUS.register(new VeinChunkEventHandler());
 * </pre>
 */
public class VeinChunkEventHandler {

    /**
     * 将 Capability Provider 附加到 Chunk。
     * 每个 Chunk 对象创建时只触发一次。
     */
    @SubscribeEvent
    public void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(
                VeinDataCapability.KEY,
                new VeinDataCapability.Provider()
        );
    }

    /**
     * Chunk 加载（或生成）时执行矿脉生成判定。
     * {@link WorldVeinGenerator#generateForChunk} 内部已通过 isGenerated() 保证幂等。
     */
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        // 客户端不执行生成逻辑
        if (event.getWorld().isRemote) return;

        Chunk chunk = event.getChunk();
        WorldVeinGenerator.generateForChunk(
                event.getWorld(),
                chunk.x,
                chunk.z
        );
    }
}
