package com.drppp.drtech.common.glider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

final class GliderWind {
    private static final OpenSimplexNoise NOISE = new OpenSimplexNoise();

    private GliderWind() {
    }

    static void apply(EntityPlayer player, ItemStack stack) {
        ItemHangGlider glider = (ItemHangGlider) stack.getItem();
        double noise = NOISE.eval(player.posX / 19.0D, player.posZ / 19.0D);
        noise *= player.world.isRaining() ? 0.45D : 0.15D;
        double velocity = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        double wind = noise / (velocity * 0.4D + 1.0D);
        wind *= (player.posY < 256.0D ? player.posY / 256.0D : 1.0D) * 1.5D;
        if (stack.isItemDamaged()) {
            wind *= 1.0D + 0.7D * ((double) stack.getItemDamage() / stack.getMaxDamage());
        }
        player.rotationYaw += wind * glider.getWindMultiplier();
    }
}
