package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import com.drppp.drtech.common.MetaTileEntities.single.hu.LiquidBurringInfo;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.unification.material.Materials.Oxygen;
import static keqing.gtqtcore.common.block.blocks.BlockMultiblockGlass1.CasingType.FORCE_FIELD_CONSTRAINED_GLASS;

public class CommonBurnGenerator extends MetaTileEntityBaseWithControl {
    private long OutEut=0;
    private double brate=0;
    public CommonBurnGenerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maxProcess=20;
        this.process=0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new CommonBurnGenerator(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAA", "AFFFA", "AFFFA", "AAAAA")
                .aisle("AAAAA", "ACCCA", "ADDDA", "AEEEA")
                .aisle("AAAAA", "ACCCA", "ADDDA", "AEEEA")
                .aisle("AAAAA", "ACCCA", "ADDDA", "AEEEA")
                .aisle("AAAAA", "ACCCA", "ADDDA", "AEEEA")
                .aisle("AAAAA", "ACCCA", "ADDDA", "AEEEA")
                .aisle("AASAA", "AAAAA", "AAAAA", "AAAAA")
                .where('S', selfPredicate())
                .where('C', states(getCasingStateC()))
                .where('D', states(getCasingStateD()))
                .where('E', states(getGlassesStateE()))
                .where('A',
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)
                                .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE)))
                )
                .where('F', abilities(MultiblockAbility.MUFFLER_HATCH))
                .build();
    }

    protected IBlockState getCasingStateC() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX);
    }

    protected IBlockState getCasingStateD() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE);
    }
    protected IBlockState getGlassesStateE() {
        return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        energyContainer.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer = new EnergyContainerList(energyContainer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.cbg.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.cbg.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.cbg.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.cbg.tooltip.4"));
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(this.isWorkingEnabled(), this.isActive()).setWorkingStatusKeys("gregtech.multiblock.idling", "gregtech.multiblock.work_paused", "gregtech.multiblock.running").addEnergyUsageLine(this.energyContainer).addCustom((keyManager, syncer) -> {
            if (this.isStructureFormed()) {
                    long out = syncer.syncLong(this.OutEut);
                    IKey amountInfo = KeyUtil.number(TextFormatting.BLUE, out, " EU/t");
                    keyManager.add(KeyUtil.lang("drtech.multiblock.cbg.out", amountInfo));
                    double rate = syncer.syncDouble(brate);
                    IKey rateInfo = KeyUtil.string(TextFormatting.BLUE, String.valueOf(rate));
                    keyManager.add(KeyUtil.lang("drtech.multiblock.cbg.rate", rateInfo));
            }
        }).addProgressLine(getProgress(), getMaxProgress()).addWorkingStatusLine();
    }

    private void clearInputTanks()
    {
        for (var slot : this.inputFluidInventory.getFluidTanks()) {
            if (slot.getFluidAmount() > 0) {
                slot.drain(slot.getFluid(),true);
            }
        }
    }
    private boolean hasBurnFluid()
    {
        if(this.inputFluidInventory==null)
            return false;
        for (var slot : this.inputFluidInventory.getFluidTanks())
        {
            if(LiquidBurringInfo.ContainsFuel(slot.getFluid()))
            {
               return true;
            } else if (LiquidBurringInfo.ContainsRocketFuel(slot.getFluid())) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void updateFormedValid() {
        if(!getWorld().isRemote)
        {
            if( !isActive() && hasBurnFluid())
            {
                setActive(true);
            }
            else if(this.process==0 && !hasBurnFluid())
            {
                setActive(false);
            }
            if(this.isWorkingEnabled() && isActive())
            {
                if(this.process++>=getMaxProgress())
                {
                    this.process=0;
                    long fuel=0;
                    long fuel_eut=0;
                    long rocket_fuel=0;
                    long rocket_fuel_eut=0;
                    long oxygen=0;
                    this.OutEut=0;
                    //天才第一步 获取输入仓所有的可燃流体 和助燃剂
                    if(this.inputFluidInventory==null)
                        return;
                    for (var slot : this.inputFluidInventory.getFluidTanks())
                    {
                        if(slot.getFluidAmount()>0)
                        {
                            if(LiquidBurringInfo.ContainsFuel(slot.getFluid()))
                            {
                                fuel += slot.getFluidAmount();
                                fuel_eut += (long) LiquidBurringInfo.getMlHu(slot.getFluid()) * slot.getFluidAmount();
                            } else if (LiquidBurringInfo.ContainsRocketFuel(slot.getFluid())) {
                                rocket_fuel += slot.getFluidAmount();
                                rocket_fuel_eut += (long) LiquidBurringInfo.getRocketMlHu(slot.getFluid()) * slot.getFluidAmount();
                            }else if(slot.getFluid().isFluidEqual(Oxygen.getFluid(FluidStorageKeys.LIQUID, 1000)))
                            {
                                oxygen += slot.getFluidAmount();
                            }
                        }
                    }
                    //第三步计算 流体助燃剂比例
                    if(oxygen!=0)
                    {

                        double rate = (double) (fuel + rocket_fuel) /(oxygen);
                        brate=rate;
                        var fuel_rate = 1.6f* Math.pow(Math.E,-0.04*rate);
                        var rocket_fuel_rate = 1.7f* Math.pow(Math.E,-0.005*rate);
                        //第四步 计算每Tick产出EU 扣除所有流体开始产出
                        this.OutEut = (long)((double) fuel_eut / 20 * fuel_rate) + (long)((double) rocket_fuel_eut / 20 * rocket_fuel_rate);
                        clearInputTanks();
                    }
                    else
                    {
                        this.OutEut =0;
                    }
                }
                addEnergy(OutEut);
            }
        }
    }
}
