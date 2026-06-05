package com.drppp.drtech.lootgames.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import com.drppp.drtech.lootgames.api.tileentity.TileEntityGameMaster;

public class BlockSmartSubordinate extends BlockGame {
    private static boolean underBreaking = false;

    public BlockSmartSubordinate() {
        setRegistryName("lootgames_smart_subordinate");
        setTranslationKey("lootgames.smart_subordinate");
    }

    @Override
    public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (!worldIn.isRemote && !underBreaking) {
            underBreaking = true;
            destroyStructure(worldIn, pos);
            underBreaking = false;
        }
        super.breakBlock(worldIn, pos, state);
    }

    private void destroyStructure(World worldIn, BlockPos pos) {
        BlockPos masterPos = getMasterPos(worldIn, pos);
        TileEntity te = worldIn.getTileEntity(masterPos);
        if (te instanceof TileEntityGameMaster<?>) {
            ((TileEntityGameMaster<?>) te).destroyGameBlocks();
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            BlockPos masterPos = getMasterPos(worldIn, pos);
            TileEntity te = worldIn.getTileEntity(masterPos);
            if (te instanceof TileEntityGameMaster<?>) {
                ((TileEntityGameMaster<?>) te).onSubordinateBlockClicked(pos, playerIn);
            } else {
                playerIn.sendMessage(new TextComponentString("The game doesn't seem to work. Please report this to the mod author."));
            }
        }
        return true;
    }

    public BlockPos getMasterPos(World world, BlockPos pos) {
        BlockPos foundPos = pos;
        IBlockState state;

        // Check if the current block itself is a SmartSubordinate
        if (world.getBlockState(pos).getBlock() instanceof BlockSmartSubordinate) {
            // find master by going west first, then north
        }

        while (true) {
            foundPos = foundPos.add(-1, 0, 0);
            state = world.getBlockState(foundPos);
            if (world.getTileEntity(foundPos) instanceof TileEntityGameMaster<?>) {
                break;
            }
            if (!(state.getBlock() instanceof BlockSmartSubordinate)) {
                foundPos = foundPos.add(1, 0, 0);
                break;
            }
        }

        while (true) {
            foundPos = foundPos.add(0, 0, -1);
            state = world.getBlockState(foundPos);
            if (world.getTileEntity(foundPos) instanceof TileEntityGameMaster<?>) {
                break;
            }
            if (!(state.getBlock() instanceof BlockSmartSubordinate)) {
                foundPos = foundPos.add(0, 0, 1);
                break;
            }
        }

        return foundPos;
    }
}
