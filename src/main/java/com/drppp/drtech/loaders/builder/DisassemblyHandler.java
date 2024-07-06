package com.drppp.drtech.loaders.builder;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.metatileentity.*;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.electric.MetaTileEntityHull;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;

import static com.drppp.drtech.loaders.DrtechReceipes.DISASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.Ash;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;

public class DisassemblyHandler {
    private static final Map<String, Tuple<ItemStack, Integer>> circuitToUse = createCircuitMap(Arrays.asList(
            MetaItems.BASIC_CIRCUIT_BOARD.getStackForm(),
            MetaItems.INTEGRATED_CIRCUIT_MV.getStackForm(),
            MetaItems.PROCESSOR_ASSEMBLY_HV.getStackForm(),
            MetaItems.WORKSTATION_EV.getStackForm(),
            MetaItems.MAINFRAME_IV.getStackForm(),
            MetaItems.NANO_MAINFRAME_LUV.getStackForm(),
            MetaItems.QUANTUM_MAINFRAME_ZPM.getStackForm(),
            MetaItems.CRYSTAL_MAINFRAME_UV.getStackForm(),
            MetaItems.WETWARE_PROCESSOR_LUV.getStackForm(),
            MetaItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM.getStackForm(),
            MetaItems.WETWARE_SUPER_COMPUTER_UV.getStackForm(),
            MetaItems.WETWARE_MAINFRAME_UHV.getStackForm()
    ));

    public static void buildDisassemblerRecipes() {
        Map<ResourceLocation, MetaTileEntity> mteMap = new HashMap<>();
        Map<MetaTileEntity, IRecipe> recipeMap = new HashMap<>();
        GregTechAPI.MTE_REGISTRY.forEach(mte -> {
            if ((mte instanceof EnergyContainerHandler.IEnergyChangeListener
                    || mte instanceof MultiblockControllerBase
                    || mte instanceof SteamMetaTileEntity
            )
                    && (!(mte instanceof MetaTileEntityHull))) {

                if (!mteMap.containsKey(mte.metaTileEntityId))
                    mteMap.put(mte.metaTileEntityId, mte);
            }
        });
        ForgeRegistries.RECIPES.forEach(iRecipe -> {
            MetaTileEntity mte;
            if ((mte = testAndGetMTE(iRecipe.getRecipeOutput())) != null) {
                if (mteMap.containsKey(GregTechAPI.MTE_REGISTRY.getNameForObject(mte))) {
                    if (iRecipe.getIngredients().size() != 1)
                        recipeMap.put(mte, recipeMap.containsKey(mte) ? null : iRecipe);
                }
            }
        });
        recipeMap.forEach(DisassemblyHandler::buildDisassemblerRecipe);
    }

    public static void buildDisassemblerRecipe(MetaTileEntity mte, IRecipe recipe) {
        if (recipe != null) {
            List<ItemStack> outputItems = new ArrayList<>();
            recipe.getIngredients().forEach(ingredient -> {
                ItemStack[] itemStacks = ingredient.getMatchingStacks();
                if (itemStacks.length == 0 || itemStacks[0].getItem() instanceof ItemTool || itemStacks[0].getItem() instanceof ItemGTTool)
                    outputItems.add(OreDictUnifier.get(dustTiny, Ash));
                else {
                    Tuple<Boolean, String> isCircuit = isCircuit(itemStacks[0]);
                    if (isCircuit.getFirst())
                        outputItems.add(circuitToUse.get(isCircuit.getSecond()).getFirst());
                    else
                        outputItems.add(itemStacks[0]);
                }
            });

            long voltage = 0;
            if (mte instanceof ITieredMetaTileEntity) {
                voltage = GTValues.V[((ITieredMetaTileEntity) mte).getTier()];
            }
            else if (mte instanceof IEnergyContainer) {
                IEnergyContainer energy = ((IEnergyContainer) mte);
                voltage = (int) energy.getInputVoltage();
                if (voltage == 0)
                    voltage = (int) energy.getOutputVoltage();

            }
            if (mte instanceof MultiblockControllerBase) {
                if (voltage != 0)
                    voltage = 0;
                for (ItemStack itemStack : outputItems) {
                    Tuple<ItemStack, Integer> tempTuple;
                    MetaTileEntity tempMTE;
                    if ((voltage = ((tempTuple = circuitToUse.get(itemStack.getItemDamage())) != null ? tempTuple.getSecond() : 0)) != 0) {
                        voltage = GTValues.V[(int)voltage];
                        break;
                    }
                    else if ((tempMTE = testAndGetMTE(itemStack)) != null
                            && tempMTE instanceof TieredMetaTileEntity
                            ) {
                        voltage = GTValues.V[((ITieredMetaTileEntity) tempMTE).getTier()];
                        break;
                    }
                }
            }
            voltage = Math.max(voltage, 32);
            int itemCount = recipe.getIngredients().size();

            RecipeBuilder<?> builder = DISASSEMBLER_RECIPES.recipeBuilder()
                    .EUt((int) voltage)
                    .duration(itemCount * 100) // 5s per output item
                    .inputs(mte.getStackForm());
                builder.outputs(outputItems);
                builder.buildAndRegister();
        }
    }
    private static MetaTileEntity testAndGetMTE(ItemStack itemStack) {
        if (GTUtility.getMetaTileEntity(itemStack)!=null )
            return GTUtility.getMetaTileEntity(itemStack);
        return null;
    }
    private static Map<String, Tuple<ItemStack, Integer>> createCircuitMap(List<ItemStack> circuitsMasterList) {
        Map<String, Tuple<ItemStack, Integer>> circuits = new HashMap<>();
        circuitsMasterList.forEach(output ->
                OreDictUnifier.getOreDictionaryNames(output).forEach(name ->
                        OreDictUnifier.getAllWithOreDictionaryName(name).forEach(itemStack ->
                                circuits.put(name, new Tuple<>(output, circuitsMasterList.indexOf(output) + 1))
                        )
                )
        );
        return circuits;
    }

    private static Tuple<Boolean, String> isCircuit(ItemStack stack) {
        Set<String> stackOres = OreDictUnifier.getOreDictionaryNames(stack);
        for (String circuitOre : circuitToUse.keySet()) {
            for (String stackOre : stackOres) {
                if (circuitOre.equals(stackOre)) {
                    return new Tuple<>(true, stackOre);
                }
            }
        }
        return new Tuple<>(false, "");
    }
}
