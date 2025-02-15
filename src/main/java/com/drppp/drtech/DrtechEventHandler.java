package com.drppp.drtech;

import com.drppp.drtech.World.SaveDatas.PlayerSpawnData;
import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import com.drppp.drtech.common.Entity.EntityFirstRocket;
import gregtech.api.unification.material.event.MaterialEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechEventHandler {
    public static int ctrlflag = 0;
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
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
    }

    @SideOnly(Side.CLIENT)
    public static class Keybinds {
        public static final KeyBinding CTRL = new KeyBinding("key.ctrl", Keyboard.KEY_LCONTROL, "key.categories.drtech");

        public static void registerKeybinds() {
            ClientRegistry.registerKeyBinding(CTRL);
        }
    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        System.out.println("!111");
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return; // 只在经验条渲染时执行
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        // 检查玩家是否骑乘 EntityAdvancedRocket
        if (player.getRidingEntity() instanceof EntityAdvancedRocket) {
            EntityAdvancedRocket rocket = (EntityAdvancedRocket) player.getRidingEntity();
            int fuelAmount = rocket.getFuelAmount();
            int maxFuel = 1000; // 假设最大燃料量为 100

            // 获取屏幕分辨率
            ScaledResolution resolution = new ScaledResolution(mc);
            int width = resolution.getScaledWidth();
            int height = resolution.getScaledHeight();

            // 绘制背景条
            int barWidth = 100; // 进度条宽度
            int barHeight = 10; // 进度条高度
            int x = (width - barWidth) / 2; // 水平居中
            int y = height - 30; // 距离屏幕底部 30 像素

            Gui.drawRect(x, y, x + barWidth, y + barHeight, 0xFF000000); // 黑色背景

            // 绘制燃料条
            int fuelWidth = (int) ((float) fuelAmount / maxFuel * barWidth);
            Gui.drawRect(x, y, x + fuelWidth, y + barHeight, 0xFF00FF00); // 绿色燃料条

            // 绘制文字
            String text = "Fuel: " + fuelAmount + "/" + maxFuel;
            mc.fontRenderer.drawStringWithShadow(text, x, y - 12, 0xFFFFFF); // 白色文字
        }
    }

}
