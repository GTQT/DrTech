package com.drppp.drtech.Blocks.Pipe;

import com.drppp.drtech.Client.render.LaserPipeRenderer;
import gregtech.common.pipelike.laser.BlockLaserPipe;
import gregtech.common.pipelike.laser.LaserPipeType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;


public class BlockMyLaserPipe extends BlockLaserPipe {
    public BlockMyLaserPipe(@NotNull LaserPipeType pipeType) {
        super(pipeType);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return LaserPipeRenderer.INSTANCE.getBlockRenderType();
    }

}
