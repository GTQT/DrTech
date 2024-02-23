package com.drppp.drtech;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Items.ItemsInit;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public final class DrTechModelRegister {
    @SubscribeEvent
    public static void onModelReg(ModelRegistryEvent event) {
        onModelRegistration();

    }
    @SideOnly(Side.CLIENT)
    public static void onModelRegistration() {
        OBJLoader.INSTANCE.addDomain(Tags.MODID);
        ModelResourceLocation model = new ModelResourceLocation(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY.getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY), 0, model);

        ModelResourceLocation model1 = new ModelResourceLocation(BlocksInit.BLOCK_HOMO_EYE.getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_HOMO_EYE), 0, model1);

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR1), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR1.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR2), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR2.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_CONNECTOR3), 0, new ModelResourceLocation(BlocksInit.BLOCK_CONNECTOR3.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlocksInit.BLOCK_GOLDEN_SEA), 0, new ModelResourceLocation(BlocksInit.BLOCK_GOLDEN_SEA.getRegistryName(), "inventory"));

        ItemsInit.registerItemModels();
    }
}
