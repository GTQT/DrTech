package com.drppp.drtech.loaders.builder;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.metatileentity.*;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.electric.MetaTileEntityHull;
import keqing.gtqtcore.api.recipes.GTQTcoreRecipeMaps;
import keqing.gtqtcore.common.items.GTQTMetaItems;
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
    public  static  List<ItemStack> black_list = new ArrayList<>();
    public static final Map<String, Tuple<ItemStack, Integer>> circuitToUse = createCircuitMap(Arrays.asList(
            GTQTMetaItems.GENERAL_CIRCUIT_LV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_MV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_HV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_EV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_IV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_LuV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_ZPM.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_UV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_UHV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_UEV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_UIV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_UXV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_OpV.getStackForm(),
            GTQTMetaItems.GENERAL_CIRCUIT_MAX.getStackForm()
    ));

    public static void buildDisassemblerRecipes() {
        Map<ResourceLocation, MetaTileEntity> mteMap = new HashMap<>();
        Map<MetaTileEntity, IRecipe> recipeMap = new HashMap<>();
        GregTechAPI.mteManager.getRegistries().forEach(mter -> {
            mter.getKeys().forEach(item->{
                var mte = mter.getObject(item);
                if ((mte instanceof EnergyContainerHandler.IEnergyChangeListener
                        || mte instanceof MultiblockControllerBase
                        || mte instanceof SteamMetaTileEntity
                )
                        && (!(mte instanceof MetaTileEntityHull))) {
                    if (!mteMap.containsKey(mte.metaTileEntityId))
                        mteMap.put(mte.metaTileEntityId, mte);
                }
            });
        });
        ForgeRegistries.RECIPES.forEach(iRecipe -> {
            MetaTileEntity mte;
            if ((mte = testAndGetMTE(iRecipe.getRecipeOutput())) != null) {
                GregTechAPI.mteManager.getRegistries().forEach(mter -> {
                    mter.getKeys().forEach(item->{
                        if (mteMap.containsKey(mter.getObject(item))) {
                            if (iRecipe.getIngredients().size() != 1)
                                recipeMap.put(mte, recipeMap.containsKey(mte) ? null : iRecipe);
                        }
                    });
                });

            }
        });
		//合成拆解 仅限GT MetileEntity
        recipeMap.forEach(DisassemblyHandler::buildDisassemblerRecipe);
        //RecipeMap拆解 不拆出流体
        Collection<RecipeMap> Recipes = new ArrayList<>();
        Recipes.add(RecipeMaps.ASSEMBLER_RECIPES);
        Recipes.add(GTQTcoreRecipeMaps.COMPONENT_ASSEMBLER_RECIPES);
        for(RecipeMap map:Recipes)
        {
            Collection<Recipe> rc = map.getRecipeList();
            rc.forEach(DisassemblyHandler::buildAssDisassemblerRecipe);
        }
    }

    private static void buildAssDisassemblerRecipe(Recipe recipe) {
        if(recipe!=null)
        {
            if(DISASSEMBLER_RECIPES.findRecipe(recipe.getEUt(),recipe.getOutputs(),recipe.getFluidOutputs()) != null)
            {
                return;
            }
            boolean cando =true;
            List<ItemStack> outputItems = recipe.getOutputs();
            for (int i = 0; i < outputItems.size(); i++) {
                for (int j = 0; j < black_list.size(); j++) {
                    if(black_list.get(j).getItem()==outputItems.get(i).getItem() && black_list.get(j).getMetadata()==outputItems.get(i).getMetadata())
                    {
                        cando=false;
                        break;
                    }
                }
            }
            List<ItemStack[]> inItems = new ArrayList<>();
            long voltage = recipe.getEUt();
            int duration = recipe.getDuration();
            if(outputItems.size()>0 && recipe.getInputs().size()>0 && cando)
            {
                recipe.getInputs().forEach(x->inItems.add(x.getInputStacks().clone()));
                for (var s:inItems)
                {
                      if(s.length>0)
                      {
                          //消除矿磁带入的多余物品
                          s[0] = new ItemStack(s[0].getItem(),s[0].getCount(),s[0].getMetadata());
                          if(s.length>1)
                          {
                              for (int i = 1; i < s.length; i++) {
                                  s[i] = ItemStack.EMPTY;
                              }
                          }
						  //排除编程电路
                          if(s[0].getItem() == MetaItems.INTEGRATED_CIRCUIT.getMetaItem() && s[0].getMetadata()== MetaItems.INTEGRATED_CIRCUIT.getMetaValue())
                              s[0] = ItemStack.EMPTY;
						  //排除工具
                          if ( s[0].getItem() instanceof ItemTool || s[0].getItem() instanceof ItemGTTool)
                              s[0] = OreDictUnifier.get(dustTiny, Ash).copy();
						  //电路板处理
                          else
                          {
                              Tuple<Boolean, String> isCircuit = isCircuit(s[0]);
                              if (isCircuit.getFirst())
                              {
								  //转换为通用电路
                                  int count = s[0].getCount();
                                  ItemStack ciru = circuitToUse.get(isCircuit.getSecond()).getFirst();
                                  s[0] = new ItemStack(ciru.getItem(),count,ciru.getMetadata());
                              }
                          }
                      }
                }
                ResourceLocation resourceLocation = outputItems.get(0).getItem().getRegistryName();
                if(resourceLocation != null && resourceLocation.getNamespace().equals(GTValues.MODID))
                {
                    RecipeBuilder<?> builder = DISASSEMBLER_RECIPES.recipeBuilder()
                            .EUt((int) voltage)
                            .duration(duration)
                            .inputs(outputItems.get(0));
                    inItems.forEach(x->builder.outputs(x));
                    builder.buildAndRegister();
                }
            }

        }
    }

    public static void buildDisassemblerRecipe(MetaTileEntity mte, IRecipe recipe) {
        if (recipe != null) {
            List<ItemStack> outputItems = new ArrayList<>();
            recipe.getIngredients().forEach(ingredient -> {
                ItemStack[] itemStacks = ingredient.getMatchingStacks();
				//工具替换成灰烬
                if (itemStacks.length == 0 || itemStacks[0].getItem() instanceof ItemTool || itemStacks[0].getItem() instanceof ItemGTTool)
                    outputItems.add(OreDictUnifier.get(dustTiny, Ash));
                else {
					//如果是电路替换为通用电路 否接直接添加
                    Tuple<Boolean, String> isCircuit = isCircuit(itemStacks[0]);
                    if (isCircuit.getFirst())
                        outputItems.add(circuitToUse.get(isCircuit.getSecond()).getFirst());
                    else
                        outputItems.add(itemStacks[0]);
                }
            });
			//获取电压
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
			//拆解物品数量
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

    public static Tuple<Boolean, String> isCircuit(ItemStack stack) {
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
