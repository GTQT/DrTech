package com.drppp.drtech.common.drone.item;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;

public final class ItemDroneProgramCard extends Item {

    public ItemDroneProgramCard() {
        setRegistryName(Tags.MODID, "drone_program_card");
        setTranslationKey(Tags.MODID + ".drone_program_card");
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
    }

    public void writeProgram(ItemStack stack, DroneProgramGraph graph) {
        DroneItemData.setProgram(stack, DroneProgramNbtCodec.write(graph));
    }

    public Optional<DroneProgramGraph> readProgram(ItemStack stack) throws DroneProgramFormatException {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(DroneItemData.PROGRAM_TAG, 10)) {
            return Optional.empty();
        }
        return Optional.of(DroneProgramNbtCodec.read(root.getCompoundTag(DroneItemData.PROGRAM_TAG)));
    }

    public boolean hasProgram(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(DroneItemData.PROGRAM_TAG, 10);
    }
}
