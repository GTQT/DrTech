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
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static gregtech.common.items.behaviors.AbstractMaterialPartBehavior.getPartMaterial;
import static net.minecraft.entity.EntityList.createEntityFromNBT;
import static net.minecraft.world.chunk.storage.AnvilChunkLoader.spawnEntity;

public class MeTaTileEntityWindDrivenGenerator extends MetaTileEntityBaseWithControl {
    public EntityWindRotor rotor = null;

    public int outEnergyRate=0;
    public int[] outEnergyBase= {60,180,360};
    private int tick=0;
    private int level;
    public MeTaTileEntityWindDrivenGenerator(ResourceLocation metaTileEntityId,int level) {
        super(metaTileEntityId);
        this.level = level;
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
        return new MeTaTileEntityWindDrivenGenerator(this.metaTileEntityId,this.level);
    }
    public Vec3d getDroneSpawnPosition( ) {

        double altitude =  this.getPos().getY() + 3;

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

       // rotor = (EntityWindRotor) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, this.getWorld(), pos.x, pos.y, pos.z, true);
        rotor = (EntityWindRotor) createEntityFromNBT(nbttagcompound,this.getWorld());
        rotor.setPosition(pos.x,pos.y,pos.z);
        rotor.rotationYaw=180;
        spawnEntity(rotor,this.getWorld());
        rotor.machinePos = new BlockPos(this.getPos().getX(),this.getPos().getY(),this.getPos().getZ());
    }
    @Override
    protected void updateFormedValid() {
        if(!this.isActive())
            setActive(true);
        if(!this.getWorld().isRemote)
        {
            tick++;
            outEnergyRate=0;
            if(this.rotor==null && this.isActive() && this.isWorkingEnabled())
            {
                spawnDroneEntity();
            }
            else if(this.rotor!=null && !this.isWorkingEnabled() )
            {
                this.rotor.setDead();
                this.rotor=null;

            }
            for (int i = 0; i < this.getInputInventory().getSlots(); i++) {
                ItemStack stack = this.getInputInventory().getStackInSlot(i);
                //需要根据转子的材料重量 和 转子效率 转子产能  和当前高度获取一个产出计算
                //材料重量按照 150为基准 越重效率越低 最低为0.66倍率  最高为1.5倍
                //根据高度最高倍率1.5
                //HV EV IV三个等级的风力发电
                //基础值 320 1200 4096
                //下雨影响产出倍率 下雨1.2 雷暴1.5
                if(stack.getItem()== MetaItems.TURBINE_ROTOR.getMetaItem() && stack.getMetadata()==MetaItems.TURBINE_ROTOR.getMetaValue())
                {
                    Material material = getPartMaterial(stack);
                    float weight=1;
                    if(material.getMass()<=150)
                    {
                        weight = (150f - (float)material.getMass())/(float)material.getMass();
                        weight = Math.min(1.5f,weight);
                    }else{
                        weight = ((float)material.getMass()-150f)/(float)material.getMass();
                        weight = Math.max(0.666f,weight);
                    }
                    float height = Math.min(1.5f,Math.max(1f,this.getPos().getY()*0.01f));
                    float eff = TurbineRotorBehavior.getRotorEfficiency(stack)*0.01f;
                    float pow = TurbineRotorBehavior.getRotorPower(stack)*0.01f;
                    this.outEnergyRate=(int)(outEnergyBase[level-1]*weight*height*eff*pow);
                    if(tick>=25){
                        tick=0;
                        new TurbineRotorBehavior().applyRotorDamage(stack,1);
                    }
                    break;
                }

            }

            if(this.getWorld().isRaining() && !this.getWorld().isThundering())
            {
                this.outEnergyRate *=1.2;
            }else if(this.getWorld().isThundering())
            {
                this.outEnergyRate *=1.5;
            }

            this.getOutEnergyContainer().addEnergy(outEnergyRate);
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentString("发电:"+this.outEnergyRate+"EU/T"));
    }
}
