package com.drppp.drtech.common.Items;

import com.drppp.drtech.Client.render.Items.GeoItemRender;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.GeoItems.GeoItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import software.bernie.example.registry.ItemRegistry;

public class GeoItemsInit {
    private static IForgeRegistry<Item> itemRegistry;
    public static GeoItem GEO_ITEM_1;
    public static void init(RegistryEvent.Register<Item> event)
    {
        itemRegistry = event.getRegistry();
        GEO_ITEM_1 = registerItem(new GeoItem(),"test_item");
    }
    public static <T extends Item> T registerItem(T item, String name) {
        registerItem(item, new ResourceLocation(Tags.MODID, name));
        return item;
    }

    public static <T extends Item> T registerItem(T item, ResourceLocation name) {
        itemRegistry.register(item.setRegistryName(name).setTranslationKey(name.toString().replace(":", ".")));
        return item;
    }
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry() {
        ModelLoader.setCustomModelResourceLocation(GEO_ITEM_1, 0,
                new ModelResourceLocation(Tags.MODID + ":windrotor", "inventory"));


        GEO_ITEM_1.setTileEntityItemStackRenderer(new GeoItemRender());
    }
}
