package com.brachy84.mechtech.network.packets;

import com.brachy84.mechtech.client.Sounds;
import com.brachy84.mechtech.client.render.Lightning;
import com.brachy84.mechtech.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class STeslaCoilEffect implements IPacket {

    private Vec3d source;
    private Vec3d target;

    public STeslaCoilEffect() {
    }

    public STeslaCoilEffect(final Vec3d source, final Vec3d target) {
        this.source = source;
        this.target = target;
    }

    public Vec3d getSource() {
        return source;
    }

    public Vec3d getTarget() {
        return target;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeDouble(source.x);
        buf.writeDouble(source.y);
        buf.writeDouble(source.z);
        buf.writeDouble(target.x);
        buf.writeDouble(target.y);
        buf.writeDouble(target.z);
    }

    @Override
    public void read(PacketBuffer buf) {
        source = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        target = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Lightning lightning = new Lightning(Minecraft.getMinecraft().world, source, target)
                .setColor(new Color(83, 166, 189, 153).getRGB(), new Color(167, 192, 199, 204).getRGB())
                .setup();
        Minecraft.getMinecraft().effectRenderer.addEffect(lightning);
        Minecraft.getMinecraft().world.playSound(source.x, source.y, source.z, Sounds.TESLA_ZAP, SoundCategory.BLOCKS, 1, 1.3f, false);
        return null;
    }
}
