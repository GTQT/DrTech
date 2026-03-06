package com.brachy84.mechtech.common.machines;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.client.ClientHandler;
import com.brachy84.mechtech.client.gui.ErrorTextWidget;
import com.brachy84.mechtech.client.gui.SlotArmorWorkbench;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityArmorWorkbench extends MetaTileEntity {

    protected static final int[][] slotPositions = {
            {8, 8},
            {26, 8},
            {8, 26},
            {26, 26},
            {8, 44},
            {26, 44},
            {8, 62},
            {26, 62},
            {44, 8},
            {44, 26},
            {44, 44},
            {44, 62},
    };

    protected static final int[][] batterySlots = {
            {115, 60},
            {133, 60},
            {151, 60}
    };

    private static final String REQUIRED = "mechtech.modular_workbench.error1";
    private static final String INCOMPATIBLE = "mechtech.modular_workbench.error2";
    private static final String NOT_MODULE = "mechtech.modular_workbench.error3";
    private static final String MAX_MODULES = "mechtech.modular_workbench.error4";
    private static final String INVALID_SLOT = "mechtech.modular_workbench.error5";

    private ItemStack lastArmor = ItemStack.EMPTY;
    private CustomItemHandler mainSlot;
    private CustomItemHandler moduleSlotHandler;
    private CustomItemHandler batterySlotHandler;
    private Runnable mainSlotChanger = () -> {
    };
    private Runnable moduleSlotChanger = () -> {
    };

    public MetaTileEntityArmorWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity gtte) {
        return new MetaTileEntityArmorWorkbench(metaTileEntityId);
    }

    private void clearModuleSlots() {
        moduleSlotHandler.clear();
        batterySlotHandler.clear();
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        mainSlot = new CustomItemHandler(1) {

            @Override
            protected void onContentsChanged(int slot) {
                mainSlotChanger.run();
            }
        };
        moduleSlotHandler = new CustomItemHandler(slotPositions.length) {
            @Override
            protected void onContentsChanged(int slot) {
                moduleSlotChanger.run();
            }
        };
        batterySlotHandler = new CustomItemHandler(batterySlots.length) {
            @Override
            protected void onContentsChanged(int slot) {
                moduleSlotChanger.run();
            }
        };
        return super.createImportItemHandler();
    }

    private IGuiTexture getBackground(EntityPlayer player) {
        return player.getEntityWorld().isRemote ? ClientHandler.ARMOR_WORKBENCH_BACKGROUND : IGuiTexture.EMPTY;
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(getBackground(player), 176, 166);
        builder.bindPlayerInventory(player.inventory);
        ErrorTextWidget errorTextWidget = new ErrorTextWidget(136, 8).setCentered(true).setWidth(71);
        builder.widget(errorTextWidget);

        final List<SlotArmorWorkbench> slots = new ArrayList<>();
        final WidgetGroup slotsGroup = new WidgetGroup(Position.ORIGIN, new Size(176, 84));
        for (int i = 0; i < slotPositions.length; i++) {
            int[] pos = slotPositions[i];
            SlotArmorWorkbench slot = new SlotArmorWorkbench(moduleSlotHandler, i, pos[0], pos[1])
                    .setType(SlotArmorWorkbench.Type.MODULE)
                    .setFilter(stack -> {
                        ItemStack armor = this.mainSlot.getStackInSlot(0);
                        ModularArmor modularArmor = ModularArmor.get(armor);
                        return modularArmor != null && isValidModule(stack, armor, modularArmor, errorTextWidget);
                    });
            slot.setBackgroundTexture(GuiTextures.SLOT);
            slot.setActive(false);
            slot.setVisible(false);
            slotsGroup.addWidget(slot);
            slots.add(slot);
        }
        for (int i = 0; i < batterySlots.length; i++) {
            int[] pos = batterySlots[i];
            SlotArmorWorkbench slot;
            if (i == 0) {
                slot = new SlotArmorWorkbench(batterySlotHandler, i, pos[0], pos[1])
                        .setType(SlotArmorWorkbench.Type.BATTERY)
                        .setFilter(stack -> ModularArmor.get(stack) == null && (isValidBatteryItem(stack)));
                slot.setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY);
            } else {
                slot = new SlotArmorWorkbench(batterySlotHandler, i, pos[0], pos[1])
                        .setType(SlotArmorWorkbench.Type.BATTERY)
                        .setFilter(stack -> ModularArmor.get(stack) == null && (isValidTankItem(stack)));
                slot.setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CANISTER_OVERLAY);
            }
            slot.setActive(false);
            slot.setVisible(false);
            slotsGroup.addWidget(slot);
            slots.add(slot);
        }
        builder.image(-26, 0, 26, 88, ClientHandler.ARMOR_SLOTS_BACKGROUND);
        ArmorInventoryWrapper armorInventory = new ArmorInventoryWrapper(player);
        builder.slot(armorInventory, 0, -18, 62, GuiTextures.SLOT);
        builder.slot(armorInventory, 1, -18, 44, GuiTextures.SLOT);
        builder.slot(armorInventory, 2, -18, 26, GuiTextures.SLOT);
        builder.slot(armorInventory, 3, -18, 8, GuiTextures.SLOT);

        SlotWidget mainSlot = new SlotArmorWorkbench(this.mainSlot, 0, 79, 26)
                .setType(SlotArmorWorkbench.Type.ARMOR)
                .setFilter(stack -> ModularArmor.get(stack) != null)
                .setBackgroundTexture(GuiTextures.SLOT);
        builder.widget(mainSlot);
        builder.widget(slotsGroup);
        mainSlotChanger = () -> onMainSlotChanged(player, slots, errorTextWidget);
        moduleSlotChanger = () -> packModules(player, this.mainSlot.getStackInSlot(0));
        builder.bindOpenListener(() -> {
            ItemStack armor = this.mainSlot.getStackInSlot(0);
            if (!armor.isEmpty()) {
                unpackModules(armor, slots, errorTextWidget);
                this.lastArmor = armor;
            }
        });
        builder.bindCloseListener(() -> {
            mainSlotChanger = () -> {
            };
            moduleSlotChanger = () -> {
            };
        });
        return builder.build(getHolder(), player);
    }

    private List<IModule> getModules() {
        List<IModule> modules = new ArrayList<>();
        for (int i = 0; i < moduleSlotHandler.getSlots(); i++) {
            IModule module = IModule.getOf(moduleSlotHandler.getStackInSlot(i));
            if (module != null)
                modules.add(module);
        }
        return modules;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        int paintingColor = getPaintingColorForRendering();
        pipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(paintingColor)));
        Textures.CRAFTING_TABLE.renderOriented(renderState, translation, pipeline, getFrontFacing());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound nbt = super.writeToNBT(data);
        nbt.setTag("LastArmor", lastArmor.serializeNBT());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        lastArmor = new ItemStack(data.getCompoundTag("LastArmor"));
    }

    private void onMainSlotChanged(EntityPlayer player, List<SlotArmorWorkbench> slots, ErrorTextWidget errorTextWidget) {
        ItemStack stack = this.mainSlot.getStackInSlot(0);
        for (SlotArmorWorkbench widget : slots) {
            widget.setActive(false);
            widget.setVisible(false);
        }
        if (!stack.isEmpty()) {
            // armor piece is inserted to slot
            unpackModules(stack, slots, errorTextWidget);
            lastArmor = stack.copy();
            this.mainSlot.setSlotSilent(0, lastArmor);
        } else if (!lastArmor.isEmpty()) {
            // armor piece is taken out
            // packModules(lastArmor);
            clearModuleSlots();
            this.mainSlot.setSlotSilent(0, ItemStack.EMPTY);
            player.inventory.setItemStack(lastArmor.copy());
            lastArmor = ItemStack.EMPTY;
        }
    }

    private void unpackModules(EntityPlayer player, ItemStack stack, List<SlotArmorWorkbench> slots, ErrorTextWidget errorTextWidget) {
        unpackModules(stack, slots, errorTextWidget);
    }

    private void unpackModules(ItemStack stack, List<SlotArmorWorkbench> slots, ErrorTextWidget errorTextWidget) {
        ModularArmor modularArmor = ModularArmor.get(stack);
        if (modularArmor != null) {
            List<ItemStack> stacks = ModularArmor.getModuleStacksOf(stack);
            if (stacks.size() > modularArmor.getModuleSlots())
                throw new IllegalStateException("There were more modules than allowed");
            // unpack modules
            for (int i = 0; i < stacks.size(); i++) {
                moduleSlotHandler.setSlotSilent(i, stacks.get(i));
            }
            // unpack batteries
            stacks = ModularArmor.getBatteries(stack);
            for (int i = 0; i < stacks.size(); i++) {
                batterySlotHandler.setSlotSilent(i, stacks.get(i));
            }
            // update filter for module slots
            int count = 0;
            for (SlotArmorWorkbench widget : slots) {
                if (widget.getType() == SlotArmorWorkbench.Type.MODULE) {
                    if (count == modularArmor.getModuleSlots())
                        continue;
                    count++;
                }
                widget.setActive(true);
                widget.setVisible(true);
            }
        }
    }

    private void packModules(EntityPlayer player, ItemStack armor) {
        packModules(armor);
    }

    private void packModules(ItemStack armor) {
        // pack modules
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < moduleSlotHandler.getSlots(); i++) {
            ItemStack stack1 = moduleSlotHandler.getStackInSlot(i);
            if (!stack1.isEmpty()) {
                stacks.add(stack1);
            }
        }
        ModularArmor.writeModulesTo(stacks, armor);
        stacks.clear();
        // pack batteries
        for (int i = 0; i < batterySlotHandler.getSlots(); i++) {
            ItemStack stack1 = batterySlotHandler.getStackInSlot(i);
            if (!stack1.isEmpty()) {
                stacks.add(stack1);
            }
        }
        ModularArmor.setBatteries(armor, stacks);
        // remove items from all module and battery slots
        //clearModuleSlots();
    }

    private boolean isValidModule(ItemStack moduleStack, ItemStack armorStack, ModularArmor modularArmor, ErrorTextWidget errorTextWidget) {
        IModule module = IModule.getOf(moduleStack);
        if (module == null) {
            errorTextWidget.updateText(NOT_MODULE);
            return false;
        }
        if (!module.canPlaceIn(modularArmor.getSlot(), armorStack, moduleSlotHandler)) {
            errorTextWidget.updateText(INVALID_SLOT);
            return false;
        }
        if (!(module.maxModules() <= 0 || module.maxModules() > IModule.moduleCount(module, moduleSlotHandler))) {
            errorTextWidget.updateText(MAX_MODULES);
            return false;
        }
        List<IModule> modules = getModules();
        for (IModule module1 : modules) {
            if (module1.getIncompatibleModules().contains(module) || module.getIncompatibleModules().contains(module1)) {
                errorTextWidget.updateText(INCOMPATIBLE, module1.getLocalizedName());
                return false;
            }
        }
        for (IModule module1 : module.getRequiredModules()) {
            if (!modules.contains(module1)) {
                errorTextWidget.updateText(REQUIRED, module1.getLocalizedName());
                return false;
            }
        }
        return true;
    }

    private boolean isValidBatteryItem(ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            long totalCap = 0;
            for (int j = 0; j < batterySlotHandler.getSlots(); j++) {
                ItemStack stack1 = batterySlotHandler.getStackInSlot(j);
                if (stack1.isEmpty()) continue;
                IElectricItem electricItem1 = stack1.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem1 == null) continue;
                long max = electricItem1.getMaxCharge();
                if (Long.MAX_VALUE - totalCap > max || (totalCap += max) >= Long.MAX_VALUE)
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean isValidTankItem(ItemStack stack) {
        ModularArmor armor = ModularArmor.get(this.lastArmor);
        if (armor != null && armor.getMaxFluidSize() > 0) {
            IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null) {
                return getCapacity(fluidHandler) <= armor.getMaxFluidSize();
            }
        }
        return false;
    }

    private static int getCapacity(IFluidHandlerItem fluidHandlerItem) {
        int total = 0;
        for (IFluidTankProperties property : fluidHandlerItem.getTankProperties()) {
            total += property.getCapacity();
        }
        return total;
    }

    private static class ArmorInventoryWrapper implements IItemHandlerModifiable {

        private final EntityPlayer player;

        public ArmorInventoryWrapper(EntityPlayer player) {
            this.player = player;
        }

        @Override
        public int getSlots() {
            return 4;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return player.inventory.armorInventory.get(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            ItemStack currentStack = getStackInSlot(slot);
            EntityEquipmentSlot equipmentSlot = stack.getItem().getEquipmentSlot(stack);
            if (stack.isEmpty() || !currentStack.isEmpty() || equipmentSlot == null || equipmentSlot.getIndex() != slot)
                return stack;
            ItemStack copy = stack.copy();
            copy.setCount(1);
            if (!simulate)
                setStackInSlot(slot, copy);
            if (stack.getCount() == 1)
                return ItemStack.EMPTY;
            stack.shrink(1);
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack currentStack = getStackInSlot(slot);
            if (amount <= 0 || currentStack.isEmpty())
                return ItemStack.EMPTY;
            if (!simulate)
                setStackInSlot(slot, ItemStack.EMPTY);
            return currentStack;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            player.inventory.armorInventory.set(slot, stack);
        }
    }
}
