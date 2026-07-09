package com.drppp.drtech.Client;

import com.brachy84.mechtech.MechTech;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.CommonProxy;
import com.drppp.drtech.hooked.HookClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    public void preLoad() {
        super.preLoad();

    }
    @Override
    public void registerItemRenderer(Item item, int meta, String id) {

    }

    @Override
    public void setAutoJump(EntityLivingBase entityLiving, boolean value) {
        if (entityLiving instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) entityLiving;
            boolean enabled = value && Minecraft.getMinecraft().gameSettings.autoJump;
            ObfuscationReflectionHelper.setPrivateValue(EntityPlayerSP.class, player, enabled, "field_189811_cr", "autoJumpEnabled");
        }
    }

    @Override
    public String getHookKeyDisplayName() {
        return HookClientHooks.keyFire.getDisplayName();
    }
}
