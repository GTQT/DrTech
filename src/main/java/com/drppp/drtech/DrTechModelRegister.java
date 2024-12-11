package com.drppp.drtech;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.Client.Particle.EntityParticleGroup;
import com.drppp.drtech.Client.Particle.EntityParticleSpray;
import com.drppp.drtech.Client.Particle.InstantParticleRender;
import com.drppp.drtech.Client.Particle.ParticleRenderer;
import com.drppp.drtech.Client.render.BulletRenderer;
import com.drppp.drtech.Client.render.PlasmaBulletRenderer;
import com.drppp.drtech.Client.render.TachyonRenderer;
import com.drppp.drtech.common.Entity.EntityGunBullet;
import com.drppp.drtech.common.Entity.EntityHyperGunBullet;
import com.drppp.drtech.common.Entity.EntityPlasmaBullet;
import com.drppp.drtech.common.Entity.EntityTachyonBullet;
import com.drppp.drtech.common.Items.GeoItemsInit;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public final class DrTechModelRegister {
    @SubscribeEvent
    public static void onModelReg(ModelRegistryEvent event) {
        onModelRegistration();
        EntityRenderReg();
    }
    @SideOnly(Side.CLIENT)
    public static void onModelRegistration() {

        ModelResourceLocation model = new ModelResourceLocation(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY.getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY), 0, model);
        ModelResourceLocation model1 = new ModelResourceLocation(BlocksInit.BLOCK_HOMO_EYE.getRegistryName(), "inventory");
       ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_HOMO_EYE), 0, model1);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR1), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR1.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR2), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR2.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR3), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR3.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_GOLDEN_SEA), 0, new ModelResourceLocation(BlocksInit.BLOCK_GOLDEN_SEA.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_PEACEFUL_TABLE), 0, new ModelResourceLocation(BlocksInit.BLOCK_PEACEFUL_TABLE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_STORAGE_PAIL), 0, new ModelResourceLocation(BlocksInit.BLOCK_STORAGE_PAIL.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_WASTE_DIRT), 0, new ModelResourceLocation(BlocksInit.BLOCK_WASTE_DIRT.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_SAP_BAG), 0, new ModelResourceLocation(BlocksInit.BLOCK_SAP_BAG.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_ADVANCED_CAULDRON), 0, new ModelResourceLocation(BlocksInit.BLOCK_ADVANCED_CAULDRON.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_TIME_TABLE), 0, new ModelResourceLocation(BlocksInit.BLOCK_TIME_TABLE.getRegistryName(), "inventory"));

        ItemsInit.registerItemModels();
        GeoItemsInit.onModelRegistry();
    }
    @SideOnly(Side.CLIENT)
    public static void EntityRenderReg()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityParticleGroup.class, manager -> new ParticleRenderer(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityHyperGunBullet.class, manager -> (Render<EntityHyperGunBullet>) new BulletRenderer(manager,
                new ResourceLocation(Tags.MODID, "textures/entity/hyper_bullet.png")));
        RenderingRegistry.registerEntityRenderingHandler(EntityPlasmaBullet.class, manager -> new PlasmaBulletRenderer(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGunBullet.class, manager -> (Render<EntityGunBullet>) new BulletRenderer(manager,
                new ResourceLocation(Tags.MODID, "textures/entity/bullet.png")));
        RenderingRegistry.registerEntityRenderingHandler(EntityTachyonBullet.class, manager -> (Render<EntityTachyonBullet>) new TachyonRenderer(manager,
                new ResourceLocation(Tags.MODID, "textures/entity/tachyon.png")));
        RenderingRegistry.registerEntityRenderingHandler(EntityParticleSpray.class, manager -> new InstantParticleRender(manager));

    }
}
