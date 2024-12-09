package com.drppp.drtech.Mixin.gregtech;

import gregtech.api.capability.IDistinctBusController;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

import static com.drppp.drtech.DrtConfig.EnableMutiProcessOutput;

@Mixin(RecipeMapMultiblockController.class)
public abstract class RecipeMapMultiblockControllerMixin extends MultiblockWithDisplayBase implements IDataInfoProvider, ICleanroomReceiver, IDistinctBusController {
    @Shadow
    protected MultiblockRecipeLogic recipeMapWorkable;

    @Shadow
    private ICleanroomProvider cleanroom;
    public RecipeMapMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "addDisplayText", at = @At("TAIL")) // 在方法结束时插入代码
    private void onAddDisplayText(List<ITextComponent> textList, CallbackInfo ci) {
        if(super.isStructureFormed() && EnableMutiProcessOutput)
        {
            textList.add(new TextComponentString("超净环境:"+cleanroom.isClean()));
            if (super.isActive() && recipeMapWorkable.isActive()) {
                textList.add(new TextComponentString("预计产物:"));

                // 通过反射获取 fluidOutputs 和 itemOutputs
                try {
                    Field fluidOutputsField = AbstractRecipeLogic.class.getDeclaredField("fluidOutputs");
                    fluidOutputsField.setAccessible(true);
                    List<FluidStack> fluidOutputs = (List<FluidStack>) fluidOutputsField.get(recipeMapWorkable);
                    Field itemOutputsField = AbstractRecipeLogic.class.getDeclaredField("itemOutputs");
                    itemOutputsField.setAccessible(true);
                    NonNullList<ItemStack> itemOutputs = (NonNullList<ItemStack>) itemOutputsField.get(recipeMapWorkable);
                    // 处理 fluidOutputs
                    if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                        for (FluidStack fluidStack : fluidOutputs) {
                            textList.add(new TextComponentString(fluidStack.getLocalizedName()+ " x" + fluidStack.amount+"l"));
                        }
                    }

                    // 处理 itemOutputs
                    if (itemOutputs != null && !itemOutputs.isEmpty()) {
                        for (ItemStack itemStack : itemOutputs) {
                            if (!itemStack.isEmpty()) {
                                textList.add(new TextComponentString(itemStack.getDisplayName() + " x" + itemStack.getCount()));
                            }
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}