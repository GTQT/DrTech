package com.brachy84.mechtech.common.machines;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Function;

public class EnergySink extends MetaTileEntity implements IEnergyContainer {

    private long voltage = 0;
    private int amps = 1;

    private int setTier = 0;
    private boolean active = false;

    public EnergySink(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new EnergySink(metaTileEntityId);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder()
                .widget(new CycleButtonWidget(7, 7, 30, 20, GTValues.VNF, () -> setTier, tier -> {
                    setTier = tier;
                    voltage = GTValues.V[setTier];
                }));
        builder.label(7, 32, "Voltage");
        builder.widget(new ImageWidget(7, 44, 156, 20, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 50, 152, 16, () -> String.valueOf(voltage), value -> {
            if (!value.isEmpty()) {
                voltage = Long.parseLong(value);
                setTier = 0;
            }
        }).setAllowedChars(TextFieldWidget2.NATURAL_NUMS).setMaxLength(19).setValidator(getTextFieldValidator()));

        builder.label(7, 74, "Amperage");
        builder.widget(new ClickButtonWidget(7, 87, 20, 20, "-", data -> amps = amps-- == -1 ? 0 : amps--));
        builder.widget(new ImageWidget(29, 87, 118, 20, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(31, 93, 114, 16, () -> String.valueOf(amps), value -> {
            if (!value.isEmpty()) {
                amps = Integer.parseInt(value);
            }
        }).setMaxLength(10).setNumbersOnly(0, Integer.MAX_VALUE));
        builder.widget(new ClickButtonWidget(149, 87, 20, 20, "+", data -> amps++));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setLong("Voltage", voltage);
        data.setInteger("Amps", amps);
        data.setByte("Tier", (byte) setTier);
        data.setBoolean("Active", active);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        voltage = data.getLong("Voltage");
        amps = data.getInteger("Amps");
        setTier = data.getByte("Tier");
        active = data.getBoolean("Active");
        super.readFromNBT(data);
    }

    public Function<String, String> getTextFieldValidator() {
        return val -> {
            if (val.isEmpty()) {
                return "0";
            }
            long num;
            try {
                num = Long.parseLong(val);
            } catch (NumberFormatException ignored) {
                return "0";
            }
            if (num < 0) {
                return "0";
            }
            return val;
        };
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
        return l1;
    }

    @Override
    public boolean inputsEnergy(EnumFacing enumFacing) {
        return true;
    }

    @Override
    public long changeEnergy(long l) {
        return l;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getInputAmperage() {
        return amps;
    }

    @Override
    public long getInputVoltage() {
        return voltage;
    }
}
