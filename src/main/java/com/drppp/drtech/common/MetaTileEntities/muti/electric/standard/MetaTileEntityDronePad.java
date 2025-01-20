package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.api.recipes.properties.DroneDimensionProperty;
import com.drppp.drtech.common.Entity.EntityDrone;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.drppp.drtech.loaders.DrtechReceipes.DRONE_PAD;

public class MetaTileEntityDronePad extends RecipeMapMultiblockController {

    public EntityDrone drone = null;
    public boolean droneReachedSky;
    private AxisAlignedBB landingAreaBB;

    public MetaTileEntityDronePad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DRONE_PAD);
        this.recipeMapWorkable = new DronePadWorkable(this);
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
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("    F     F", "    F     F", "     FCCCF ", "     F   F ", "           ", "           ")
                .aisle("F          ", "F          ", "FFFFFCXXXCF", " AAAF     F", " AAA       ", "           ")
                .aisle("           ", "           ", "FAAACXXXXXC", "P###P      ", "P###P      ", " AAA       ")
                .aisle("           ", "           ", "FAAACXXXXXC", "A#G#A      ", "A#G#A      ", " AMA       ")
                .aisle("           ", "           ", "FAAACXXXXXC", "P###P      ", "P###P      ", " AAA       ")
                .aisle("F          ", "F          ", "FFFFFCXXXCF", " AAAF     F", " AAA       ", "           ")
                .aisle("    F     F", "    F     F", "     FCSCF ", "     F   F ", "           ", "           ")
                .where('S', this.selfPredicate())
                .where('C', states(getFirstCasingState()))
                .where('X', states(getSecondCasingState()))
                .where('A', states(getThirdCasingState())
                        .setMinGlobalLimited(25)
                        .or(autoAbilities(true, true, true, true, true, false, false)))
                .where('G', states(getFourthCasingState()))
                .where('P', states(getBoilerCasingState()))
                .where('F', states(getFrameState()))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
                .where('#', air())
                .build();
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


    public EntityDrone getDrone() {
        return this.drone;
    }

    public void setDrone(EntityDrone drone) {
        this.drone = drone;
    }

    public void setDroneDead(boolean setReachedSky) {
        if (this.drone != null) {
            this.drone.setDead();
            this.drone = null;
        }
        this.droneReachedSky = setReachedSky;
    }

    public boolean hasDrone() {
        if (getDrone() != null && !getDrone().isDead) {
            for (EntityDrone entity : this.getWorld().getEntitiesWithinAABB(EntityDrone.class, this.landingAreaBB)) {
                if (entity == getDrone()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void spawnDroneEntity(boolean descending) {

        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", "drtech:drone");
        Vec3d pos = this.getDroneSpawnPosition(descending);

        EntityDrone drone = ((EntityDrone) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, this.getWorld(), pos.x, pos.y, pos.z, true));

        if (drone != null) {
            setDrone(drone.withPadPos(getPos()));
        }

        if (getDrone() != null) {
            getDrone().setRotationFromFacing(this.getFrontFacing());
            if (descending) {
                getDrone().setDescendingMode();
                getDrone().setPadAltitude(this.getPos().getY());
            }
        }
    }

    public Vec3d getDroneSpawnPosition(boolean descending) {

        double altitude = descending ? 296.D : this.getPos().getY() + 1.;

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

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.setStructureAABB();
    }

    public void setStructureAABB() {

        double x = this.getPos().getX();
        double y = this.getPos().getY();
        double z = this.getPos().getZ();

        switch (this.getFrontFacing()) {

            case EAST -> {
                this.landingAreaBB = new AxisAlignedBB(x - 1, y + 1, z + 1, x - 3, y + 2, z - 1);
            }
            case SOUTH -> {
                this.landingAreaBB = new AxisAlignedBB(x - 1, y + 1, z - 1, x + 1, y + 2, z - 3);
            }
            case WEST -> {
                this.landingAreaBB = new AxisAlignedBB(x + 1, y + 1, z - 1, x + 3, y + 2, z + 1);
            }
            default -> {
                this.landingAreaBB = new AxisAlignedBB(x + 1, y + 1, z + 1, x - 1, y + 2, z + 3);
            }
        }

    }

    public boolean checkRecipe(@NotNull Recipe recipe) {
        for (int dimension : recipe.getProperty(DroneDimensionProperty.getInstance(), IntLists.EMPTY_LIST)) {
            if (dimension == this.getWorld().provider.getDimension()) {
                return true;
            }
        }
        return false;
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

    public static class DronePadWorkable extends MultiblockRecipeLogic {

        public DronePadWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @NotNull
        @Override
        public MetaTileEntityDronePad getMetaTileEntity() {
            return (MetaTileEntityDronePad) super.getMetaTileEntity();
        }

        @Override
        public boolean isAllowOverclocking() {
            return false;
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return ((MetaTileEntityDronePad) metaTileEntity).checkRecipe(recipe) && super.checkRecipe(recipe);
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            super.setupRecipe(recipe);
            this.getMetaTileEntity().spawnDroneEntity(false);
        }

        @Override
        protected void updateRecipeProgress() {
            super.updateRecipeProgress();

            if (!this.getMetaTileEntity().droneReachedSky && this.getMetaTileEntity().getDrone() != null && this.getMetaTileEntity().getDrone().reachedSky()) {
                this.getMetaTileEntity().setDroneDead(true);
            }

            if (maxProgressTime - progressTime == 240 && this.getMetaTileEntity().droneReachedSky) {
                this.getMetaTileEntity().spawnDroneEntity(true);
            }
        }

        @Override
        protected void completeRecipe() {
            if (this.getMetaTileEntity().hasDrone()) {
                super.completeRecipe();
            } else {
                this.progressTime = 0;
                this.setMaxProgress(0);
                this.recipeEUt = 0;
                this.fluidOutputs = null;
                this.itemOutputs = null;
                this.hasNotEnoughEnergy = false;
                this.wasActiveAndNeedsUpdate = true;
                this.parallelRecipesPerformed = 0;
                this.overclockResults = new int[]{0, 0};
            }
            this.getMetaTileEntity().setDroneDead(false);
        }
    }
}