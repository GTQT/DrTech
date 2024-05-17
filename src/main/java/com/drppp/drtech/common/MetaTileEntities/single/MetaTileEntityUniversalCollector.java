package com.drppp.drtech.common.MetaTileEntities.single;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;

import java.util.ArrayList;

import static gregtech.api.GTValues.VA;

public class MetaTileEntityUniversalCollector extends TieredMetaTileEntity {
    protected final ICubeRenderer renderer;
    boolean isActive=true;
    boolean isWorkingEnabled=true;
    int tick=0;
    public MetaTileEntityUniversalCollector(ResourceLocation metaTileEntityId, int tier, ICubeRenderer renderer) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityUniversalCollector(this.metaTileEntityId,getTier(),renderer);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }
    @Override
    public void update() {
        super.update();
        if(energyContainer.getEnergyStored()>100)
        {
            isActive=true;
            isWorkingEnabled=isActive;
        }else
        {
            isActive=false;
            isWorkingEnabled=isActive;
        }
        if(!getWorld().isRemote&&isWorkingEnabled)
        {
            energyContainer.changeEnergy(-100);
            if(++tick>20)
            {
                tick=0;
                getRangeTileentities();
            }
        }
    }
    public boolean isAdjacentBlockAStorage( BlockPos pos) {
        // 检查所有六个方向（东、西、南、北、上、下）
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacentPos = pos.offset(facing);
            TileEntity tileEntity = this.getWorld().getTileEntity(adjacentPos);

            if (tileEntity instanceof IInventory) {
                return true; // 发现一个实现了IInventory接口的TileEntity，表明这是一个可存储物品的方块
            }
        }
        return false;
    }
    private void getRangeTileentities()
    {
        if(isAdjacentBlockAStorage(this.getPos()))
        {
            int range =(int) Math.pow((getTier()+1),2);
            BlockPos centerPos=this.getPos() ;
            int radius = range;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos currentPos = centerPos.add(x, y, z);
                        MetaTileEntity te = GTUtility.getMetaTileEntity(this.getWorld(),currentPos);
                        if(te != null)
                        {
                            for (EnumFacing facing : EnumFacing.values()) {
                                BlockPos adjacentPos = this.getPos().offset(facing);
                                TileEntity tileEntity = this.getWorld().getTileEntity(adjacentPos);
                                if (tileEntity instanceof IInventory) {
                                    GTTransferUtils.moveInventoryItems(te.getExportItems(),tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,null));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
