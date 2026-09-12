package com.meowmel.cropQT.item;

import com.drppp.drtech.DrTechMain;
import com.meowmel.cropQT.item.behavior.CropAnalyzerBehavior;
import com.meowmel.cropQT.item.behavior.FertilizerApplicatorBehavior;
import com.meowmel.cropQT.item.behavior.WateringCanBehavior;
import gregtech.api.items.metaitem.FilteredFluidStats;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;

/**
 * 作物系统的工具物品。
 *
 * <p>两个都是 GT {@link MetaItem}，行为拆成 {@code IItemBehaviour} 组件：
 * <ul>
 *     <li>{@link #FERTILIZER_APPLICATOR} —— 一次性施肥器，自带耐久（NBT 存），右键作物架施肥</li>
 *     <li>{@link #WATERING_CAN} —— 浇水壶，靠 {@link FilteredFluidStats} 获得流体能力，
 *         装水浇水、装液体肥料就施肥</li>
 *     <li>{@link #CROP_ANALYZER} —— 作物分析仪，充能物品，潜行右键开界面分析种子</li>
 * </ul>
 *
 * <p>模型按 GT 的约定放在 {@code assets/gregtech/models/item/metaitems/<名字>.json}；
 * 多模型物品则是 {@code <名字>/1.json}、{@code /2.json}（索引+1）。
 */
public class MetaItemCropTools extends StandardMetaItem {

    /** 浇水壶的容量。 */
    public static final int WATERING_CAN_CAPACITY = 4000;

    public static MetaItem<?>.MetaValueItem FERTILIZER_APPLICATOR;
    public static MetaItem<?>.MetaValueItem WATERING_CAN;
    public static MetaItem<?>.MetaValueItem CROP_ANALYZER;

    public MetaItemCropTools() {
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public void registerSubItems() {
        FERTILIZER_APPLICATOR = this.addItem(0, "fertilizer_applicator")
                .addComponents(new FertilizerApplicatorBehavior());

        // 分析仪：充能物品，潜行右键空气开界面分析种子
        CROP_ANALYZER = this.addItem(2, "crop_analyzer")
                .addComponents(ElectricStats.createElectricItem(
                        CropAnalyzerBehavior.CAPACITY, CropAnalyzerBehavior.TIER))
                .addComponents(new CropAnalyzerBehavior());

        WATERING_CAN = this.addItem(1, "watering_can")
                // 两个模型：1 = 有液体，2 = 空壶（索引由 WateringCanBehavior 给）
                .setModelAmount(2)
                // allowPartialFill = true：一次只倒 500 mB，必须支持部分填充
                .addComponents(new FilteredFluidStats(WATERING_CAN_CAPACITY, true, null))
                .addComponents(new WateringCanBehavior());
    }
}
