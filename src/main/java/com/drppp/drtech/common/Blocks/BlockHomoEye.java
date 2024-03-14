package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import com.drppp.drtech.Tile.TileEntityHomoEye;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHomoEye extends Block {
    public BlockHomoEye() {
        super(Material.IRON);
        this.setResistance(10.0F);
        this.disableStats();
        this.setRegistryName(Tags.MODID,"homo_eye");
        this.setCreativeTab(DrTechMain.Mytab);
        this.setTranslationKey(Tags.MODID+".homo_eye");
         this.setLightOpacity(255);
    }

    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityHomoEye();
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

}
