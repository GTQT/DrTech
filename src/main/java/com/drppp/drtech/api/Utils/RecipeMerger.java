package com.drppp.drtech.api.Utils;

import java.util.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RecipeMerger {
    public static void yunSuan(List<CustomeRecipe> list) {
        for (int i = 0; i < list.size(); i++) {
            CustomeRecipe r = list.get(i);
            if(i==list.size()-1)
                continue;
            for (int j = 0; j < list.size(); j++) {
                if (j == i ) {
                    continue;
                }
                if(!r.outputItems.isEmpty())
                    for (ItemStack item : r.outputItems) {
                        for (ItemStack inItem : list.get(j).inputItems) {
                            if (item.isItemEqual(inItem) && item.getCount()!=inItem.getCount()) {
                                if(item.getCount()==0 || inItem.getCount()==0)
                                    continue;
                                int beishu = getGBS(item.getCount(), inItem.getCount());
                                r.calculationMuti(beishu / item.getCount());
                                list.get(j).calculationMuti(beishu / inItem.getCount());
                                yunSuan(list);
                            }
                        }
                    }
                if(!r.outputFluids.isEmpty())
                    for (FluidStack fluid : r.outputFluids) {
                        for (FluidStack inFluid : list.get(j).inputFluids) {
                            if (fluid.isFluidEqual(inFluid) && !(fluid.amount==inFluid.amount)) {
                                if(fluid.amount==0 || inFluid.amount==0)
                                    continue;
                                int beishu = getGBS(fluid.amount, inFluid.amount);
                                r.calculationMuti(beishu / fluid.amount);
                                list.get(j).calculationMuti(beishu / inFluid.amount);
                                yunSuan(list);
                            }
                        }
                    }
            }
        }
    }
    private static int getGBS(int a, int b) {
        return (a * b) / gcd(a, b); // 用最大公约数求解最小公倍数
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    public static void yunSuanR(List<CustomeRecipe> list)
    {
        CustomeRecipe last = list.get(list.size()-1);
        FindInput(list,last);
    }
    public static void FindInput(List<CustomeRecipe> list,CustomeRecipe rec)
    {

    }
}
