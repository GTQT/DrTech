package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.loaders.recipes.DrtechReceipes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySolarTower extends RecipeMapMultiblockController {
    public static int max_heat = 100000;
    public int heat = 0;
    public int tick = 0;
    public int eff = 1;
    private int tire = 0;

    public MetaTileEntitySolarTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DrtechReceipes.SOLAR_TOWER);
        this.recipeMapWorkable = new SolarTowerRecipeLogic(this);
    }
    @Override
    public boolean usesMui2() {
        return false;
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("####TTT####", "###TTTTT###", "##TTTTTTT##", "#TTTTTTTTT#", "TTTTTTTTTTT", "TTTTTTTTTTT", "TTTTTTTTTTT", "#TTTTTTTTT#", "##TTTTTTT##", "###TTTTT###", "####TTT####")
                .aisle("####TTT####", "###TTTTT###", "##TTTTTTT##", "#TTTTTTTTT#", "TTTTYYYTTTT", "TTTTYYYTTTT", "TTTTYYYTTTT", "#TTTTTTTTT#", "##TTTTTTT##", "###TTTTT###", "####TTT####")
                .aisle("###########", "#####T#####", "####TTT####", "###TTTTT###", "##TTYYYTT##", "#TTTYYYTTT#", "##TTYYYTT##", "###TTTTT###", "####TTT####", "#####T#####", "###########")
                .aisle("###########", "#####T#####", "####TTT####", "###TTTTT###", "##TTYYYTT##", "#TTTYYYTTT#", "##TTYYYTT##", "###TTTTT###", "####TTT####", "#####T#####", "###########")
                .aisle("###########", "###########", "#####T#####", "####TTT####", "###TYYYT###", "##TTYYYTT##", "###TYYYT###", "####TTT####", "#####T#####", "###########", "###########")
                .aisle("###########", "###########", "#####T#####", "####TTT####", "###TTTTT###", "##TTTYTTT##", "###TTTTT###", "####TTT####", "#####T#####", "###########", "###########")
                .aisle("###########", "###########", "###########", "###########", "#####G#####", "####GYG####", "#####G#####", "###########", "###########", "###########", "###########").setRepeatable(15)
                .aisle("###########", "###########", "###########", "#####R#####", "####RRR####", "###RRYRR###", "####RRR####", "#####R#####", "###########", "###########", "###########").setRepeatable(5)
                .aisle("###########", "###########", "###########", "###########", "#####Y#####", "####YYY####", "#####Y#####", "###########", "###########", "###########", "###########")
                .aisle("###########", "###########", "###########", "###########", "###########", "#####S#####", "###########", "###########", "###########", "###########", "###########")
                .where('S', selfPredicate())
                .where('#', any())
                .where('T', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.SOLAR_TOWER_CASING)).setMinGlobalLimited(210)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setExactLimit(1))

                )
                .where('Y', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.SALT_INHIBITION_CASING)))
                .where('G', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.HEAT_CUT_OFF_CASING)))
                .where('R', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.HEAT_INHIBITION_CASING)))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SALT_INHIBITION_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySolarTower(this.metaTileEntityId);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Eff", this.eff);
        data.setInteger("Heat", this.heat);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.heat = data.getInteger("Heat");
        this.eff = data.getInteger("Eff");
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        BlockPos pos = this.getPos().add(0, -27, 0);
        getTire(pos);
    }

    private void getTire(BlockPos pos) {
        int[][] block = new int[31][31];
        for (int i = pos.getX() - 15, ii = 0; i <= pos.getX() + 15; i++, ii++) {
            for (int j = pos.getZ() - 15, jj = 0; j <= pos.getZ() + 15; j++, jj++) {
                IBlockState state = getWorld().getBlockState(new BlockPos(i, pos.getY(), j));
                if (state == BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.SOLAR_REFLECTION_CASING))
                    block[ii][jj] = 1;
                else
                    block[ii][jj] = 0;
            }
        }
        this.tire = DrtechUtils.getSolarTire(block);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.solarpower.tip.1"));
        tooltip.add(I18n.format("drtech.machine.solarpower.tip.2"));
        tooltip.add(I18n.format("drtech.machine.solarpower.tip.3"));
        tooltip.add(I18n.format("drtech.machine.solarpower.tip.4"));
        tooltip.add(I18n.format("drtech.machine.solarpower.tip.5"));
        tooltip.add(I18n.format("drtech.machine.solarpower.tip.6"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("drtech.machine.solarpower.tire", this.tire));
        textList.add(new TextComponentTranslation("drtech.machine.solarpower.eff", this.eff));
        textList.add(new TextComponentTranslation("drtech.machine.solarpower.heat", this.heat));
    }

    private int getBuff() {
        switch (this.tire) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 4;
            case 4:
                return 8;
            case 5:
                return 16;
            default:
                return 0;
        }
    }

    private int getReflectConut() {
        switch (this.tire) {
            case 0:
                return 0;
            case 1:
                return 36;
            case 2:
                return 88;
            case 3:
                return 156;
            case 4:
                return 240;
            case 5:
                return 340;
            default:
                return 0;
        }
    }

    public boolean isDaytime(World world) {
        long timeOfDay = world.getWorldTime() % 24000;
        // 白天时间从0到12000刻
        return timeOfDay < 12000;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }


    protected class SolarTowerRecipeLogic extends MultiblockRecipeLogic {

        public SolarTowerRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public void update() {
            super.update();
            tick++;
            if (tick > 200) {
                eff += 1;
                eff = Math.min(eff, 100);
                if (isDaytime(getWorld())) {
                    double x = (7000 - Math.pow(Math.abs(heat - 5000), 0.8)) / 7000;
                    double h = getReflectConut() * x * (10 + Math.pow(2, tire - 1));
                    heat += (int) h;
                    heat = Math.min(heat, max_heat);
                } else {
                    heat -= 10;
                    heat = Math.max(heat, 0);
                }
                tick = 0;
            }
        }

        @Override
        protected void outputRecipeOutputs() {
            this.fluidOutputs.clear();
            List<FluidStack> fluidOutput = new ArrayList<>();
            if (eff == 100 && heat >= 30000) {
                for (int i = 0; i < this.getInputTank().getTanks(); i++) {
                    if (isFluidSalt(this.getInputTank().getTankAt(i).getFluid().getFluid())) {
                        int amount = Math.min(this.getInputTank().getTankAt(i).getFluidAmount(), heat);
                        this.getInputTank().getTankAt(i).drain(amount, true);
                        fluidOutput.add(new FluidStack(DrtechMaterials.HotSunSalt.getFluid(), amount));
                    }
                }
                GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, fluidOutput);
            }
        }

        public boolean isFluidSalt(Fluid fluid) {
            return DrtechMaterials.HotSunSalt.getFluid().equals(fluid);
        }
    }
}
