package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

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
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

public class MetaTileEntityExtremeExterminationChamber extends MetaTileEntityBaseWithControl {

    private static final int BASE_ENERGY_CONSUMPTION = 1920;  // 固定能耗 EU/t
    private static final int BASE_XP_OUTPUT = 120;             // 固定经验 mB
    private static final int MIN_PROCESS_TICKS = 20;           // 最低运行时间
    private static final float HEALTH_TO_TICK_RATIO = 0.5f;    // 血量→时间系数
    private static final float LOOTING_TIME_REDUCTION = 0.1f;  // 每级抢夺加速10%
    private static final int MAX_LOOTING_LEVEL = 4;            // 抢夺生效上限
    private static Method lootTableMethod;

    static {
        try {
            lootTableMethod = EntityLiving.class.getDeclaredMethod("getLootTable");
            lootTableMethod.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
    }

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

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getCasingState3() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    private static ResourceLocation getLootTable(EntityLiving entity) {
        if (lootTableMethod == null) return null;
        try {
            return (ResourceLocation) lootTableMethod.invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityExtremeExterminationChamber(this.metaTileEntityId);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.addCustom((keyManager, syncManager) -> {
            String name = syncManager.syncString(currentMobName);
            keyManager.add(IKey.str("正在处理：" + name));

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
                                .changeListener(
                                        (newItem, onlyAmountChanged, client, init) -> inventory.onContentsChanged(0))));
    }

    @Override
    public void updateFormedValid() {
        super.updateFormedValid();
        if (getWorld().isRemote) return;

        if (energyContainer.getEnergyStored() < BASE_ENERGY_CONSUMPTION) return;

        int tier = GTUtility.getTierByVoltage(energyContainer.getInputVoltage());
        outputMultiplier = calculateOutputMultiplier(tier);

        if (currentMob == null) {
            totalProcessTicks = 0;
        }

        if (totalProcessTicks == 0) {
            EntityLiving mob = inventory.getMob();
            if (mob == null) return;

            if (!spawnMobInstance(mob)) return;

            int looting = extractLootingLevel();
            displayedLooting = looting;

            totalProcessTicks = calculateProcessTime(currentMob.getMaxHealth(), looting);
            processTicks = 0;
            currentMobName = currentMob.getName();

            return;
        }

        if (processTicks < totalProcessTicks) {
            energyContainer.changeEnergy(-BASE_ENERGY_CONSUMPTION);
            if (processTicks % 5 == 0)
                currentMob.attackEntityFrom(DamageSource.GENERIC, 1);

            processTicks++;
            return;
        }

        finishProcessing();
    }

    private boolean spawnMobInstance(EntityLiving template) {
        try {
            currentMob = template.getClass().getConstructor(World.class).newInstance(getWorld());
            NBTTagCompound tag = template.writeToNBT(new NBTTagCompound());
            currentMob.readFromNBT(tag);
            currentMob.setHealth(currentMob.getMaxHealth());

            EnumFacing relativeBack = RelativeDirection.BACK.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                    isFlipped());

            currentMob.setPosition(getPos().getX() + relativeBack.getXOffset() * 2 + 0.5, getPos().getY() + 1, getPos().getZ() + relativeBack.getZOffset() * 2 + 0.5);

            getWorld().spawnEntity(currentMob);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private int extractLootingLevel() {
        for (int i = 0; i < getInputInventory().getSlots(); i++) {
            ItemStack stack = getInputInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) {
                int level = net.minecraft.enchantment.EnchantmentHelper
                        .getEnchantmentLevel(net.minecraft.init.Enchantments.LOOTING, stack);
                return Math.min(level, MAX_LOOTING_LEVEL);
            }
        }
        return 0;
    }

    private int calculateProcessTime(float maxHealth, int looting) {
        int base = Math.max(MIN_PROCESS_TICKS, (int) (maxHealth * HEALTH_TO_TICK_RATIO));
        float reduction = Math.min(looting, MAX_LOOTING_LEVEL) * LOOTING_TIME_REDUCTION;
        return Math.max(MIN_PROCESS_TICKS, (int) (base * (1.0f - reduction)));
    }


    private int calculateOutputMultiplier(int tier) {
        return tier < 1 ? 1 : (1 << ((tier - 1) / 2));
    }


    private void finishProcessing() {
        if (currentMob == null) {
            resetProcessing();
            return;
        }

        List<ItemStack> drops = generateDrops(currentMob, displayedLooting);

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                drop.setCount(drop.getCount() * outputMultiplier);
                GTTransferUtils.insertItem(getOutputInventory(), drop, false);
            }
        }

        FluidStack xp = new FluidStack(XP_JUICE.getFluid(), BASE_XP_OUTPUT * outputMultiplier);
        GTTransferUtils.addFluidsToFluidHandler(getOutputFluidInventory(), false, Collections.singletonList(xp));

        currentMob.setDead();

        resetProcessing();
    }

    private List<ItemStack> generateDrops(EntityLiving mob, int looting) {
        List<ItemStack> result = new ArrayList<>();
        World world = mob.world;
        EntityPlayer player = server.getPlayerList().getPlayerByUUID(getOwnerGT());
        ResourceLocation lootLoc = getLootTable(mob);

        // LootTable掉落
        if (lootLoc != null) {
            LootTable table = world.getLootTableManager().getLootTableFromLocation(lootLoc);
            LootContext ctx = new LootContext.Builder((WorldServer) world)
                    .withLootedEntity(mob).withPlayer(player)
                    .withLuck(looting).withDamageSource(DamageSource.MAGIC)
                    .build();
            result.addAll(table.generateLootForPools(world.rand, ctx));
        }
        return result;
    }

    private void resetProcessing() {
        currentMob = null;
        currentMobName = "";
        processTicks = 0;
        totalProcessTicks = 0;
        displayedLooting = 0;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG", "GAAAG", "XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG", "GAAAG", "XXXXX")
                .aisle("XXXXX", "G###G", "GAAAG", "GAAAG", "GAAAG", "GAAAG", "XXXXX")
                .aisle("XXSXX", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "FGGGF", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(10)
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                .where('G', states(getCasingState3()))
                .where('F', frames(Materials.Steel))
                .where('A', air())
                .where('#', blocks(Blocks.END_ROD))
                .build();
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.serializeNBT());
        data.setInteger("processTicks", processTicks);
        data.setInteger("totalProcessTicks", totalProcessTicks);
        data.setInteger("outputMultiplier", outputMultiplier);
        data.setInteger("displayedLooting", displayedLooting);
        data.setString("currentMobName", currentMobName);
        currentMob.setDead();
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inventory.deserializeNBT(data.getCompoundTag("inventory"));
        processTicks = data.getInteger("processTicks");
        totalProcessTicks = data.getInteger("totalProcessTicks");
        outputMultiplier = data.getInteger("outputMultiplier");
        displayedLooting = data.getInteger("displayedLooting");
        currentMobName = data.getString("currentMobName");
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (currentMob != null) currentMob.setDead();
        resetProcessing();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        InformationHandler.topTooltips("为你生成并屠宰怪物！", tooltip);
        super.addInformation(stack, player, tooltip, advanced);
        TooltipBuilder.create().addSpecialLogic().build(this, tooltip);
        tooltip.add(I18n.format("将EnderIO的灵魂瓶置入UI内以生成怪物"));
        tooltip.add(I18n.format("基础耗能：1920 EU/t,每超频一次产物翻倍"));
        tooltip.add(I18n.format("每次运行生产120L液体经验"));
        tooltip.add(I18n.format("最低时间：20t，继续超频会使产物翻4倍"));
        tooltip.add(I18n.format("配方时间取决于怪物血量"));
        tooltip.add(I18n.format("以加速运行并应用其抢夺等级（最高4级）"));
    }

    private class MobCardHolder extends GTItemStackHandler {
        public MobCardHolder(MetaTileEntity mte) {
            super(mte);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        public EntityLiving getMob() {
            ItemStack stack = getStackInSlot(0);
            if (!(stack.getItem() instanceof crazypants.enderio.base.item.soulvial.ItemSoulVial)) return null;
            CapturedMob captured = CapturedMob.create(stack);
            if (captured == null) return null;
            Entity e = captured.getEntity(getWorld(), false);
            return (e instanceof EntityLiving) ? (EntityLiving) e : null;
        }
    }

}