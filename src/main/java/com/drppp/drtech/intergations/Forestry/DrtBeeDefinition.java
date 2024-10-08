package com.drppp.drtech.intergations.Forestry;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.MetaItems.ItemCombs;
import forestry.api.apiculture.*;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.IAllele;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.genetics.BeeDefinition;
import forestry.apiculture.genetics.IBeeDefinition;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.core.genetics.alleles.AlleleHelper;
import forestry.core.genetics.alleles.EnumAllele;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.integration.forestry.ForestryModule;
import gregtech.integration.forestry.bees.GTBeeDefinition;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static forestry.api.apiculture.EnumBeeChromosome.*;


public enum DrtBeeDefinition implements IBeeDefinition {

    // Organic
    BORAX(DRTBranchDefinition.DRT_METAL, "borax", true, 0xC8C8DA, 0x0000FF,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.30f);
                beeSpecies.addProduct(getDrtComb(DrtCombType.BORAX), 0.1f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HOT);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.GOURD);
            },
            dis -> dis.registerMutation(GTBeeDefinition.SALTY, GTBeeDefinition.LITHIUM, 10)
            ),
    WITHER(DRTBranchDefinition.DRT_METAL, "wither", true, 0x040102, 0x144F5B,
          beeSpecies -> {
        beeSpecies.addProduct(getForestryComb(EnumHoneyComb.SIMMERING), 0.50f);
        beeSpecies.addProduct(getDrtComb(DrtCombType.WITHER), 0.30f);
        beeSpecies.setHumidity(EnumHumidity.ARID);
        beeSpecies.setTemperature(EnumTemperature.HELLISH);
    },
    template -> {
        AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
        AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
        AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.NETHER);
    },
    dis -> {
        IBeeMutationBuilder mutation= dis.registerMutation(BeeDefinition.DEMONIC, GTBeeDefinition.ASH, 10);
        mutation.restrictBiomeType(BiomeDictionary.Type.NETHER);
    }
    ),
    BRIGHT(DRTBranchDefinition.DRT_RAREMETAL, "bright", true, 0x7A007A, 0xFFFFFF,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.SIMMERING), 0.50f);
                beeSpecies.addProduct(getDrtComb(DrtCombType.BRIGHT), 0.30f);
                beeSpecies.addProduct(getDrtComb(DrtCombType.WITHER), 0.10f);
                beeSpecies.setHumidity(EnumHumidity.ARID);
                beeSpecies.setTemperature(EnumTemperature.HELLISH);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.NETHER);
            },
            dis -> {
                IBeeMutationBuilder mutation = dis.registerMutation(DrtBeeDefinition.WITHER, GTBeeDefinition.ASH, 5);
                mutation.restrictTemperature(EnumTemperature.HOT);
                mutation.requireResource("blockTricalciumPhosphate");
            }
    ),
    MUTAGENIC_AGENT(DRTBranchDefinition.DRT_RAREMETAL, "mutagenic_agent", true, 0xa39f00, 0x1fff5b,
            beeSpecies -> {
                beeSpecies.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.30f);
                beeSpecies.addProduct(getDrtComb(DrtCombType.MUTAGENIC_AGENT), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.HOT);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.END);
            },
            dis -> {
                dis.registerMutation(GTBeeDefinition.PLUTONIUM, GTBeeDefinition.URANIUM, 5);
            }
    );
    private final DRTBranchDefinition branch;
    private final DRTAlleleBeeSpecies species;
    private final Consumer<DRTAlleleBeeSpecies> speciesProperties;
    private final Consumer<IAllele[]> alleles;
    private final Consumer<DrtBeeDefinition> mutations;
    private IAllele[] template;
    private IBeeGenome genome;
    private final Supplier<Boolean> generationCondition;

    DrtBeeDefinition(DRTBranchDefinition branch,
                    String binomial,
                    boolean dominant,
                    int primary,
                    int secondary,
                    Consumer<DRTAlleleBeeSpecies> speciesProperties,
                    Consumer<IAllele[]> alleles,
                    Consumer<DrtBeeDefinition> mutations) {
        this(branch, binomial, dominant, primary, secondary, speciesProperties, alleles, mutations, () -> true);
    }

    DrtBeeDefinition(DRTBranchDefinition branch,
                    String binomial,
                    boolean dominant,
                    int primary,
                    int secondary,
                    Consumer<DRTAlleleBeeSpecies> speciesProperties,
                    Consumer<IAllele[]> alleles,
                    Consumer<DrtBeeDefinition> mutations,
                    Supplier<Boolean> generationCondition) {
        this.alleles = alleles;
        this.mutations = mutations;
        this.speciesProperties = speciesProperties;
        String lowercaseName = this.toString().toLowerCase(Locale.ENGLISH);
        String species = WordUtils.capitalize(lowercaseName);

        String uid = "drtech.bee.species" + species;
        String description = "for.bees.description." + lowercaseName;
        String name = "for.bees.species." + lowercaseName;

        this.branch = branch;
        this.species = new DRTAlleleBeeSpecies(Tags.MODID, uid, name, "DrTech", description, dominant,
                branch.getBranch(), binomial, primary, secondary);
        this.generationCondition = generationCondition;
    }

    public static void initBees() {
        for (DrtBeeDefinition bee : values()) {
            bee.init();
        }
        for (DrtBeeDefinition bee : values()) {
            bee.registerMutations();
        }
    }

    private static ItemStack getForestryComb(EnumHoneyComb type) {
        return ModuleApiculture.getItems().beeComb.get(type, 1);
    }

    private static ItemStack getDrtComb(DrtCombType type) {
        return new ItemStack(ItemCombs.ITEM_COMBS, 1, type.ordinal());
    }

    private void setSpeciesProperties(DRTAlleleBeeSpecies beeSpecies) {
        this.speciesProperties.accept(beeSpecies);
    }

    private void setAlleles(IAllele[] template) {
        this.alleles.accept(template);
    }

    private void registerMutations() {
        if (generationCondition.get()) {
            this.mutations.accept(this);
        }
    }

    private void init() {
        if (generationCondition.get()) {
            setSpeciesProperties(species);

            template = branch.getTemplate();
            AlleleHelper.getInstance().set(template, SPECIES, species);
            setAlleles(template);

            // noinspection ConstantConditions
            genome = BeeManager.beeRoot.templateAsGenome(template);

            BeeManager.beeRoot.registerTemplate(template);
        }
    }

    private IBeeMutationBuilder registerMutation(IBeeDefinition parent1, IBeeDefinition parent2, int chance) {
        return registerMutation(parent1.getGenome().getPrimary(), parent2.getGenome().getPrimary(), chance);
    }

    private IBeeMutationBuilder registerMutation(IAlleleBeeSpecies parent1, IBeeDefinition parent2, int chance) {
        return registerMutation(parent1, parent2.getGenome().getPrimary(), chance);
    }

    private IBeeMutationBuilder registerMutation(IBeeDefinition parent1, IAlleleBeeSpecies parent2, int chance) {
        return registerMutation(parent1.getGenome().getPrimary(), parent2, chance);
    }

    private IBeeMutationBuilder registerMutation(IAlleleBeeSpecies parent1, IAlleleBeeSpecies parent2, int chance) {
        // noinspection ConstantConditions
        return BeeManager.beeMutationFactory.createMutation(parent1, parent2, getTemplate(), chance);
    }

    @Override
    public final IAllele @NotNull [] getTemplate() {
        return Arrays.copyOf(template, template.length);
    }

    @NotNull
    @Override
    public final IBeeGenome getGenome() {
        return genome;
    }

    @NotNull
    @Override
    public final IBee getIndividual() {
        return new Bee(genome);
    }

    @NotNull
    @Override
    public final ItemStack getMemberStack(@NotNull EnumBeeType beeType) {
        // noinspection ConstantConditions
        return BeeManager.beeRoot.getMemberStack(getIndividual(), beeType);
    }
}
