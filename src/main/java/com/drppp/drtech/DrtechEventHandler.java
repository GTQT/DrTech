package com.drppp.drtech;

import com.drppp.drtech.Network.PacketJumpKey;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.World.SaveDatas.PlayerSpawnData;
import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import com.drppp.drtech.common.Blocks.BlockWoodAxle;
import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import com.drppp.drtech.common.Entity.EntityFirstRocket;
import gregtech.api.unification.material.event.MaterialEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import static com.drppp.drtech.common.Blocks.BlockWoodAxle.FACING;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechEventHandler {
    public static int ctrlflag = 0;
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP && DrtConfig.EnableRocketDitch) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            World world = player.world;

            // 获取持久化数据
            PlayerSpawnData spawnData = PlayerSpawnData.get(world);

            // 检查玩家是否首次登录
            if (!spawnData.hasPlayerSpawned(player.getUniqueID())) {
                // 标记玩家已生成火箭
                spawnData.markPlayerSpawned(player.getUniqueID());

                // 生成 Rocket 实体
                EntityFirstRocket rocket = new EntityFirstRocket(world);
                rocket.setPosition(player.posX, 255, player.posZ);
                world.spawnEntity(rocket);

                // 让玩家骑乘 Rocket
                player.startRiding(rocket);
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(MaterialEvent event) {
        DrtechMaterials.init();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
         if (Keybinds.CTRL.isKeyDown()) {
            ctrlflag = 1;
        } else
            ctrlflag = 0;
        if(Keybinds.ROCKET_FLY.isKeyDown())
        {
            SyncInit.PRESSED_NETWORK.sendToServer(new PacketJumpKey(true));
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Keybinds {
        public static final KeyBinding CTRL = new KeyBinding("key.ctrl", Keyboard.KEY_LCONTROL, "key.categories.drtech");
        public static final KeyBinding ROCKET_FLY = new KeyBinding("key.rocket_fly", Keyboard.KEY_SPACE, "key.categories.drtech");

        public static void registerKeybinds() {
            ClientRegistry.registerKeyBinding(CTRL);
            ClientRegistry.registerKeyBinding(ROCKET_FLY);
        }
    }
}
