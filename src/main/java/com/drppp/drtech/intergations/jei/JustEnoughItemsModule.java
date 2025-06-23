package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

import java.util.ArrayList;
import java.util.List;


@JEIPlugin
public class JustEnoughItemsModule extends IntegrationSubmodule implements IModPlugin {
    public static IGuiHelper guiHelper;
    @Override
    public void registerCategories( IRecipeCategoryRegistration registry) {
        //guiHelper = registry.getJeiHelpers().getGuiHelper();
        //registry.addRecipeCategories(new CanDoWorkMachineJeiCategory(registry.getJeiHelpers().getGuiHelper()));
    }
    @Override
    public void register(IModRegistry registry) {
        /*
        var uid = Tags.MODID+":CanDoWorkMachines";
        List<CanDoWorkMachineJei> Machines = new ArrayList<>();
        Machines.add(new CanDoWorkMachineJei());
        registry.addRecipes(Machines,uid);

         */
    }

}
