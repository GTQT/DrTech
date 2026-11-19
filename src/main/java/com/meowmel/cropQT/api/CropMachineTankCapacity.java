package com.meowmel.cropQT.api;

import gregtech.api.GTValues;

/**
 * 作物机器的流体罐容量。
 *
 * <h2>为什么不用 {@code GTUtility.defaultTankSizeFunction}</h2>
 * CEU 的默认函数是给普通单方块配方机器用的：≤LV 8000、MV 12000、HV 16000、
 * EV 32000、IV+ 64000。CropsNH 的机器继承 GT5U {@code MTEBasicMachine}，用的是
 * {@code getCapacityForTier}：8000 / 16000 / 32000 / 64000 / 128000 / 256000。
 * 两者从 HV 起就分叉，直接套默认函数会让种子机、繁殖机的罐<b>小一半到四倍</b>，
 * 超频后的流体吞吐根本不够。
 *
 * <p>这里按 GT5U 原表重写，并保留源端的两个缩放：
 * <ul>
 *     <li>种子生成器：{@code tier < IV ? forTier(tier) / 10 : forTier(tier)}</li>
 *     <li>作物繁殖机：{@code tier < IV ? forTier(tier) / 5 : forTier(tier)}</li>
 *     <li>作物合成器：{@code forTier(tier)}（不缩放）</li>
 * </ul>
 *
 * <p>注意：这是<b>单罐</b>容量。种子机/繁殖机只有一个输入罐；合成器也只有一个 UUM 罐。
 */
public final class CropMachineTankCapacity {

    /** 未知档位的回退值（GT5U default 分支）。 */
    public static final int DEFAULT_CAPACITY = 256_000;

    private CropMachineTankCapacity() {
    }

    /**
     * GT5U {@code MTEBasicMachine.getCapacityForTier} 的等价实现。
     *
     * <p>ULV 8000、LV 16000、MV 32000、HV 64000、EV 128000、IV+ 256000。
     */
    public static int forTier(int tier) {
        switch (tier) {
            case GTValues.ULV:
                return 8_000;
            case GTValues.LV:
                return 16_000;
            case GTValues.MV:
                return 32_000;
            case GTValues.HV:
                return 64_000;
            case GTValues.EV:
                return 128_000;
            default:
                return DEFAULT_CAPACITY;
        }
    }

    /** 种子生成器：IV 以下只有 1/10 容量，避免低档机器一次吞掉整池肥料。 */
    public static int forSeedGenerator(int tier) {
        int capacity = forTier(tier);
        return tier < GTValues.IV ? Math.max(1, capacity / 10) : capacity;
    }

    /** 作物繁殖机：IV 以下 1/5。 */
    public static int forBreeder(int tier) {
        int capacity = forTier(tier);
        return tier < GTValues.IV ? Math.max(1, capacity / 5) : capacity;
    }

    /** 作物合成器：不缩放，直接用原表。 */
    public static int forSynthesizer(int tier) {
        return forTier(tier);
    }
}
