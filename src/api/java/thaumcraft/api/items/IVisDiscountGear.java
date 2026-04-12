package thaumcraft.api.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Thaumcraft 6 API stub — IVisDiscountGear.
 * Implement this on armor items to provide a vis cost discount.
 *
 * This is a compile-time stub. At runtime, Thaumcraft provides the real class.
 */
public interface IVisDiscountGear {

    /**
     * Called by Thaumcraft to query the vis discount provided by this armor piece.
     *
     * @param stack  the armor ItemStack
     * @param player the player wearing the armor
     * @return the discount percentage (e.g. 5 for 5% discount)
     */
    int getVisDiscount(ItemStack stack, EntityPlayer player);
}
