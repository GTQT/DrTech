package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.machine.MetaTileEntityCropHarvester;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * 作物收割机的干活逻辑：范围收割成熟作物。
 *
 * <p>只有一件事，没有任何开关 —— 只要通电就一直扫。要限制范围就把半径调小，
 * 不必再给个「开关」这种多余的状态。
 *
 * <h2>顺序不能反</h2>
 * {@code getHarvestDrops()} 必须在 {@code harvest()} <b>之前</b>调：
 * 掉落表是按当前的 {@code stats} 和成熟度算的，先 {@code harvest()} 把 stage 归零
 * 就什么都掉不出来了。
 *
 * <p>收割后作物<b>不会消失</b>，只是回到 0 阶段重新长 —— 所以同一株可以反复收。
 * 杂草一律跳过，那是作物监管机除草开关的活。
 */
public class CropHarvesterLogic extends CropRangeLogic<MetaTileEntityCropHarvester> {

    /** 每次收割的耗电 = 本机电压 / 这个除数。 */
    private static final int HARVEST_ENERGY_DIVISOR = 8;

    public CropHarvesterLogic(MetaTileEntityCropHarvester machine) {
        super(machine);
    }

    /** 没有开关，永远在工作。 */
    @Override
    public boolean isAnyEnabled() {
        return true;
    }

    @Override
    protected void actOn(TileCropStick crop) {
        if (!crop.hasCrop() || crop.isWeedPlant() || !crop.isMature()) {
            return;
        }
        if (!consumeEnergy(energyPerAction(HARVEST_ENERGY_DIVISOR))) {
            return;
        }
        // 先取掉落再收割，反过来 stage 归零就掉不出东西了
        List<ItemStack> drops = crop.getHarvestDrops();
        crop.harvest();
        for (ItemStack drop : drops) {
            GTTransferUtils.insertItem(machine.getExportItems(), drop, false);
        }
        machine.markDirty();
    }

    // ==================== 存档 ====================

    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("progress", getProgress());
    }

    public void readFromNBT(NBTTagCompound data) {
        setProgress(data.getInteger("progress"));
    }
}
