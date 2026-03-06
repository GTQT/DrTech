package com.brachy84.mechtech.client;

import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.api.armor.modules.Binoculars;
import com.brachy84.mechtech.common.MTConfig;
import com.brachy84.mechtech.common.items.MTMetaItems;
import gregtech.api.items.armor.ArmorUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = MechTech.MODID, value = Side.CLIENT)
public class ClientEventHandler {

    private static final ArmorUtils.ModularHUD HUD = new ArmorUtils.ModularHUD();
    private static final List<String> hudStrings = new ArrayList<>();
    private static final LongArrayList charge = new LongArrayList();
    private static final LongArrayList maxCharge = new LongArrayList();

    @SubscribeEvent
    public static void onRenderArmorHUD(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.inGameHasFocus && mc.world != null && !mc.gameSettings.showDebugInfo && Minecraft.isGuiEnabled()) {

            boolean hasArmor = false;
            for (int i = 0; i < 4; i++) {
                ItemStack stack = mc.player.inventory.armorInventory.get(i);
                ModularArmor modularArmor = ModularArmor.get(stack);
                if (modularArmor != null) {
                    if (ModularArmor.getCapacity(stack) > 0) {
                        charge.add(ModularArmor.getEnergy(stack));
                        maxCharge.add(ModularArmor.getCapacity(stack));
                    }
                    modularArmor.addHUDInfo(stack, hudStrings);
                    hasArmor = true;
                }
            }
            if (hasArmor) {
                HUD.newString(I18n.format("metaarmor.hud.energy_lvl", String.format("%.1f", batteryPercentage()) + "%"));
                if (!hudStrings.isEmpty()) {
                    for (String string : hudStrings) {
                        HUD.newString(string);
                    }
                }
                HUD.draw();
                HUD.reset();
            }
            hudStrings.clear();
        }
    }

    private static float batteryPercentage() {
        float percentage = 0;
        byte max = 0;
        for (int i = 0; i < charge.size(); i++) {
            max++;
            percentage += (float) charge.getLong(i) * 100.0F / maxCharge.getLong(i);
        }
        charge.clear();
        maxCharge.clear();
        return (percentage / max);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW) //set to low so other mods don't accidentally destroy it easily
    public static void handleFovEvent(FOVUpdateEvent event) {

        IAttributeInstance iattributeinstance = event.getEntity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        float f = 1 / ((float) (((iattributeinstance.getAttributeValue() / (double) event.getEntity().capabilities.getWalkSpeed() + 1.0D) / 2.0D)));

        EntityPlayerSP player = Minecraft.getMinecraft().player;

        float zoom = (float) (1 / MTConfig.modularArmor.modules.binocularZoom);

        if (Mouse.isButtonDown(1)) {
            ItemStack binoculars = MTMetaItems.BINOCULARS.getStackForm();
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() != binoculars.getItem() || stack.getMetadata() != binoculars.getMetadata()) {
                stack = player.getHeldItemOffhand();
            }
            if (stack.getItem() == binoculars.getItem() && stack.getMetadata() == binoculars.getMetadata()) {
                event.setNewfov(event.getNewfov() * zoom * f);//*speedFOV;
                return;
            }
        }

        ItemStack helmet = player.inventory.armorInventory.get(3);
        ModularArmor modularArmor = ModularArmor.get(helmet);
        if (modularArmor != null) {
            NBTTagCompound armorData = ModularArmor.getArmorData(helmet);
            if (!armorData.getBoolean("zoom"))
                return;
            List<IModule> modules = ModularArmor.getModulesOf(helmet);
            for (IModule module : modules) {
                if (module instanceof Binoculars) {
                    event.setNewfov(event.getNewfov() * zoom * f);//*speedFOV;
                    break;
                }
            }
        }
    }
}
