package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.api.capability.impl.BatteryEnergyContainerHandler;
import com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityBatteryEnergyHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {
    protected final boolean isExportHatch;
    protected final int amperage;
    protected final IEnergyContainer energyContainer;

    public MetaTileEntityBatteryEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.itemInventory = this.createImportItemHandler();
        this.isExportHatch = isExportHatch;
        this.amperage = amperage;
        if (isExportHatch) {
            this.energyContainer = BatteryEnergyContainerHandler.emitterContainer(this, GTValues.V[tier] * 64L * (long) amperage, GTValues.V[tier], amperage);
            ((BatteryEnergyContainerHandler) this.energyContainer).setSideOutputCondition((s) -> {
                return s == this.getFrontFacing();
            });
        } else {
            this.energyContainer = BatteryEnergyContainerHandler.receiverContainer(this, GTValues.V[tier] * 16L * (long) amperage, GTValues.V[tier], amperage);
        }

    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBatteryEnergyHatch(this.metaTileEntityId, this.getTier(), this.amperage, this.isExportHatch);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            this.getOverlay().renderSided(this.getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[this.getTier()]));
        }

    }

    public void update() {
        super.update();
        this.checkWeatherOrTerrainExplosion((float) this.getTier(), this.getTier() * 10, this.energyContainer);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((ItemStackHandler) itemInventory).deserializeNBT(data.getCompoundTag("selfItemInventory"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("selfItemInventory", ((ItemStackHandler) itemInventory).serializeNBT());
        return data;
    }

    private @NotNull SimpleOverlayRenderer getOverlay() {
        if (this.isExportHatch) {
            if (this.amperage <= 2) {
                return Textures.ENERGY_OUT_MULTI;
            } else if (this.amperage <= 4) {
                return Textures.ENERGY_OUT_HI;
            } else {
                return this.amperage <= 16 ? Textures.ENERGY_OUT_ULTRA : Textures.ENERGY_OUT_MAX;
            }
        } else if (this.amperage <= 2) {
            return Textures.ENERGY_IN_MULTI;
        } else if (this.amperage <= 4) {
            return Textures.ENERGY_IN_HI;
        } else {
            return this.amperage <= 16 ? Textures.ENERGY_IN_ULTRA : Textures.ENERGY_IN_MAX;
        }
    }

    public MultiblockAbility<IEnergyContainer> getAbility() {
        return this.isExportHatch ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {

    }

    public void registerAbilities(List<IEnergyContainer> abilityList) {
        abilityList.add(this.energyContainer);
    }

    protected boolean openGUIOnRightClick() {
        return true;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = 2;
        int colSize = 2;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                        18 + 18 * colSize + 94)
                .label(6, 6, getMetaFullName());

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                builder.widget(new SlotWidget(itemInventory, index++, 88 - rowSize * 9 + x * 18, 18 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * colSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote) {
            if (itemInventory.getSlots() > 0) {
                for (int i = 0; i < itemInventory.getSlots(); i++) {
                    var pos = getPos();
                    if (!itemInventory.getStackInSlot(i).isEmpty()) {
                        getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemInventory.getStackInSlot(i)));
                        itemInventory.extractItem(i, 1, false);
                    }

                }
            }
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);

    }

    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VNF[this.getTier()];
        this.addDescriptorTooltip(stack, world, tooltip, advanced);
        if (this.isExportHatch) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", this.energyContainer.getOutputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", this.energyContainer.getOutputAmperage()));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", this.energyContainer.getInputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", this.energyContainer.getInputAmperage()));
        }

        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", this.energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    protected void addDescriptorTooltip(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        if (this.isExportHatch) {
            if (this.amperage > 2) {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.output_hi_amp.tooltip"));
            } else {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.output.tooltip"));
            }
        } else if (this.amperage > 2) {
            tooltip.add(I18n.format("gregtech.machine.energy_hatch.input_hi_amp.tooltip"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.energy_hatch.input.tooltip"));
        }

    }

    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    public boolean canRenderFrontFaceX() {
        return this.isExportHatch;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (this == DrTechMetaTileEntities.BATTERY_INPUT_ENERGY_HATCH[0]) {
            for (MetaTileEntityBatteryEnergyHatch hatch : DrTechMetaTileEntities.BATTERY_INPUT_ENERGY_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityBatteryEnergyHatch hatch : DrTechMetaTileEntities.BATTERY_INPUT_ENERGY_HATCH_4A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityBatteryEnergyHatch hatch : DrTechMetaTileEntities.BATTERY_INPUT_ENERGY_HATCH_16A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityBatteryEnergyHatch hatch : DrTechMetaTileEntities.BATTERY_INPUT_ENERGY_HATCH_64A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
        } else if (this.getClass() != MetaTileEntityBatteryEnergyHatch.class) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    public void doExplosion(float explosionPower) {
        if (this.getController() != null) {
            this.getController().explodeMultiblock(explosionPower);
        } else {
            super.doExplosion(explosionPower);
        }

    }

    protected ItemStackHandler createImportItemHandler() {
        return new ItemStackHandler(4) {

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if ((electricItem != null && getTier() >= electricItem.getTier()) ||
                        (ConfigHolder.compat.energy.nativeEUToFE &&
                                stack.hasCapability(CapabilityEnergy.ENERGY, null))) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }


}
