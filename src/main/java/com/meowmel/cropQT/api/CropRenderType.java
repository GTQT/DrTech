package com.meowmel.cropQT.api;

/**
 * 作物渲染模式
 *
 * CROSS  - 十字交叉 (两个45°对角面, 类似原版小麦/马铃薯/胡萝卜)
 *          适合: 矮小密集的农作物
 *
 * HASH   - 井字架状 (四个面沿方块四边排列, 类似原版甘蔗/地狱疣)
 *          适合: 高大/稀疏/茎状的作物
 *
 * FLOWER - 花坛状 (四个面沿方块中线排成 # 字, 且向四边各外扩 2/16)
 *          适合: 花类, 让花朵看起来比作物架宽一圈
 */
public enum CropRenderType {
    CROSS,
    HASH,
    FLOWER
}
