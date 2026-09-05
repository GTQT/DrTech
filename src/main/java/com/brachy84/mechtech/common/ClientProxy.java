package com.brachy84.mechtech.common;

import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.client.ClientEventHandler;
import com.brachy84.mechtech.client.ClientHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = MechTech.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void preLoad() {
        super.preLoad();
        ClientHandler.preInit();
    }

    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }
}
