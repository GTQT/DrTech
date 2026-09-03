package com.drppp.drtech.common.glider;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.wings.WingsFlightHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public final class ItemHangGlider extends Item {
    private final double horizontalSpeed;
    private final double verticalSpeed;
    private final double sneakHorizontalSpeed;
    private final double sneakVerticalSpeed;
    private final double windMultiplier;
    private final double airResistance;

    public ItemHangGlider(String name, boolean advanced) {
        setRegistryName(Tags.MODID, name);
        setTranslationKey(Tags.MODID + "." + name);
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
        horizontalSpeed = advanced ? 0.04D : 0.025D;
        verticalSpeed = 0.55D;
        sneakHorizontalSpeed = advanced ? 0.08D : 0.05D;
        sneakVerticalSpeed = 0.675D;
        windMultiplier = advanced ? 0.75D : 1.4D;
        airResistance = advanced ? 0.99D : 0.985D;
        setMaxDamage(advanced ? 2202 : 818);
        addPropertyOverride(new ResourceLocation("status"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                if (isBroken(stack)) {
                    return 2.0F;
                }
                if (entity instanceof EntityPlayer && GliderFlightHandler.isDeployed((EntityPlayer) entity)
                        && GliderFlightHandler.getGlider((EntityPlayer) entity) == stack) {
                    return 1.0F;
                }
                return 0.0F;
            }
        });
    }

    public boolean isBroken(ItemStack stack) {
        return stack.isItemDamaged() && stack.getItemDamage() >= stack.getMaxDamage() - 1;
    }

    public double getHorizontalSpeed(boolean sneaking) {
        return sneaking ? sneakHorizontalSpeed : horizontalSpeed;
    }

    public double getVerticalSpeed(boolean sneaking) {
        return sneaking ? sneakVerticalSpeed : verticalSpeed;
    }

    public double getWindMultiplier() {
        return windMultiplier;
    }

    public double getAirResistance() {
        return airResistance;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (hand != EnumHand.MAIN_HAND || isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!chest.isEmpty() && chest.getItem() instanceof ItemElytra) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        GliderFlightData data = GliderFlightCapability.get(player);
        if (data != null) {
            data.setDeployed(!data.isDeployed());
            if (!world.isRemote) {
                if (data.isDeployed()) {
                    WingsFlightHandler.setFlying(player, false);
                }
                GliderNetwork.sync(player);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean getIsRepairable(ItemStack stack, ItemStack ingredient) {
        for (ItemStack leather : OreDictionary.getOres("leather")) {
            if (ItemStack.areItemsEqual(leather, ingredient)) {
                return true;
            }
        }
        return false;
    }
}
