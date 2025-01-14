package com.drppp.drtech.common;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.event.MobHordeEvent;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class CommonProxy {
    public void preLoad(){

    }
    public void load() {
        if(DrtConfig.HordeSwitch.MobHordeEvent) {
            int i;
            for (i = 1; i <= 1; i++) {
                if(DrtConfig.HordeSwitch.ZombieHordeEvent)new MobHordeEvent((p) -> new EntityZombie(p.world), (int) Math.pow(2, i), (int) Math.pow(2, i) * 2, String.format("zombies_%s", i)).setMaximumDistanceUnderground(i * 5).setNightOnly(true);
                if(DrtConfig.HordeSwitch.SkeletonMobHordeEvent)new MobHordeEvent((p) -> new EntitySkeleton(p.world), (int) Math.pow(2, i), (int) Math.pow(2, i) * 2, String.format("skeleton_%s", i)).setMaximumDistanceUnderground(i * 5).setNightOnly(true);
                if(DrtConfig.HordeSwitch.CreeperMobHordeEvent)new MobHordeEvent((p) -> new EntityCreeper(p.world), (int) Math.pow(2, i), (int) Math.pow(2, i) * 2, String.format("creeper_%s", i)).setMaximumDistanceUnderground(i * 5).setNightOnly(true);

            }
        }
    }

}