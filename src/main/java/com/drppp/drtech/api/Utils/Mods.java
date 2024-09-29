package com.drppp.drtech.api.Utils;

import gregtech.api.util.ModIncompatibilityException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static keqing.gtqtcore.api.utils.GTQTUtil.getItemById;
import static keqing.gtqtcore.api.utils.GTQTUtil.getMetaItemById;

public enum Mods {

    Forestry("forestry"),
    Genetics("genetics");


    private final String ID;
    private final Function<Mods, Boolean> extraCheck;
    private Boolean modLoaded;

    /**
     * @param ID  Mod ID.
     */
    Mods(String ID) {
        this.ID =ID;
        this.extraCheck = null;
    }

    Mods(String ID, Function<Mods, Boolean> extraCheck) {
        this.ID = ID;
        this.extraCheck = extraCheck;
    }

    public String getID() {
        return this.ID;
    }

    @NotNull
    public ItemStack getItemByID(String name) {
        return getItemById(this.ID, name);
    }

    @NotNull
    public ItemStack getItemByID(String name, int amount) {
        return getItemById(this.ID, name, amount);
    }

    @NotNull
    public ItemStack getItemByID(String name, NBTTagCompound nbt) {
        return getItemById(this.ID, name, nbt);
    }

    @NotNull
    public ItemStack getItemByID(String name, int amount, NBTTagCompound nbt) {
        return getItemById(this.ID, name, amount, nbt);
    }

    @NotNull
    public ItemStack getMetaItemByID(String name, int meta) {
        return getMetaItemById(this.ID, name, meta);
    }

    @NotNull
    public ItemStack getMetaItemByID(String name, int meta, int amount) {
        return getMetaItemById(this.ID, name, meta, amount);
    }

    @NotNull
    public ItemStack getMetaItemByID(String name, int meta, NBTTagCompound nbt) {
        return getMetaItemById(this.ID, name, meta, nbt);
    }

    public ItemStack getMetaItemByID(String name, int meta, int amount, NBTTagCompound nbt) {
        return getMetaItemById(this.ID, name, meta, amount, nbt);
    }

    /**
     * Check if Mod with given {@link #ID} is loaded.
     *
     * @return  If Mod is loaded then return true, else return false.
     */
    public boolean isModLoaded() {
        //  Assign all Mod in this class a {@link modLoaded} parameter.
        if (this.modLoaded == null) {
            this.modLoaded = Loader.isModLoaded(this.ID);
            if (this.modLoaded) {
                //  If Mod has {@link extraCheck}, and it is not meeting the conditions,
                //  then return false (this is also a extended predicate condiction).
                if (this.extraCheck != null && !this.extraCheck.apply(this)) {
                    this.modLoaded = false;
                }
            }
        }
        return this.modLoaded;
    }

    /**
     * Used to throw an exception if this Mod is found to be loaded.
     *
     * <p>
     *     <strong>This must be called in or after {@link FMLPreInitializationEvent}!</strong>
     * </p>
     *
     * @param customMessages  Exception Messages.
     */
    public void throwIncompatibilityIfLoaded(String... customMessages) {
        if (this.isModLoaded()) {
            String modName = TextFormatting.BOLD + this.ID + TextFormatting.RESET;
            List<String> messages = new ArrayList<>();

            messages.add(modName + " mod detected, this mod is in compatible with GregTech Lite Core.");
            messages.addAll(Arrays.asList(customMessages));

            if (FMLLaunchHandler.side() == Side.SERVER) {
                throw new RuntimeException(String.join(",", messages));
            } else {
                throwClientIncompatibilityException(messages);
            }
        }
    }

    /**
     * Throw a Client-only Incompability Exception.
     *
     * @param messages  Exception message.
     *
     * @see ModIncompatibilityException
     */
    @SideOnly(Side.CLIENT)
    private static void throwClientIncompatibilityException(List<String> messages) {
        throw new ModIncompatibilityException(messages);
    }

    //  Helpers for the extra checker

    /**
     * Test if the mod version string contains the passed value.
     *
     * @param versionPart  Version Part.
     */
    private static Function<Mods, Boolean> versionContains(String versionPart) {
        return mod -> {
            //  If Mod ID is null, then return false.
            if (mod.ID == null)
                return false;
            //  If mod is not be loaded, then return false.
            if (!mod.isModLoaded())
                return false;
            //  Get Mod Container by Mod ID.
            ModContainer container = Loader.instance().getIndexedModList().get(mod.ID);
            //  If Mod Container is null, then return false.
            if (container == null)
                return false;
            //  Get Mod version by Mod Container.
            return container.getVersion().contains(versionPart);
        };
    }

    /**
     * Test if the mod version string does not contain the passed value.
     *
     * @param versionPart  Version Part.
     */
    private static Function<Mods, Boolean> versionExcludes(String versionPart) {
        return mod -> {
            //  If Mod ID is null, then return false.
            if (mod.ID == null)
                return false;
            //  If mod is not be loaded, then return false.
            if (!mod.isModLoaded())
                return false;
            //  Get Mod Container by Mod ID.
            ModContainer container = Loader.instance().getIndexedModList().get(mod.ID);
            //  If Mod Container is null, then return false.
            if (container == null)
                return false;
            //  Get Mod version by Mod Container.
            return !container.getVersion().contains(versionPart);
        };
    }
}
