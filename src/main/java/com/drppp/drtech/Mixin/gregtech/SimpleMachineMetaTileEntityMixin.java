package com.drppp.drtech.Mixin.gregtech;

import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(SimpleMachineMetaTileEntity.class)
public abstract class SimpleMachineMetaTileEntityMixin extends WorkableTieredMetaTileEntity implements IActiveOutputSide, IGhostSlotConfigurable {

    private ItemStackHandler store = new ItemStackHandler(9);
    public SimpleMachineMetaTileEntityMixin(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
    }

    @Inject(method = "createGuiTemplate", at = @At("RETURN"), cancellable = true)
    public void onCreateGuiTemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> info) {
        ModularUI.Builder build =info.getReturnValue();
        for (int j = 0; j < store.getSlots(); j++) {
            build.slot(store,j,j%3*-18-18, 23+j/3*18,true,true, gregtech.api.gui.GuiTextures.SLOT);
        }
        //build.widget(new ClickButtonWidget(79,0,18,18,"Setting",this::onButtonClick));
        //build.widget(new MachineSceneWidget(0,0,180,180,this.getPos()));
    }

    private void onButtonClick(Widget.ClickData clickData) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("StoryInventory",store.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if(data.hasKey("StoryInventory"))
            store.deserializeNBT(data.getCompoundTag("StoryInventory"));
    }
}
