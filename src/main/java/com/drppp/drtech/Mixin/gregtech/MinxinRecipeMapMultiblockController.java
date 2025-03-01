package com.drppp.drtech.Mixin.gregtech;

import com.drppp.drtech.api.Muti.DrtMultiblockAbility;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeMapMultiblockController.class)
public abstract class MinxinRecipeMapMultiblockController extends MultiblockWithDisplayBase implements IDataInfoProvider, ICleanroomReceiver, IDistinctBusController {
    @Shadow
    protected IItemHandlerModifiable inputInventory;
    @Shadow
    protected IItemHandlerModifiable outputInventory;
    @Shadow
    protected IMultipleTankHandler inputFluidInventory;
    @Shadow
    protected IMultipleTankHandler outputFluidInventory;
    @Shadow
    public  RecipeMap<?> recipeMap;

    public MinxinRecipeMapMultiblockController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "initializeAbilities", at = @At("TAIL"), cancellable = true)
    protected void initializeAbilities(CallbackInfo ci) {
        var im_item = this.getAbilities(MultiblockAbility.IMPORT_ITEMS);
        var im_itemCC = this.getAbilities(DrtMultiblockAbility.IMPORT_ITEM_FLUID);
        List<IItemHandlerModifiable> itemlist = new ArrayList<>();
        itemlist.addAll(im_item);
        itemlist.addAll(im_itemCC);
        this.inputInventory = new ItemHandlerList(itemlist);
        var im_fluid = this.getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        List<IFluidTank> tanks = new ArrayList<>();
        tanks.addAll(im_fluid);
        if(!im_itemCC.isEmpty())
        {
            for (int i = 0; i < im_itemCC.size(); i++) {
                tanks.addAll(im_itemCC.get(i).getFluidTanks());
            }
        }
        this.inputFluidInventory = new FluidTankList(true, tanks);

        var ex_item = this.getAbilities(MultiblockAbility.EXPORT_ITEMS);
        var ex_itemCC = this.getAbilities(DrtMultiblockAbility.EXPORT_ITEM_FLUID);
        List<IItemHandlerModifiable> ex_itemlist = new ArrayList<>();
        ex_itemlist.addAll(ex_item);
        ex_itemlist.addAll(ex_itemCC);
        this.outputInventory = new ItemHandlerList(ex_itemlist);
        var ex_fluid = this.getAbilities(MultiblockAbility.EXPORT_FLUIDS);
        List<IFluidTank> extanks = new ArrayList<>();
        extanks.addAll(ex_fluid);
        if(!ex_itemCC.isEmpty())
        {
            for (int i = 0; i < ex_itemCC.size(); i++) {
                extanks.addAll(ex_itemCC.get(i).getFluidTanks());
            }
        }
        this.outputFluidInventory = new FluidTankList(true, extanks);
    }
    @Overwrite
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler);
        if (checkEnergyIn) {
            predicate = predicate.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1));
        }

        if (checkItemIn && this.recipeMap.getMaxInputs() > 0) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
            predicate = predicate.or(abilities(DrtMultiblockAbility.IMPORT_ITEM_FLUID).setPreviewCount(1));
        }

        if (checkItemOut && this.recipeMap.getMaxOutputs() > 0) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
            predicate = predicate.or(abilities(DrtMultiblockAbility.EXPORT_ITEM_FLUID).setPreviewCount(1));
        }

        if (checkFluidIn && this.recipeMap.getMaxFluidInputs() > 0) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
            predicate = predicate.or(abilities(DrtMultiblockAbility.IMPORT_ITEM_FLUID).setPreviewCount(1));
        }

        if (checkFluidOut && this.recipeMap.getMaxFluidOutputs() > 0) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
            predicate = predicate.or(abilities(DrtMultiblockAbility.EXPORT_ITEM_FLUID).setPreviewCount(1));
        }

        return predicate;
    }
}
