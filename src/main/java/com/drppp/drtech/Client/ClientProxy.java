package com.drppp.drtech.Client;

import com.drppp.drtech.Client.render.StructureSelectRenderer;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.CommonProxy;
import com.drppp.drtech.common.drtMetaEntities;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber({Side.CLIENT})
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    public void preLoad() {
        super.preLoad();
        drtMetaEntities.initRenderers();
    }
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        StructureSelectRenderer.render(event);
    }

}
