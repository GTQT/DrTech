package com.drppp.drtech.common.event;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class RecipeHandler {

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // 作物架: 4个木棍
        event.getRegistry().register(
            new ShapedOreRecipe(
                new ResourceLocation(Tags.MODID, "crop_stick"),
                new ItemStack(BlocksInit.CROP_STICK, 2),
                " S ",
                "S S",
                " S ",
                'S', "stickWood"
            ).setRegistryName(new ResourceLocation(Tags.MODID, "crop_stick"))
        );

        // 作物分析器
        event.getRegistry().register(
            new ShapedOreRecipe(
                new ResourceLocation(Tags.MODID, "crop_analyzer"),
                new ItemStack(ItemsInit.CROP_ANALYZER),
                " G ",
                " R ",
                " I ",
                'G', "paneGlass",
                'R', "dustRedstone",
                'I', "ingotIron"
            ).setRegistryName(new ResourceLocation(Tags.MODID, "crop_analyzer"))
        );
    }
}
