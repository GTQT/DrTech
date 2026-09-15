package com.meowmel.cropQT.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 作物的一个生长前置条件。
 *
 * <p>实现方自己决定「看哪一格、比什么」——{@link SubSoilRequirement} 看的是下方第二格，
 * 将来的光照 / 生物群系要求可以看其它东西。
 *
 * <p>注意：条件不满足时<b>不阻止生长</b>，而是把生长速度压到极低（软惩罚）。
 * 判断结果只用来给玩家提示，见 {@code TileCropStick} 的生长逻辑。
 */
public interface GrowthRequirement {

    /** 该条件在当前环境下是否满足。 */
    boolean isMet(World world, BlockPos cropPos);

    /**
     * 给玩家看的短描述，例如「铜块」。
     *
     * <p>用于分析仪、TOP 和 tooltip 的「缺少 XXX」提示。
     *
     * <p><b>回退约定</b>：实现方应当返回能拿到的<em>最可读</em>的名字——通常是某个代表物品的
     * 显示名（由 Minecraft 本地化）；拿不到任何代表物品时退回内部名。
     * 想要完全本地化的标签，调用方可以优先尝试
     * {@code I18n.format("cropqt.subsoil." + getName())}，失败再退到本方法。
     */
    String getDisplayName();
}
