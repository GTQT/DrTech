package com.drppp.drtech.intergations.Forestry;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeSpeciesBuilder;
import forestry.api.genetics.*;
import forestry.apiculture.genetics.alleles.AlleleBeeSpecies;
import forestry.core.genetics.alleles.AlleleFloat;
import gregtech.integration.IntegrationModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DRTAlleleBeeSpecies extends AlleleBeeSpecies {
    public static IAlleleFloat speedBlinding;

    public DRTAlleleBeeSpecies(String modId, String uid, String unlocalizedName, String authority, String unlocalizedDescription, boolean dominant, IClassification branch, String binomial, int primaryColor, int secondaryColor) {
        super(modId, uid, unlocalizedName, authority, unlocalizedDescription, dominant, branch, binomial, primaryColor, secondaryColor);
        AlleleManager.alleleRegistry.registerAllele(this, new IChromosomeType[]{EnumBeeChromosome.SPECIES});
    }

    public @NotNull IAlleleBeeSpeciesBuilder addProduct(@NotNull ItemStack product, @NotNull Float chance) {
        if (product == ItemStack.EMPTY) {
            IntegrationModule.logger.warn("GTAlleleBeeSpecies#addProduct() passed an empty ItemStack for allele {}! Setting default", this.getUID());
            product = new ItemStack(Items.BOAT);
        }

        if (chance <= 0.0F || chance > 1.0F) {
            IntegrationModule.logger.warn("GTAlleleBeeSpecies#addProduct() passed a chance value out of bounds for allele {}! Setting to 0.1", this.getUID());
            chance = 0.1F;
        }

        return super.addProduct(product, chance);
    }

    public @NotNull IAlleleBeeSpeciesBuilder addSpecialty(@NotNull ItemStack specialty, @NotNull Float chance) {
        if (specialty == ItemStack.EMPTY) {
            IntegrationModule.logger.warn("GTAlleleBeeSpecies#addProduct() passed an empty ItemStack for allele {}! Setting default", this.getUID());
            specialty = new ItemStack(Items.BOAT);
        }

        if (chance <= 0.0F || chance > 1.0F) {
            IntegrationModule.logger.warn("GTAlleleBeeSpecies#addSpecialty() passed a chance value out of bounds for allele {}! Setting to 0.1", this.getUID());
            chance = 0.1F;
        }

        return super.addSpecialty(specialty, chance);
    }

    public static void setupAlleles() {
        IAlleleFloat allele = (IAlleleFloat)AlleleManager.alleleRegistry.getAllele("magicbees.speedBlinding");
        if (allele == null) {
            allele = new AlleleFloat("drtech", "drtech.speedBlinding", "drtech.speedBlinding", 2.0F, false);
            AlleleManager.alleleRegistry.registerAllele((IAllele)allele, new IChromosomeType[]{EnumBeeChromosome.SPEED});
        }

        speedBlinding = (IAlleleFloat)allele;
    }
}
