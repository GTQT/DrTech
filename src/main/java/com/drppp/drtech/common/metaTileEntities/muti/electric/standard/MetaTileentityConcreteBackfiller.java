package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.element.Elements;

import gregtech.api.pattern.element.StructureDefinition;

public class MetaTileentityConcreteBackfiller extends MetaTileEntityBaseWithControl {
    public int level = 1;
    public int range = 0;
    public int eut = 0;
    private int YHead = 0;

    public MetaTileentityConcreteBackfiller(ResourceLocation metaTileEntityId, int level) {
        super(metaTileEntityId);
        this.level = level;
        if (level == 1) {
            range = 16;
            eut = 96;
            maxProcess = 20;
        } else if (level == 2) {
            range = 64;
            eut = 1536;
            maxProcess = 5;
        }
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (!this.isWorkingEnabled()) {
                if (canwork())
                    setWorkingEnabled(true);
            } else {
                if (!canwork()) {
                    setWorkingEnabled(false);
                    process = 0;
                    return;
                }
                this.process++;
                if (YHead == getPos().getY()) {
                    process = 0;
                    setWorkingEnabled(false);
                    return;
                }
                if (process >= getMaxProgress()) {
                    process = 0;

                    int radius = range;
                    for (int i = -radius; i <= radius; i++) {
                        for (int j = -radius; j <= radius; j++) {
                            if (isBlockAir(getWorld(), new BlockPos(getPos().getX() + i, YHead, getPos().getZ() + j))) {
                                setBlockToDirt(getWorld(), new BlockPos(getPos().getX() + i, YHead, getPos().getZ() + j));
                                tryConsumeFluid();
                            }
                        }
                    }
                    YHead++;
                    if (YHead >= getPos().getY())
                        YHead = 0;
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

    private boolean canwork() {
        if (YHead >= getPos().getY() - 1) {
            YHead = 0;
            return false;
        }
        if (this.getEnergyContainer().getEnergyStored() > eut && this.getInputFluidInventory() != null && this.getInputFluidInventory().getTanks() > 0) {
            for (int i = 0; i < this.getInputFluidInventory().getTanks(); i++) {
                if (this.getInputFluidInventory().getTankAt(i).getFluid() != null && this.getInputFluidInventory().getTankAt(i).getFluid().getFluid() == Materials.Concrete.getFluid()) {
                    if (this.getInputFluidInventory().getTankAt(i).getFluidAmount() >= 144) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryConsumeFluid() {
        if (this.getInputFluidInventory() != null && this.getInputFluidInventory().getTanks() > 0)
            for (int i = 0; i < this.getInputFluidInventory().getTanks(); i++) {
                if (this.getInputFluidInventory().getTankAt(i).getFluid() != null && this.getInputFluidInventory().getTankAt(i).getFluid().getFluid() == Materials.Concrete.getFluid()) {
                    if (this.getInputFluidInventory().getTankAt(i).getFluidAmount() >= 144) {
                        this.getInputFluidInventory().getTankAt(i).drain(144, true);
                        return true;
                    }
                }
            }
        return false;
    }

    private static final StructureDefinition<?> STRUCTURE_DEFINITION_1 =
            StructureDefinition.getOrBuild("drtech:concrete_backfiller/1",
                    () -> buildTemplate(1));

    private static final StructureDefinition<?> STRUCTURE_DEFINITION_2 =
            StructureDefinition.getOrBuild("drtech:concrete_backfiller/2",
                    () -> buildTemplate(2));

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return level == 2 ? STRUCTURE_DEFINITION_2 : STRUCTURE_DEFINITION_1;
    }

    private static StructureDefinition<?> buildTemplate(int level) {
        return DeclarativePatternBuilder.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .self('S', MetaTileentityConcreteBackfiller.class)
                .where('X', Elements.chain(
                        Elements.counted(0, 4096, Elements.block(getCasingState(level))),
                        Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 1, 1, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 3, 1),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1)))
                .blocks('C', getCasingState(level))
                .frames('F', level == 2 ? Materials.Titanium : Materials.Steel)
                .any('#')
                .buildStructureDefinition();

    }

    public IBlockState getCasingState() {
        return getCasingState(level);
    }

    private static IBlockState getCasingState(int level) {
        if (level == 2)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);

    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        if (level == 2)
            return Textures.STABLE_TITANIUM_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileentityConcreteBackfiller(this.metaTileEntityId, this.level);
    }
}
