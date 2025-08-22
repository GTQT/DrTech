package com.drppp.drtech.common.event;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.World.Biome.BiomeHandler;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommonHandler {
    private int tickCounter = 0;
    private static final int TEN_MINUTES_IN_TICKS = 2 * 20 * 20;
    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
        // 确保实体是玩家
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

            // 获取玩家脚下的方块位置
            BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);

            // 确保玩家在水中
            if (player.isInWater() || player.isInLava()) {
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
    @SubscribeEvent
    public void onGrassBroken(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        IBlockState state = event.getState();
        Block block = state.getBlock();
        if ((block == Blocks.TALLGRASS || block == Blocks.GRASS) && !world.isRemote) {
            Random rand = world.rand;
            if (rand.nextFloat() < 0.1F) {
                ItemStack seedStack = new ItemStack(ItemsInit.ITEM_XJC_SEED, 1);
                event.getDrops().add(seedStack);
            }
        }
    }
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || !DrtConfig.Upload) return;
        tickCounter++;
        if (tickCounter >= TEN_MINUTES_IN_TICKS) {
            tickCounter = 0;
            MinecraftServer server = event.world.getMinecraftServer();
            if (server != null) {
                List<String> list =new ArrayList<>();
                for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                    list.add(player.getName());
                }
                if(list.size()>0)
                {
                    JDBC jdbc = new JDBC(list);
                    jdbc.run();
                }
            }
        }
    }
}