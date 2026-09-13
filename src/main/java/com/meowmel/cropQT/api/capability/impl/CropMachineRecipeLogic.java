package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.machine.MetaTileEntityCropMachine;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 五台作物机器的公共「干活逻辑」。
 *
 * <h2>为什么继承 RecipeLogicEnergy 而不是自己写一套</h2>
 * 进度、扣电、断电续跑、电源开关、存档、网络同步、{@code IWorkable} 这一整套
 * {@link RecipeLogicEnergy} 都已经有了。自己写一遍只是把它们抄一次，还抄不出
 * 界面要的「没电」指示。所以我们只回答机器真正关心的问题，其余全交给基类。
 *
 * <h2>不查配方表</h2>
 * 这五台的「配方」是运行时算出来的，塞不进 RecipeMap 的配料树。做法是
 * {@link #shouldSearchForRecipes()} 永远返回 false，把基类那个「该不该去找新配方」
 * 的时机占为己有 —— 它只在「没有进行中的轮次 + 电源开着」时被调用，
 * 正好就是该开下一轮的时候。基类给的那张 RecipeMap 只用来撑界面。
 *
 * <h2>一轮的流程</h2>
 * <pre>
 *   shouldSearchForRecipes()：问 canStart()，通过就 startCycle()
 *     ↓ 每 tick（progressTime > 0）
 *   updateRecipeProgress()：电够就扣电推进；电不够原地保留进度
 *     ↓ progressTime 跑满
 *   再问一次 canStart()：通过就 onWorkComplete() 结算，不通过就停在最后一刻等
 * </pre>
 * 收尾前那次 {@code canStart()} 是刻意留的 —— 产物槽满了就停在原地等玩家腾地方，
 * 而不是把产物挤掉或者白跑一轮。
 *
 * @param <M> 宿主机器类型
 */
public abstract class CropMachineRecipeLogic<M extends MetaTileEntityCropMachine> extends RecipeLogicEnergy {

    @NotNull
    protected final M machine;

    /**
     * @param energy 能量容器的取值器。基类构造器就会立刻用它，而那时候
     *               {@code MetaTileEntityCropMachine} 的字段还没赋值，
     *               所以只能传一个延迟求值的 {@code Supplier} —— 由机器那边
     *               写成 {@code () -> energyContainer}。
     */
    protected CropMachineRecipeLogic(M machine, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energy) {
        super(machine, recipeMap, energy);
        this.machine = machine;
    }

    // ==================== 子类回答的四个问题 ====================

    /** 现在能不能开工；收尾前也会再问一次。 */
    protected abstract boolean canStart();

    /** 这一轮要跑多少 tick。返回 ≤ 0 表示这轮开不了（比如育种机没解析出方案）。 */
    protected abstract int getWorkDuration();

    /** 本轮每 tick 消耗多少 EU。 */
    protected abstract long getEnergyPerTick();

    /** 一轮跑完的结算：扣材料、出产物。 */
    protected abstract void onWorkComplete();

    // ==================== 状态机 ====================

    @Override
    protected boolean shouldSearchForRecipes() {
        // 基类在这里问「要不要去 RecipeMap 里找一条新配方」。我们不查表，
        // 但这一刻正好是「该开下一轮」的时机，所以拿来做开工判定。
        // 返回 false：永远不进 trySearchNewRecipe（那才会真的去查表）。
        //
        // wasActiveAndNeedsUpdate 那道闸不能省：base.update() 的次序是
        //   ① updateRecipeProgress() —— 跑满就 completeRecipe()，progressTime 归零、置 latch
        //   ② progressTime == 0 && shouldSearchForRecipes() —— 于是同一个 tick 里就会开下一轮
        //   ③ latch —— setActive(false)
        // 不挡的话第 ③ 步会把刚点亮的 active 立刻摁灭，机器接着跑但正面贴图不亮、
        // 也没有声音。我们的机器只要输入管够就会连续开工，一定会撞上，
        // 所以多等一个 tick 再开下一轮。
        if (!wasActiveAndNeedsUpdate && canStart()) {
            startCycle();
        }
        return false;
    }

    private void startCycle() {
        int duration = getWorkDuration();
        if (duration <= 0) {
            return;
        }
        // 基类约定：progressTime 从 1 起算，因为 updateRecipeProgress() 被
        // `progressTime > 0` 门控 —— 从 0 起算的话机器会一直杵着不动
        progressTime = 1;
        setMaxProgress(duration);
        recipeEUt = getEnergyPerTick();
        setActive(true);
        machine.markDirty();
    }

    @Override
    protected void updateRecipeProgress() {
        // 每 tick 现问、不缓存：基因提取机的耗电跟着输入槽里那袋种子走，
        // 缓存下来会出现「耗电按旧的算、产出按新的算」
        long energyPerTick = getEnergyPerTick();
        recipeEUt = energyPerTick;      // 让存档和 TOP 里的数字跟当前一致

        IEnergyContainer energy = getEnergyContainer();
        if (energy.getEnergyStored() < energyPerTick) {
            hasNotEnoughEnergy = true;  // 基类字段：界面上的「没电」指示看它
            return;                     // 断电：进度保留，来电接着跑
        }
        hasNotEnoughEnergy = false;
        energy.removeEnergy(energyPerTick);

        if (++progressTime <= maxProgressTime) {
            return;
        }
        // 收尾前再确认一次条件，放不下就停在最后一刻等
        if (!canStart()) {
            progressTime = maxProgressTime;
            return;
        }
        completeRecipe();
    }

    @Override
    protected void completeRecipe() {
        onWorkComplete();
        // 基类负责清进度、清 recipeEUt，并置 wasActiveAndNeedsUpdate ——
        // 下一 tick 它会据此把 active 关掉。产物已经在 onWorkComplete() 里
        // 直接写进输出槽了，基类的 outputRecipeOutputs() 对我们是空操作
        super.completeRecipe();
    }

    @NotNull
    public M getMachine() {
        return machine;
    }

    /** 世界，收尾判定之类的地方要用。 */
    protected World world() {
        return machine.getWorld();
    }
}
