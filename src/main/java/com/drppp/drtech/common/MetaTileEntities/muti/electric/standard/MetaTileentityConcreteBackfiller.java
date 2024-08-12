package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;

public class MetaTileentityConcreteBackfiller extends MetaTileEntityBaseWithControl {
    public int level=1;
    public int range = 0;
    public int eut = 0;
    private int YHead=0;

    public MetaTileentityConcreteBackfiller(ResourceLocation metaTileEntityId,int level) {
        super(metaTileEntityId);
        this.level = level;
        if(level==1)
        {
            range = 16;
            eut=96;
            maxProcess = 20;
        }
        else if (level==2) {
            range=64;
            eut=1536;
            maxProcess=5;
        }
    }

    @Override
    protected void updateFormedValid() {
        if(!getWorld().isRemote)
        {
            if(!this.isWorkingEnabled())
            {
                if(canwork())
                    setWorkingEnabled(true);
            }else
            {
                if(!canwork())
                {
                    setWorkingEnabled(false);
                    process=0;
                    return;
                }
                this.process++;
                if(YHead==getPos().getY())
                {
                    process=0;
                    setWorkingEnabled(false);
                    return;
                }
                if(process >= getMaxProgress())
                {
                    process=0;

                    int radius = range;
                       for (int i = -radius; i <= radius; i++) {
                            for (int j = -radius; j <= radius; j++) {
                                if (isBlockAir(getWorld(),new BlockPos(getPos().getX()+i,YHead,getPos().getZ()+j))) {
                                    setBlockToDirt(getWorld(),new BlockPos(getPos().getX()+i,YHead,getPos().getZ()+j));
                                    tryConsumeFluid();
                                }
                            }
                        }
                        YHead++;
                        if(YHead>=getPos().getY())
                            YHead=0;
                }
            }
        }
    }
    public boolean isBlockAir(World world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        return blockState.getBlock() == Blocks.AIR;
    }
    public void setBlockToDirt(World world, BlockPos pos) {
        world.setBlockState(pos, MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT));
    }
    private boolean canwork()
    {
        if(YHead>=getPos().getY()-1)
        {
            YHead=0;
            return false;
        }
        if(this.getEnergyContainer().getEnergyStored()>eut && this.getInputFluidInventory()!=null && this.getInputFluidInventory().getTanks()>0 )
        {
            for (int i = 0; i < this.getInputFluidInventory().getTanks(); i++) {
                if(this.getInputFluidInventory().getTankAt(i).getFluid()!=null && this.getInputFluidInventory().getTankAt(i).getFluid().getFluid() == Materials.Concrete.getFluid())
                {
                  if(this.getInputFluidInventory().getTankAt(i).getFluidAmount()>=144)
                  {
                      return true;
                  }
                }
            }
        }
        return false;
    }

    private boolean tryConsumeFluid() {
        if(this.getInputFluidInventory()!=null && this.getInputFluidInventory().getTanks()>0)
        for (int i = 0; i < this.getInputFluidInventory().getTanks(); i++) {
            if(this.getInputFluidInventory().getTankAt(i).getFluid()!=null && this.getInputFluidInventory().getTankAt(i).getFluid().getFluid() == Materials.Concrete.getFluid())
            {
                if(this.getInputFluidInventory().getTankAt(i).getFluidAmount()>=144)
                {
                    this.getInputFluidInventory().getTankAt(i).drain(144,true);
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                )
                .where('C', states(getCasingState()))
                .where('F', getFramePredicate())
                .where('#', any())
                .build();
    }
    public IBlockState getCasingState() {
        if (level==2)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        if (level==2)
            return Textures.STABLE_TITANIUM_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    private TraceabilityPredicate getFramePredicate() {
        if (level==2)
            return frames(Materials.Titanium);
        return frames(Materials.Steel);
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileentityConcreteBackfiller(this.metaTileEntityId,this.level);
    }
}
