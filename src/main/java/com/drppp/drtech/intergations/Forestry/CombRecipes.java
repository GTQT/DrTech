package com.drppp.drtech.intergations.Forestry;

import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.loaders.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.integration.forestry.bees.GTCombType;
import gregtech.integration.forestry.bees.GTDropItem;
import keqing.gtqtcore.api.unification.GTQTMaterials;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import com.google.common.collect.ImmutableMap;
import forestry.api.recipes.RecipeManagers;
import forestry.core.ModuleCore;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;

import java.util.Arrays;

public class CombRecipes {

    public static void initDRTCombs() {
        addCentrifugeToMaterial(DrtCombType.BORAX, new Material[] { Materials.Borax,Materials.Salt,Materials.RockSalt }, new int[] { 30 * 100,10 * 100,10 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addProcessGT(DrtCombType.BORAX, new Material[] { Materials.Borax }, Voltage.LV);
        addProcessGT(DrtCombType.BRIGHT, new Material[] { Materials.NetherStar }, Voltage.EV);
        addCentrifugeToMaterial(DrtCombType.WITHER, new Material[] { Materials.Coal }, new int[] { 60 * 100 },
                new int[] { 9 }, Voltage.MV, DrMetaItems.SKULL_DUST.getStackForm(), 15 * 100);
        addCentrifugeToMaterial(DrtCombType.MUTAGENIC_AGENT, new Material[] {  }, new int[] {  },
                new int[] { 9 }, Voltage.HV, new ItemStack(GTDropItem.getByNameOrId("gregtech:gt.honey_drop"),1,3), 100 * 100);
        addProcessGT(DrtCombType.MUTAGENIC_AGENT, new Material[] { Materials.Uranium238,Materials.Uranium235,Materials.Plutonium239 }, Voltage.EV);

        addCombProductProcess(GTCombType.COAL, new Material[] { Materials.Coal }, Voltage.LV);
        addCombProductProcess(GTCombType.COKE, new Material[] { Materials.Coke }, Voltage.LV);
        addCombProductProcess(GTCombType.OIL, new Material[] { Materials.Oilsands }, Voltage.LV);
        addCombProductProcess(GTCombType.APATITE, new Material[] { Materials.Apatite, Materials.TricalciumPhosphate }, Voltage.LV);
        addCombProductProcess(GTCombType.REDALLOY, new Material[] { Materials.RedAlloy, Materials.Redstone, Materials.Copper },
                Voltage.LV);
        addCombProductProcess(GTCombType.STAINLESSSTEEL, new Material[] { Materials.StainlessSteel, Materials.Iron,
                Materials.Chrome, Materials.Manganese, Materials.Nickel }, Voltage.HV);
        addCombProductProcess(GTCombType.STONE, new Material[] { Materials.Soapstone, Materials.Talc, Materials.Apatite,
                Materials.Phosphate, Materials.TricalciumPhosphate }, Voltage.LV);
        addCombProductProcess(GTCombType.CERTUS,
                new Material[] { Materials.CertusQuartz, Materials.Quartzite, Materials.Barite }, Voltage.LV);
        addCombProductProcess(GTCombType.REDSTONE, new Material[] { Materials.Redstone, Materials.Cinnabar }, Voltage.LV);
        addCombProductProcess(GTCombType.LAPIS,
                new Material[] { Materials.Lapis, Materials.Sodalite, Materials.Lazurite, Materials.Calcite },
                Voltage.LV);
        addCombProductProcess(GTCombType.RUBY, new Material[] { Materials.Ruby, Materials.Redstone }, Voltage.LV);
        addCombProductProcess(GTCombType.SAPPHIRE,
                new Material[] { Materials.Sapphire, Materials.GreenSapphire, Materials.Almandine, Materials.Pyrope },
                Voltage.LV);
        addCombProductProcess(GTCombType.DIAMOND, new Material[] { Materials.Diamond, Materials.Graphite }, Voltage.LV);
        addCombProductProcess(GTCombType.OLIVINE, new Material[] { Materials.Olivine, Materials.Bentonite, Materials.Magnesite,
                Materials.GlauconiteSand }, Voltage.LV);
        addCombProductProcess(GTCombType.EMERALD, new Material[] { Materials.Emerald, Materials.Beryllium, Materials.Thorium },
                Voltage.LV);
        addCombProductProcess(GTCombType.PYROPE,
                new Material[] { Materials.Pyrope, Materials.Aluminium, Materials.Magnesium, Materials.Silicon },
                Voltage.LV);
        addCombProductProcess(GTCombType.GROSSULAR,
                new Material[] { Materials.Grossular, Materials.Aluminium, Materials.Silicon }, Voltage.LV);
        addCombProductProcess(GTCombType.COPPER, new Material[] { Materials.Copper, Materials.Tetrahedrite,
                Materials.Chalcopyrite, Materials.Malachite, Materials.Pyrite, Materials.Stibnite }, Voltage.LV);
        addCombProductProcess(GTCombType.TIN, new Material[] { Materials.Tin, Materials.Cassiterite, Materials.CassiteriteSand },
                Voltage.LV);
        addCombProductProcess(GTCombType.LEAD, new Material[] { Materials.Lead, Materials.Galena }, Voltage.LV);
        addCombProductProcess(GTCombType.NICKEL, new Material[] { Materials.Nickel, Materials.Garnierite, Materials.Pentlandite,
                Materials.Cobaltite, Materials.Wulfenite, Materials.Powellite }, Voltage.LV);
        addCombProductProcess(GTCombType.ZINC, new Material[] { Materials.Sphalerite, Materials.Sulfur }, Voltage.LV);
        addCombProductProcess(GTCombType.SILVER, new Material[] { Materials.Silver, Materials.Galena }, Voltage.LV);
        addCombProductProcess(GTCombType.GOLD, new Material[] { Materials.Gold, Materials.Magnetite }, Voltage.LV);
        addCombProductProcess(GTCombType.SULFUR, new Material[] { Materials.Sulfur, Materials.Pyrite, Materials.Sphalerite },
                Voltage.LV);
        addCombProductProcess(GTCombType.GALLIUM, new Material[] { Materials.Gallium, Materials.Niobium }, Voltage.LV);
        addCombProductProcess(GTCombType.ARSENIC, new Material[] { Materials.Realgar, Materials.Cassiterite, Materials.Zeolite },
                Voltage.LV);
        addCombProductProcess(
                GTCombType.IRON, new Material[] { Materials.Iron, Materials.Magnetite, Materials.BrownLimonite,
                        Materials.YellowLimonite, Materials.VanadiumMagnetite, Materials.BandedIron, Materials.Pyrite },
                Voltage.LV);
        addCombProductProcess(GTCombType.BAUXITE, new Material[] { Materials.Bauxite, Materials.Aluminium }, Voltage.LV);
        addCombProductProcess(GTCombType.ALUMINIUM, new Material[] { Materials.Aluminium, Materials.Bauxite }, Voltage.LV);
        addCombProductProcess(GTCombType.MANGANESE, new Material[] { Materials.Manganese, Materials.Grossular,
                Materials.Spessartine, Materials.Pyrolusite, Materials.Tantalite }, Voltage.LV);
        addCombProductProcess(GTCombType.TITANIUM,
                new Material[] { Materials.Titanium, Materials.Ilmenite, Materials.Bauxite, Materials.Rutile },
                Voltage.EV);
        addCombProductProcess(GTCombType.MAGNESIUM, new Material[] { Materials.Magnesium, Materials.Magnesite }, Voltage.LV);
        addCombProductProcess(GTCombType.CHROME, new Material[] { Materials.Chrome, Materials.Ruby, Materials.Chromite,
                Materials.Redstone, Materials.Neodymium, Materials.Bastnasite }, Voltage.HV);
        addCombProductProcess(GTCombType.TUNGSTEN,
                new Material[] { Materials.Tungsten, Materials.Tungstate, Materials.Scheelite, Materials.Lithium },
                Voltage.IV);
        addCombProductProcess(GTCombType.PLATINUM,
                new Material[] { Materials.Platinum, Materials.Cooperite, Materials.Palladium }, Voltage.HV);
        addCombProductProcess(GTCombType.MOLYBDENUM, new Material[] { Materials.Molybdenum, Materials.Molybdenite,
                Materials.Powellite, Materials.Wulfenite }, Voltage.LV);
        addCombProductProcess(GTCombType.LITHIUM,
                new Material[] { Materials.Lithium, Materials.Lepidolite, Materials.Spodumene }, Voltage.MV);
        addCombProductProcess(GTCombType.SALT, new Material[] { Materials.Salt, Materials.RockSalt, Materials.Saltpeter },
                Voltage.MV);
        addCombProductProcess(GTCombType.ELECTROTINE,
                new Material[] { Materials.Electrotine, Materials.Electrum, Materials.Redstone }, Voltage.MV);
        addCombProductProcess(GTCombType.ALMANDINE,
                new Material[] { Materials.Almandine, Materials.Pyrope, Materials.Sapphire, Materials.GreenSapphire },
                Voltage.LV);
        addCombProductProcess(GTCombType.URANIUM, new Material[] { Materials.Uranium238, Materials.Pitchblende,
                Materials.Uraninite, Materials.Uranium235 }, Voltage.EV);
        addCombProductProcess(GTCombType.PLUTONIUM, new Material[] { Materials.Plutonium239, Materials.Uranium235 }, Voltage.EV);
        addCombProductProcess(GTCombType.NAQUADAH,
                new Material[] { Materials.Naquadah, Materials.NaquadahEnriched, Materials.Naquadria }, Voltage.IV);
        addCombProductProcess(GTCombType.NAQUADRIA,
                new Material[] { Materials.Naquadria, Materials.NaquadahEnriched, Materials.Naquadah }, Voltage.LUV);
        addCombProductProcess(GTCombType.THORIUM, new Material[] { Materials.Thorium, Materials.Uranium238, Materials.Coal },
                Voltage.EV);
        addCombProductProcess(GTCombType.LUTETIUM, new Material[] { Materials.Lutetium, Materials.Thorium }, Voltage.IV);
        addCombProductProcess(GTCombType.AMERICIUM, new Material[] { Materials.Americium, Materials.Lutetium }, Voltage.LUV);
        addCombProductProcess(GTCombType.TRINIUM, new Material[] { Materials.Trinium, Materials.Naquadah, Materials.Naquadria },
                Voltage.ZPM);
        addCombProductProcess(GTCombType.INDIUM, new Material[] { Materials.Aluminium, Materials.Indium }, Voltage.ZPM);
        addCentrifugeToItemStack(GTCombType.INDIUM,new ItemStack[]{ OreDictUnifier.get(OrePrefix.dust,Materials.Aluminium),
                OreDictUnifier.get(OrePrefix.dust,Materials.Indium) },new int[]{4000,1500},Voltage.LUV);
        addCombProductProcess(DrtCombType.CRYOLITE, new Material[] { GTQTMaterials.Cryolite }, Voltage.MV);
        addExtractorProcess(DrtCombType.PRIMITIVE_STRAIN_A, GTQTMaterials.Enzymesa.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.PRIMITIVE_STRAIN_B, GTQTMaterials.Enzymesb.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.PRIMITIVE_STRAIN_C, GTQTMaterials.Enzymesc.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.PRIMITIVE_STRAIN_D, GTQTMaterials.Enzymesd.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.PRIMITIVE_STRAIN_E, GTQTMaterials.Enzymese.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.COMMON_MINE_STRAIN, GTQTMaterials.Enzymesaa.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.DIRECTED_PLATINUM_STRAIN, GTQTMaterials.Enzymesab.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.UNIVERSAL_DEMON_STRAIN, GTQTMaterials.Enzymesac.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.UNIVERSAL_BYPRODUCT_STRAINS, GTQTMaterials.Enzymesad.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.INDUSTRIAL_SYNTHETIC_STRAINS, GTQTMaterials.Enzymesba.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.INDUSTRIAL_REDUCTION_CULTURE, GTQTMaterials.Enzymesbb.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.INDUSTRIAL_OXIDIZING_BACTERIA, GTQTMaterials.Enzymesbc.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.INDUSTRIAL_CATALYTIC_STRAINS, GTQTMaterials.Enzymesbd.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.DIRECTED_LANTHANIDE_STRAINS, GTQTMaterials.Enzymesea.getFluid(500),Voltage.EV,60);
        addExtractorProcess(DrtCombType.FUEL, Materials.Diesel.getFluid(500),Voltage.ULV,32);
        addExtractorProcess(DrtCombType.HIGH_CETANE_DIESEL, Materials.CetaneBoostedDiesel.getFluid(500),Voltage.ULV,32);
        addExtractorProcess(DrtCombType.GASOLINE, Materials.Gasoline.getFluid(500),Voltage.ULV,32);
        addCentrifugeToItemStack(DrtCombType.ETHYLENE,new ItemStack[]{ForestryUtil.getDropStack(DrtDropType.ETHYLENE)},new int[]{7000},Voltage.LV);
        addCentrifugeToItemStack(DrtCombType.TETRAFLUOROETHYLENE,new ItemStack[]{ForestryUtil.getDropStack(DrtDropType.TETRAFLUOROETHYLENE),ForestryUtil.getDropStack(DrtDropType.ETHYLENE)},new int[]{3000,7000},Voltage.HV);
        addExtractorProcess(ForestryUtil.getDropStack(DrtDropType.ETHYLENE),Materials.Ethylene.getFluid(500),Voltage.LV,20);
        addExtractorProcess(ForestryUtil.getDropStack(DrtDropType.TETRAFLUOROETHYLENE),Materials.Tetrafluoroethylene.getFluid(500),Voltage.HV,20);
    }
    private static void addCombProductProcess(DrtCombType comb, Material[] material, Voltage volt)
    {
        Material rongye = volt.ordinal()<4?Materials.HydrofluoricAcid:Materials.PhthalicAcid;
        RecipeBuilder<?> builder = DrtechReceipes.COMBS_PRODUCT.recipeBuilder()
                .duration(200*(volt.ordinal()+1))
                .EUt(volt.getAutoclaveEnergy());
        for (var item:material)
        {
            if (OreDictUnifier.get(OrePrefix.crushedPurified, item, 4).isEmpty()) continue;
            builder.output(OrePrefix.crushedPurified, item, 4);
        }
        if(builder.getOutputs().size()>0)
        {
            builder.inputs(GTUtility.copy(4 * builder.getOutputs().size(), ForestryUtil.getCombStack(comb)))
                    .fluidInputs(rongye.getFluid(576 * builder.getOutputs().size()))
                    .buildAndRegister();
        }
    }
    private static void addCombProductProcess(GTCombType comb, Material[] material, Voltage volt)
    {
        Material rongye = volt.ordinal()<4?Materials.HydrofluoricAcid:Materials.PhthalicAcid;
        RecipeBuilder<?> builder = DrtechReceipes.COMBS_PRODUCT.recipeBuilder()
                .duration(200*(volt.ordinal()+1))
                .EUt(volt.getAutoclaveEnergy());
        for (var item:material)
        {
            if (OreDictUnifier.get(OrePrefix.crushedPurified, item, 4).isEmpty()) continue;
            builder.output(OrePrefix.crushedPurified, item, 4);
        }
        if(builder.getOutputs().size()>0)
        {
            builder.inputs(GTUtility.copy(2 * builder.getOutputs().size(), gregtech.integration.forestry.ForestryUtil.getCombStack(comb)))
                    .fluidInputs(rongye.getFluid(576 * builder.getOutputs().size()))
                    .buildAndRegister();
        }
    }
    private static void addChemicalProcess(DrtCombType comb, Material inMaterial, Material outMaterial, Voltage volt) {
        if (OreDictUnifier.get(OrePrefix.crushedPurified, outMaterial, 4).isEmpty() ||
                OreDictUnifier.get(OrePrefix.crushed, inMaterial).isEmpty() ||
                inMaterial.hasFlag(MaterialFlags.DISABLE_ORE_BLOCK))
            return;

        RecipeBuilder<?> builder = RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, ForestryUtil.getCombStack(comb)))
                .input(OrePrefix.crushed, inMaterial)
                .fluidInputs(volt.getFluid())
                .output(OrePrefix.crushedPurified, outMaterial, 4)
                .duration(volt.getChemicalTime())
                .EUt(volt.getChemicalEnergy());

        OreProperty property = inMaterial.getProperty(PropertyKey.ORE);
        if (property != null && !property.getOreByProducts().isEmpty()) {
            Material byproduct = property.getOreByProducts().get(0);
            if (byproduct != null && byproduct.hasProperty(PropertyKey.FLUID)) {
                if (!byproduct.hasProperty(PropertyKey.BLAST)) {
                    builder.fluidOutputs(byproduct.getFluid(GTValues.L));
                }
            }
        }

        if (volt.compareTo(Voltage.IV) > 0) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        builder.buildAndRegister();
    }

    /**
     * Currently only used separately for DrtCombType.MOLYBDENUM
     *
     * @param circuitNumber should not conflict with addProcessGT
     **/
    private static void addAutoclaveProcess(DrtCombType comb, Material material, Voltage volt, int circuitNumber) {
        if (OreDictUnifier.get(OrePrefix.crushedPurified, material, 4).isEmpty()) return;

        RecipeBuilder<?> builder = RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, ForestryUtil.getCombStack(comb)))
                .circuitMeta(circuitNumber)
                .fluidInputs(
                        Materials.Mutagen.getFluid((int) Math.max(1, material.getMass() + volt.getMutagenAmount())))
                .output(OrePrefix.crushedPurified, material, 4)
                .duration((int) (material.getMass() * 128))
                .EUt(volt.getAutoclaveEnergy());
        if (volt.compareTo(Voltage.HV) > 0) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        builder.buildAndRegister();
    }

    private static void addExtractorProcess(DrtCombType comb, FluidStack fluidStack, Voltage volt, int duration) {
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(ForestryUtil.getCombStack(comb))
                .fluidOutputs(fluidStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy() / 2)
                .buildAndRegister();
    }
    private static void addExtractorProcess(ItemStack comb, FluidStack fluidStack, Voltage volt, int duration) {
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(comb)
                .fluidOutputs(fluidStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy() / 2)
                .buildAndRegister();
    }
    private static void addProcessGT(DrtCombType comb, Material[] material, Voltage volt) {
        addCombProductProcess(comb,material,volt);
        for (int i = 0; i < material.length; i++) {
            addChemicalProcess(comb, material[i], material[i], volt);
            addAutoclaveProcess(comb, material[i], volt, i + 1);

        }
    }

    private static void addCentrifugeToMaterial(DrtCombType comb, Material[] material, int[] chance, int[] stackSize,
                                                Voltage volt, ItemStack beeWax, int waxChance) {
        addCentrifugeToMaterial(comb, material, chance, stackSize, volt, volt.getCentrifugeTime(), beeWax, waxChance);
    }

    private static void addCentrifugeToMaterial(DrtCombType comb, Material[] material, int[] chance, int[] stackSize,
                                                Voltage volt, int duration, ItemStack beeWax, int waxChance) {
        ItemStack[] output = new ItemStack[material.length + 1];
        stackSize = Arrays.copyOf(stackSize, material.length);
        chance = Arrays.copyOf(chance, output.length);
        chance[chance.length - 1] = waxChance;
        for (int i = 0; i < material.length; i++) {
            if (chance[i] == 0) continue;

            if (Math.max(1, stackSize[i]) % 9 == 0) {
                output[i] = OreDictUnifier.get(OrePrefix.dust, material[i], Math.max(1, stackSize[i]) / 9);
            } else if (Math.max(1, stackSize[i]) % 4 == 0) {
                output[i] = OreDictUnifier.get(OrePrefix.dustSmall, material[i], Math.max(1, stackSize[i]) / 4);
            } else {
                output[i] = OreDictUnifier.get(OrePrefix.dustTiny, material[i], Math.max(1, stackSize[i]));
            }
        }
        if (beeWax != ItemStack.EMPTY) {
            output[output.length - 1] = beeWax;
        } else {
            output[output.length - 1] = ModuleCore.getItems().beeswax.getItemStack();
        }

        addCentrifugeToItemStack(comb, output, chance, volt, duration);
    }

    /**
     * @param volt required Tier of system. If it's lower than MV, it will also add forestry centrifuge.
     * @param item must be less than or equal to 9.
     **/
    private static void addCentrifugeToItemStack(DrtCombType comb, ItemStack[] item, int[] chance, Voltage volt) {
        addCentrifugeToItemStack(comb, item, chance, volt, volt.getCentrifugeTime());
    }

    private static void addCentrifugeToItemStack(DrtCombType comb, ItemStack[] item, int[] chance, Voltage volt,
                                                 int duration) {
        ItemStack combStack = ForestryUtil.getCombStack(comb);

        // Start of the Forestry Map
        ImmutableMap.Builder<ItemStack, Float> product = new ImmutableMap.Builder<>();
        // Start of the GregTech Map
        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(combStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy());

        int numGTOutputs = 0;
        for (int i = 0; i < item.length; i++) {
            if (item[i] == null || item[i] == ItemStack.EMPTY) continue;
            // Add to Forestry Map
            product.put(item[i], chance[i] / 10000.0f);
            // Add to GregTech Map
            if (numGTOutputs < RecipeMaps.CENTRIFUGE_RECIPES.getMaxOutputs()) {
                if (chance[i] >= 10000) {
                    builder.outputs(item[i]);
                } else {
                    builder.chancedOutput(item[i], chance[i], 0);
                }
                numGTOutputs++;
            }
        }

        // Finalize Forestry Map
        if (volt.compareTo(Voltage.MV) < 0) {
            if (ModuleFactory.machineEnabled(MachineUIDs.CENTRIFUGE)) {
                RecipeManagers.centrifugeManager.addRecipe(duration, combStack, product.build());
            }
        }
        // Finalize GregTech Map
        builder.buildAndRegister();
    }
    private static void addCentrifugeToItemStack(GTCombType comb, ItemStack[] item, int[] chance, Voltage volt) {
        addCentrifugeToItemStack(comb, item, chance, volt, volt.getCentrifugeTime());
    }

    private static void addCentrifugeToItemStack(GTCombType comb, ItemStack[] item, int[] chance, Voltage volt,
                                                 int duration) {
        ItemStack combStack = gregtech.integration.forestry.ForestryUtil.getCombStack(comb);

        // Start of the Forestry Map
        ImmutableMap.Builder<ItemStack, Float> product = new ImmutableMap.Builder<>();
        // Start of the GregTech Map
        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(combStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy());

        int numGTOutputs = 0;
        for (int i = 0; i < item.length; i++) {
            if (item[i] == null || item[i] == ItemStack.EMPTY) continue;
            // Add to Forestry Map
            product.put(item[i], chance[i] / 10000.0f);
            // Add to GregTech Map
            if (numGTOutputs < RecipeMaps.CENTRIFUGE_RECIPES.getMaxOutputs()) {
                if (chance[i] >= 10000) {
                    builder.outputs(item[i]);
                } else {
                    builder.chancedOutput(item[i], chance[i], 0);
                }
                numGTOutputs++;
            }
        }

        // Finalize Forestry Map
        if (volt.compareTo(Voltage.MV) < 0) {
            if (ModuleFactory.machineEnabled(MachineUIDs.CENTRIFUGE)) {
                RecipeManagers.centrifugeManager.addRecipe(duration, combStack, product.build());
            }
        }
        // Finalize GregTech Map
        builder.buildAndRegister();
    }
    private enum Voltage {

        ULV,
        LV,
        MV,
        HV,
        EV,
        IV,
        LUV,
        ZPM,
        UV,
        UHV,
        UEV,
        UIV,
        UXV,
        OPV,
        MAX;

        public int getVoltage() {
            return (int) GTValues.V[ordinal()];
        }

        public int getChemicalEnergy() {
            return getVoltage() * 3 / 4;
        }

        public int getAutoclaveEnergy() {
            return (int) ((getVoltage() * 3 / 4) * Math.max(1, Math.pow(2, 5 - ordinal())));
        }

        public int getCentrifugeEnergy() {
            return this == Voltage.ULV ? 5 : (getVoltage() / 16) * 15;
        }

        public int getChemicalTime() {
            return 64 + ordinal() * 32;
        }

        public int getCentrifugeTime() {
            return 128 * (Math.max(1, ordinal()));
        }

        public FluidStack getFluid() {
            if (this.compareTo(Voltage.MV) < 0) {
                return Materials.Water.getFluid((this.compareTo(Voltage.ULV) > 0) ? 1000 : 500);
            } else if (this.compareTo(Voltage.HV) < 0) {
                return Materials.DistilledWater.getFluid(1000);
            } else if (this.compareTo(Voltage.EV) < 0) {
                return Materials.SulfuricAcid.getFluid(125);
            } else if (this.compareTo(Voltage.IV) < 0) {
                return Materials.HydrochloricAcid.getFluid(125);
            } else if (this.compareTo(Voltage.LUV) < 0) {
                return Materials.HydrofluoricAcid.getFluid((int) (Math.pow(2, this.compareTo(Voltage.HV)) * 125));
            } else {
                return Materials.FluoroantimonicAcid.getFluid((int) (Math.pow(2, this.compareTo(Voltage.LUV)) * 125));
            }
        }

        public int getMutagenAmount() {
            return 9 * ((this.compareTo(Voltage.MV) < 0) ? 10 : 10 * this.compareTo(Voltage.MV));
        }
    }
}
