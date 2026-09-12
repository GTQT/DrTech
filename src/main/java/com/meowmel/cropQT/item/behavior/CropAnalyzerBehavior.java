package com.meowmel.cropQT.item.behavior;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.EnvironmentCalculator;
import com.meowmel.cropQT.api.GrowthRequirement;
import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 作物分析仪：手持的种子分析工具。
 *
 * <h2>两个功能，两条触发路径</h2>
 * <ul>
 *     <li><b>潜行 + 右键</b> → 打开界面，把种子放进输入槽分析</li>
 *     <li><b>直接右键作物架</b> → 在聊天栏打印一份现场报告（土壤/水肥/环境分）</li>
 * </ul>
 * 潜行这条是干净的：{@code onItemUse} 一见潜行就放行，
 * 于是无论指着什么方块，最终都由 {@code onItemRightClick} 开界面 ——
 * MC 本身的规矩就是潜行跳过方块交互。
 *
 * <h2>为什么是充能物品</h2>
 * 分析不再白送 —— 每次分析要耗 {@link #ENERGY_PER_SCAN} EU，得先充电。
 * 这与 GT 的扫描仪同价（都是 HV 档），手持胜在不用回基地。
 *
 * <h2>槽位为什么不存档</h2>
 * 照搬 GT 自己的 {@code ProgrammingToolkit}：界面里的槽位是<b>临时</b>的，
 * 关界面时内容物会掉在脚下（像工作台）。把槽位写进物品 NBT 要处理一堆同步边界，
 * 而「分析完顺手拿走」本来就是这个界面的用法。
 */
public class CropAnalyzerBehavior implements ItemUIFactory, IItemBehaviour {

    /** 一次分析耗多少 EU。与扫描仪同价。 */
    public static final long ENERGY_PER_SCAN = GTValues.VA[GTValues.HV];
    /** 电池容量：满电够分析 {@code 24000 / 480 = 50} 次。 */
    public static final long CAPACITY = ENERGY_PER_SCAN * 50;
    /** 充电档位：MV，免得前期拿到也用不了。 */
    public static final int TIER = GTValues.MV;

    /**
     * 界面高度。
     *
     * <p>不是标准的 166 —— 玩家背包由 {@code bindPlayerInventory()} 挂在面板<b>底部</b>，
     * 内容区就只剩上面的 83 像素，塞不下「标题 + 两个槽 + 电量 + 四行信息」。
     * 加高到 192 让内容区变成 109 像素，最后一行信息刚好落在背包上方。
     */
    private static final int PANEL_HEIGHT = 192;

    // ==================== 触发：潜行右键开界面 ====================

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            // 不潜行就什么都不做，让方块那边的「看作物架」逻辑走
            return pass(held);
        }
        if (!world.isRemote) {
            MetaItemGuiFactory.open(player, hand);
        }
        return success(held);
    }

    // ==================== 触发：右键作物架出报告 ====================

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (player.isSneaking()) {
            // 潜行一律是「开界面」，交给 onItemRightClick —— 否则潜行对着作物架
            // 会出报告、对着空地会开界面，同一个手势两种结果
            return pass(held);
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCropStick)) {
            return pass(held);
        }
        if (world.isRemote) {
            // 报告是服务端发的；客户端认出这是作物架就到此为止，
            // 别再往下传到 onItemRightClick 去
            return success(held);
        }
        TileCropStick tile = (TileCropStick) te;

        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "══════ 作物分析报告 ══════"));
        if (!tile.hasCrop()) {
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + "状态: " +
                    (tile.isDoubleCropStick() ? "杂交模式(等待中)" : "空")));
        } else {
            CropType type = tile.getCropType();
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "作物: " + TextFormatting.WHITE +
                    (type != null ? type.getDisplayName() : tile.getCropId())));
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Tier: " + TextFormatting.WHITE +
                    (type != null ? type.getTier() : "?")));
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE +
                    tile.getGrowthStage() + "/" + (type != null ? type.getMaxGrowthStage() : "?")));
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "成熟: " + TextFormatting.WHITE +
                    (tile.isMature() ? "是 ✔" : "否")));
            describeStats(player, tile);
            if (tile.isWeedPlant()) {
                player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "⚠ 这是杂草!"));
            }
        }
        describeEnvironment(player, world, pos, tile);
        return success(held);
    }

    private static void describeStats(EntityPlayer player, TileCropStick tile) {
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "--- 属性 ---"));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "  Growth:     " +
                buildStatBar(tile.getStats().getGrowth()) + TextFormatting.WHITE + " " + tile.getStats().getGrowth()));
        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  Gain:       " +
                buildStatBar(tile.getStats().getGain()) + TextFormatting.WHITE + " " + tile.getStats().getGain()));
        player.sendMessage(new TextComponentString(TextFormatting.BLUE + "  Resistance: " +
                buildStatBar(tile.getStats().getResistance()) + TextFormatting.WHITE + " " +
                tile.getStats().getResistance()));
    }

    private static void describeEnvironment(EntityPlayer player, World world, BlockPos pos, TileCropStick tile) {
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "--- 环境 ---"));
        float light = EnvironmentCalculator.calcLight(world, pos);
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        float nutrients = EnvironmentCalculator.calcNutrients(world, pos);
        float baseScore = EnvironmentCalculator.calcEnvironmentScore(world, pos);
        float envScore = EnvironmentCalculator.calcEnvironmentScore(world, pos,
                tile.getWaterRatio(), tile.getFertilizerRatio());

        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  光照: " + TextFormatting.WHITE +
                String.format("%.0f%%", light * 100)));
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  湿度: " + TextFormatting.WHITE +
                String.format("%.0f%%", humidity * 100)));
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  营养: " + TextFormatting.WHITE +
                String.format("%.0f%%", nutrients * 100)));
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  水位: " + TextFormatting.WHITE +
                tile.getWaterStorage() + "/" + tile.getMaxWater() + TextFormatting.GRAY +
                String.format(" (+%.0f%%)", tile.getWaterRatio() * EnvironmentCalculator.WATER_BONUS * 100)));
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  肥位: " + TextFormatting.WHITE +
                tile.getFertilizerStorage() + "/" + tile.getMaxFertilizer() + TextFormatting.GRAY +
                String.format(" (+%.0f%%)", tile.getFertilizerRatio() * EnvironmentCalculator.FERTILIZER_BONUS * 100)));
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  综合: " + TextFormatting.WHITE +
                String.format("%.0f%%", envScore * 100) + TextFormatting.GRAY +
                String.format("（基础 %.0f%%）", baseScore * 100)));

        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "--- 种植条件 ---"));
        ISoilList soil = tile.getSoilType();
        player.sendMessage(new TextComponentString(TextFormatting.AQUA + "  土壤: " + TextFormatting.WHITE +
                (soil == null ? "非土壤（种不下去）" : localizeSoil(soil.getName()))));

        CropType cropType = tile.getCropType();
        if (cropType == null) {
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + "  底土: —（先种上作物）"));
        } else if (!cropType.hasSubSoilRequirement()) {
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + "  底土: 无要求"));
        } else {
            List<GrowthRequirement> unmet = tile.getUnmetRequirements();
            if (unmet.isEmpty()) {
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "  底土: 满足"));
            } else {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "  底土: 缺少 " +
                        TextFormatting.WHITE + unmet.get(0).getDisplayName() +
                        TextFormatting.RED + "（生长速度大幅下降）"));
            }
        }
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "══════════════════════════"));
    }

    // ==================== 界面 ====================

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        ItemStack analyzer = guiData.getUsedItemStack();
        ModularPanel panel = GTGuis.createPanel(analyzer, 176, PANEL_HEIGHT);

        // 一次一袋：槽位限 1 个，免得整摞塞进去只出来一袋
        ItemStackHandler input = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isUnanalyzedSeed(stack);
            }
        };
        ItemStackHandler output = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return false;
            }
        };

        LongSyncValue chargeSync = new LongSyncValue(() -> getCharge(analyzer));
        syncManager.syncValue("cropqt_analyzer_charge", chargeSync);

        // 每 tick 试一次：输入槽里有未分析的种子、电够，就出结果。
        // 放 tick 里而不是监听输入槽变化，是因为电量本身也会变 —— 充上电之后
        // 应该自动继续，不该要求玩家把种子拿出来再放进去。
        // 只跑服务端：客户端那份物品栈是同步来的影子，在上面扣电只会闪一下再被覆盖。
        syncManager.onServerTick(() -> tryAnalyze(analyzer, input, output));
        // 关界面时把槽里的东西丢出来，别凭空消失
        syncManager.addCloseListener(player -> dropContents(player, input, output));

        panel.child(IKey.lang("cropqt.analyzer.title").asWidget().pos(6, 6));

        panel.child(new ItemSlot()
                .pos(40, 24)
                .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                .slot(SyncHandlers.itemSlot(input, 0).singletonSlotGroup().accessibility(true, true)));
        panel.child(IKey.str("→").asWidget().pos(78, 29));
        panel.child(new ItemSlot()
                .pos(116, 24)
                .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                .slot(SyncHandlers.itemSlot(output, 0).singletonSlotGroup().accessibility(false, true)));

        // 电量
        panel.child(IKey.dynamic(() -> I18n.format(
                        "cropqt.analyzer.charge", chargeSync.getLongValue(), CAPACITY))
                .asWidget().pos(6, 48));

        // 信息区：直接读输出槽里的种子（它已经被 MUI2 同步到客户端了），
        // 不需要额外的同步通道
        panel.child(IKey.dynamic(() -> infoLine(output.getStackInSlot(0), 0)).asWidget().pos(6, 62));
        panel.child(IKey.dynamic(() -> infoLine(output.getStackInSlot(0), 1)).asWidget().pos(6, 74));
        panel.child(IKey.dynamic(() -> infoLine(output.getStackInSlot(0), 2)).asWidget().pos(6, 86));
        panel.child(IKey.dynamic(() -> infoLine(output.getStackInSlot(0), 3)).asWidget().pos(6, 98));

        return panel.bindPlayerInventory();
    }

    /** 输出槽空了就是还没分析，提示玩家怎么用。 */
    private static String infoLine(ItemStack analyzed, int line) {
        if (analyzed.isEmpty()) {
            return line == 0 ? I18n.format("cropqt.analyzer.hint") : "";
        }
        CropType type = CropRegistry.get(ItemCropSeed.getCropId(analyzed));
        if (type == null) {
            return "";
        }
        CropStats stats = ItemCropSeed.getCropStats(analyzed);
        switch (line) {
            case 0:
                return I18n.format("cropqt.analyzer.info.name", type.getDisplayName(), type.getTier());
            case 1:
                return I18n.format("cropqt.analyzer.info.soil", describeSoil(type.getSoilTypes()));
            case 2:
                return I18n.format("cropqt.analyzer.info.subsoil", describeSubSoil(type));
            default:
                return I18n.format("cropqt.analyzer.info.stats",
                        stats.getGrowth(), stats.getGain(), stats.getResistance());
        }
    }

    // ==================== 分析逻辑 ====================

    private static void tryAnalyze(ItemStack analyzer, ItemStackHandler input, ItemStackHandler output) {
        if (!output.getStackInSlot(0).isEmpty()) {
            return;     // 产物还没拿走，别覆盖
        }
        ItemStack seed = input.getStackInSlot(0);
        if (!isUnanalyzedSeed(seed)) {
            return;
        }
        String cropId = ItemCropSeed.getCropId(seed);
        if (cropId.isEmpty() || !CropRegistry.exists(cropId)) {
            return;
        }

        IElectricItem electric = analyzer.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electric == null || electric.getCharge() < ENERGY_PER_SCAN) {
            return;     // 没电就干等着，玩家充上电会自动继续
        }

        long used = electric.discharge(ENERGY_PER_SCAN, TIER, true, false, false);
        if (used < ENERGY_PER_SCAN) {
            return;
        }
        // 只取一袋。槽位限 1 已经保证了这点，这里再按数量扣一次 —— 万一哪天
        // 上限被放宽，也不会出现「一摞种子换一袋」这种吃物品的事
        input.extractItem(0, 1, false);
        output.setStackInSlot(0, ItemCropSeed.createSeedBag(cropId, ItemCropSeed.getCropStats(seed).analyze()));
    }

    private static boolean isUnanalyzedSeed(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof ItemCropSeed
                && !ItemCropSeed.getCropId(stack).isEmpty()
                && !ItemCropSeed.getCropStats(stack).isAnalyzed();
    }

    private static long getCharge(ItemStack analyzer) {
        IElectricItem electric = analyzer.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electric == null ? 0L : electric.getCharge();
    }

    private static void dropContents(EntityPlayer player, ItemStackHandler input, ItemStackHandler output) {
        if (player.world.isRemote) {
            return;
        }
        for (int slot = 0; slot < input.getSlots(); slot++) {
            ItemStack stack = input.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                player.dropItem(stack.copy(), false);
                input.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        for (int slot = 0; slot < output.getSlots(); slot++) {
            ItemStack stack = output.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                player.dropItem(stack.copy(), false);
                output.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    // ==================== 文本 ====================

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        lines.add(I18n.format("metaitem.crop_analyzer.tooltip.1"));
        lines.add(I18n.format("metaitem.crop_analyzer.tooltip.2"));
        lines.add(I18n.format("metaitem.crop_analyzer.tooltip.3"));
        lines.add(I18n.format("metaitem.crop_analyzer.tooltip.4"));
    }

    private static String describeSoil(@Nullable ISoilList soil) {
        return soil == null ? "不限" : localizeSoil(soil.getName());
    }

    private static String describeSubSoil(CropType type) {
        if (!type.hasSubSoilRequirement()) {
            return "不限";
        }
        return type.getSubSoilRequirement().getDisplayName();
    }

    /** 土壤组的本地化名；键缺失时退回名字本身，别显示成 {@code cropqt.soil.xxx}。 */
    private static String localizeSoil(String name) {
        String key = "cropqt.soil." + name;
        String text = new TextComponentTranslation(key).getUnformattedText();
        return text.startsWith("cropqt.soil.") ? name : text;
    }

    private static String buildStatBar(int value) {
        StringBuilder bar = new StringBuilder(TextFormatting.GREEN + "[");
        int filled = value * 10 / 31;
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append(value >= 24 ? TextFormatting.RED : TextFormatting.GREEN).append("|");
            } else {
                bar.append(TextFormatting.DARK_GRAY).append(".");
            }
        }
        bar.append(TextFormatting.GREEN).append("]");
        return bar.toString();
    }
}
