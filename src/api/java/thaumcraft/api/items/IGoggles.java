package thaumcraft.api.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Thaumcraft 6 API stub — IGoggles.
 * Implement this on helmet items to grant Goggles of Revealing functionality.
 * Controls both aura node visibility and vis HUD popup display.
 *
 * This is a compile-time stub. At runtime, Thaumcraft provides the real class.
 */
public interface IGoggles {

    /**
     * Called by Thaumcraft to check if the helmet should show vis/aura popups.
     *
     * @param stack  the helmet ItemStack
     * @param player the entity wearing the helmet
     * @return true to show aura node popups and vis information
     */
    boolean showIngamePopups(ItemStack stack, EntityLivingBase player);
}
