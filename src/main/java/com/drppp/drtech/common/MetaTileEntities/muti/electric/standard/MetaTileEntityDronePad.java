package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.drppp.drtech.loaders.recipes.DrtechReceipes.DRONE_PAD;

import gregtech.api.pattern.BlockPatternTemplate;

import gregtech.api.pattern.SoftTemplate;

import gregtech.api.pattern.TemplatePool;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.TraceabilityPredicate;

public class MetaTileEntityDronePad extends RecipeMapMultiblockController {

    public boolean droneReachedSky;

    public MetaTileEntityDronePad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DRONE_PAD);
    }

    private static IBlockState getFirstCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getSecondCasingState() {
        return MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    private static IBlockState getThirdCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getFourthCasingState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    private static IBlockState getBoilerCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    private static IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @NotNull
    private static final SoftTemplate TEMPLATE = TemplatePool.getInstance().register(
            "drtech:drone_pad",
            MetaTileEntityDronePad::buildTemplate
    );

    @Override
    protected BlockPatternTemplate createStructureTemplate() {
        return TEMPLATE.get();
    }

    private static BlockPatternTemplate buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("    F     F", "    F     F", "     FCCCF ", "     F   F ", "           ", "           ")
                .aisle("F          ", "F          ", "FFFFFCXXXCF", " AAAF     F", " AAA       ", "           ")
                .aisle("           ", "           ", "FAAACXXXXXC", "P###P      ", "P###P      ", " AAA       ")
                .aisle("           ", "           ", "FAAACXXXXXC", "A#G#A      ", "A#G#A      ", " AMA       ")
                .aisle("           ", "           ", "FAAACXXXXXC", "P###P      ", "P###P      ", " AAA       ")
                .aisle("F          ", "F          ", "FFFFFCXXXCF", " AAAF     F", " AAA       ", "           ")
                .aisle("    F     F", "    F     F", "     FCSCF ", "     F   F ", "           ", "           ")
                .where('S', selfPredicate(MetaTileEntityDronePad.class))
                .where('C', states(getFirstCasingState()))
                .where('X', states(getSecondCasingState()))
                .where('A', states(getThirdCasingState())
                        .setMinGlobalLimited(25)
                        .or(staticRecipeMapAutoAbilities(true, true, true, true, true, false, false)))
                .where('G', states(getFourthCasingState()))
                .where('P', states(getBoilerCasingState()))
                .where('F', states(getFrameState()))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
                .where('#', air())
                .buildTemplate();

    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDronePad(this.metaTileEntityId);
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("droneReachedSky", this.droneReachedSky);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.droneReachedSky = data.getBoolean("droneReachedSky");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.droneReachedSky);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.droneReachedSky = buf.readBoolean();
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }



    private static TraceabilityPredicate staticDisplayAutoAbilities(boolean maintenance, boolean muffler) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();
        if (maintenance && false) {
            predicate = predicate.or(abilities(MultiblockAbility.MAINTENANCE_HATCH)
                    .setMinGlobalLimited(gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0)
                    .setMaxGlobalLimited(1));
        }
        if (muffler) {
            predicate = predicate.or(abilities(MultiblockAbility.MUFFLER_HATCH)
                    .setMinGlobalLimited(1)
                    .setMaxGlobalLimited(1));
        }
        return predicate;
    }
    private static TraceabilityPredicate staticRecipeMapAutoAbilities(boolean energyIn,
                                                                      boolean maintenance,
                                                                      boolean itemIn,
                                                                      boolean itemOut,
                                                                      boolean fluidIn,
                                                                      boolean fluidOut,
                                                                      boolean muffler) {
        return staticRecipeMapAutoAbilities(energyIn, maintenance, itemIn, itemOut, fluidIn, fluidOut, muffler, 2);
    }

    private static TraceabilityPredicate staticRecipeMapAutoAbilities(boolean energyIn,
                                                                      boolean maintenance,
                                                                      boolean itemIn,
                                                                      boolean itemOut,
                                                                      boolean fluidIn,
                                                                      boolean fluidOut,
                                                                      boolean muffler,
                                                                      int maxEnergyInputs) {
        TraceabilityPredicate predicate = staticDisplayAutoAbilities(maintenance, muffler);
        if (energyIn) {
            predicate = predicate.or(abilities(MultiblockAbility.INPUT_ENERGY)
                    .setMinGlobalLimited(1)
                    .setMaxGlobalLimited(maxEnergyInputs)
                    .setPreviewCount(1));
        }
        if (itemIn) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        }
        if (itemOut) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
        }
        if (fluidIn) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
        }
        if (fluidOut) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
        }
        return predicate;
    }
}