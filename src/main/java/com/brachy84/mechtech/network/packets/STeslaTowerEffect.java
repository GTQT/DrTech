package com.brachy84.mechtech.network.packets;

import com.brachy84.mechtech.client.Sounds;
import com.brachy84.mechtech.client.render.Lightning;
import com.brachy84.mechtech.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class STeslaTowerEffect implements IPacket {

    private BlockPos source;
    private Vec3d target;
    private float scale = 12f;

    public STeslaTowerEffect() {
    }

    public STeslaTowerEffect(final BlockPos source, final Vec3d target) {
        this.source = source;
        this.target = target;
    }

    public STeslaTowerEffect setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public BlockPos getSource() {
        return source;
    }

    public Vec3d getTarget() {
        return target;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(source);
        buf.writeDouble(target.x);
        buf.writeDouble(target.y);
        buf.writeDouble(target.z);
        buf.writeFloat(scale);
    }

    @Override
    public void read(PacketBuffer buf) {
        source = buf.readBlockPos();
        target = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        scale = buf.readFloat();
    }

    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Vec3d s = new Vec3d(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5);
        Vec3d t = target;
        if (s.x != t.x || s.z != t.z) {
            Vec3d d = t.subtract(s);    // difference of start & end
            double x = d.x, z = d.z;
            double l = Math.sqrt(x * x + z * z);    //
            x /= l;                                 // normalize d on horizontal axis
            z /= l;                                 //
            s = s.add(x * 4, 0, z * 4); // move start by normalized d * 4
            // result is a position inside the toroid
        }

        Lightning lightning = new Lightning(Minecraft.getMinecraft().world, s, t)
                .setColor(new Color(83, 166, 189, 190).getRGB(), new Color(167, 192, 199, 255).getRGB())
                .setScale(scale)
                .setup();
        Minecraft.getMinecraft().effectRenderer.addEffect(lightning);
        Minecraft.getMinecraft().world.playSound(s.x, s.y, s.z, Sounds.TESLA_ZAP, SoundCategory.BLOCKS, 3, 0.7f, false);
        return null;
    }
}
