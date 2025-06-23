package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.ItemHandler.FilterItemStackHandler;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import gregtech.api.capability.IWorkable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityTypeFilter extends MetaTileEntity {
    protected final ICubeRenderer renderer = Textures.FILTER_OVERLAY;
    Collection<OrePrefix> collection = OrePrefix.values();
    List<OrePrefix> orePrefixes = new ArrayList<>(collection);
    OrePrefix current = OrePrefix.ore;
    OrePrefix forifx;
    int forindex=0;
    int filter_index=0;
    public MetaTileEntityTypeFilter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.itemInventory = new ItemStackHandler(36);
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return gregtech.client.renderer.texture.Textures.VOLTAGE_CASINGS[1];
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline =  ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        this.getBaseRenderer().render(renderState, translation, colouredPipeline);
        this.renderOverlays(renderState, translation, pipeline);

    }

    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.render(renderState, translation, pipeline);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add("按照选中的类型进行过滤输入");
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(Textures.BACKGROUND, 198, 218);
        for (int i = 0; i < 36; i++) {
            builder.slot((IItemHandlerModifiable) itemInventory,i,5+i%9*18,3+i/9*18,true,true, GuiTextures.SLOT);
        }
        var scroll = new ScrollableListWidget(5,80,125,54);
        scroll.addWidget( new AdvancedTextWidget(0,0,this::addDisplayText,0x52135));



        builder.widget(scroll);

        builder.widget(new IncrementButtonWidget(128, 77, 30, 20, 1, 4, 16, 64, this::setCurrentParallel)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        builder.widget(new IncrementButtonWidget(159, 77, 30, 20, -1, -4, -16, -64, this::setCurrentParallel)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        builder.widget(new ImageWidget(130, 100, 53, 20, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(148, 105, 21, 20, this::getParallelAmountToString, val -> {
            if (val != null && !val.isEmpty() && val.matches("-?\\d+")) {
                setCurrentParallel(Integer.parseInt(val));
            }
        }));
        builder.widget(new AdvancedTextWidget(132,123,this::addDisplayText1,0x161655));
        builder.bindPlayerInventory(entityPlayer.inventory, 117+18);
        return builder.build(this.getHolder(), entityPlayer);
    }
    public void setCurrentParallel(int parallelAmount) {
        this.filter_index = Math.min(Math.max(this.filter_index + parallelAmount,0),orePrefixes.size()-1);
        this.current = (OrePrefix) orePrefixes.toArray()[filter_index];
    }
    public String getParallelAmountToString() {
        return Integer.toString(this.filter_index);
    }
    private String findUnlocalizedName() {
        String[] special = new String[]{
                "polymer","slab","dirtyGravel","reduced","lampGt","fenceGate","circuit","log","battery"
                ,"paneGlass","shard","clump","dye","component","craftingLens","door","crystalline","blockGlass"
                ,"stair","stone","cleanGravel","fence"
        };
            String localizationKey = String.format("item.material.oreprefix.polymer.%s", this.forifx.name);
            if (LocalizationUtils.hasKey(localizationKey)) {
                return localizationKey;
            }


        return String.format("item.material.oreprefix.%s", this.forifx.name);
    }
    private String findUnlocalizedName(OrePrefix fix) {
        String[] special = new String[]{
                "polymer","slab","dirtyGravel","reduced","lampGt","fenceGate","circuit","log","battery"
                ,"paneGlass","shard","clump","dye","component","craftingLens","door","crystalline","blockGlass"
                ,"stair","stone","cleanGravel","fence"
        };
        String localizationKey = String.format("item.material.oreprefix.polymer.%s", fix.name);
        if (LocalizationUtils.hasKey(localizationKey)) {
            return localizationKey;
        }


        return String.format("item.material.oreprefix.%s", fix.name);
    }
    protected void addDisplayText(List<ITextComponent> textList) {
        for ( forindex = 0; forindex < orePrefixes.size(); forindex++) {
            forifx = orePrefixes.get(forindex);
            textList.add(new TextComponentString("No."+forindex));
            textList.add(new TextComponentTranslation(findUnlocalizedName(),""));
        }
    }
    protected void addDisplayText1(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation(findUnlocalizedName(current),""));
    }
    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new FilterItemStackHandler(current,itemInventory));
        }else
            return super.getCapability(capability, side);
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityTypeFilter(this.metaTileEntityId);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("filter_index",filter_index);
        data.setTag("inventory_self",((ItemStackHandler)itemInventory).serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        filter_index = data.getInteger("filter_index");
        this.current = (OrePrefix) orePrefixes.toArray()[filter_index];
        ((ItemStackHandler)itemInventory).deserializeNBT(data.getCompoundTag("inventory_self"));
    }

    
}
