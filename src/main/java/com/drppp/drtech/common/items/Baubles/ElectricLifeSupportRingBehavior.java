package com.drppp.drtech.common.Items.Baubles;

import baubles.api.BaubleType;
import gregtech.integration.baubles.BaubleBehavior;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;

public class ElectricLifeSupportRingBehavior extends BaubleBehavior {
    public ElectricLifeSupportRingBehavior() {
        super(BaubleType.RING);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if(!player.world.isRemote && player instanceof EntityPlayer)
        {
            if(hasEnergy(stack))
            {
                EntityPlayer player1 = (EntityPlayer)player;
                boolean haveInfoUpdated = false;

                long ratio = 2000;

                float currentHealth = player.getHealth();
                if(currentHealth < player.getMaxHealth())
                {
                    if (drainenergy(stack,ratio,true)) {
                        drainenergy(stack,ratio,false);
                        if(player1.getHealth()>0)
                            player1.setHealth(currentHealth+1);
                        haveInfoUpdated = true;
                    }
                }

                FoodStats foodStats = player1.getFoodStats();
                if(foodStats.needFood())
                {
                    if (drainenergy(stack,ratio,true)) {
                        drainenergy(stack,ratio,false);
                        foodStats.addStats(1, 0.2f);
                        haveInfoUpdated = true;
                    }
                }

                float currentAbsAmount = player.getAbsorptionAmount();
                if(currentAbsAmount < 20.0f)
                {
                    if (drainenergy(stack,ratio,true)) {
                        drainenergy(stack,ratio,false);
                        player.setAbsorptionAmount(currentAbsAmount + 0.5f);
                        haveInfoUpdated = true;
                    }
                }

                if(haveInfoUpdated)
                {
                    player1.inventoryContainer.detectAndSendChanges();
                }

            }

        }
    }
    private boolean hasEnergy(ItemStack item)
    {
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)
            return  false;
        if(tag!=null&&!tag.hasKey("Charge"))
            return false;
        if(tag.getLong("Charge")<=0)
            return false;
        return true;
    }
    //返回是否能消耗能量
    private boolean drainenergy(ItemStack item,long amount,boolean simulate)
    {
        NBTTagCompound tag = item.getTagCompound();
        if(tag==null)
            return  false;
        long leftEnergy = tag.getLong("Charge");
        if(leftEnergy<amount)
            return false;
        if(!simulate)
        {
            leftEnergy -=amount;
            tag.setLong("Charge",leftEnergy);
            item.setTagCompound(tag);
        }
        return true;
    }
}
