package com.drppp.drtech.Items.Baubles;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtechEventHandler;
import com.drppp.drtech.Tags;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.toolitem.IGTToolDefinition;
import gregtech.api.unification.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class ElectricFlightRing extends DrtechBaubleItem{
    public ElectricFlightRing()
    {
        this.setRegistryName(Tags.MODID,getToolId());
        this.setTranslationKey(Tags.MODID+".electric_flight_ring");
        this.setCreativeTab(DrTechMain.Mytab);
        this.setMaxStackSize(1);
    }
    @Override

    public String getToolId() {
        return "electric_flight_ring";
    }


    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase entity) {
        if(entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)entity;
            if(!player.isSpectator() && !player.capabilities.isCreativeMode)
            {
                    if(!player.capabilities.allowFlying)
                    {
                        enableFlyingAbility(player);
                    }
                    else
                    {
                        if(player.capabilities.isFlying)
                        {
                                if(DrtechEventHandler.ctrlflag==1)
                                {
                                    Vec3d vec3d = player.getLookVec();
                                    player.motionX += vec3d.x * 0.4D + (vec3d.x * 1.5D - player.motionX) * 0.5D;
                                    player.motionY += vec3d.y * 0.4D + (vec3d.y * 1.5D - player.motionY) * 0.5D;
                                    player.motionZ += vec3d.z * 0.4D + (vec3d.z * 1.5D - player.motionZ) * 0.5D;

                                    float pitch = player.rotationPitch, yaw = player.rotationYaw;
                                    for (int p = 0; p < 3; p++) {
                                        float newYaw = yaw + getRandomFromRange(40, -40);
                                        float newPitch = pitch + getRandomFromRange(40, -40);
                                        Vec3d shootPosition = new Vec3d(
                                                -MathHelper.sin(newYaw * 0.0174F) * MathHelper.cos(newPitch * 0.0174F),
                                                -MathHelper.sin(newPitch * 0.0174F),
                                                MathHelper.cos(newYaw * 0.0174F) * MathHelper.cos(newPitch * 0.0174F));
                                        player.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, player.posX,
                                                player.posY + player.getEyeHeight() - 0.2f, player.posZ, shootPosition.x, shootPosition.y , shootPosition.z , 1);
                                    }
                                }

                        }
                    }

            }
        }
    }

    private int getRandomFromRange(int max, int min)
    {
        return new Random().nextInt(max-min)+min;
    }


    private void enableFlyingAbility(EntityPlayer player)
    {
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
    }

    private void disableFlyingAbility(EntityPlayer player)
    {
        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        // TODO Auto-generated method stub
        super.onUnequipped(itemstack, player);
        if(player instanceof EntityPlayer)
        {
            disableFlyingAbility((EntityPlayer)player);
        }
    }
}
