package com.drppp.drtech.common;

import com.drppp.drtech.Tags;
import com.drppp.drtech.api.event.MobHordeEvent;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class CommonProxy {
    public void preLoad(){

    }
    public void load() {
        new MobHordeEvent((p) -> new EntityZombie(p.world), 4, 8, "zombies_a").setMaximumDistanceUnderground(10).setNightOnly(true);
        new MobHordeEvent((p) -> new EntityZombie(p.world), 8, 16, "zombies_b").setMaximumDistanceUnderground(15).setNightOnly(true);
        new MobHordeEvent((p) -> new EntityZombie(p.world), 16, 32, "zombies_c").setMaximumDistanceUnderground(20).setNightOnly(true);
        new MobHordeEvent((p) -> new EntityZombie(p.world), 32, 64, "zombies_d").setMaximumDistanceUnderground(25).setNightOnly(true);
    }
}