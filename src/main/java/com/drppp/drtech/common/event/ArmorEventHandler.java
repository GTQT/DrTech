package com.drppp.drtech.common.event;

import com.drppp.drtech.Tags;
import com.drppp.drtech.api.armor.IModule;
import com.drppp.drtech.api.armor.ModularArmor;
import com.drppp.drtech.api.armor.Modules;
import com.drppp.drtech.api.armor.modules.OxygenMask;
import com.drppp.drtech.common.loaders.recipes.Recipes;
import micdoodle8.mods.galacticraft.api.event.oxygen.GCCoreOxygenSuffocationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * 模块化装甲的通用事件订阅（由原 mechtech CommonProxy 收编）。
 */
@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ArmorEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        Recipes.init();
    }

    @SubscribeEvent
    public static void onEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        if (event.getFrom().isEmpty())
            return;
        ModularArmor modularArmor = ModularArmor.get(event.getFrom());
        if (modularArmor != null) {
            modularArmor.onUnequip(event.getEntity().world, event.getEntityLiving(), event.getFrom(), event.getTo());
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            for (int i = 0; i < 4; i++) {
                ItemStack stack = player.inventory.armorInventory.get(i);
                ModularArmor modularArmor = ModularArmor.get(stack);
                if (modularArmor != null) {
                    List<IModule> modules = ModularArmor.getModulesOf(stack);
                    for (IModule module : modules) {
                        if (module == Modules.SHOCK_ABSORBER) {
                            event.setStrength(event.getStrength() * 0.2f);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Optional.Method(modid = "galacticraftcore")
    @SubscribeEvent
    public static void GCOxygen(GCCoreOxygenSuffocationEvent.Pre event) {
        if (event.getEntity() instanceof EntityPlayer player) {
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (stack != null && !stack.isEmpty()) {
                ModularArmor modularArmor = ModularArmor.get(stack);
                if (modularArmor != null) {
                    for (IModule module : ModularArmor.getModulesOf(stack)) {
                        if (module instanceof OxygenMask mask) {
                            if (mask.drainOxygen(player, 20) == 20) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
