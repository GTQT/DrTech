package com.drppp.drtech.common.MetaTileEntities.muti.electric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Client.Particle.DrtechLaserBeamParticle;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.Blocks.BlocksInit;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPatternTemplate;
import gregtech.api.pattern.FormedStructureView;
import gregtech.api.pattern.SoftTemplate;
import gregtech.api.pattern.TemplatePool;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static gregtech.api.util.RelativeDirection.FRONT;
import static gregtech.api.util.RelativeDirection.RIGHT;
import static gregtech.api.util.RelativeDirection.UP;

public class MetaTileEntityEnergyTransTower extends MultiblockWithDisplayBase implements IControllable {
    private boolean isActive = true;
    private boolean isWorkingEnabled = true;
    private TileEntityConnector connector;
    private int beamCount;

    public MetaTileEntityEnergyTransTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        this.isWorkingEnabled = isWorkingEnabled;
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
    public boolean usesMui2() {
        return false;
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
        if (this.getWorld().isRemote) {
            return;
        }
        if (this.connector == null) {
            getConnectorPos();
        }
        if (beamCount++ > 20) {
            beamCount = 0;
            writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.connector = null;
    }

    @Override
    protected void formStructure(@NotNull FormedStructureView formed) {
        super.formStructure(formed);
        getConnectorPos();
    }

    private void getConnectorPos() {
        BlockPos connectorPos = null;
        switch (this.getFrontFacing()) {
            case SOUTH:
                connectorPos = new BlockPos(this.getPos().getX(), this.getPos().getY() + 10, this.getPos().getZ() - 1);
                break;
            case NORTH:
                connectorPos = new BlockPos(this.getPos().getX(), this.getPos().getY() + 10, this.getPos().getZ() + 1);
                break;
            case EAST:
                connectorPos = new BlockPos(this.getPos().getX() - 1, this.getPos().getY() + 10, this.getPos().getZ());
                break;
            case WEST:
                connectorPos = new BlockPos(this.getPos().getX() + 1, this.getPos().getY() + 10, this.getPos().getZ());
                break;
            default:
                break;
        }

        if (connectorPos != null && this.getWorld().getTileEntity(connectorPos) instanceof TileEntityConnector) {
            this.connector = (TileEntityConnector) this.getWorld().getTileEntity(connectorPos);
        }
    }

    private static final SoftTemplate TEMPLATE = TemplatePool.getInstance().register(
            "drtech:trans_tower",
            MetaTileEntityEnergyTransTower::buildTemplate
    );

    @Override
    protected @NotNull BlockPatternTemplate createStructureTemplate() {
        return TEMPLATE.get();
    }

    private static BlockPatternTemplate buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, FRONT, UP)
                .aisle("CSC", "CCC", "CCC")
                .aisleRepeatable(6, 6, "###", "GLG", "#G#")
                .aisle("#C#", "CCC", "#C#")
                .aisle("#G#", "GCG", "#G#")
                .aisle("#G#", "GCG", "#G#")
                .aisle("###", "#D#", "###")
                .where('S', selfPredicate(MetaTileEntityEnergyTransTower.class))
                .where('#', any())
                .where('C', states(getCasingState())
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                )
                .where('G', blocks(Blocks.IRON_BARS))
                .where('L', frames(Materials.Steel))
                .where('D', blocks(BlocksInit.BLOCK_CONNECTOR1, BlocksInit.BLOCK_CONNECTOR2, BlocksInit.BLOCK_CONNECTOR3))
                .buildTemplate();
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
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.3"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (this.connector != null) {
            BlockPos targetPos = getPrimaryConnectionTarget();
            textList.add(new TextComponentTranslation("drtech.machine.energytrans.connect", targetPos == null ? ":None" :
                    ": X:" + targetPos.getX() + " Y:" + targetPos.getY() + " Z:" + targetPos.getZ()));
            textList.add(new TextComponentTranslation("drtech.machine.energytrans.energy", String.valueOf(this.connector.StoredEnergy)));
            textList.add(new TextComponentTranslation("drtech.machine.energytrans.posdist", targetPos == null ? "0" :
                    String.valueOf(DrtechUtils.getPosDist(targetPos, this.connector.getPos()))));
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

    private void clearpos(Widget.ClickData clickData) {
        World world = getWorld();
        if (world != null && !world.isRemote && this.connector != null) {
            this.connector.removeAllConnections(true);
            writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
        }
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
        } else if (dataId == GregtechDataCodes.UPDATE_PARTICLE) {
            try {
                readParticles(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeParticles(@NotNull PacketBuffer buf) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList connectionTags = new NBTTagList();
        if (this.connector != null) {
            for (TileEntityConnector.WireConnection connection : this.connector.getConnections()) {
                NBTTagCompound connectionTag = new NBTTagCompound();
                connectionTag.setInteger("Sx", this.connector.getPos().getX());
                connectionTag.setInteger("Sy", this.connector.getPos().getY());
                connectionTag.setInteger("Sz", this.connector.getPos().getZ());
                connectionTag.setInteger("Ex", connection.target.getX());
                connectionTag.setInteger("Ey", connection.target.getY());
                connectionTag.setInteger("Ez", connection.target.getZ());
                connectionTags.appendTag(connectionTag);
            }
        }
        tag.setTag("Connections", connectionTags);
        buf.writeCompoundTag(tag);
    }

    @SideOnly(Side.CLIENT)
    private void readParticles(@NotNull PacketBuffer buf) throws IOException {
        NBTTagCompound tag = buf.readCompoundTag();
        if (tag == null) {
            return;
        }
        NBTTagList connectionTags = tag.getTagList("Connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < connectionTags.tagCount(); i++) {
            NBTTagCompound connectionTag = connectionTags.getCompoundTagAt(i);
            BlockPos startPos = new BlockPos(connectionTag.getInteger("Sx"), connectionTag.getInteger("Sy"), connectionTag.getInteger("Sz"));
            BlockPos endPos = new BlockPos(connectionTag.getInteger("Ex"), connectionTag.getInteger("Ey"), connectionTag.getInteger("Ez"));
            operateClient(startPos, endPos, 20);
        }
    }

    @SideOnly(Side.CLIENT)
    public void operateClient(BlockPos startPos, BlockPos endPos, int age) {
        GTParticleManager.INSTANCE.addEffect(new DrtechLaserBeamParticle(this, startPos, endPos, age));
    }

    @Nullable
    private BlockPos getPrimaryConnectionTarget() {
        if (this.connector == null || this.connector.getConnections().isEmpty()) {
            return null;
        }
        return this.connector.getConnections().get(0).target;
    }
}
