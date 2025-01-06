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
import gregtech.api.util.Mods;
import gregtech.integration.forestry.ForestryModule;
import gregtech.integration.forestry.bees.GTBeeDefinition;
import gregtech.integration.forestry.bees.GTCombType;
import gregtech.integration.forestry.mutation.MaterialMutationCondition;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static forestry.api.apiculture.EnumBeeChromosome.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.util.Mods.ExtraBees;


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
            dis -> {
                IBeeMutationBuilder mutation =  dis.registerMutation(GTBeeDefinition.SALTY, GTBeeDefinition.LITHIUM, 10);
                mutation.addMutationCondition(new MaterialMutationCondition(Borax));
            }
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
    ), PRIMITIVE_STRAIN_A(DRTBranchDefinition.DRT_NOBLEGAS, "primitive_strain_a", false, 0xB106B1, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.PRIMITIVE_STRAIN_A), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    PRIMITIVE_STRAIN_B(DRTBranchDefinition.DRT_NOBLEGAS, "primitive_strain_b", false, 0x9B2B2B, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.PRIMITIVE_STRAIN_B), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    PRIMITIVE_STRAIN_C(DRTBranchDefinition.DRT_NOBLEGAS, "primitive_strain_c", false, 0x85294C, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.PRIMITIVE_STRAIN_C), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    PRIMITIVE_STRAIN_D(DRTBranchDefinition.DRT_NOBLEGAS, "primitive_strain_d", false, 0x89B289, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.PRIMITIVE_STRAIN_D), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    PRIMITIVE_STRAIN_E(DRTBranchDefinition.DRT_NOBLEGAS, "primitive_strain_e", false, 0x8F8A54, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.PRIMITIVE_STRAIN_E), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    COMMON_MINE_STRAIN(DRTBranchDefinition.DRT_NOBLEGAS, "common_mine_strain", false, 0x04B5BD, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.COMMON_MINE_STRAIN), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    DIRECTED_PLATINUM_STRAIN(DRTBranchDefinition.DRT_NOBLEGAS, "directed_platinum_strain", false, 0x43BE7A, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.DIRECTED_PLATINUM_STRAIN), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    UNIVERSAL_DEMON_STRAIN(DRTBranchDefinition.DRT_NOBLEGAS, "universal_demon_strain", false, 0x546D20, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.UNIVERSAL_DEMON_STRAIN), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    UNIVERSAL_BYPRODUCT_STRAINS(DRTBranchDefinition.DRT_NOBLEGAS, "universal_byproduct_strains", false, 0x5AB007, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.UNIVERSAL_BYPRODUCT_STRAINS), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    INDUSTRIAL_SYNTHETIC_STRAINS(DRTBranchDefinition.DRT_NOBLEGAS, "industrial_synthetic_strains", false, 0x7A4127, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.INDUSTRIAL_SYNTHETIC_STRAINS), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    INDUSTRIAL_REDUCTION_CULTURE(DRTBranchDefinition.DRT_NOBLEGAS, "industrial_reduction_culture", false, 0x742A9A, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.INDUSTRIAL_REDUCTION_CULTURE), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    INDUSTRIAL_OXIDIZING_BACTERIA(DRTBranchDefinition.DRT_NOBLEGAS, "industrial_oxidizing_bacteria", false, 0x6E8E6E, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.INDUSTRIAL_OXIDIZING_BACTERIA), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    INDUSTRIAL_CATALYTIC_STRAINS(DRTBranchDefinition.DRT_NOBLEGAS, "industrial_catalytic_strains", false, 0x6B6060, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.INDUSTRIAL_CATALYTIC_STRAINS), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),

    DIRECTED_LANTHANIDE_STRAINS(DRTBranchDefinition.DRT_NOBLEGAS, "directed_lanthanide_strains", false, 0x41BA77, 0xffffff,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.DIRECTED_LANTHANIDE_STRAINS), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.WARM);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.BOTH_1);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.MUSHROOMS);
            },
            dis -> {

            }
    ),
    FUEL(DRTBranchDefinition.DRT_ORGANIC, "fuel", true,  0x9C6F40,0xDBA800,
            beeSpecies -> {
                beeSpecies.addProduct(getExtraBeesComb(3), 0.35f);
                beeSpecies.addSpecialty(getDrtComb(DrtCombType.FUEL), 0.25f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.VANILLA);
            },
            dis -> {
                dis.registerMutation(ForestryUtil.getSpecies(ExtraBees,"distilled"),GTBeeDefinition.OIL,8);
            }
    ),
    HIGH_CETANE_DIESEL(DRTBranchDefinition.DRT_ORGANIC, "high_cetane_diesel", false,  0x9C6F40,0xB5C806,
            beeSpecies -> {
                beeSpecies.addProduct(getExtraBeesComb(3), 0.35f);
                beeSpecies.addSpecialty(getDrtComb(DrtCombType.HIGH_CETANE_DIESEL), 0.15f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
                beeSpecies.setHasEffect();
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.VANILLA);
            },
            dis -> {
                dis.registerMutation(DrtBeeDefinition.FUEL,GTBeeDefinition.OIL,3);
            }
    ),
    GASOLINE(DRTBranchDefinition.DRT_ORGANIC, "gasoline", true,  0xBE4E07,0xBD7F06,
            beeSpecies -> {
                beeSpecies.addProduct(getExtraBeesComb(3), 0.35f);
                beeSpecies.addSpecialty(getDrtComb(DrtCombType.GASOLINE), 0.25f);
                beeSpecies.setHumidity(EnumHumidity.NORMAL);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.VANILLA);
            },
            dis -> {
                dis.registerMutation(ForestryUtil.getSpecies(ExtraBees,"distilled"),GTBeeDefinition.OIL,8);
            }
    ),ETHYLENE(DRTBranchDefinition.DRT_NOBLEGAS, "ethylene", true,  0x9AA4A5, 0x9AA4A5,
            beeSpecies -> {
                beeSpecies.addProduct(getDrtComb(DrtCombType.ETHYLENE), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.VANILLA);
            },
            dis -> {
                dis.registerMutation(GTBeeDefinition.HYDROGEN,GTBeeDefinition.OIL,12);
            }
    ),TETRAFLUOROETHYLENE(DRTBranchDefinition.DRT_NOBLEGAS, "tetrafluoroethylene", true,  0x585858, 0x585858,
            beeSpecies -> {
                beeSpecies.addSpecialty(getDrtComb(DrtCombType.TETRAFLUOROETHYLENE), 0.15f);
                beeSpecies.addProduct(getDrtComb(DrtCombType.ETHYLENE), 0.35f);
                beeSpecies.setHumidity(EnumHumidity.DAMP);
                beeSpecies.setTemperature(EnumTemperature.NORMAL);
            },
            template -> {
                AlleleHelper.getInstance().set(template, FLOWERING, EnumAllele.Flowering.AVERAGE);
                AlleleHelper.getInstance().set(template, HUMIDITY_TOLERANCE, EnumAllele.Tolerance.NONE);
                AlleleHelper.getInstance().set(template, FLOWER_PROVIDER, EnumAllele.Flowers.VANILLA);
            },
            dis -> {
                dis.registerMutation(DrtBeeDefinition.ETHYLENE,GTBeeDefinition.FLUORINE,12);
            }
    ) ,
    CRYOLITE(DRTBranchDefinition.DRT_GEM, "cryolite", false, 0x6ac6d4, 0xaedfe8,
              beeSpecies -> {
        beeSpecies.addProduct(getGTComb(GTCombType.STONE), 0.30f);
        beeSpecies.addProduct(getDrtComb(DrtCombType.CRYOLITE), 0.25f);
        beeSpecies.setHumidity(EnumHumidity.DAMP);
        beeSpecies.setTemperature(EnumTemperature.HOT);
    },
    template -> AlleleHelper.getInstance().set(template, SPEED, EnumAllele.Speed.SLOWER),
    dis -> {
        IBeeMutationBuilder mutation = dis.registerMutation(GTBeeDefinition.ALUMINIUM, GTBeeDefinition.SALTY, 10);
        //mutation.addMutationCondition(new MaterialMutationCondition( GTQTMaterials.Cryolite));
    });
    //边框    内部
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
    @Optional.Method(modid = Mods.Names.EXTRA_BEES)
    private static ItemStack getExtraBeesComb(int meta) {
        return ExtraBees.getItem("honey_comb", meta);
    }

    private static ItemStack getForestryComb(EnumHoneyComb type) {
        return ModuleApiculture.getItems().beeComb.get(type, 1);
    }

    private static ItemStack getDrtComb(DrtCombType type) {
        return new ItemStack(ItemCombs.ITEM_COMBS, 1, type.ordinal());
    }
    private static ItemStack getGTComb(GTCombType type) {
        return new ItemStack(ForestryModule.COMBS, 1, type.ordinal());
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
