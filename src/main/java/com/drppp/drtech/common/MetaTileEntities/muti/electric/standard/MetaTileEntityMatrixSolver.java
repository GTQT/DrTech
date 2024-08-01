package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMatrixSolver extends MetaTileEntityBaseWithControl{
    int mode = 0;
    List<RecipeMap> recipemaps = new ArrayList<>();
    public MetaTileEntityMatrixSolver(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FXXXF", "FXXXF", "#####", "FXXXF", "#####", "XXXXX","XXXXX")
                .aisle("XXXXX", "XXXXX", "#####", "XXXXX", "#####", "X###X","X###X")
                .aisle("XXXXX", "XXXXX", "##G##", "XXGXX", "##G##", "X#G#X","X###X")
                .aisle("XXXXX", "XXXXX", "#####", "XXXXX", "#####", "X#S#X","X###X")
                .aisle("FXXXF", "FXXXF", "#####", "FXXXF", "#####", "XXXXX","XXXXX")
                .where('S', selfPredicate())
                .where('X',any() )
                .where('G',states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE)))
                .where('F', frames(Materials.StainlessSteel))
                .where('#', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                        )
                )
                .build();
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.LARGE_STEEL_BOILER;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMatrixSolver(this.metaTileEntityId);
    }
    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 18, 18, "", this::changeProductType)
                .setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipText("drtech.multiblock.lbh.changep"));
        return group;
    }
    private void changeProductType(Widget.ClickData clickData)
    {
        this.mode = (++this.mode)%3;
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if(this.mode==0)
            textList.add(new TextComponentString("工作方式:"+"扫描配方"));
        else if(this.mode==1)
            textList.add(new TextComponentString("工作方式:"+"不定元矩阵运算"));
        else if(this.mode==2)
            textList.add(new TextComponentString("工作方式:"+"执行配方"));
        textList.add(new TextComponentString("配方仓库:"+ (this.recipemaps.size()==0?"空":this.recipemaps.size()+"个")));
    }

    @Override
    protected void updateFormedValid() {
        if(!this.getWorld().isRemote && this.isWorkingEnabled())
        {
            if(this.inputInventory!=null && this.inputInventory.getSlots()>0)
            {
                if(mode==0)
                {
                    recipemaps = new ArrayList<>();
                    for (int i = 0; i < this.inputInventory.getSlots(); i++) {
                        ItemStack item = this.inputInventory.getStackInSlot(i).copy();
                        if(GTUtility.getMetaTileEntity(item) != null)
                        {
                            var machine = GTUtility.getMetaTileEntity(item);
                            if(machine.getRecipeMap() !=null)
                            {
                                recipemaps.add(machine.getRecipeMap());
                            }
                        }
                    }
                }else if(mode==1)
                {

                }
                else if(mode==2)
                {

                }

            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("mode",this.mode);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.mode = data.getInteger("mode");
    }
}
