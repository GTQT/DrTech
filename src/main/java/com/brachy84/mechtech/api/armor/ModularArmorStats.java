package com.brachy84.mechtech.api.armor;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ModularArmorStats implements IItemMaxStackSizeProvider, IItemCapabilityProvider {

    public static final IFluidTankProperties DEFAULT_TANK = new FluidTankProperties(null, 0);

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return getCapability(capability, facing) != null;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM) {
                    return GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM.cast(createElectricItem(itemStack));
                }
                if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                    return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(createFluidHandler(itemStack));
                }
                return null;
            }
        };
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int i) {
        return 1;
    }

    private static IElectricItem createElectricItem(ItemStack stack) {
        return new IElectricItem() {
            private List<BiConsumer<ItemStack, Long>> listeners = new ArrayList<>();

            @Override
            public boolean canProvideChargeExternally() {
                return false;
            }

            @Override
            public boolean chargeable() {
                return true;
            }

            private void triggerListeners(long charge) {
                listeners.forEach(listener -> listener.accept(stack, charge));
            }

            @Override
            public void addChargeListener(BiConsumer<ItemStack, Long> biConsumer) {
                listeners.add(biConsumer);
            }

            @Override
            public long charge(long amount, int tier, boolean ignoreLimit, boolean simulate) {
                if (amount == 0 || tier < getTier())
                    return 0;
                long filled = ModularArmor.fill(stack, amount, tier, simulate);
                if (!simulate && filled > 0)
                    triggerListeners(getCharge());
                return filled;
            }

            @Override
            public long discharge(long amount, int tier, boolean ignoreLimit, boolean externally, boolean simulate) {
                if (amount == 0 || externally || tier < getTier())
                    return 0;
                long drained = ModularArmor.drain(stack, amount, tier, simulate);
                if (!simulate && drained > 0)
                    triggerListeners(getCharge());
                return drained;
            }

            /**
             * Transfer limit is defined be contained batteries
             */
            @Override
            public long getTransferLimit() {
                return ModularArmor.getTransferLimit(stack);
            }

            @Override
            public long getMaxCharge() {
                return ModularArmor.getCapacity(stack);
            }

            @Override
            public long getCharge() {
                return ModularArmor.getEnergy(stack);
            }

            @Override
            public int getTier() {
                return ModularArmor.getTier(stack);
            }
        };
    }

    private static IFluidHandlerItem createFluidHandler(ItemStack stack) {
        return new IFluidHandlerItem() {

            @Nonnull
            @Override
            public ItemStack getContainer() {
                return stack;
            }

            @Override
            public IFluidTankProperties[] getTankProperties() {
                IFluidTankProperties[] properties = new IFluidTankProperties[0];
                for (IFluidHandlerItem fluidHandler : ModularArmor.getFluidHandlers(stack)) {
                    properties = ArrayUtils.addAll(properties, fluidHandler.getTankProperties());
                }
                if (properties.length == 0) {
                    // JEI doesn't like items with 0 tanks
                    properties = ArrayUtils.add(properties, DEFAULT_TANK);
                }
                return properties;
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if (resource == null || resource.amount <= 0) {
                    return 0;
                }
                FluidStack toFill = resource.copy();
                toFill.amount = ModularArmor.fillFluid(stack, toFill, !doFill);
                return toFill.amount;
            }

            @Nullable
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                if (resource == null || resource.amount <= 0) {
                    return null;
                }
                FluidStack toDrain = resource.copy();
                toDrain.amount = ModularArmor.drainFluid(stack, toDrain, !doDrain);
                return toDrain;
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                return null;
            }
        };
    }
}
