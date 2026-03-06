package com.brachy84.mechtech.api.armor;

import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public interface IModule extends IItemBehaviour {

    @Nullable
    static IModule getOf(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem) {
            return getOf(((MetaItem<?>) stack.getItem()).getItem(stack));
        }
        return null;
    }

    @Nullable
    static IModule getOf(MetaItem<?>.MetaValueItem mvi) {
        for (IItemComponent component : mvi.getAllStats()) {
            if (component instanceof IModule)
                return (IModule) component;
        }
        return null;
    }

    /**
     * Returns how many modules of the module are in the handler.
     * Can be used in {@link #canPlaceIn(EntityEquipmentSlot, ItemStack, IItemHandler)} to set a maximum.
     *
     * @param module  to check for
     * @param handler to check in
     * @return module count
     */
    static int moduleCount(IModule module, IItemHandler handler) {
        int count = 0;
        ItemStack moduleItem = module.getMetaValueItem().getStackForm();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && moduleItem.getItem() == stack.getItem() && moduleItem.getMetadata() == stack.getMetadata())
                count += stack.getCount();
        }
        return count;
    }

    /**
     * Called when the module is placed in the workbench.
     *
     * @return how many of this modules can be in the same armor piece
     */
    default int maxModules() {
        return 1;
    }

    /**
     * Called when the module is placed in the workbench. If any if these modules is not already there, it can not be placed.
     *
     * @return required modules
     */
    default Collection<IModule> getRequiredModules() {
        return Collections.emptyList();
    }

    /**
     * Called when the module is placed in the workbench. If any if these modules is already there, it can not be placed.
     *
     * @return incompatible modules
     */
    default Collection<IModule> getIncompatibleModules() {
        return Collections.emptyList();
    }

    /**
     * Called each tick (serverside) when the armor piece with this module is worn
     *
     * @param world             current world
     * @param player            wearing player
     * @param modularArmorPiece armor piece
     * @param armorData         nbt data of armor piece
     */
    default void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
    }

    /**
     * Called each tick (clientside) when the armor piece with this module is worn
     *
     * @param world             current world
     * @param player            wearing player
     * @param modularArmorPiece armor piece
     * @param armorData         nbt data of armor piece
     */
    default void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
    }


    /**
     * Called when the worn modular armor piece is taken of
     *
     * @param world             current world
     * @param player            wearing player
     * @param modularArmorPiece armor piece
     * @param newStack          the replacing item
     */
    default void onUnequip(World world, EntityLivingBase player, ItemStack modularArmorPiece, ItemStack newStack) {
    }

    /**
     * Used to determin if the module can be placed inside that armor piece
     *
     * @param slot              slot of the armor piece
     * @param modularArmorPiece armor piece
     * @param modularSlots      item handler of the modular slots of the {@link com.brachy84.mechtech.common.machines.MetaTileEntityArmorWorkbench}
     * @return if the armor accepts this module
     */
    boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots);

    /**
     * Tooltip that will be added to the armor piece item (NOT THE MODULE)
     *
     * @param itemStack   armor piece
     * @param worldIn     current world
     * @param lines       tooltip
     * @param tooltipFlag flag
     */
    default void addTooltip(@Nonnull ItemStack itemStack, @Nullable World worldIn, @Nonnull List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
    }

    /**
     * Called when the module is saved to the armor piece
     *
     * @param nbt        armor data (write here)
     * @param moduleItem this module as item
     */
    default void writeExtraDataToArmor(NBTTagCompound nbt, ItemStack moduleItem) {
    }

    /**
     * Called when the ItemStack from this module is created. Write data here that was written in {@link #writeExtraDataToArmor(NBTTagCompound, ItemStack)}
     *
     * @param nbt data stored in the armor
     * @return data stored in the item
     */
    @Nullable
    default NBTTagCompound writeExtraDataToModuleItem(NBTTagCompound nbt) {
        return null;
    }

    /**
     * @return the meta item this module was added to
     */
    MetaItem<?>.MetaValueItem getMetaValueItem();

    @Deprecated
    default ItemStack getDestroyedStack() {
        return ItemStack.EMPTY;
    }

    /**
     * @return the name of this module. Will only show in the armor piece tooltip
     */
    @SideOnly(Side.CLIENT)
    default String getLocalizedName() {
        return I18n.format("mechtech.modules." + getModuleId() + ".name");
    }

    /**
     * @return a unique id
     */
    String getModuleId();

    /**
     * Adds lines to the HUD
     *
     * @param armorPiece armor piece item
     * @param armorData  data stored in armor (This is NOT the data from {@link #writeExtraDataToArmor(NBTTagCompound, ItemStack)}),
     *                   it is the same data as in {@link #onClientTick(World, EntityPlayer, ItemStack, NBTTagCompound))
     * @param hudStrings Strings to add to the HUD
     */
    @SideOnly(Side.CLIENT)
    default void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
    }

    /**
     * Adds tooltip lines to the item form
     *
     * @param itemStack this module as item
     * @param lines     tooltip
     */
    @Override
    default void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }
}
