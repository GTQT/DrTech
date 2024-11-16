package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.Items.Behavior.BluePrintBehavior;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import keqing.gtqtcore.client.utils.RenderBufferHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MetaTileEntityUniversalCollector extends TieredMetaTileEntity implements IWorkable, IFastRenderMetaTileEntity {
    protected final ICubeRenderer renderer;
    boolean isActive=true;
    boolean isWorkingEnabled=true;
    ItemStackHandler inventory = new ItemStackHandler(45);
    private final FluidTankList fluidTankList;
    int tick=0;
    boolean autooutput=true;
    private int range=0;
    private int lx=0,ly=0,lz=0;
    boolean showRange = false;
    public MetaTileEntityUniversalCollector(ResourceLocation metaTileEntityId, int tier, ICubeRenderer renderer) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        FluidTank[] fluidsHandlers = new FluidTank[18];
        for (int i = 0; i < fluidsHandlers.length; i++) {
            fluidsHandlers[i] = new NotifiableFluidTank(16000 +16000*getTier(), this, true);
        }
        this.fluidTankList = new FluidTankList(false, fluidsHandlers);
        this.range =(int) Math.pow((getTier()+1),2);
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityUniversalCollector(this.metaTileEntityId,getTier(),renderer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderOverlays(renderState, translation, pipeline);

    }
    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive(), isWorkingEnabled());
    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(Textures.BACKGROUND, 198, 218);
        for (int i = 0; i < 45; i++) {
            builder.slot(inventory,i,5+i%9*18,3+i/9*18,true,true, GuiTextures.SLOT);
        }
        for (int i = 0; i < 9; i++) {
                    builder.widget(
                            new TankWidget(fluidTankList.getTankAt(i),  i* 18+5, 95 , 18, 18)
                                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                                    .setContainerClicking(true, true)
                                    .setAlwaysShowFull(true));
        }
        for (int i = 0; i < 9; i++) {
            builder.widget(
                    new TankWidget(fluidTankList.getTankAt(i+9),  i* 18+5, 95+18 , 18, 18)
                            .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                            .setContainerClicking(true, true)
                            .setAlwaysShowFull(true));
        }
        builder.widget(new IncrementButtonWidget(165, 3, 30, 20, 1, 4, 16, 64, this::setCurrentParallel)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        builder.widget(new IncrementButtonWidget(165, 36, 30, 20, -1, -4, -16, -64, this::setCurrentParallel)
                        .setDefaultTooltip()
                        .setShouldClientCallback(false));

        builder.widget(new TextFieldWidget2(175, 66, 51, 20, this::getParallelAmountToString, val -> {
                    if (val != null && !val.isEmpty()) {
                        setCurrentParallel(Integer.parseInt(val));
                    }
                }));
        builder.widget(new ClickButtonWidget(165,87,30,20,"弹出",this::setAuto));
        builder.widget(new AdvancedTextWidget(172,107,this::addDisplayText,0x161655));
        builder.widget(new ClickButtonWidget(165,117,28,18,"范围显示",this::setShowRange));
        builder.bindPlayerInventory(entityPlayer.inventory, 117+18);
        return builder.build(this.getHolder(), entityPlayer);
    }
    public void setCurrentParallel(int parallelAmount) {
        this.range = Math.min(Math.max(this.range + parallelAmount,1),(int) Math.pow((getTier()+1),2));
    }
    public String getParallelAmountToString() {
        return Integer.toString(this.range);
    }
    public void setAuto(Widget.ClickData clickData)
    {
        this.autooutput = !this.autooutput;
    }
    public void setShowRange(Widget.ClickData clickData)
    {
        this.showRange = !this.showRange;
        writeCustomData(8888,packetBuffer -> packetBuffer.writeBoolean(this.showRange));
    }
    protected void addDisplayText(List<ITextComponent> textList) {
        if(this.autooutput)
            textList.add(new TextComponentString("YES"));
        else
            textList.add(new TextComponentString("NO"));
    }
    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote)
        {
            if(energyContainer.getEnergyStored()<GTValues.V[getTier()]/4)
            {
                isWorkingEnabled=false;
            }else
            {
                isWorkingEnabled=true;
            }
            if(isWorkingEnabled)
            {
                energyContainer.changeEnergy(-GTValues.V[getTier()]/4);
                if(++tick>80)
                {
                    tick=0;
                    getRangeTileentities();
                }
				//是否开启自动弹出 开启就查找有没有挨着的容器 并把物品/流体弹出
				if(autooutput)
				{
                    energyContainer.changeEnergy(-GTValues.V[getTier()]/4);
                    getStorageContainer(this.getWorld(),this.getPos());
				}
            }
        }

    }
    private void getRangeTileentities()
    {
            BlockPos centerPos=this.getPos() ;
            int radius = range;
            for (int x = -radius; x <= radius; x++) {

                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos currentPos = centerPos.add(x, y, z);
                        MetaTileEntity te = GTUtility.getMetaTileEntity(this.getWorld(),currentPos);
                        if(te != null && !(te instanceof MetaTileEntityQuantumTank) &&!(te instanceof MetaTileEntityQuantumChest) && currentPos!=centerPos)
                        {
                            GTTransferUtils.moveInventoryItems(te.getExportItems(),inventory);
                            GTTransferUtils.transferFluids(te.getExportFluids(),this.fluidTankList);
                        }
                    }
                }
            }
    }
    public  void getStorageContainer(World world, BlockPos center) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacentPos = center.offset(facing);
            if (world.getTileEntity(adjacentPos)!=null && world.getTileEntity(adjacentPos).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,facing)) {
                var iInventory = world.getTileEntity(adjacentPos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,facing);
                GTTransferUtils.moveInventoryItems(inventory,iInventory);
            }
            if(world.getTileEntity(adjacentPos)!=null && world.getTileEntity(adjacentPos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,facing)){
                var fluidinventory = world.getTileEntity(adjacentPos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,facing);
                GTTransferUtils.transferFluids(this.fluidTankList,fluidinventory);
            }
            if( GTUtility.getMetaTileEntity(this.getWorld(),adjacentPos)!=null && GTUtility.getMetaTileEntity(this.getWorld(),adjacentPos) instanceof MetaTileEntityQuantumTank){
                var s = GTUtility.getMetaTileEntity(this.getWorld(),adjacentPos);
                GTTransferUtils.transferFluids(this.fluidTankList,s.getFluidInventory());
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.serializeNBT());
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setInteger("range",range);
        data.setBoolean("auto",this.autooutput);
        if(fluidTankList!=null)
           data.setTag("fluidlist", fluidTankList.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("inventory")) {
            inventory.deserializeNBT(data.getCompoundTag("inventory"));
        }
        if(data.hasKey("fluidlist"))
            fluidTankList.deserializeNBT(data.getCompoundTag("fluidlist"));
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.range = data.getInteger("range");
        this.autooutput = data.getBoolean("auto");
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeBoolean(isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
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
        }else if(dataId==8888)
        {
            this.showRange = buf.readBoolean();
        }
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory);
        }else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            for (int i = 0; i < fluidTankList.getTanks(); i++)
            {
                if(fluidTankList.getTankAt(i).getFluidAmount()>0)
                {
                    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fluidTankList.getTankAt(i));
                }
            }
        }else if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        }else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }
    @Override
    public boolean isActive() {
        return this.isActive;
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
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add("收集范围内的小机器、输出总线/仓的物品/流体");
        tooltip.add("等级:"+GTValues.VN[getTier()]);
        tooltip.add("收集范围:"+range);
        tooltip.add("耗电:"+(GTValues.V[getTier()]/4));
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (showRange) {
            var pos = getPos();
            BlockPos[] poses = new BlockPos[2];
            poses[0] = getPos().add(-range, -range, -range);
            poses[1] = getPos().add(range, range, range);
            double doubleX =  (pos.getX() - x) * 1;
            double doubleY =  (pos.getY() - y) * 1;
            double doubleZ =  (pos.getZ() - z) * 1;
            if (range <= 0) return; // 确保范围有效

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // 渲染立方体面
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            RenderBufferHelper.renderCubeFace(buffer,
                    poses[0].getX(), poses[0].getY(), poses[0].getZ(),
                    poses[1].getX() + 1, poses[1].getY() + 1, poses[1].getZ() + 1,
                    0.2f, 1f, 0.2f, 0.25f, true);
            tessellator.draw();

            // 渲染立方体边框
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            GlStateManager.glLineWidth(3);
            RenderBufferHelper.renderCubeFrame(buffer,
                    poses[0].getX(), poses[0].getY(), poses[0].getZ(),
                    poses[1].getX() + 1, poses[1].getY() + 1, poses[1].getZ() + 1,
                    0.0f, 1f, 0f, 0.5f);
            tessellator.draw();

            // 恢复状态
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }

    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-range,-range,-range),getPos().add(range,range,range));
    }
}
