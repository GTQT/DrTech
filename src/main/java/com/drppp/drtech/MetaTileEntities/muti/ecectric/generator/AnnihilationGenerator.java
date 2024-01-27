package com.drppp.drtech.MetaTileEntities.muti.ecectric.generator;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses;
import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.MetaTileEntities.Logic.AnnihilationGeneratorLogic;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AnnihilationGenerator extends MultiblockWithDisplayBase implements IDataInfoProvider, IWorkable, IControllable {
    private final AnnihilationGeneratorLogic logic;
    protected IEnergyContainer energyContainer = new EnergyContainerList(new ArrayList());
    protected ItemHandlerList itemImportInventory;
    protected TileEntity entity;
    private int leve;
    public AnnihilationGenerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.logic = new AnnihilationGeneratorLogic(this);
    }

    @Override
    protected void updateFormedValid() {
        if(!this.getWorld().isRemote)
        {

            BlockPos pos = this.getPos();
            if(this.frontFacing == EnumFacing.EAST)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX()-2,pos.getY()+3,pos.getZ()));
            if(this.frontFacing == EnumFacing.WEST)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX()+2,pos.getY()+3,pos.getZ()));
            if(this.frontFacing == EnumFacing.SOUTH)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX(),pos.getY()+3,pos.getZ()-2));
            if(this.frontFacing == EnumFacing.NORTH)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX(),pos.getY()+3,pos.getZ()+2));
            var slots = itemImportInventory.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack item =  itemImportInventory.getStackInSlot(i);
                if(item.getItem()== MyMetaItems.ENERGY_ELEMENT_1.getMetaItem() && item.getMetadata()==MyMetaItems.ENERGY_ELEMENT_1.getMetaValue())
                {
                    if(((TileEntityGravitationalAnomaly)entity).weight+50 <= 400)
                    {
                        ((TileEntityGravitationalAnomaly)entity).weight +=50;
                        itemImportInventory.extractItem(i,1,false);
                    }
                }else if(item.getItem()== MyMetaItems.ENERGY_ELEMENT_2.getMetaItem() && item.getMetadata()==MyMetaItems.ENERGY_ELEMENT_2.getMetaValue())
                {
                    if(((TileEntityGravitationalAnomaly)entity).weight+100 <= 800)
                    {
                        ((TileEntityGravitationalAnomaly)entity).weight +=100;
                        itemImportInventory.extractItem(i,1,false);
                    }
                }
                else if(item.getItem()== MyMetaItems.ENERGY_ELEMENT_3.getMetaItem() && item.getMetadata()==MyMetaItems.ENERGY_ELEMENT_3.getMetaValue())
                {
                    if(((TileEntityGravitationalAnomaly)entity).weight+150 <= 1200)
                    {
                        ((TileEntityGravitationalAnomaly)entity).weight +=150;
                        itemImportInventory.extractItem(i,1,false);
                    }
                }
                else if(item.getItem()== MyMetaItems.ENERGY_ELEMENT_4.getMetaItem() && item.getMetadata()==MyMetaItems.ENERGY_ELEMENT_4.getMetaValue())
                {
                    if(((TileEntityGravitationalAnomaly)entity).weight+200 <= 1600)
                    {
                        ((TileEntityGravitationalAnomaly)entity).weight +=200;
                        itemImportInventory.extractItem(i,1,false);
                    }
                }
                else if(item.getItem()== MyMetaItems.ENERGY_ELEMENT_5.getMetaItem() && item.getMetadata()==MyMetaItems.ENERGY_ELEMENT_5.getMetaValue())
                {
                    if(((TileEntityGravitationalAnomaly)entity).weight+300 <= 2000)
                    {
                        ((TileEntityGravitationalAnomaly)entity).weight +=300;
                        itemImportInventory.extractItem(i,1,false);
                    }
                }
            }
            if(((TileEntityGravitationalAnomaly)entity).weight>0)
            {
                this.logic.setActive(true);
                this.logic.setWorkingEnabled(true);
            }
            else
            {
                this.logic.setActive(false);
                //this.logic.setWorkingEnabled(false);
            }
            this.markDirty();
            logic.updateLogic((TileEntityGravitationalAnomaly)entity);
        }

    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAA", "AAAAA","BBBBB", "BBBBB","BBBBB","TTTTT")
                .aisle("AAAAA", "AAAAA","BXXXB", "B###B","BXXXB","TTTTT")
                .aisle("AAAAA", "AAAAA","BXXXB", "B#W#B","BXXXB","TTTTT")
                .aisle("AAAAA", "AAAAA","BXXXB", "B###B","BXXXB","TTTTT")
                .aisle("AASAA", "AAAAA","BBBBB", "BBBBB","BBBBB","TTTTT")
                .where('S', selfPredicate())
                .where('T', states(getCasingState()))
                .where('B', states(getGlassesState()))
                .where('W', blocks(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY))
                .where('X', heatingCoils())
                .where('A',
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)
                                .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                                .or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE)))
                        )
                .where('#', any())
                .build();
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.GRAVITATION_FIELD_CASING);
    }
    protected IBlockState getGlassesState() {
        return BlocksInit.TRANSPARENT_CASING.getState(MetaGlasses.CasingType.NE_BORON_SILICATE_GLASS_BLOCK);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STABLE_TITANIUM_CASING;
    }
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.annihilation_generator.tooltip.1", new Object[0]));
        tooltip.add(I18n.format("drtech.machine.annihilation_generator.tooltip.2", new Object[0]));
        tooltip.add(I18n.format("drtech.machine.annihilation_generator.tooltip.3", new Object[0]));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new AnnihilationGenerator(this.metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (!isStructureFormed()) {
            TextComponentTranslation textComponentTranslation = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            textComponentTranslation.setStyle((new Style()).setColor(TextFormatting.GRAY));
            textList.add((new TextComponentTranslation("gregtech.multiblock.invalid_structure"))
                    .setStyle((new Style()).setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, textComponentTranslation))));
        } else {

            IEnergyContainer energyContainer = this.energyContainer;
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0L) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VN[GTUtility.getFloorTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (!this.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            } else if (this.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                int currentProgress = (int)(((float)getProgress()/(float)getMaxProgress())*100);
                textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));

            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }
            textList.add(new TextComponentTranslation("drtech.multiblock.tire",this.leve));
            textList.add(new TextComponentTranslation("drtech.multiblock.beilv",this.leve*0.25));
            textList.add(new TextComponentTranslation("gregtech.multiblock.weight",this.logic.weight)
                    .setStyle((new Style()).setColor(TextFormatting.YELLOW))
            );
            textList.add(new TextComponentTranslation("gregtech.multiblock.mEUt",this.logic.getmEUt(),"EU/T")
                    .setStyle((new Style()).setColor(TextFormatting.YELLOW))
            );
        }
    }
    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList());
        this.itemImportInventory =  new ItemHandlerList(Collections.emptyList());
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        energyContainer.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer=new EnergyContainerList(energyContainer);
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));

        Object type = context.get("CoilType");
        if (type instanceof IHeatingCoilBlockStats) {
            this.leve = ((IHeatingCoilBlockStats)type).getLevel();
        } else {
            this.leve = 1;
        }
    }
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public boolean isActive() {
        return (isStructureFormed() && this.logic.isActive() && this.logic.isWorkingEnabled());
    }

    @Override
    public boolean isWorkingEnabled() {
        return logic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        logic.setWorkingEnabled(b);
    }

    @Override
    public int getProgress() {
        return logic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return logic.getMaxProgress();
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return new LinkedList<>();
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        return this.logic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.logic.readFromNBT(data);
    }
    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public ItemHandlerList getItemImportInventory() {
        return itemImportInventory;
    }
    public int getLeve() {
        return leve;
    }
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.logic.receiveCustomData(dataId, buf);
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.logic.writeInitialSyncData(buf);
    }
    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.logic.receiveInitialSyncData(buf);
    }
    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        super.addToolUsages(stack, world, tooltip, advanced);
    }


}

