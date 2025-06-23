package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing1;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MetaTileEntityLargeLightningRod extends MetaTileEntityBaseWithControl {
    long MAX_ENERGY_STORE = 5000000000L;
    long energy_store = 0;
    public MetaTileEntityLargeLightningRod(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeLightningRod(this.metaTileEntityId);
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("             ", "             ", "             ", "             ", "             ", "             ", "      A      ", "             ", "             " )
                .aisle("             ", "             ", "             ", "             ", "             ", "             ", "      A      ", "             ", "             " )
                .aisle("             ", "             ", "             ", "             ", "             ", "             ", "     AAA     ", "             ", "             " )
                .aisle("      B      ", "             ", "             ", "             ", "             ", "             ", "     BBB     ", "             ", "             " )
                .aisle("     B B     ", "      B      ", "      C      ", "      C      ", "      C      ", "      C      ", "    BBBBB    ", "     BBB     ", "     CCC     " )
                .aisle("    B   B    ", "             ", "      A      ", "      A      ", "      A      ", "      A      ", "  ABBBBBBBA  ", "    BAAAB    ", "    C C C    " )
                .aisle("   B  D  B   ", "    B B B    ", "    CABAC    ", "    CABAC    ", "    CABAC    ", "    CABAC    ", "AAABBBBBBBAAA", "    BACAB    ", "    CCCCC    " )
                .aisle("    B   B    ", "             ", "      A      ", "      A      ", "      A      ", "      A      ", "  ABBBBBBBA  ", "    BAAAB    ", "    C C C    " )
                .aisle("     B B     ", "      B      ", "      C      ", "      C      ", "      C      ", "      C      ", "    BBBBB    ", "     BBB     ", "     CCC     " )
                .aisle("      B      ", "             ", "             ", "             ", "             ", "             ", "     BBB     ", "             ", "             " )
                .aisle("             ", "             ", "             ", "             ", "             ", "             ", "     AAA     ", "             ", "             " )
                .aisle("             ", "             ", "             ", "             ", "             ", "             ", "      A      ", "             ", "             " )
                .aisle("             ", "             ", "             ", "             ", "             ", "             ", "      A      ", "             ", "             " )
                .where('D', selfPredicate())
                .where('B', states(getCasingState())
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setExactLimit(1).setPreviewCount(1).or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        ))
                .where('A',frames(Materials.Iron))
                .where('C',blocks(Blocks.IRON_BARS))
                .where(' ', any())
                .build();
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.PALLADIUM_SUBSTATION_CASING;
    }
    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PALLADIUM_SUBSTATION);
    }
    @SideOnly(Side.CLIENT)
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return com.drppp.drtech.Client.Textures.LIGHTING_ROD_OVERLAY;
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                isWorkingEnabled());
    }
    @Override
    protected void updateFormedValid() {
        if(!getWorld().isRemote )
        {
            World aWorld = this.getWorld();
            Random aXSTR = new Random();
            if(outEnergyContainer.getEnergyStored()<outEnergyContainer.getEnergyCapacity())
            {
                long change = this.outEnergyContainer.addEnergy(Math.min(energy_store, GTValues.V[GTValues.ZPM])*512);
                this.energy_store -= change;
                this.energy_store = Math.max(this.energy_store,0);
            }
            if (this.energy_store > 0) {
                this.setActive(true);
                this.energy_store = Math.max(this.energy_store-this.energy_store/100 + 1,0l);
            } else {
                this.setActive(false);
            }
            if (getOffsetTimer() % 256 == 0 && (aWorld.isThundering() || (aWorld.isRaining() && aXSTR.nextInt(10) == 0))) {
                int aRodValue = 0;
                boolean isRodValid = true;
                int aX = this.getPos().getX();
                int aY = this.getPos().getY()+8;
                int aZ = this.getPos().getZ();
                for (int i = this.getPos().getY() + 9; i < aWorld.getHeight() - 1; i++) {
                    if (isRodValid && this.getWorld().getBlockState(new BlockPos(aX, i, aZ)).getBlock().equals(Blocks.IRON_BARS)) {
                        aRodValue++;
                    } else {
                        isRodValid = false;
                        if (this.getWorld().getBlockState(new BlockPos(aX, i, aZ)).getBlock() != Blocks.AIR) {
                            aRodValue = 0;
                            break;
                        }
                    }
                }
                if (!aWorld.isThundering() && ((aY + aRodValue) < 128)) aRodValue = 0;
                if (aXSTR.nextInt(4 * aWorld.getHeight()) < (aRodValue * (aY + aRodValue))) {
                    this.energy_store = MAX_ENERGY_STORE;
                    aWorld.addWeatherEffect(new EntityLightningBolt(aWorld, aX, aY + aRodValue, aZ, false));
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("EnergyStored",energy_store);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.energy_store = data.getLong("EnergyStored");
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentString("内部缓存:"+energy_store +"/"+MAX_ENERGY_STORE));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add("最大往动力舱输入速度为512A ZPM");
        tooltip.add("可缓存"+MAX_ENERGY_STORE+"EU");
        tooltip.add("每Tick会减少当前1%的电量");
        tooltip.add("雷雨天每256tick发电一次");
        tooltip.add("雨天每256tick十分之一概率发电一次");
    }
}
