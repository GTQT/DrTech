package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;
import com.drppp.drtech.api.recipes.properties.DroneDimensionProperty;
import com.drppp.drtech.common.Entity.EntityDrone;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.UUID;

import static com.drppp.drtech.loaders.DrtechReceipes.DRONE_PAD;

public class MetaTileEntityDronePad extends RecipeMapMultiblockController {

    private AxisAlignedBB landingAreaBB;
    private EntityDrone drone = null;
    private UUID droneUUID = null;
    public boolean droneReachedSky;

    public MetaTileEntityDronePad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DRONE_PAD);
        this.recipeMapWorkable = new DronePadWorkable(this);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC ", "     ", "     ")
                .aisle("CPPPC", " AAA ", " AAA ")
                .aisle("CPPPC", " AAA ", " AAA ")
                .aisle("CPPPC", " AAA ", " AAA ")
                .aisle(" CSC ", "     ", "     ")
                .where(' ', any())
                .where('A', air())
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState()).or(autoAbilities(true, false, true, true, false, false, false)))
                .where('P', states(this.getPadState()))
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    protected IBlockState getPadState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDronePad(this.metaTileEntityId);
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    public EntityDrone getDrone() {
        if (this.drone == null && this.droneUUID != null) {
            this.drone = findDrone(this.droneUUID);
        }
        return this.drone;
    }

    public void setDrone(EntityDrone drone) {
        this.drone = drone;
        this.droneUUID = drone.getUniqueID();
    }

    public void setDroneDead(boolean setReachedSky) {
        if (this.drone != null) {
            this.drone.setDead();
            this.drone = null;
            this.droneUUID = null;
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
        nbttagcompound.setString("id", "susy:drone");
        Vec3d pos = this.getDroneSpawnPosition(descending);

        setDrone((EntityDrone) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, this.getWorld(), pos.x, pos.y, pos.z, true));

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

    private EntityDrone findDrone(UUID droneUUID) {
        for (EntityDrone entity: this.getWorld().getEntitiesWithinAABB(EntityDrone.class, this.landingAreaBB.setMaxY(301))) {
            if (entity.getUniqueID().equals(droneUUID)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        if (this.droneUUID != null) {
            data.setUniqueId("droneUUID", this.droneUUID);
        }
        data.setBoolean("droneReachedSky", this.droneReachedSky);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasUniqueId("droneUUID")) {
            this.droneUUID = data.getUniqueId("droneUUID");
        }
        this.droneReachedSky = data.getBoolean("droneReachedSky");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.droneUUID != null) {
            buf.writeUniqueId(this.droneUUID);
        }
        buf.writeBoolean(this.droneReachedSky);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            this.droneUUID = buf.readUniqueId();
        } catch (Exception ignored) {}
        this.droneReachedSky = buf.readBoolean();
    }

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