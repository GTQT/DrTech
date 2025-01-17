package com.drppp.drtech.common.enent;

import com.drppp.drtech.api.Utils.DamageSources;
import com.drppp.drtech.common.Items.armor.DrtechArmorItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.*;

import static com.drppp.drtech.DrtConfig.EnvironmentSwitch.EnableAirPollution;
import static net.minecraft.inventory.EntityEquipmentSlot.*;

public final class DimensionBreathabilityHandler {

    private static final Map<Integer, BreathabilityInfo> dimensionBreathabilityMap = new HashMap<>();

    public static final List<Integer> NETHER_TYPE_ID= Arrays.asList(-1,1);
    public static final List<Integer> BENEATH_TYPE_ID= Arrays.asList(10,51,52,53,54);


    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {
        if(!EnableAirPollution)return;

        dimensionBreathabilityMap.clear();

        for(int id:NETHER_TYPE_ID)
        {
            dimensionBreathabilityMap.put(id, new BreathabilityInfo(DamageSources.getSuffocationDamage(), 2));
        }
        for(int id:BENEATH_TYPE_ID)
        {
            dimensionBreathabilityMap.put(id, new BreathabilityInfo(DamageSources.getSuffocationDamage(), 0.5));
        }

    }


    public static boolean tickAir(EntityPlayer player, FluidStack oxyStack) {
        if (player.isCreative()) return true;
        Optional<IFluidHandlerItem> tank = player.inventory.mainInventory.stream()
                .map(a -> a.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                .filter(Objects::nonNull)
                .filter(a -> {
                    FluidStack drain = a.drain(oxyStack, false);
                    return drain != null && drain.amount > 0;
                }).findFirst();
        return tank.isPresent();
    }

    public static boolean isInHazardousEnvironment(EntityPlayer player) {
        return dimensionBreathabilityMap.containsKey(player.dimension);
    }

    public static void tickPlayer(EntityPlayer player) {
        if (isInHazardousEnvironment(player)) {
            if (player.getItemStackFromSlot(HEAD).getItem() instanceof DrtechArmorItem item) {
                if (item.isValid(player.getItemStackFromSlot(HEAD), player)) {
                    double damageAbsorbed = item.tryTick(player.getItemStackFromSlot(HEAD), player);
                    if (damageAbsorbed > 0) {
                        dimensionBreathabilityMap.get(player.dimension).damagePlayer(player, damageAbsorbed);
                    }
                    return;
                }
            }
            dimensionBreathabilityMap.get(player.dimension).damagePlayer(player);
        }
    }


    public static final class BreathabilityInfo {
        public DamageSource damageType;
        public double defaultDamage;

        public BreathabilityInfo(DamageSource damageType, double defaultDamage) {
            this.damageType = damageType;
            this.defaultDamage = defaultDamage;
        }

        public void damagePlayer(EntityPlayer player) {
            player.attackEntityFrom(damageType, (float) defaultDamage);
        }
        public void damagePlayer(EntityPlayer player, double amount) {
            if (defaultDamage > amount) {
                player.attackEntityFrom(damageType, (float) defaultDamage - (float) amount);
            }
        }

    }
}