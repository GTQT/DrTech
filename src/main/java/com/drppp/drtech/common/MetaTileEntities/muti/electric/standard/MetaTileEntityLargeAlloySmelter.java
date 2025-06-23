package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities.LARGE_ALLOY_SMELTER;
import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityLargeAlloySmelter extends RecipeMapMultiblockController {
    private int leve = 1;

    public MetaTileEntityLargeAlloySmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ALLOY_SMELTER_RECIPES);
        this.recipeMapWorkable = new SelfRecipeLogic(this, true);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle(" AAA ", " BBB ", " CCC ", " BBB ", " AAA ")
                .aisle("AAAAA", "B   B", "C   C", "B   B", "AAAAA")
                .aisle("AAAAA", "B   B", "C   C", "B   B", "AAMAA")
                .aisle("AAAAA", "B   B", "C   C", "B   B", "AAAAA")
                .aisle(" ASA ", " BBB ", " CCC ", " BBB ", " AAA ")
                .where('S', selfPredicate())
                .where('C', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('A', states(getCasingState()).setMinGlobalLimited(20)
                        .or(autoAbilities(true, true, true, true, false, false, false)))
                .where('B', heatingCoils())
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("#XEX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .aisle("XXXXX", "C###C", "G###G", "C###C", "XXXXX")
                .aisle("XXXXX", "C###C", "G###G", "C###C", "XXMXX")
                .aisle("XXXXH", "C###C", "G###G", "C###C", "XXXXX")
                .aisle("#ISO#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .where('S', LARGE_ALLOY_SMELTER, EnumFacing.SOUTH)
                .where('X', getCasingState())
                .where('G', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                .where('M', MetaTileEntities.MUFFLER_HATCH[GTValues.HV], EnumFacing.UP)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV], EnumFacing.NORTH)
                .where('H', MetaTileEntities.MAINTENANCE_HATCH, EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState());
        GregTechAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('C', entry.getKey()).build()));
        return shapeInfo;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeAlloySmelter(this.metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.large_alloy_smelter.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.large_alloy_smelter.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.large_alloy_smelter.tooltip.3"));
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc"));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof IHeatingCoilBlockStats) {
            this.leve = ((IHeatingCoilBlockStats) type).getLevel();
        } else {
            this.leve = 1;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("CoinLevel", this.leve);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.leve = data.getInteger("CoinLevel");
    }


    protected class SelfRecipeLogic extends MultiblockRecipeLogic {
        public SelfRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = (int) (maxProgress * 0.5);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if (GTValues.V[i] == this.getMaxVoltage())
                    tire = i;
            }
            return (tire + leve - 1) * 2;
        }
    }
}
