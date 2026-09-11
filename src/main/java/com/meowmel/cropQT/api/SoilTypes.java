package com.meowmel.cropQT.api;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.init.Blocks;

/**
 * 预定义的土壤组。
 *
 * <p>登记顺序就是 {@link SoilRegistry#getSoilFor} 的匹配优先级——具体的组排在前面，
 * 笼统的组排在后面。改动顺序前先想清楚一个方块会不会被前面的组先吃掉。
 *
 * <p>保水/保肥上限的语义：值越大，同样一次浇水能撑越久，储量加成也越容易吃满。
 * 石头类几乎不保水，所以矿石叶必须频繁补水——这是设计意图，不是遗漏。
 */
public final class SoilTypes {

    /** 耕地。默认土壤，各项属性居中偏上。 */
    public static final SoilList farmland = new SoilList("farmland");
    /** 泥土 / 草方块。 */
    public static final SoilList dirtGrass = new SoilList("dirt_grass");
    /** 石头类（含花岗岩 / 闪长岩 / 安山岩变体）。几乎不保水，矿石叶用。 */
    public static final SoilList stone = new SoilList("stone");
    /** 沙子类（含红沙）。保水极低，仙人掌 / 干旱作物用。 */
    public static final SoilList sand = new SoilList("sand");
    /** 菌丝 / 灰化土。蘑菇用。 */
    public static final SoilList mushroom = new SoilList("mushroom");
    /** 下界岩。 */
    public static final SoilList netherrack = new SoilList("netherrack");
    /** 末地石。 */
    public static final SoilList end = new SoilList("end");
    /** 沙砾。 */
    public static final SoilList gravel = new SoilList("gravel");
    /** 灵魂沙。 */
    public static final SoilList soulsand = new SoilList("soul_sand");
    /** 砖块。 */
    public static final SoilList brick = new SoilList("brick");
    /** 水体。给需要在水中生长的作物用。 */
    public static final SoilList water = new SoilList("water");

    private static final int HIGH = 4000;
    private static final int MID = 2500;
    private static final int LOW = 1000;
    private static final int TINY = 250;

    static {
        registerAll();
    }

    private SoilTypes() {
    }

    /**
     * 强制触发类初始化。
     *
     * <p>调用本方法（或访问任意静态字段）都会执行下面的静态块完成登记。
     * 提供这个空方法是为了让初始化时机显式可读，而不是靠隐式触碰。
     */
    public static void ensureRegistered() {
        // 方法体为空是故意的：调用静态方法本身就会触发 <clinit>
    }

    private static void registerAll() {
        // ---- 特定组优先 ----
        farmland.registerBlock(Blocks.FARMLAND)
                .setWaterCapacity(HIGH).setFertilizerCapacity(HIGH).setBaseNutrients(1.00f);

        mushroom.registerBlock(Blocks.MYCELIUM)
                .registerBlock(Blocks.DIRT, 2)   // 灰化土
                .setWaterCapacity(MID).setFertilizerCapacity(HIGH).setBaseNutrients(0.70f);

        soulsand.registerBlock(Blocks.SOUL_SAND)
                .setWaterCapacity(TINY).setFertilizerCapacity(TINY).setBaseNutrients(0.40f);

        netherrack.registerBlock(Blocks.NETHERRACK)
                .setWaterCapacity(TINY).setFertilizerCapacity(LOW).setBaseNutrients(0.15f);

        end.registerBlock(Blocks.END_STONE)
                .setWaterCapacity(TINY).setFertilizerCapacity(TINY).setBaseNutrients(0.30f);

        sand.registerBlock(Blocks.SAND)
                .setWaterCapacity(TINY).setFertilizerCapacity(TINY).setBaseNutrients(0.15f);

        gravel.registerBlock(Blocks.GRAVEL)
                .setWaterCapacity(LOW).setFertilizerCapacity(TINY).setBaseNutrients(0.20f);

        brick.registerBlock(Blocks.BRICK_BLOCK)
                .setWaterCapacity(LOW).setFertilizerCapacity(LOW).setBaseNutrients(0.20f);

        water.registerBlock(Blocks.WATER)
                .registerBlock(Blocks.FLOWING_WATER)
                .setWaterCapacity(0).setFertilizerCapacity(0).setBaseNutrients(0.10f);

        // ---- 石头类 ----
        // 原版石头一个方块就覆盖了花岗岩(meta 1/2)、闪长岩(3/4)、安山岩(5/6)。
        //
        // 注意：这里不能用 MetaBlocks.COMPRESSED——GT 只给带 INGOT / GEM 属性的材料生成压缩块
        // （见 MetaBlocks.createGeneratedBlock 的谓词），而石头类材料两个属性都没有。
        // GT 自己的石头方块在 MetaBlocks.STONE_BLOCKS 里，单独登记。
        stone.registerBlock(Blocks.STONE)
                .registerBlock(Blocks.COBBLESTONE)
                .registerBlock(Blocks.OBSIDIAN)
                .setWaterCapacity(LOW).setFertilizerCapacity(LOW).setBaseNutrients(0.20f);
        for (StoneVariantBlock variant : MetaBlocks.STONE_BLOCKS.values()) {
            stone.registerBlock(variant);   // 大理石 / 黑花岗岩 / 红花岗岩 / 玄武岩 / 混凝土…
        }

        // ---- 泥土类放最后：笼统，避免抢走上面的特定方块 ----
        dirtGrass.registerBlock(Blocks.DIRT)
                .registerBlock(Blocks.GRASS)
                .setWaterCapacity(MID).setFertilizerCapacity(MID).setBaseNutrients(0.50f);

        // ---- 登记顺序 = 匹配优先级 ----
        SoilRegistry.register(farmland);
        SoilRegistry.register(mushroom);
        SoilRegistry.register(soulsand);
        SoilRegistry.register(netherrack);
        SoilRegistry.register(end);
        SoilRegistry.register(sand);
        SoilRegistry.register(gravel);
        SoilRegistry.register(brick);
        SoilRegistry.register(water);
        SoilRegistry.register(stone);
        SoilRegistry.register(dirtGrass);
    }
}
