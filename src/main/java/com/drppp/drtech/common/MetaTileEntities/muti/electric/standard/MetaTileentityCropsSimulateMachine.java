package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static keqing.gtqtcore.common.block.blocks.BlockMultiblockGlass1.CasingType.SILICATE_GLASS;

public class MetaTileentityCropsSimulateMachine extends MetaTileEntityBaseWithControl {
    private ItemStack seed = new ItemStack(Items.AIR);
    private int seedCout = 0;

    public MetaTileentityCropsSimulateMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maxProcess=100;
        this.process=0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileentityCropsSimulateMachine(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AASAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "AAAAA")
                .where('S', selfPredicate())
                .where('B', states(getGlassesState()))
                .where('X',  blocks(Blocks.FARMLAND))
                .where('A',
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)
                                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN)))
                )
                .where('#', any())
                .build();
    }
    protected IBlockState getGlassesState() {
        return GTQTMetaBlocks.blockMultiblockGlass1.getState(SILICATE_GLASS);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }
    @Override
    public boolean usesMui2() {
        return true;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.crops.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crops.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.crops.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crops.tooltip.4"));
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(this.isWorkingEnabled(), this.isActive()).setWorkingStatusKeys("gregtech.multiblock.idling", "gregtech.multiblock.work_paused", "gregtech.multiblock.running").addEnergyUsageLine(this.energyContainer).addCustom((keyManager, syncer) -> {
            if (this.isStructureFormed()) {
                    var seed = syncer.syncItemStack(this.seed);
                    var seedCout = syncer.syncInt(this.seedCout);
                    IKey seedInfo = KeyUtil.string(TextFormatting.BLUE, seed.getDisplayName()+"*" +seedCout );
                    keyManager.add(KeyUtil.lang("drtech.multiblock.crops.seed", seedInfo));
            }
        }).addProgressLine(getProgress(), getMaxProgress()).addWorkingStatusLine();
    }

    @Override
    protected void updateFormedValid() {
        if(!getWorld().isRemote )
        {
            if(this.isWorkingEnabled())
            {
                if(this.process++>=getMaxProgress() )
                {
                    this.process=0;
                    if(this.inputInventory!=null && this.inputInventory.getSlots()>0)
                    {
                        seedCout=0;
                        for (int i = 0; i < this.inputInventory.getSlots(); i++) {
                            var is = this.inputInventory.getStackInSlot(i);
                            if(is!=null && DrtechUtils.ItemCrops.containsKey(is.getItem()))
                            {
                                this.seed = is.copy();
                                this.seedCout += is.getCount();
                            }
                        }
                    }
                    //out
                    var crop = DrtechUtils.ItemCrops.get(this.seed.getItem());;
                    NonNullList outlist = NonNullList.create();
                    if(crop!=null)
                    {
                        for (int i = 0; i < this.seedCout; i++) {
                            BlockCrops block_crop = (BlockCrops)crop.getBlock();
                            block_crop.getDrops(outlist,getWorld(),getPos(),crop,4);
                        }
                        GTTransferUtils.addItemsToItemHandler(this.outputInventory,false,outlist);
                    }
                }
            }
        }
    }
}
