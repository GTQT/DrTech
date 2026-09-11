package com.meowmel.cropQT.item;

import com.drppp.drtech.DrTechMain;
import com.meowmel.cropQT.item.behavior.FertilizerApplicatorBehavior;
import com.meowmel.cropQT.item.behavior.WateringCanBehavior;
import gregtech.api.items.metaitem.FilteredFluidStats;
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
 * </ul>
 *
 * <p>模型按 GT 的约定放在 {@code assets/gregtech/models/item/metaitems/<名字>.json}。
 */
public class MetaItemCropTools extends StandardMetaItem {

    /** 浇水壶的容量。 */
    public static final int WATERING_CAN_CAPACITY = 4000;

    public static MetaItem<?>.MetaValueItem FERTILIZER_APPLICATOR;
    public static MetaItem<?>.MetaValueItem WATERING_CAN;

    public MetaItemCropTools() {
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public void registerSubItems() {
        FERTILIZER_APPLICATOR = this.addItem(0, "fertilizer_applicator")
                .addComponents(new FertilizerApplicatorBehavior());

        WATERING_CAN = this.addItem(1, "watering_can")
                // allowPartialFill = true：一次只倒 500 mB，必须支持部分填充
                .addComponents(new FilteredFluidStats(WATERING_CAN_CAPACITY, true, null))
                .addComponents(new WateringCanBehavior());
    }
}
