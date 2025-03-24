package com.drppp.drtech.common.MetaTileEntities.muti.electric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.Client.Particle.DrtechLaserBeamParticle;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.api.Utils.DrtechUtils;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityEnergyTransTower extends MultiblockWithDisplayBase implements IControllable {
    private boolean isActive=true, isWorkingEnabled = true;
    private IEnergyContainer inenergyContainer;
    private IEnergyContainer outenergyContainer;
    private TileEntityConnector connector;
    private BlockPos pos;
    private int beamCount;
    public MetaTileEntityEnergyTransTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }
    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }
    @Override
    protected void updateFormedValid() {

      if(!this.getWorld().isRemote)
      {
          beamCount++;
          if(this.connector==null)
          {
              getConnectorPos();
          }
          if(this.connector !=null && this.inenergyContainer!= null)
          {
              this.inenergyContainer.changeEnergy(-this.connector.fill(this.inenergyContainer.getEnergyStored()));
              this.connector.markDirty();
          }
          if(this.connector !=null && this.outenergyContainer!= null)
          {
              this.outenergyContainer.changeEnergy(this.connector.drain(this.outenergyContainer.getEnergyCapacity() -this.outenergyContainer.getEnergyStored()));
              this.connector.markDirty();
          }
          if (beamCount >20) {
              beamCount = 0;
              writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
          }
      }

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if(this.connector!=null)
         this.connector.success = 0;

    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        getConnectorPos();
    }

    private void getConnectorPos()
    {
            BlockPos pos = null;
            switch (this.getFrontFacing()){

                case SOUTH:
                    pos = new BlockPos(this.getPos().getX(),this.getPos().getY()+10,this.getPos().getZ()-1);
                    break;
                case NORTH:
                    pos = new BlockPos(this.getPos().getX(),this.getPos().getY()+10,this.getPos().getZ()+1);
                    break;
                case EAST:
                    pos = new BlockPos(this.getPos().getX()-1,this.getPos().getY()+10,this.getPos().getZ());
                    break;
                case WEST:
                    pos = new BlockPos(this.getPos().getX()+1,this.getPos().getY()+10,this.getPos().getZ());
                    break;
            }

        if(pos!= null && this.getWorld().getTileEntity(pos)!= null && this.getWorld().getTileEntity(pos) instanceof TileEntityConnector)
        {
            this.connector = (TileEntityConnector) this.getWorld().getTileEntity(pos);
            this.connector.success = 1;
            this.pos = pos;
        }
    }
    private void initializeAbilities() {
        this.inenergyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.outenergyContainer =new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }
    private void resetTileAbilities() {
        this.inenergyContainer = new EnergyContainerList(new ArrayList());
        this.outenergyContainer = new EnergyContainerList(new ArrayList());
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("CSC", "CCC", "CCC")
                .aisle("###", "GLG", "#G#").setRepeatable(6, 6)
                .aisle("#C#", "CCC", "#C#")
                .aisle("#G#", "GCG", "#G#")
                .aisle("#G#", "GCG", "#G#")
                .aisle("###", "#D#", "###")
                .where('S', selfPredicate())
                .where('#', any())
                .where('C', states(getCasingState())
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                )
                .where('G', blocks(Blocks.IRON_BARS))
                .where('L', frames(Materials.Steel))
                .where('D', blocks(BlocksInit.BLOCK_CONNECTOR1,BlocksInit.BLOCK_CONNECTOR2,BlocksInit.BLOCK_CONNECTOR3))
                .build();
    }
    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityEnergyTransTower(this.metaTileEntityId);
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.2"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if(this.connector!=null)
        {
            textList.add(new TextComponentTranslation("drtech.machine.energytrans.connect",this.connector.beforePos==null?":无":": X:"+
                    this.connector.beforePos.getX()+" Y:" + this.connector.beforePos.getY() +" Z:" + this.connector.beforePos.getZ()));
            textList.add(new TextComponentTranslation("drtech.machine.energytrans.energy",String.valueOf(this.connector.StoredEnergy)));
            if(this.connector.beforePos!=null)
                textList.add(new TextComponentTranslation("drtech.machine.energytrans.posdist",String.valueOf(DrtechUtils.getPosDist(this.connector.beforePos,this.connector.getPos()))));
            else
                textList.add(new TextComponentTranslation("drtech.machine.energytrans.posdist",String.valueOf("0")));
        }
    }
    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 18, 18, "", this::clearpos)
                .setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipText("gtqtcore.multiblock.energytrans.clearpos"));
        return group;
    }
    private void clearpos(Widget.ClickData clickData)
    {
        this.connector.beforePos = null;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("ClearPos",1);
        if(!this.getWorld().isRemote)
            DrtechUtils.sendTileEntityClientUpdate(this.connector,nbt);
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeBoolean(isWorkingEnabled);
        writeParticles(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
        try {
            readParticles(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
        else if (dataId == GregtechDataCodes.UPDATE_PARTICLE) {
            try {
                readParticles(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    private void writeParticles(@NotNull PacketBuffer buf) {
        //buf.writeVarInt(beamCount);
        NBTTagCompound tag = new NBTTagCompound();
        if(this.connector!=null && this.connector.beforePos!=null)
        {
            tag.setInteger("Sx",this.connector.getPos().getX());
            tag.setInteger("Sy",this.connector.getPos().getY());
            tag.setInteger("Sz",this.connector.getPos().getZ());
            tag.setInteger("Ex",this.connector.beforePos.getX());
            tag.setInteger("Ey",this.connector.beforePos.getY());
            tag.setInteger("Ez",this.connector.beforePos.getZ());

        }
        buf.writeCompoundTag(tag);
    }
    @SideOnly(Side.CLIENT)
    private void readParticles(@NotNull PacketBuffer buf) throws IOException {
        NBTTagCompound tag = buf.readCompoundTag();
        if(tag.hasKey("Ex"))
        {
            BlockPos Spos = new BlockPos(tag.getInteger("Sx"),tag.getInteger("Sy"),tag.getInteger("Sz"));
            BlockPos Epos = new BlockPos(tag.getInteger("Ex"),tag.getInteger("Ey"),tag.getInteger("Ez"));
            operateClient(Spos,Epos,20);
        }

    }
    @SideOnly(Side.CLIENT)
    public void operateClient(BlockPos Spos,BlockPos Epos,int age) {
        GTParticleManager.INSTANCE.addEffect(new DrtechLaserBeamParticle(this,Spos,Epos,age));
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, GuiSyncManager guiSyncManager) {
        return null;
    }
}
