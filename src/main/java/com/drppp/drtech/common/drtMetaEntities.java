package com.drppp.drtech.common;

import com.drppp.drtech.Client.Entity.EntityAdvancedRocketRender;
import com.drppp.drtech.Client.render.Entity.EntityRemderFactory;
import com.drppp.drtech.Client.render.Entity.RenderUTiGolem;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import com.drppp.drtech.common.Entity.moster.EntityUTiGolem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class drtMetaEntities {

    public static void init() {
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MODID, "uti_golem"), EntityUTiGolem.class,"UTi Golem",4,DrTechMain.instance,64,3,true);
        EntityRegistry.registerEgg(new ResourceLocation(Tags.MODID, "uti_golem"),0x48e06e, 0x199038);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MODID, "advanced_rocket"), EntityAdvancedRocket.class,"Advanced Rocket",5,DrTechMain.instance,64,3,true);
        EntityRegistry.registerEgg(new ResourceLocation(Tags.MODID, "advanced_rocket"),0x58e12e, 0x1412038);
    }

    @SideOnly(Side.CLIENT)
    public static void initRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityUTiGolem.class,new EntityRemderFactory<>(RenderUTiGolem.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityAdvancedRocket.class, new EntityAdvancedRocketRender(Minecraft.getMinecraft().getRenderManager()));
    }

}