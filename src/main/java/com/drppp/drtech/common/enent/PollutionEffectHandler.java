package com.drppp.drtech.common.enent;

import com.drppp.drtech.World.Biome.BiomeHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PollutionEffectHandler {

    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
        // 确保实体是玩家
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

            // 获取玩家脚下的方块位置
            BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);

            // 确保玩家在水中
            if (player.isInWater()) {
                // 获取当前的群系
                Biome biome = player.world.getBiome(pos);

                // 检查群系是否是污染群系
                if (biome== BiomeHandler.POLLUTION_BIOME) {
                    // 给玩家施加中毒效果
                    player.addPotionEffect(new PotionEffect(MobEffects.POISON, 100)); // 持续时间(游戏刻)和效果等级可以根据需要进行调整
                }
            }
        }
    }
}