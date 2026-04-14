package com.drppp.drtech.api.crop;

/**
 * 作物渲染模式
 *
 * CROSS  - 十字交叉 (两个45°对角面, 类似原版小麦/马铃薯/胡萝卜)
 *          适合: 矮小密集的农作物
 *
 * HASH   - 井字架状 (四个面沿方块四边排列, 类似原版甘蔗/地狱疣)
 *          适合: 高大/稀疏/茎状的作物
 */
public enum CropRenderType {
    CROSS,
    HASH
}
