package forestry.api.apiculture;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Forestry API stub — IArmorApiarist.
 * Implement this on armor items to protect the wearer from bee effects.
 *
 * This is a compile-time stub. At runtime, Forestry provides the real class.
 */
public interface IArmorApiarist {

    /**
     * Called by Forestry to check if the armor piece protects against a bee effect.
     *
     * @param entity    the entity wearing the armor
     * @param armor     the armor ItemStack
     * @param cause     the string identifier of the bee effect
     * @param doProtect if true, actually apply protection; if false, just query
     * @return true if the armor piece provides protection
     */
    boolean protectEntity(EntityLivingBase entity, ItemStack armor, String cause, boolean doProtect);
}
