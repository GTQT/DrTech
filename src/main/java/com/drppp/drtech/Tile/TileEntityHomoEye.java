package com.drppp.drtech.Tile;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

public class TileEntityHomoEye extends TileEntity implements ITickable {
    private static final double EOH_STAR_FIELD_RADIUS = 13;

    // Prevent culling when block is out of frame so model can remain active.
    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        // Assuming your block is at (x, y, z)
        double x = this.getPos().getX();
        double y = this.getPos().getY();
        double z = this.getPos().getZ();

        // Create a bounding box that extends 'size' blocks in all directions from the block.
        return new AxisAlignedBB(
                x - EOH_STAR_FIELD_RADIUS,
                y - EOH_STAR_FIELD_RADIUS,
                z - EOH_STAR_FIELD_RADIUS,
                x + EOH_STAR_FIELD_RADIUS + 1,
                y + EOH_STAR_FIELD_RADIUS + 1,
                z + EOH_STAR_FIELD_RADIUS + 1);
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    private float size = 10;
    private float rotationSpeed = 10;

    // Fun fact, these methods were entirely written by ChatGPT3... Take that as you will.
    public static <T> ArrayList<T> selectNRandomElements(Collection<T> inputList, long n) {
        ArrayList<T> randomElements = new ArrayList<>(8);
        ArrayList<T> inputArray = new ArrayList<>(inputList);
       for (var s:inputArray)
       {
           randomElements.add(s);
       }
        return randomElements;
    }

    public static float generateRandomFloat(float a, float b) {
        Random rand = new Random();
        return rand.nextFloat() * (b - a) + a;
    }

    public long getTier() {
        return tier;
    }

    public void setTier(long tier) {
        this.tier = tier;
    }

    private long tier = 9;

    public float getSize() {
        return size;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    @Override
    public void update() {
        angle += 10.0f;
    }


    public static class OrbitingObject {

        public OrbitingObject(Block block, float distance, float rotationSpeed, float orbitSpeed, float xAngle,
                              float zAngle, float scale) {
            this.block = block;
            this.distance = distance;
            this.rotationSpeed = rotationSpeed;
            this.orbitSpeed = orbitSpeed;
            this.xAngle = xAngle;
            this.zAngle = zAngle;
            this.scale = scale;
        }

        public final Block block;
        public final float distance;
        public final float rotationSpeed;
        public final float orbitSpeed;
        public final float xAngle;
        public final float zAngle;
        public final float scale;
    }

    public ArrayList<OrbitingObject> getOrbitingObjects() {
        return orbitingObjects;
    }

    private final ArrayList<OrbitingObject> orbitingObjects = new ArrayList<>();
    private static final Map<String, Block> BLOCKS = new HashMap<>();

    static {
        BLOCKS.put("dim1", Blocks.GRASS);
        BLOCKS.put("dim2", Blocks.GRASS);
        BLOCKS.put("dim3", Blocks.GRASS);
        BLOCKS.put("dim4", Blocks.GRASS);
        BLOCKS.put("dim5", Blocks.GRASS);
        BLOCKS.put("dim6", Blocks.GRASS);
        BLOCKS.put("dim7", Blocks.GRASS);
        BLOCKS.put("dim8", Blocks.GRASS);
    }

    private static final float MAX_ANGLE = 30;

    // This must be set last.
    public void generateImportantInfo() {

        int index = 0;
        for (Block block : selectNRandomElements(BLOCKS.values(), tier + 1)) {

            float xAngle = generateRandomFloat(-MAX_ANGLE, MAX_ANGLE);
            float zAngle = generateRandomFloat(-MAX_ANGLE, MAX_ANGLE);
            index += 1.0;
            float distance = index + generateRandomFloat(-0.2f, 0.2f);
            float scale = generateRandomFloat(0.2f, 0.9f);
            float rotationSpeed = generateRandomFloat(0.5f, 1.5f);
            float orbitSpeed = generateRandomFloat(0.5f, 1.5f);
            orbitingObjects.add(new OrbitingObject(block, distance, rotationSpeed, orbitSpeed, xAngle, zAngle, scale));
        }
    }

    // Used to track the rotation of the star/planets.
    public float angle;

    private static final String EOH_NBT_TAG = "EOH:";
    private static final String ROTATION_SPEED_NBT_TAG = EOH_NBT_TAG + "rotationSpeed";
    private static final String SIZE_NBT_TAG = EOH_NBT_TAG + "size";
    private static final String TIER_NBT_TAG = EOH_NBT_TAG + "tier";

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        // Save other stats.
        compound.setFloat(ROTATION_SPEED_NBT_TAG, rotationSpeed);
        compound.setFloat(SIZE_NBT_TAG, size);
        compound.setLong(TIER_NBT_TAG, tier);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        // Load other stats.
        rotationSpeed = compound.getFloat(ROTATION_SPEED_NBT_TAG);
        size = compound.getFloat(SIZE_NBT_TAG);
        tier = compound.getLong(TIER_NBT_TAG);
    }
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return super.getUpdatePacket();
    }

}
