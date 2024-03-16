package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.common.Entity.EntityDrone;
import com.drppp.drtech.common.Entity.EntityWindRotor;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityLogFactory;
import com.drppp.drtech.loaders.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MeTaTileEntityWindDrivenGenerator extends MetaTileEntityBaseWithControl {
    private AxisAlignedBB landingAreaBB;
    public EntityWindRotor rotor = null;
    public MeTaTileEntityWindDrivenGenerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }


    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" XX", "XXX", "XX ")
                .aisle(" XX", "X#X", "XX ")
                .aisle(" XX", "X#X", "XX ")
                .aisle(" XX", "X#X", "XX ")
                .aisle(" XX", "XSX", "XX ")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(4)
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setExactLimit(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setExactLimit(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                )
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.ASEPTIC_MACHINE_CASING;
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.ASEPTIC_MACHINE_CASING);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MeTaTileEntityWindDrivenGenerator(this.metaTileEntityId);
    }
    public Vec3d getDroneSpawnPosition( ) {

        double altitude =  this.getPos().getY() + 10;

        switch (this.getFrontFacing()) {
            case EAST -> {
                return new Vec3d(this.getPos().getX() - 1.5, altitude, this.getPos().getZ() + 0.5);
            }
            case SOUTH -> {
                return new Vec3d(this.getPos().getX() + 0.5, altitude, this.getPos().getZ() - 1.5);
            }
            case WEST -> {
                return new Vec3d(this.getPos().getX() + 2.5, altitude, this.getPos().getZ() + 0.5);
            }
            default -> {
                return new Vec3d(this.getPos().getX() + 0.5, altitude, this.getPos().getZ() + 2.5);
            }
        }

    }
    public void spawnDroneEntity() {

        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", "drtech:wind_rotor");
        Vec3d pos = this.getDroneSpawnPosition();

        rotor = (EntityWindRotor) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, this.getWorld(), pos.x, pos.y, pos.z, true);

    }
    @Override
    protected void updateFormedValid() {
        if(!this.isActive())
            setActive(true);
        if(!this.getWorld().isRemote)
        {
            if(this.rotor==null && this.isActive() && this.isWorkingEnabled())
            {
                spawnDroneEntity();
            }
            else if(this.rotor!=null && !this.isWorkingEnabled() )
            {
                this.rotor.setDead();
                this.rotor=null;
            }
        }
    }
}
