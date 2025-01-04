package com.drppp.drtech.api.Utils;

import com.drppp.drtech.loaders.builder.DisassemblyHandler;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomeRecipe {
    public List<ItemStack> inputItems=new ArrayList<>();
    public List<ItemStack> noConsumptionItems=new ArrayList<>();
    public List<FluidStack> inputFluids=new ArrayList<>();

    public List<ItemStack> outputItems=new ArrayList<>();
    public List<FluidStack> outputFluids=new ArrayList<>();

    public List<ItemStack> machineItems=new ArrayList<>();
    public List<MetaTileEntity> machines=new ArrayList<>();
    public int deep=1;
    public long eut=0;
    public int during=0;
    public boolean is_broken =false;
    public CustomeRecipe(){

    }
    public CustomeRecipe(NBTTagCompound nbt){
        // 读取ItemStack列表
        inputItems = readItemListFromNBT(nbt.getTagList("InputItems", Constants.NBT.TAG_COMPOUND));
        noConsumptionItems = readItemListFromNBT(nbt.getTagList("NoConsumptionItems", Constants.NBT.TAG_COMPOUND));
        outputItems = readItemListFromNBT(nbt.getTagList("OutputItems", Constants.NBT.TAG_COMPOUND));
        machineItems = readItemListFromNBT(nbt.getTagList("MachineItems", Constants.NBT.TAG_COMPOUND));

        // 读取FluidStack列表
        inputFluids = readFluidListFromNBT(nbt.getTagList("InputFluids", Constants.NBT.TAG_COMPOUND));
        outputFluids = readFluidListFromNBT(nbt.getTagList("OutputFluids", Constants.NBT.TAG_COMPOUND));

        // 读取基本数据类型
        deep = nbt.getInteger("Deep");
        eut = nbt.getLong("Eut");
        during = nbt.getInteger("During");
        is_broken = nbt.getBoolean("IsBroken");
        machines.clear();
        for (var item : machineItems)
        {
            var machine = GTUtility.getMetaTileEntity(item);
            if(machine!=null)
                machines.add(machine);
        }
    }
    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        // 写入ItemStack列表
        nbt.setTag("InputItems", writeItemListToNBT(inputItems));
        nbt.setTag("NoConsumptionItems", writeItemListToNBT(noConsumptionItems));
        nbt.setTag("OutputItems", writeItemListToNBT(outputItems));
        nbt.setTag("MachineItems", writeItemListToNBT(machineItems));

        // 写入FluidStack列表
        nbt.setTag("InputFluids", writeFluidListToNBT(inputFluids));
        nbt.setTag("OutputFluids", writeFluidListToNBT(outputFluids));

        // 写入基本数据类型
        nbt.setInteger("Deep", deep);
        nbt.setLong("Eut", eut);
        nbt.setInteger("During", during);
        nbt.setBoolean("IsBroken", is_broken);

        return nbt;
    }

    private NBTTagList writeItemListToNBT(List<ItemStack> items) {
        NBTTagList nbtList = new NBTTagList();
        for (ItemStack item : items) {
            NBTTagCompound itemNBT = new NBTTagCompound();
            item.writeToNBT(itemNBT);
            nbtList.appendTag(itemNBT);
        }
        return nbtList;
    }

    private NBTTagList writeFluidListToNBT(List<FluidStack> fluids) {
        NBTTagList nbtList = new NBTTagList();
        for (FluidStack fluid : fluids) {
            NBTTagCompound fluidNBT = new NBTTagCompound();
            fluid.writeToNBT(fluidNBT);
            nbtList.appendTag(fluidNBT);
        }
        return nbtList;
    }
    private List<ItemStack> readItemListFromNBT(NBTTagList nbtList) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < nbtList.tagCount(); i++) {
            NBTTagCompound itemNBT = nbtList.getCompoundTagAt(i);
            ItemStack item = new ItemStack(itemNBT);
            items.add(item);
        }
        return items;
    }

    private List<FluidStack> readFluidListFromNBT(NBTTagList nbtList) {
        List<FluidStack> fluids = new ArrayList<>();
        for (int i = 0; i < nbtList.tagCount(); i++) {
            NBTTagCompound fluidNBT = nbtList.getCompoundTagAt(i);
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidNBT);
            fluids.add(fluid);
        }
        return fluids;
    }
    public boolean ListContainsItem(List<ItemStack> list, ItemStack item)
    {
        boolean b = false;
        for (int i = 0; i < list.size(); i++) {
            if(!list.get(i).isEmpty() && list.get(i).getItem()==item.getItem() && list.get(i).getMetadata()== item.getMetadata())
            {
                b=true;
                break;
            }
        }
        return b;
    }
    public void GetDataFromRecipe(Recipe recipe)
    {
        if(recipe!=null)
        {
            var itemInputs = recipe.getInputs();
            var fluidInputs = recipe.getFluidInputs();
            var itemOutputs = recipe.getOutputs();
            var fluidOutputs = recipe.getFluidOutputs();
            var eut = recipe.getEUt();
            var during = recipe.getDuration();

            if(!itemInputs.isEmpty())
            {//inItems.add(x.getInputStacks().clone())
                List<ItemStack[]> inItems = new ArrayList<>();
                itemInputs.forEach(x -> {
                    if (!x.isNonConsumable()) {
                        inItems.add(x.getInputStacks().clone());
                    }
                });
                for (var s:inItems)
                {
                    if(s.length>0)
                    {
                        //消除矿磁带入的多余物品
                        s[0] = new ItemStack(s[0].getItem(),s[0].getCount(),s[0].getMetadata());
                        if(s.length>1)
                        {
                            for (int i = 1; i < s.length; i++) {
                                s[i] = ItemStack.EMPTY;
                            }
                        }
                        //排除编程电路
                        if(s[0].getItem() == MetaItems.INTEGRATED_CIRCUIT.getMetaItem() && s[0].getMetadata()== MetaItems.INTEGRATED_CIRCUIT.getMetaValue())
                            s[0] = ItemStack.EMPTY;
                        //排除工具
                        if ( s[0].getItem() instanceof ItemTool || s[0].getItem() instanceof ItemGTTool)
                            s[0] = ItemStack.EMPTY;
                        //电路板处理
                        else
                        {
                            Tuple<Boolean, String> isCircuit = DisassemblyHandler.isCircuit(s[0]);
                            if (isCircuit.getFirst())
                            {
                                //转换为通用电路
                                int count = s[0].getCount();
                                ItemStack ciru = DisassemblyHandler.circuitToUse.get(isCircuit.getSecond()).getFirst();
                                s[0] = new ItemStack(ciru.getItem(),count,ciru.getMetadata());
                            }
                        }
                        if(!s[0].isEmpty())
                            this.inputItems.add(s[0]);
                    }
                }
            }
            if(!fluidInputs.isEmpty())
            {
                fluidInputs.forEach(x->this.inputFluids.add(x.getInputFluidStack()));
            }
            if(!itemOutputs.isEmpty())
            {
                itemOutputs.forEach(x->this.outputItems.add(x.copy()));
            }
            if(!fluidOutputs.isEmpty())
            {
                fluidOutputs.forEach(x->this.outputFluids.add(x.copy()));
            }

            this.eut  = eut;
            this.during = during;
            this.is_broken = false;
        }
    }

    public static CustomeRecipe mergeRecipes(CustomeRecipe recipe1, CustomeRecipe recipe2) {
        CustomeRecipe mergedRecipe = new CustomeRecipe();
        mergedRecipe.inputItems = mergeItemStacks(recipe1.inputItems, recipe2.inputItems);
        mergedRecipe.outputItems = mergeItemStacks(recipe1.outputItems, recipe2.outputItems);
        mergedRecipe.inputFluids = mergeFluidStacks(recipe1.inputFluids, recipe2.inputFluids);
        mergedRecipe.outputFluids = mergeFluidStacks(recipe1.outputFluids, recipe2.outputFluids);
        mergedRecipe.machineItems = mergeMachineStacks(recipe1.machineItems, recipe2.machineItems);
        mergedRecipe.cancelOutFluids();
        mergedRecipe.cancelOutItems();
        mergedRecipe.eut = recipe1.eut+ recipe2.eut;
        mergedRecipe.during = recipe1.during+ recipe2.during;
        mergedRecipe.deep = Math.max(1,recipe1.deep+recipe2.deep);
        mergedRecipe.is_broken =CheckIsBroken(mergedRecipe);
        return mergedRecipe;
    }

    public static boolean CheckIsBroken(CustomeRecipe mergedRecipe) {
        boolean b =false;
        for (var item : mergedRecipe.inputItems)
        {
            if(item.getCount()<=0)
                mergedRecipe.is_broken=true;
        }
        for (var item : mergedRecipe.outputItems)
        {
            if(item.getCount()<=0)
                mergedRecipe.is_broken=true;
        }
        for (var item : mergedRecipe.inputFluids)
        {
            if(item.amount<=0)
                mergedRecipe.is_broken=true;
        }
        for (var item : mergedRecipe.outputFluids)
        {
            if(item.amount<=0)
                mergedRecipe.is_broken=true;
        }
        return b;
    }

    private static List<ItemStack> mergeMachineStacks(List<ItemStack> list1, List<ItemStack> list2) {
        List<ItemStack> mergedList = new ArrayList<>(list1);
        for (ItemStack item : list2) {
            ItemStack existingItem = mergedList.stream().filter(i -> i.isItemEqual(item)).findFirst().orElse(null);
            if (existingItem == null) {
                mergedList.add(item);
            } 
        }

        return mergedList;
    }
    private static List<ItemStack> mergeItemStacks(List<ItemStack> list1, List<ItemStack> list2) {
        List<ItemStack> mergedList = new ArrayList<>(list1);

        for (ItemStack item : list2) {
            ItemStack existingItem = mergedList.stream().filter(i -> i.isItemEqual(item)).findFirst().orElse(null);
            if (existingItem != null) {
                existingItem.setCount(existingItem.getCount() + item.getCount());
            } else {
                mergedList.add(item);
            }
        }

        return mergedList;
    }

    private static List<FluidStack> mergeFluidStacks(List<FluidStack> list1, List<FluidStack> list2) {
        List<FluidStack> mergedList = new ArrayList<>(list1);

        for (FluidStack fluid : list2) {
            FluidStack existingFluid = mergedList.stream().filter(f -> f.isFluidEqual(fluid)).findFirst().orElse(null);
            if (existingFluid != null) {
                existingFluid.amount += fluid.amount;
            } else {
                mergedList.add(fluid);
            }
        }

        return mergedList;
    }

    // 消除 input 和 output 中的相同物品
    private void cancelOutItems() {
        for (ItemStack outputItem : new ArrayList<>(outputItems)) { // 遍历 output 的副本
            ItemStack inputItem = inputItems.stream()
                    .filter(i -> i.isItemEqual(outputItem))
                    .findFirst()
                    .orElse(null);

            if (inputItem != null) {
                int minCount = Math.min(inputItem.getCount(), outputItem.getCount());
                inputItem.setCount(inputItem.getCount() - minCount);
                outputItem.setCount(outputItem.getCount() - minCount);

                if (inputItem.getCount() == 0) {
                    inputItems.remove(inputItem);
                }
                if (outputItem.getCount() == 0) {
                    outputItems.remove(outputItem);
                }
            }
        }
    }

    // 消除 input 和 output 中的相同液体
    private void cancelOutFluids() {
        for (FluidStack outputFluid : new ArrayList<>(outputFluids)) { // 遍历 output 的副本
            FluidStack inputFluid = inputFluids.stream()
                    .filter(f -> f.isFluidEqual(outputFluid))
                    .findFirst()
                    .orElse(null);

            if (inputFluid != null) {
                int minAmount = Math.min(inputFluid.amount, outputFluid.amount);
                inputFluid.amount = (inputFluid.amount - minAmount);
                outputFluid.amount=(outputFluid.amount - minAmount);

                if (inputFluid.amount == 0) {
                    inputFluids.remove(inputFluid);
                }
                if (outputFluid.amount == 0) {
                    outputFluids.remove(outputFluid);
                }
            }
        }
    }

    // 根据倍率 rate 调整 input 和 output 的数量
    public void calculationMuti(int rate) {
        for (ItemStack item : inputItems) {
            item.setCount(item.getCount() * rate);
        }
        for (ItemStack item : outputItems) {
            item.setCount(item.getCount() * rate);
        }

        for (FluidStack fluid : inputFluids) {
            fluid.amount=(fluid.amount * rate);
        }
        for (FluidStack fluid : outputFluids) {
            fluid.amount=(fluid.amount * rate);
        }
        eut *=rate;
        during *=rate;
    }

    // 简化 input 和 output 的数量，使它们都除以最大公约数
    public void reduceToSmallest() {
        List<Integer> allCounts = new ArrayList<>();
        allCounts.addAll(inputItems.stream().map(ItemStack::getCount).collect(Collectors.toList()));
        allCounts.addAll(outputItems.stream().map(ItemStack::getCount).collect(Collectors.toList()));
        allCounts.addAll(inputFluids.stream().map(x -> x.amount).collect(Collectors.toList()));
        allCounts.addAll(outputFluids.stream().map(x -> x.amount).collect(Collectors.toList()));

        int gcd = gcdFromList(allCounts);
        if (gcd > 1) {
            for (ItemStack item : inputItems) {
                item.setCount(item.getCount() / gcd);
            }
            for (ItemStack item : outputItems) {
                item.setCount(item.getCount() / gcd);
            }
            for (FluidStack fluid : inputFluids) {
                fluid.amount=(fluid.amount / gcd);
            }
            for (FluidStack fluid : outputFluids) {
                fluid.amount=(fluid.amount / gcd);
            }
        }
    }

    // 计算列表中所有数值的最大公约数
    private int gcdFromList(List<Integer> values) {
        if (values.isEmpty()) return 1;

        int result = values.get(0);
        for (int value : values) {
            result = gcd(result, value);
            if (result == 1) break; // 如果已经是 1，直接返回
        }
        return result;
    }
    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    public boolean CheckCustomerRecipes(IItemHandlerModifiable itemhandler, IMultipleTankHandler tanks) {
        for (int i = 0; i < inputItems.size(); i++) {
            ItemStack recipeitem = inputItems.get(i).copy();
            if (recipeitem.isEmpty()) {
                continue;
            }
            // 确保 slots 不为 null
            List<ItemStack> slots = GTUtility.itemHandlerToList(itemhandler);
            if (slots == null || slots.isEmpty()) {
                return false;
            }

            // 增加 f 的非空检查
            List<ItemStack> filterslot = slots.stream()
                    .filter(f -> f != null && f.isItemEqual(recipeitem))
                    .collect(Collectors.toList());
            if (filterslot == null || filterslot.isEmpty()) {
                return false;
            }

            int count = 0;
            for (ItemStack iii : filterslot) {
                count += iii.getCount();
            }
            if (count < recipeitem.getCount()) { // 修复边界条件
                return false;
            }
        }

        for (int i = 0; i < inputFluids.size(); i++) {
            FluidStack recipeitem = inputFluids.get(i).copy();
            if (recipeitem == null || recipeitem.amount == 0) {
                continue;
            }

            // 确保 slots 不为 null
            List<FluidStack> slots = GTUtility.fluidHandlerToList(tanks);
            if (slots == null || slots.isEmpty()) {
                return false;
            }

            // 增加 f 的非空检查
            List<FluidStack> filterslot = slots.stream()
                    .filter(f -> f != null && f.isFluidEqual(recipeitem))
                    .collect(Collectors.toList());
            if (filterslot == null || filterslot.isEmpty()) {
                return false;
            }

            int count = 0;
            for (FluidStack iii : filterslot) {
                count += iii.amount;
            }
            if (count < recipeitem.amount) { // 修复边界条件
                return false;
            }
        }

        return true;
    }


    public void RunRecipe(IItemHandlerModifiable itemhandler, IMultipleTankHandler tanks,IItemHandlerModifiable outputhandler, IMultipleTankHandler outputtanks) {
        if (CheckCustomerRecipes(itemhandler, tanks) && !this.is_broken) {
            // 扣除物品
            for (int i = 0; i < inputItems.size(); i++) {
                ItemStack recipeItem = inputItems.get(i).copy();
                if (recipeItem.isEmpty()) {
                    continue;
                }
                int requiredCount = recipeItem.getCount();
                for (int slot = 0; slot < itemhandler.getSlots(); slot++) {
                    ItemStack stackInSlot = itemhandler.getStackInSlot(slot);
                    if (stackInSlot.isItemEqual(recipeItem)) {
                        int availableCount = stackInSlot.getCount();
                        int toRemove = Math.min(availableCount, requiredCount);
                        itemhandler.extractItem(slot,toRemove,false);
                        requiredCount -= toRemove;
                        if (requiredCount <= 0) {
                            break;
                        }
                    }
                }
            }
    
            // 扣除流体
            for (int i = 0; i < inputFluids.size(); i++) {
                FluidStack recipeFluid = inputFluids.get(i).copy();
                if (recipeFluid == null || recipeFluid.amount == 0) {
                    continue;
                }
                int requiredAmount = recipeFluid.amount;
                for (int tank = 0; tank < tanks.getTanks(); tank++) {
                    FluidStack fluidInTank = tanks.getTankAt(tank).getFluid();
                    if (fluidInTank != null && fluidInTank.isFluidEqual(recipeFluid)) {
                        int availableAmount = fluidInTank.amount;
                        int toDrain = Math.min(availableAmount, requiredAmount);
                        tanks.getTankAt(tank).drain(toDrain,true);
                        requiredAmount -= toDrain;
                        if (requiredAmount <= 0) {
                            break;
                        }
                    }
                }
            }
            GTTransferUtils.addItemsToItemHandler(outputhandler, false, outputItems);
            GTTransferUtils.addFluidsToFluidHandler(outputtanks, false, outputFluids);

        }
    }
    
}
