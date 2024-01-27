package com.drppp.drtech.Blocks;

import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses;
import com.drppp.drtech.Blocks.Pipe.BlockMyLaserPipe;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import gregtech.common.pipelike.laser.LaserPipeType;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class BlocksInit {
    public static final BlockGravitationalAnomaly BLOCK_GRAVITATIONAL_ANOMALY = new BlockGravitationalAnomaly();
    public static final MetaGlasses TRANSPARENT_CASING = new MetaGlasses("glasses_casing");
    public static final MetaCasing COMMON_CASING = new MetaCasing();
    public static final BlockMyLaserPipe MY_LASER_PIPE = new BlockMyLaserPipe(LaserPipeType.values()[0]);
    public  static void init(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BLOCK_GRAVITATIONAL_ANOMALY);
        GameRegistry.registerTileEntity(TileEntityGravitationalAnomaly.class, new ResourceLocation(Tags.MODID, "gravitational_anomaly"));
        event.getRegistry().register(TRANSPARENT_CASING);
        event.getRegistry().register(COMMON_CASING);


        //管道 未生效
        MY_LASER_PIPE.setRegistryName("my_laser_pipe_normal");
        MY_LASER_PIPE.setTranslationKey("my_laser_pipe_normal");
        MY_LASER_PIPE.setCreativeTab(DrTechMain.Mytab);
        event.getRegistry().register(MY_LASER_PIPE);


    }

    public static Vector3f randomSpherePoint(double x0, double y0, double z0, Vec3d radius, Random rand) {
        double u = rand.nextDouble();
        double v = rand.nextDouble();
        double theta = 6.283185307179586 * u;
        double phi = Math.acos(2.0 * v - 1.0);
        double x = x0 + radius.x * Math.sin(phi) * Math.cos(theta);
        double y = y0 + radius.y * Math.sin(phi) * Math.sin(theta);
        double z = z0 + radius.z * Math.cos(phi);
        return new Vector3f((float)x, (float)y, (float)z);
    }

}
