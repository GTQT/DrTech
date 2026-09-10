package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import crazypants.enderio.util.CapturedMob;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.tooltips.InformationHandler;
import gregtech.api.util.tooltips.TooltipBuilder;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static crafttweaker.mc1120.CraftTweaker.server;
import static crazypants.enderio.base.fluid.Fluids.XP_JUICE;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.StructureDefinition;

public class MetaTileEntityExtremeExterminationChamber extends MetaTileEntityBaseWithControl {

    private static final int BASE_ENERGY_CONSUMPTION = 1920;
    private static final int BASE_XP_OUTPUT = 120;
    private static final int MIN_PROCESS_TICKS = 20;
    private static final float HEALTH_TO_TICK_RATIO = 0.5f;
    private static final float LOOTING_TIME_REDUCTION = 0.1f;
    private static final int MAX_LOOTING_LEVEL = 4;
    private static final int DURABILITY_TICK_INTERVAL = 20;

    private static final String NBT_INVENTORY = "inventory";
    private static final String NBT_PROCESS_TICKS = "processTicks";
    private static final String NBT_TOTAL_PROCESS_TICKS = "totalProcessTicks";
    private static final String NBT_OUTPUT_MULTIPLIER = "outputMultiplier";
    private static final String NBT_DISPLAYED_LOOTING = "displayedLooting";
    private static final String NBT_CURRENT_MOB_NAME = "currentMobName";

    private static final Method LOOT_TABLE_METHOD = resolveLootTableMethod();

    private final MobCardHolder inventory;
    private String currentMobName = "";
    private int processTicks = 0;
    private int totalProcessTicks = 0;
    private int outputMultiplier = 1;
    private int displayedLooting = 0;
    private EntityLiving currentMob;

    public MetaTileEntityExtremeExterminationChamber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        inventory = new MobCardHolder(this);
    }

    // ---------------------------------------------------------------------
    // Reflection helpers
    // ---------------------------------------------------------------------

    @Nullable
    private static Method resolveLootTableMethod() {
        try {
            Method method = EntityLiving.class.getDeclaredMethod("getLootTable");
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    @Nullable
    private static ResourceLocation getLootTable(EntityLiving entity) {
        if (LOOT_TABLE_METHOD == null) {
            return null;
        }
        try {
            return (ResourceLocation) LOOT_TABLE_METHOD.invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------------------------------------------------------
    // Structure
    // ---------------------------------------------------------------------

    public static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:mob_killer",
                    MetaTileEntityExtremeExterminationChamber::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("XXXXX", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG", "GAAAG", "XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG", "GAAAG", "XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG", "GAAAG", "XXXXX")
                .aisle("XXSXX", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "XXXXX")
                .self('S', MetaTileEntityExtremeExterminationChamber.class)
                .where('X', Elements.chain(
                        Elements.counted(10, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.MUFFLER_HATCH, 1, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 1, -1),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 1, -1),
                        Elements.hatch(MultiblockAbility.EXPORT_FLUIDS, 1, -1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 2)))
                .blocks('G', getGlassState())
                .frames('F', Materials.Steel)
                .air('A')
                .blocks('#', Blocks.END_ROD)
                .buildStructureDefinition();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        return gregtech.client.renderer.texture.Textures.SOLID_STEEL_CASING;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityExtremeExterminationChamber(metaTileEntityId);
    }

    // ---------------------------------------------------------------------
    // Tick logic
    // ---------------------------------------------------------------------

    @Override
    public void updateFormedValid() {
        super.updateFormedValid();
        if (getWorld().isRemote) {
            return;
        }
        if (energyContainer.getEnergyStored() < BASE_ENERGY_CONSUMPTION) {
            return;
        }

        outputMultiplier = calculateOutputMultiplier(
                GTUtility.getTierByVoltage(energyContainer.getInputVoltage()));

        if (totalProcessTicks == 0) {
            startProcessing();
            return;
        }
        if (processTicks < totalProcessTicks) {
            tickProcessing();
            return;
        }
        finishProcessing();
    }

    private void startProcessing() {
        EntityLiving template = inventory.getMob();
        if (template == null) {
            return;
        }
        if (!spawnMobInstance(template)) {
            return;
        }

        int looting = findFirstWeaponLootingLevel();
        displayedLooting = looting;
        totalProcessTicks = calculateProcessTime(currentMob.getMaxHealth(), looting);
        processTicks = 0;
        currentMobName = currentMob.getName();
    }

    private void tickProcessing() {
        energyContainer.changeEnergy(-BASE_ENERGY_CONSUMPTION);
        if (processTicks % DURABILITY_TICK_INTERVAL == 0) {
            consumeWeaponDurability();
        }
        processTicks++;
    }

    private void finishProcessing() {
        if (currentMob == null) {
            resetProcessing();
            return;
        }

        List<ItemStack> drops = generateDrops(currentMob, displayedLooting);
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            drop.setCount(drop.getCount() * outputMultiplier);
            GTTransferUtils.insertItem(getOutputInventory(), drop, false);
        }

        FluidStack xp = new FluidStack(XP_JUICE.getFluid(), BASE_XP_OUTPUT * outputMultiplier);
        GTTransferUtils.addFluidsToFluidHandler(getOutputFluidInventory(), false, Collections.singletonList(xp));

        currentMob.setDead();
        resetProcessing();
    }

    private void resetProcessing() {
        currentMob = null;
        currentMobName = "";
        processTicks = 0;
        totalProcessTicks = 0;
        displayedLooting = 0;
    }

    // ---------------------------------------------------------------------
    // Weapon / looting helpers
    // ---------------------------------------------------------------------

    /**
     * Find the first sword/tool slot in the input inventory.
     *
     * @return slot index, or -1 if no weapon is present.
     */
    private int findFirstWeaponSlot() {
        for (int i = 0; i < getInputInventory().getSlots(); i++) {
            ItemStack stack = getInputInventory().getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) {
                return i;
            }
        }
        return -1;
    }

    private void consumeWeaponDurability() {
        int slot = findFirstWeaponSlot();
        if (slot < 0) {
            return;
        }
        ItemStack weapon = getInputInventory().getStackInSlot(slot);
        if (weapon.attemptDamageItem(1, getWorld().rand, null)) {
            getInputInventory().setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private int findFirstWeaponLootingLevel() {
        int slot = findFirstWeaponSlot();
        if (slot < 0) {
            return 0;
        }
        int level = EnchantmentHelper.getEnchantmentLevel(
                Enchantments.LOOTING, getInputInventory().getStackInSlot(slot));
        return Math.min(level, MAX_LOOTING_LEVEL);
    }

    // ---------------------------------------------------------------------
    // Mob spawning / drops
    // ---------------------------------------------------------------------

    private boolean spawnMobInstance(EntityLiving template) {
        try {
            currentMob = template.getClass().getConstructor(World.class).newInstance(getWorld());
            currentMob.readFromNBT(template.writeToNBT(new NBTTagCompound()));
            currentMob.setHealth(currentMob.getMaxHealth());

            EnumFacing back = RelativeDirection.BACK.getRelativeFacing(
                    getFrontFacing(), getUpwardsFacing(), isFlipped());
            currentMob.setPosition(
                    getPos().getX() + back.getXOffset() * 2 + 0.5,
                    getPos().getY() + 1,
                    getPos().getZ() + back.getZOffset() * 2 + 0.5);

            getWorld().spawnEntity(currentMob);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int calculateProcessTime(float maxHealth, int looting) {
        int base = Math.max(MIN_PROCESS_TICKS, (int) (maxHealth * HEALTH_TO_TICK_RATIO));
        float reduction = Math.min(looting, MAX_LOOTING_LEVEL) * LOOTING_TIME_REDUCTION;
        return Math.max(MIN_PROCESS_TICKS, (int) (base * (1.0f - reduction)));
    }

    private int calculateOutputMultiplier(int tier) {
        // 2^((tier - 1) / 2), clamped to minimum 1
        return tier < 1 ? 1 : (1 << ((tier - 1) / 2));
    }

    private List<ItemStack> generateDrops(EntityLiving mob, int looting) {
        ResourceLocation lootLoc = getLootTable(mob);
        if (lootLoc == null) {
            return Collections.emptyList();
        }

        World world = mob.world;
        EntityPlayer player = server.getPlayerList().getPlayerByUUID(getOwnerGT());
        LootTable table = world.getLootTableManager().getLootTableFromLocation(lootLoc);
        LootContext ctx = new LootContext.Builder((WorldServer) world)
                .withLootedEntity(mob)
                .withPlayer(player)
                .withLuck(looting)
                .withDamageSource(DamageSource.MAGIC)
                .build();
        return table.generateLootForPools(world.rand, ctx);
    }

    // ---------------------------------------------------------------------
    // NBT
    // ---------------------------------------------------------------------

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag(NBT_INVENTORY, inventory.serializeNBT());
        data.setInteger(NBT_PROCESS_TICKS, processTicks);
        data.setInteger(NBT_TOTAL_PROCESS_TICKS, totalProcessTicks);
        data.setInteger(NBT_OUTPUT_MULTIPLIER, outputMultiplier);
        data.setInteger(NBT_DISPLAYED_LOOTING, displayedLooting);
        data.setString(NBT_CURRENT_MOB_NAME, currentMobName);
        // Ensure any spawned entity is removed before the chunk is written.
        if (currentMob != null) {
            currentMob.setDead();
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inventory.deserializeNBT(data.getCompoundTag(NBT_INVENTORY));
        processTicks = data.getInteger(NBT_PROCESS_TICKS);
        totalProcessTicks = data.getInteger(NBT_TOTAL_PROCESS_TICKS);
        outputMultiplier = data.getInteger(NBT_OUTPUT_MULTIPLIER);
        displayedLooting = data.getInteger(NBT_DISPLAYED_LOOTING);
        currentMobName = data.getString(NBT_CURRENT_MOB_NAME);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (currentMob != null) {
            currentMob.setDead();
        }
        resetProcessing();
    }

    // ---------------------------------------------------------------------
    // Tooltip / display
    // ---------------------------------------------------------------------

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        InformationHandler.topTooltips("为你生成并屠宰怪物！", tooltip);
        TooltipBuilder.create().addSpecialLogic().build(this, tooltip);
        tooltip.add(I18n.format("将EnderIO的灵魂瓶置入UI内以生成怪物"));
        tooltip.add(I18n.format("基础耗能：1920 EU/t,每超频一次产物翻倍"));
        tooltip.add(I18n.format("每次运行生产120L液体经验"));
        tooltip.add(I18n.format("最低时间：20t，继续超频会使产物翻4倍"));
        tooltip.add(I18n.format("配方时间取决于怪物血量"));
        tooltip.add(I18n.format("以加速运行并应用其抢夺等级（最高4级）"));
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addCustom((keyManager, syncManager) -> {
            String name = syncManager.syncString(currentMobName);
            int ticksSync = syncManager.syncInt(processTicks);
            int totalSync = syncManager.syncInt(totalProcessTicks);
            int multSync = syncManager.syncInt(outputMultiplier);

            if (totalSync > 0) {
                int pct = (int) (ticksSync * 100L / totalSync);
                keyManager.add(IKey.str("§6 处理：" + name));
                keyManager.add(IKey.str("§7 进度：" + pct + "% §8| §b 倍率：x" + multSync));
                keyManager.add(IKey.str("§7 耗时：" + totalSync + "t §8| §a 能耗：" + BASE_ENERGY_CONSUMPTION + "EU/t"));
            } else {
                keyManager.add(IKey.str("§a 待机中... §7(放入灵魂瓶启动)"));
            }
        });
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------

    @Override
    protected MultiblockUIFactory createUIFactory() {
        return super.createUIFactory()
                .createFlexButton((guiData, syncManager) -> {
                    var throttle = syncManager.panel("throttle_panel", this::makeThrottlePanel, true);
                    return new ButtonWidget<>()
                            .size(18)
                            .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                            .onMousePressed(i -> {
                                if (throttle.isPanelOpen()) {
                                    throttle.closePanel();
                                } else {
                                    throttle.openPanel();
                                }
                                return true;
                            });
                });
    }

    private ModularPanel makeThrottlePanel(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        syncManager.registerSlotGroup("item_inv", 1);
        return GTGuis.createPopupPanel("boiler_throttle", 50, 50)
                .child(new ItemSlot()
                        .pos(15, 15)
                        .slot(SyncHandlers.itemSlot(inventory, 0)
                                .slotGroup("item_inv")
                                .changeListener((newItem, onlyAmountChanged, client, init) ->
                                        inventory.onContentsChanged(0))));
    }

    // ---------------------------------------------------------------------
    // Mob card holder
    // ---------------------------------------------------------------------

    private class MobCardHolder extends GTItemStackHandler {

        public MobCardHolder(MetaTileEntity mte) {
            super(mte);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Nullable
        public EntityLiving getMob() {
            ItemStack stack = getStackInSlot(0);
            if (!(stack.getItem() instanceof crazypants.enderio.base.item.soulvial.ItemSoulVial)) {
                return null;
            }
            CapturedMob captured = CapturedMob.create(stack);
            if (captured == null) {
                return null;
            }
            Entity entity = captured.getEntity(getWorld(), false);
            return entity instanceof EntityLiving ? (EntityLiving) entity : null;
        }
    }
}