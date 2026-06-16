package com.drppp.drtech;

import com.drppp.drtech.Client.Particle.EntityParticleGroup;
import com.drppp.drtech.Client.Particle.EntityParticleSpray;
import com.drppp.drtech.Client.Particle.InstantParticleRender;
import com.drppp.drtech.Client.Particle.ParticleRenderer;
import com.drppp.drtech.Client.render.BulletRenderer;
import com.drppp.drtech.Client.render.PlasmaBulletRenderer;
import com.drppp.drtech.Client.render.TachyonRenderer;
import com.drppp.drtech.common.Blocks.BlockDriedGhast;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Entity.EntityGunBullet;
import com.drppp.drtech.common.Entity.EntityHyperGunBullet;
import com.drppp.drtech.common.Entity.EntityPlasmaBullet;
import com.drppp.drtech.common.Entity.EntityTachyonBullet;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.lootgames.block.BlockDungeonBricks;
import com.drppp.drtech.lootgames.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public final class DrTechModelRegister {
    @SubscribeEvent
    public static void onModelReg(ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain("drtech");
        B3DLoader.INSTANCE.addDomain("drtech");
        onModelRegistration();
        EntityRenderReg();
    }

    @SideOnly(Side.CLIENT)
    public static void onModelRegistration() {
        // LootGames models
        // DUNGEON_BRICKS - register all 7 variants directly from blockstate
        Item dungeonBricksItem = Item.getItemFromBlock(ModBlocks.DUNGEON_BRICKS);
        for (int meta = 0; meta < BlockDungeonBricks.EnumType.values().length; meta++) {
            String variantName = BlockDungeonBricks.EnumType.byMetadata(meta).getName();
            ModelLoader.setCustomModelResourceLocation(dungeonBricksItem, meta,
                    new ModelResourceLocation(Tags.MODID + ":lootgames_dungeon_bricks", "variant=" + variantName));
        }
        // DUNGEON_LAMP - register both variants using forge_marker property syntax
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.DUNGEON_LAMP), 0,
                new ModelResourceLocation(Tags.MODID + ":lootgames_dungeon_lamp", "broken=false"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.DUNGEON_LAMP), 1,
                new ModelResourceLocation(Tags.MODID + ":lootgames_dungeon_lamp", "broken=true"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.PUZZLE_MASTER), 0, new ModelResourceLocation(Tags.MODID + ":lootgames_puzzle_master", "inventory"));
        registerBlockModel(ModBlocks.GOL_MASTER, 0, "lootgames_gol_master");
        registerBlockModel(ModBlocks.GOL_SUBORDINATE, 0, "lootgames_gol_subordinate");
        registerBlockModel(ModBlocks.MS_ACTIVATOR, 0, "lootgames_ms_activator");

        ModelResourceLocation model = new ModelResourceLocation(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY.getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY), 0, model);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR1), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR1.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR2), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR2.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR3), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR3.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_GOLDEN_SEA), 0, new ModelResourceLocation(BlocksInit.BLOCK_GOLDEN_SEA.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_DRIED_GHAST), 0, new ModelResourceLocation(BlocksInit.BLOCK_DRIED_GHAST.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_SMOOTH_BASALT), 0, new ModelResourceLocation(BlocksInit.BLOCK_SMOOTH_BASALT.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CALCITE), 0, new ModelResourceLocation(BlocksInit.BLOCK_CALCITE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_AMETHYST_BLOCK), 0, new ModelResourceLocation(BlocksInit.BLOCK_AMETHYST_BLOCK.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_BUDDING_AMETHYST), 0, new ModelResourceLocation(BlocksInit.BLOCK_BUDDING_AMETHYST.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_SMALL_AMETHYST_BUD), 0, new ModelResourceLocation(BlocksInit.BLOCK_SMALL_AMETHYST_BUD.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_MEDIUM_AMETHYST_BUD), 0, new ModelResourceLocation(BlocksInit.BLOCK_MEDIUM_AMETHYST_BUD.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_LARGE_AMETHYST_BUD), 0, new ModelResourceLocation(BlocksInit.BLOCK_LARGE_AMETHYST_BUD.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_AMETHYST_CLUSTER), 0, new ModelResourceLocation(BlocksInit.BLOCK_AMETHYST_CLUSTER.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_LANTERN), 0, new ModelResourceLocation(BlocksInit.BLOCK_LANTERN.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_SOUL_LANTERN), 0, new ModelResourceLocation(BlocksInit.BLOCK_SOUL_LANTERN.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CHAIN), 0, new ModelResourceLocation(BlocksInit.BLOCK_CHAIN.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_COMPOSTER), 0, new ModelResourceLocation(BlocksInit.BLOCK_COMPOSTER.getRegistryName(), "inventory"));
        ModelLoader.setCustomStateMapper(BlocksInit.BLOCK_DRIED_GHAST, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(BlocksInit.BLOCK_DRIED_GHAST.getRegistryName(),
                        "facing=" + state.getValue(BlockDriedGhast.FACING).getName()
                                + ",hydration=" + state.getValue(BlockDriedGhast.HYDRATION));
            }
        });
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_PEACEFUL_TABLE), 0, new ModelResourceLocation(BlocksInit.BLOCK_PEACEFUL_TABLE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_STORAGE_PAIL), 0, new ModelResourceLocation(BlocksInit.BLOCK_STORAGE_PAIL.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_WASTE_DIRT), 0, new ModelResourceLocation(BlocksInit.BLOCK_WASTE_DIRT.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_ADVANCED_CAULDRON), 0, new ModelResourceLocation(BlocksInit.BLOCK_ADVANCED_CAULDRON.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_TIME_TABLE), 0, new ModelResourceLocation(BlocksInit.BLOCK_TIME_TABLE.getRegistryName(), "inventory"));
        ItemsInit.registerItemModels();
    }

    @SideOnly(Side.CLIENT)
    private static void registerBlockModel(Block block, int meta, String modelName) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta,
                new ModelResourceLocation(Tags.MODID + ":" + modelName, "inventory"));
    }

    @SideOnly(Side.CLIENT)
    public static void EntityRenderReg() {
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
