package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.api.ItemHandler.SingleItemStackHandler;
import forestry.api.apiculture.*;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.blocks.BlockAlvearyType;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static forestry.api.apiculture.BeeManager.beeRoot;
import static gregtech.api.util.RelativeDirection.*;
import static keqing.gtqtcore.common.block.blocks.BlockMultiblockGlass1.CasingType.TI_BORON_SILICATE_GLASS;

public class MetaTileEntutyLargeBeeHive extends MultiblockWithDisplayBase implements IDataInfoProvider, IWorkable, IControllable {
    private final SingleItemStackHandler inventory = new SingleItemStackHandler(1024);
    public int productType = 0;
    public int workType = 0;
    public List<ItemStack> listdrops = new ArrayList<>();
    public int process = 0;
    public int maxProcess = 100;
    protected IEnergyContainer energyContainer = new EnergyContainerList(new ArrayList());
    protected ItemHandlerList itemImportInventory;
    protected ItemHandlerList itemExportInventory;
    private boolean isActive = true, isWorkingEnabled = true;

    public MetaTileEntutyLargeBeeHive(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public int getProgress() {
        return this.process;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcess;
    }

    @Override
    public @NotNull List<ITextComponent> getDataInfo() {
        return null;
    }


    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList());
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.itemExportInventory = new ItemHandlerList(Collections.emptyList());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.energyContainer = new EnergyContainerList(energyContainer);
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.itemExportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));

    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntutyLargeBeeHive(this.metaTileEntityId);
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
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setInteger("process", process);
        data.setInteger("productType", productType);
        data.setInteger("workType", workType);
        data.setTag("beeStore", inventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        process = data.getInteger("process");
        productType = data.getInteger("productType");
        workType = data.getInteger("workType");
        if (data.hasKey("beeStore")) {
            inventory.deserializeNBT(data.getCompoundTag("beeStore"));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }

    protected IBlockState getGlassessCasingState() {
        return GTQTMetaBlocks.blockMultiblockGlass1.getState(TI_BORON_SILICATE_GLASS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("               ", "               ", "               ", "      HHH      ", "    HHAAAHH    ", "    HAPLPAH    ", "   HAPAAAPAH   ", "   HALAAALAH   ", "   HAPAAAPAH   ", "    HAPLPAH    ", "    HHAAAHH    ", "      HHH      ", "               ", "               ", "               ")
                .aisle("               ", "               ", "      GGG      ", "   GGG   GG    ", "   G       G   ", "   G       G   ", "  G         G  ", "  G         G  ", "  G         G  ", "   G       G   ", "   G       G   ", "    GG   GG    ", "      GGG      ", "               ", "               ")
                .aisle("               ", "      HHH      ", "   HHH   HHH   ", "  H        GH  ", "  H         H  ", "  H         H  ", " H           H ", " H           H ", " H           H ", "  H         H  ", "  H         H  ", "  HG       GH  ", "   HHH   HHH   ", "      HHH      ", "               ")
                .aisle("      GGG      ", "   GGG   GGG   ", "  G         G  ", " G           G ", " G           G ", " G           G ", "G             G", "G             G", "G             G", " G           G ", " G           G ", " G           G ", "  G         G  ", "   GGG   GGG   ", "      GGG      ")
                .aisle("      AAA      ", "   OLA   ALO   ", "  P         P  ", " O           O ", " L           L ", " A           A ", "A             A", "A             A", "A             A", " A           A ", " L           L ", " O           O ", "  P         P  ", "   OLA   ALO   ", "      AAA      ")
                .aisle("     AAAAA     ", "   NA     AO   ", "  P         P  ", " N           O ", " A           A ", "A             A", "A     III     A", "A     III     A", "A     III     A", "A             A", " A           A ", " N           N ", "  P         P  ", "   NA     AN   ", "     AAAAA     ")
                .aisle("     AAAAA     ", "   NA FFF AO   ", "  PFF     FFP  ", " NF        FFO ", " AF         FA ", "A             A", "AF    JJJ    FA", "AF    JKJ    FA", "AF    JJJ    FA", "A             A", " AF         FA ", " NFF       FFN ", "  PFF     FFP  ", "   NA FFF AN   ", "     AAAAA     ")
                .aisle("      AAA      ", "   OLAFFFALO   ", "  PFFFFFFFFFP  ", " OFFFF   FFFFO ", " LFF       FFL ", " AFF FFFFF  FA ", "AFF  FKKKFF FFA", "AFF FFKKKFF FFA", "AFF FFKKKF  FFA", " AF  FFFFF  FA ", " LFF   FF  FFL ", " OFFFF    FFFO ", "  PFFFFFFFFFP  ", "   OLAFFFALO   ", "      AAA      ")
                .aisle("      GSG      ", "   GGGBBBGGG   ", "  GBBFFFFFBBG  ", " GBFFF   FFBBG ", " GBF       FBG ", " GFF FFFFF  FG ", "GBF  FKKKFF FBG", "GBF FFKJKFF FBG", "GBF FFKKKF  FBG", " GF  FFFFF  FG ", " GBF   FF  FBG ", " GBBFF    FBBG ", "  GBBFFFFFBBG  ", "   GGGBBBGGG   ", "      GGG      ")
                .aisle("      HHH      ", "    HHBBBHH    ", "  HHBBBBBBBHH  ", "  HBBBWWWBBBH  ", " HBBWWWWWWWBBH ", " HBBWBBBBBWWBH ", "HBBWWBBBBBBWBBH", "HBBWBBBBBBBWBBH", "HBBWBBBBBBWWBBH", " HBWWBBBBBWWBH ", " HBBWWWBBWWBBH ", "  HBBBWWWWBBH  ", "  HHBBBBBBBHH  ", "    HHBBBHH    ", "      HHH      ")
                .aisle("               ", "     GGGGG     ", "   GGGBBBBGG   ", "  GBBBBBBBBBG  ", "  GBBBBBBBBBG  ", " GBBBBBBBBBBBG ", " GBBBBBBBBBBBG ", " GBBBBBBBBBBBG ", " GBBBBBBBBBBBG ", " GBBBBBBBBBBBG ", "  GBBBBBBBBBG  ", "  GBBBBBBBBBG  ", "   GGBBBBBGG   ", "     GGGGG     ", "               ")
                .aisle("               ", "      HHH      ", "    HHBBBHH    ", "   HBBBBBBBH   ", "  HBBBBBBBBBH  ", "  HBBBBBBBBBH  ", " HBBBBBBBBBBBH ", " HBBBBBBBBBBBH ", " HBBBBBBBBBBBH ", "  HBBBBBBBBBH  ", "  HBBBBBBBBBH  ", "   HBBBBBBBH   ", "    HHBBBHH    ", "      HHH      ", "               ")
                .aisle("               ", "               ", "      GGG      ", "    GGBBBGG    ", "   GBBBBBBBG   ", "   GBBBBBBBG   ", "  GBBBBBBBBBG  ", "  GBBBBBBBBBG  ", "  GBBBBBBBBBG  ", "   GBBBBBBBG   ", "   GBBBBBBBG   ", "    GGBBBGG    ", "      GGG      ", "               ", "               ")
                .aisle("               ", "               ", "       H       ", "     HHBHH     ", "    HBBBBBH    ", "   HBBBBBBBH   ", "   HBBBBBBBH   ", "  HBBBBBBBBBH  ", "   HBBBBBBBH   ", "   HBBBBBBBH   ", "    HBBBBBH    ", "     HHBHH     ", "       H       ", "               ", "               ")
                .aisle("               ", "               ", "               ", "       G       ", "     GGBGG     ", "    GBBBBBG    ", "    GBBBBBG    ", "   GBBBBBBBG   ", "    GBBBBBG    ", "    GBBBBBG    ", "     GGBGG     ", "       G       ", "               ", "               ", "               ")
                .aisle("               ", "               ", "               ", "               ", "      HHH      ", "     HHHHH     ", "    HHBBBHH    ", "    HHBBBHH    ", "    HHBBBHH    ", "     HHBHH     ", "      HHH      ", "               ", "               ", "               ", "               ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "      GGG      ", "      GHG      ", "      GGG      ", "               ", "               ", "               ", "               ", "               ", "               ")
                .where('S', selfPredicate())
                .where('A', states(getGlassessCasingState()))
                .where('B', blocks(Blocks.DIRT, Blocks.GRASS))
                .where('H', blocks(Blocks.PLANKS))
                .where('I', blocks(Blocks.WOODEN_SLAB))
                .where('J', blocks(ModuleApiculture.getBlocks().apiary))
                .where('K', blocks(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.PLAIN)))
                .where('L', blocks(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.HYGRO)))
                .where('N', blocks(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.STABILISER)))
                .where('O', blocks(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.HEATER)))
                .where('P', blocks(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.FAN)))
                .where('F', blocks(Blocks.LOG))
                .where('W', blocks(Blocks.WATER))

                .where('G', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS)).or(
                        abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)
                                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)
                                                .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                                )))
                .where(' ', any())
                .build();
    }

    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 18, 18, "", this::changeProductType)
                .setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipText("drtech.multiblock.lbh.changep"));
        group.addWidget(new ClickButtonWidget(0, 18, 18, 18, "", this::changeWorkType)
                .setButtonTexture(GuiTextures.LOCK)
                .setTooltipText("drtech.multiblock.lbh.changew"));
        return group;
    }

    private void changeProductType(Widget.ClickData clickData) {
        this.productType = (++this.productType) % 2;
    }

    private void changeWorkType(Widget.ClickData clickData) {
        this.workType = (++this.workType) % 3;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (this.productType == 0)
            textList.add(new TextComponentString("生产方式:" + "蜂窝"));
        else
            textList.add(new TextComponentString("生产方式:" + "蜂群"));
        if (this.workType == 0)
            textList.add(new TextComponentString("工作方式:" + "输入"));
        else if (this.workType == 1)
            textList.add(new TextComponentString("工作方式:" + "工作"));
        else
            textList.add(new TextComponentString("工作方式:" + "输出"));

    }

    @Override
    protected void updateFormedValid() {
        if (this.isWorkingEnabled && !this.getWorld().isRemote)
            if (workType == 0) {
                process = 0;
                for (int i = 0; i < itemImportInventory.getSlots(); i++) {
                    ItemStack is = itemImportInventory.getStackInSlot(i);
                    EnumBeeType beeType = beeRoot.getType(is);
                    List<ItemStack> list = new ArrayList<>();
                    if (beeType == EnumBeeType.QUEEN) {
                        list.add(is);
                        if (GTTransferUtils.addItemsToItemHandler(this.inventory, true, list)) {
                            GTTransferUtils.addItemsToItemHandler(this.inventory, false, list);
                            this.itemImportInventory.extractItem(i, 1, false);

                        }
                    }
                }
            } else if (workType == 2) {
                listdrops.clear();
                process = 0;
                for (int i = 0; i < inventory.getSlots(); i++) {
                    List<ItemStack> list = new ArrayList<>();
                    list.add(inventory.getStackInSlot(i));
                    if (GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, true, list)) {
                        GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, false, list);
                        this.inventory.extractItem(i, 1, false);
                    }

                }

            }//产出蜂窝
            else if (this.workType == 1 && this.productType == 0) {
                long maxslot = Math.min(this.energyContainer.getInputAmperage() * this.energyContainer.getInputVoltage() / GTValues.V[GTValues.IV], 1024);
                long dradinEnergy = GTValues.V[GTValues.IV] * maxslot;
                if (maxslot > 0 && this.energyContainer.getEnergyStored() >= dradinEnergy) {
                    this.energyContainer.removeEnergy(dradinEnergy);
                } else {
                    listdrops.clear();
                    process = 0;
                    this.setWorkingEnabled(false);
                }
                if (listdrops.size() == 0) {
                    for (int i = 0; i < maxslot; i++) {
                        ItemStack is = inventory.getStackInSlot(i);
                        EnumBeeType beeType = beeRoot.getType(is);
                        if (beeType == EnumBeeType.QUEEN) {
                            BeeSimulator bs = new BeeSimulator(is.copy(), this.getWorld(), (float) getVoltageTierExact());
                            for (var drop : bs.drops) {
                                listdrops.add(drop.get((int) drop.getAmount()));
                            }
                            for (var drop : bs.specialDrops) {
                                listdrops.add(drop.get((int) drop.getAmount()));
                            }
                        }
                    }
                }
                if (process++ >= maxProcess) {
                    process = 0;
                    GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, false, listdrops);
                    listdrops.clear();
                }
                //产出公主蜂
            } else if (this.workType == 1 && this.productType == 1 && this.energyContainer.getInputVoltage() >= GTValues.V[GTValues.IV]) {
                if (this.energyContainer.getEnergyStored() >= GTValues.V[GTValues.IV])
                    this.energyContainer.removeEnergy(GTValues.V[GTValues.IV]);
                else {
                    listdrops.clear();
                    process = 0;
                    this.setWorkingEnabled(false);
                }
                if (process++ >= maxProcess * 4) {
                    listdrops.clear();
                    process = 0;
                    ItemStack is = inventory.getStackInSlot(0);
                    EnumBeeType beeType = beeRoot.getType(is);
                    if (beeType == EnumBeeType.QUEEN) {
                        IBee bee = beeRoot.getMember(is);
                        BeeSimulator bs = new BeeSimulator(is.copy(), this.getWorld(), (float) getVoltageTierExact());
                        List<ItemStack> list = new ArrayList<>();
                        list.add(bs.createIgnobleCopy());
                        if (GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, true, list)) {
                            GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, false, list);
                        }
                    }

                }
            }
    }

    public double getVoltageTierExact() {
        return Math.log((double) this.energyContainer.getEnergyCapacity() / 8d) / 1.3862943611199 + 1e-8d;
    }

    private static class BeeSimulator {

        private static IBeekeepingMode mode;
        public final ItemStack queenStack;
        boolean isValid;
        List<BeeDrop> drops = new ArrayList<>();
        List<BeeDrop> specialDrops = new ArrayList<>();
        float beeSpeed;
        float maxBeeCycles;
        String flowerType;
        String flowerTypeDescription;

        public BeeSimulator(ItemStack queenStack, World world, float t) {
            isValid = false;
            this.queenStack = queenStack.copy();
            this.queenStack.setCount(1);
            generate(world, t);
            isValid = true;
            queenStack.setCount(queenStack.getCount() - 1);
        }

        public void generate(World world, float t) {
            if (mode == null) mode = beeRoot.getBeekeepingMode(world);
            drops.clear();
            specialDrops.clear();
            if (beeRoot.getType(this.queenStack) != EnumBeeType.QUEEN) return;
            IBee queen = beeRoot.getMember(this.queenStack);
            IBeeModifier beeModifier = mode.getBeeModifier();
            float mod = beeModifier.getLifespanModifier(null, null, 1.f);
            int h = queen.getMaxHealth();
            maxBeeCycles = (float) h / (1.f / mod);
            IBeeGenome genome = queen.getGenome();
            this.flowerType = genome.getFlowerProvider()
                    .getFlowerType();
            this.flowerTypeDescription = genome.getFlowerProvider()
                    .getDescription();
            IAlleleBeeSpecies primary = genome.getPrimary();
            beeSpeed = genome.getSpeed();
            genome.getPrimary()
                    .getProductChances()
                    .forEach((key, value) -> drops.add(new BeeDrop(key, value, beeSpeed, t)));
            genome.getSecondary()
                    .getProductChances()
                    .forEach((key, value) -> drops.add(new BeeDrop(key, value / 2.f, beeSpeed, t)));
            primary.getSpecialtyChances()
                    .forEach((key, value) -> specialDrops.add(new BeeDrop(key, value, beeSpeed, t)));
        }

        public ItemStack createIgnobleCopy() {
            IBee princess = beeRoot.getMember(queenStack);
            princess.setIsNatural(false);
            return beeRoot.getMemberStack(princess, EnumBeeType.PRINCESS);
        }

        private static class BeeDrop {

            private static final float MAX_PRODUCTION_MODIFIER_FROM_UPGRADES = 17.19926784f; // 4*1.2^8
            final ItemStack stack;
            final ItemStack id;
            final float chance;
            final float beeSpeed;
            double amount;
            float t;

            public BeeDrop(ItemStack stack, float chance, float beeSpeed, float t) {
                this.stack = stack;
                this.chance = chance;
                this.beeSpeed = beeSpeed;
                this.t = t;
                id = this.stack.copy();
                evaluate();
            }

            public void evaluate() {

                this.amount = getFinalChance() * t;
            }

            public double getFinalChance() {
                double d = getFinalChance(chance, beeSpeed, 17.19926784f, MAX_PRODUCTION_MODIFIER_FROM_UPGRADES);
                return d;
            }

            private float getFinalChance(float baseChance, float speed, float prodMod, float modifier) {
                double finalchance = (1 + modifier / 6) * (Math.pow(baseChance, 0.5)) * 2f * (1 + speed) + Math.pow(prodMod, Math.pow(baseChance, 0.333f)) - 3;
                return (float) finalchance;
            }

            public double getAmount() {
                return amount;
            }

            public ItemStack get(int amount) {
                ItemStack r = stack.copy();
                amount = Math.max(amount, 5);
                r.setCount(amount);
                return r;
            }

            @Override
            public int hashCode() {
                return id.hashCode();
            }
        }
    }
}
