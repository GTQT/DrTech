package com.drppp.drtech.Tile;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.ItemHandler.PailItemStackHandler;
import com.drppp.drtech.api.TileEntity.TileEntityWithUI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.Nullable;

public class TileEntityStoragePail extends TileEntityWithUI implements IGuiHolder<PosGuiData> {

    private static final int SLOT_COLUMNS = 9;
    private static final int SLOT_COUNT = 243;
    private static final int PANEL_WIDTH = 190;
    private static final int PANEL_HEIGHT = 228;

    public PailItemStackHandler inventory = new PailItemStackHandler(SLOT_COUNT);

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("StoragePail")) {
            inventory.deserializeNBT(compound.getCompoundTag("StoragePail"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("StoragePail", inventory.serializeNBT());
        return compound;
    }

    @Override
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Tags.MODID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.canInteractWithinDefaultRange(data);

        ModularPanel panel = ModularPanel.defaultPanel("storage_pail", PANEL_WIDTH, PANEL_HEIGHT);

        panel.child(new TextWidget<>(IKey.lang(getBlockType().getTranslationKey() + ".name"))
                .pos(12, 10)
                .width(PANEL_WIDTH - 24)
                .textAlign(com.cleanroommc.modularui.utils.Alignment.Center)
                .scale(1.05f));

        panel.child(new TextWidget<>(IKey.dynamic(() ->
                        "已用槽位: " + getUsedSlots() + "/" + inventory.getSlots()))
                .pos(12, 24)
                .width(PANEL_WIDTH - 24)
                .color(0xFF6B6B6B)
                .textAlign(com.cleanroommc.modularui.utils.Alignment.Center));

        panel.child(new Widget<>()
                .pos(13, 40)
                .size(164, 110));

        panel.child(new Grid()
                .mapTo(SLOT_COLUMNS, inventory.getSlots(), index -> new ItemSlot().slot(inventory, index))
                .scrollable(new VerticalScrollData())
                .pos(14, 41)
                .size(162, 108)
                .disableThemeBackground(true)
                .name("storage_slots"));

        panel.child(SlotGroupWidget.playerInventory(7, true)
                .disableSortButtons()
                .bottom(7)
                .leftRel(0.5f));

        return panel;
    }

    private int getUsedSlots() {
        int used = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                used++;
            }
        }
        return used;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
    }
}
