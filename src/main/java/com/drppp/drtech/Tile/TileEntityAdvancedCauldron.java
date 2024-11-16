package com.drppp.drtech.Tile;

import com.drppp.drtech.common.Blocks.BlockAdvancedCauldron;
import net.minecraft.block.BlockCauldron;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityAdvancedCauldron extends TileEntity implements ITickable {

    int tick=0;
    @Override
    public void update() {
        if(++tick>=20 && !getWorld().isRemote)
        {
            tick=0;
            checkForWaterContainer();
        }
    }
    public void checkForWaterContainer() {
        World world = this.getWorld();
        BlockPos pos = this.getPos();
        var state = world.getBlockState(getPos());
        int i = (state.getValue(BlockCauldron.LEVEL)).intValue();
        if(i==3)
            return;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos neighborPos = pos.add(x, y, z);

                    if (!neighborPos.equals(pos)) {
                        IFluidHandler fluidHandler = getFluidHandler(world, neighborPos);
                        if (fluidHandler != null) {
                            FluidStack fluidStack = fluidHandler.drain(new FluidStack(FluidRegistry.WATER, 1000), false);
                            if (fluidStack != null && fluidStack.amount >= 1000) {
                                extractWater(fluidHandler);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private IFluidHandler getFluidHandler(World world, BlockPos pos) {
        // 检查方块是否具有 IFluidHandler 能力
        if (world.getTileEntity(pos) != null) {
            if (world.getTileEntity(pos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                return world.getTileEntity(pos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            }
        }
        return null;
    }

    private void extractWater(IFluidHandler fluidHandler) {
        fluidHandler.drain(new FluidStack(FluidRegistry.WATER, 1000), true);
        BlockAdvancedCauldron ac = (BlockAdvancedCauldron)world.getBlockState(getPos()).getBlock();
        if(pos !=null)
        {
            ac.setWaterLevel(world, pos, world.getBlockState(getPos()), 3);
        }
    }
}
