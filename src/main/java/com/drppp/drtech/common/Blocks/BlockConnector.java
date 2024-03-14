package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockConnector extends Block {
    private int tire;
    public BlockConnector(int tire) {
        super(Material.IRON);
        this.setResistance(10F);
        this.setRegistryName(Tags.MODID,"connector_"+tire);
        this.setCreativeTab(DrTechMain.Mytab);
        this.setTranslationKey(Tags.MODID+".connector_"+tire);
        this.tire = tire;
    }
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityConnector(tire);
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if(entity instanceof TileEntityConnector && !worldIn.isRemote)
        {
            playerIn.sendStatusMessage(new TextComponentString("EU: " + ((TileEntityConnector) entity).StoredEnergy+"/"+((TileEntityConnector) entity).MaxEnergy), true);
            return true;
        }
        return false;
    }
}
