package com.brachy84.mechtech.client.render;

import gregtech.api.util.random.XoShiRo256PlusPlusRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Lightning extends Particle {

    protected Vec3d target;

    private int minSteps = 3, maxSteps = 6;
    private int STEPS;
    private static final int BRIGHTNESS = 15 << 4;

    private float scale = 1f;
    private int color1 = 0xFFFFFFFF, color2 = 0xFFFFFFFF;

    private double[][] precomputedSteps;
    private final double[] vertices = new double[3];
    private final double[] verticesWithUV = new double[3];
    private boolean hasData = false;

    public Lightning(World worldIn, double posXIn, double posYIn, double posZIn, Vec3d target) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.target = target;
        rand = new XoShiRo256PlusPlusRandom();
        setMaxAge(2);
        particleGravity = 0;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.particleScale = rand.nextFloat() * 0.2f + 0.9f;
        canCollide = false;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.particleAlpha = 0.7f;
    }

    public Lightning(World worldIn, Entity source, Entity target) {
        this(worldIn, source.posX, source.posY, source.posZ, new Vec3d(target.posX, target.posY, target.posZ));
    }

    public Lightning(World worldIn, Vec3d source, Vec3d target) {
        this(worldIn, source.x, source.y, source.z, target);
    }

    public Lightning(World worldIn, double posXIn, double posYIn, double posZIn, Entity target) {
        this(worldIn, posXIn, posYIn, posZIn, new Vec3d(target.posX, target.posY, target.posZ));
    }

    public Lightning(World worldIn, double posXIn, double posYIn, double posZIn, BlockPos target) {
        this(worldIn, posXIn, posYIn, posZIn, new Vec3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5));
    }

    public Lightning setSteps(int min, int max) {
        this.minSteps = min;
        this.maxSteps = max;
        return this;
    }

    public Lightning setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public Lightning setColor(int color1, int color2) {
        this.color1 = color1;
        this.color2 = color2;
        return this;
    }

    public Lightning setup() {
        STEPS = rand.nextInt(maxSteps - minSteps) + minSteps + 1;
        this.precomputedSteps = new double[STEPS][3];
        regen();
        return this;
    }

    protected void regen() {
        int l = STEPS - 1;

        double lastX = posX;
        double lastY = posY;
        double lastZ = posZ;

        // total distance
        double dx = target.x - posX, dy = target.y - posY, dz = target.z - posZ;
        // average distance per section
        double avrX = dx / l, avrY = dy / l, avrZ = dz / l;

        for (int i = 0; i < l - 1; i++) {
            // average distance per section + random
            double x = avrX + (avrX * 1.4 * (rand.nextDouble() - 0.5));
            double y = avrY + (avrY * 1.4 * (rand.nextDouble() - 0.5));
            double z = avrZ + (avrZ * 1.4 * (rand.nextDouble() - 0.5));
            precomputedSteps[i][0] = x;
            precomputedSteps[i][1] = y;
            precomputedSteps[i][2] = z;
            lastX += x;
            lastY += y;
            lastZ += z;
        }

        // last section point directly at target + small random
        precomputedSteps[l - 1][0] = target.x - lastX + (avrX * 0.6 * (rand.nextDouble() - 0.5));
        precomputedSteps[l - 1][1] = target.y - lastY + (avrY * 0.6 * (rand.nextDouble() - 0.5));
        precomputedSteps[l - 1][2] = target.z - lastZ + (avrZ * 0.6 * (rand.nextDouble() - 0.5));

        // the last one doesn't seem to do anything
        precomputedSteps[l][0] = 1;
        precomputedSteps[l][1] = 1;
        precomputedSteps[l][2] = 1;
    }

    protected int getSteps() {
        return STEPS;
    }

    @Override
    public void onUpdate() {
        if (precomputedSteps == null)
            setup();
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }

    @Override
    public void renderParticle(BufferBuilder tess, Entity entityIn, float l, final float rX, final float rY, final float rZ, final float rYZ, final float rXY) {
        float red;
        float green;
        float blue;
        float alpha;

        double f6 = this.particleTextureIndexX / 16.0;
        final double f7 = f6 + 0.0324375F;
        double f8 = this.particleTextureIndexY / 16.0;
        final double f9 = f8 + 0.0324375F;

        f6 = f7;
        f8 = f9;

        double scale;

        final double[] a = new double[3];
        final double[] b = new double[3];

        double ox = 0;
        double oy = 0;
        double oz = 0;

        final EntityPlayer p = Minecraft.getMinecraft().player;
        double offX = -rZ;
        double offY = MathHelper.cos((float) (Math.PI / 2.0f + p.rotationPitch * 0.017453292F));
        double offZ = rX;

        for (int layer = 0; layer < 2; layer++) {
            if (layer == 0) {
                scale = 0.04;
                offX *= 0.001;
                offY *= 0.001;
                offZ *= 0.001;
                red = ((color1 >> 16) & 0xFF) / 255f;
                green = ((color1 >> 8) & 0xFF) / 255f;
                blue = ((color1) & 0xFF) / 255f;
                alpha = ((color1 >> 24) & 0xFF) / 255f;
            } else {
                offX = 0;
                offY = 0;
                offZ = 0;
                scale = 0.02;
                red = ((color2 >> 16) & 0xFF) / 255f;
                green = ((color2 >> 8) & 0xFF) / 255f;
                blue = ((color2) & 0xFF) / 255f;
                alpha = ((color2 >> 24) & 0xFF) / 255f;
            }

            scale *= particleScale * this.scale;

            for (int cycle = 0; cycle < 3; cycle++) {
                this.clear();

                double x = (this.prevPosX - interpPosX) - offX;
                double y = (this.prevPosY - interpPosY) - offY;
                double z = (this.prevPosZ - interpPosZ) - offZ;

                for (int s = 0; s < STEPS; s++) {
                    final double xN = x + this.precomputedSteps[s][0];
                    final double yN = y + this.precomputedSteps[s][1];
                    final double zN = z + this.precomputedSteps[s][2];

                    final double xD = xN - x;
                    final double yD = yN - y;
                    final double zD = zN - z;

                    if (cycle == 0) {
                        ox = -zD;
                        oy = 0;
                        oz = xD;
                    }
                    if (cycle == 1) {
                        ox = yD;
                        oy = -xD;
                        oz = 0;
                    }
                    if (cycle == 2) {
                        ox = 0;
                        oy = zD;
                        oz = -yD;
                    }

                    final double ss = Math
                            .sqrt(ox * ox + oy * oy + oz * oz) / ((((double) STEPS - (double) s) / STEPS) * scale);
                    ox /= ss;
                    oy /= ss;
                    oz /= ss;

                    a[0] = x + ox;
                    a[1] = y + oy;
                    a[2] = z + oz;

                    b[0] = x;
                    b[1] = y;
                    b[2] = z;

                    this.draw(red, green, blue, alpha, tess, a, b, f6, f8);

                    x = xN;
                    y = yN;
                    z = zN;
                }
            }
        }
    }

    private void clear() {
        this.hasData = false;
    }

    private void draw(float red, float green, float blue, float alpha, final BufferBuilder tess, final double[] a, final double[] b, final double f6, final double f8) {
        if (this.hasData) {
            tess.pos(a[0], a[1], a[2])
                    .tex(f6, f8)
                    .color(red, green, blue, alpha)
                    .lightmap(BRIGHTNESS, BRIGHTNESS)
                    .endVertex();
            tess.pos(this.vertices[0], this.vertices[1], this.vertices[2])
                    .tex(f6, f8)
                    .color(red, green, blue, alpha)
                    .lightmap(BRIGHTNESS, BRIGHTNESS)
                    .endVertex();
            tess.pos(this.verticesWithUV[0], this.verticesWithUV[1], this.verticesWithUV[2])
                    .tex(f6, f8)
                    .color(red, green, blue, alpha)
                    .lightmap(BRIGHTNESS, BRIGHTNESS)
                    .endVertex();
            tess.pos(b[0], b[1], b[2])
                    .tex(f6, f8)
                    .color(red, green, blue, alpha)
                    .lightmap(BRIGHTNESS, BRIGHTNESS)
                    .endVertex();
        }
        this.hasData = true;
        for (int x = 0; x < 3; x++) {
            this.vertices[x] = a[x];
            this.verticesWithUV[x] = b[x];
        }
    }

    protected double[][] getPrecomputedSteps() {
        return this.precomputedSteps;
    }
}
