package com.brachy84.mechtech.api.armor;

import com.brachy84.mechtech.common.MTConfig;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.IArmorLogic;
import gregtech.api.items.armor.ISpecialArmorLogic;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.GTLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ModularArmor implements ISpecialArmorLogic {

    public static ModularArmor get(ItemStack stack) {
        if (stack.getItem() instanceof ArmorMetaItem) {
            ArmorMetaItem.ArmorMetaValueItem metaValueItem = ((ArmorMetaItem<?>) stack.getItem()).getItem(stack);
            IArmorLogic logic = metaValueItem.getArmorLogic();
            if (logic instanceof ModularArmor) {
                return (ModularArmor) logic;
            }
        }
        return null;
    }

    public static final String BATTERIES = "Batteries";
    public static final String MODULES = "Modules";
    private final int moduleSlots;
    private final EntityEquipmentSlot slot;
    private final int maxFluidSize;


    public ModularArmor(EntityEquipmentSlot slot, int moduleSlots, int maxFluidSize) {
        this.slot = slot;
        this.moduleSlots = moduleSlots;
        this.maxFluidSize = maxFluidSize;
    }

    public int getModuleSlots() {
        return moduleSlots;
    }

    public int getMaxFluidSize() {
        return maxFluidSize;
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase entityLivingBase, @Nonnull ItemStack itemStack, DamageSource damageSource, double damage, EntityEquipmentSlot entityEquipmentSlot) {
        if (MTConfig.debug) {
            GTLog.logger.info("Get Properties for source {}, damage {}, slot {}", damageSource.damageType, damage, slot);
        }
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null || !nbt.hasKey(MODULES))
            return new ISpecialArmor.ArmorProperties(0, 0, 0);
        List<AbsorbResult> armorModules = new ArrayList<>();
        List<AbsorbResult> specialArmorModules = new ArrayList<>();
        NBTTagList modulesNbt = nbt.getTagList(MODULES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < modulesNbt.tagCount(); i++) {
            NBTTagCompound moduleNbt = modulesNbt.getCompoundTagAt(i);
            IModule module = Modules.getModule(moduleNbt.getInteger("ID"));
            if (!moduleNbt.getBoolean("Destroyed")) {
                if (!damageSource.isUnblockable() && module instanceof IArmorModule) {
                    AbsorbResult absorbResult = new AbsorbResult();
                    absorbResult.armor = ((IArmorModule) module).getArmor(slot);
                    absorbResult.toughness = ((IArmorModule) module).getToughness(slot);
                    absorbResult.setModule(module, moduleNbt);
                    armorModules.add(absorbResult);
                }
                if (module instanceof ISpecialArmorModule) {
                    AbsorbResult absorbResult = ((ISpecialArmorModule) module).getArmorProperties(entityLivingBase, itemStack, moduleNbt, damageSource, damage, entityEquipmentSlot);
                    if (absorbResult != null && !absorbResult.isZero()) {
                        absorbResult.setModule(module, moduleNbt);
                        specialArmorModules.add(absorbResult);
                    }
                }
            }
        }

        double originalDamage = damage;

        specialArmorModules.sort(AbsorbResult::compareTo);
        ISpecialArmor.ArmorProperties properties = new ISpecialArmor.ArmorProperties(0, 0, 0);
        for (AbsorbResult absorbResult : specialArmorModules) {
            properties.Priority = Math.max(properties.Priority, absorbResult.priority);
            if (Integer.MAX_VALUE - properties.AbsorbMax < absorbResult.max) {
                properties.AbsorbMax = Integer.MAX_VALUE;
            } else {
                properties.AbsorbMax += absorbResult.max;
            }
            float moduleDamage = (float) (originalDamage * absorbResult.ratio);
            if (moduleDamage > absorbResult.max) {
                moduleDamage = absorbResult.max;
                absorbResult.ratio = moduleDamage / originalDamage;
            }
            properties.AbsorbRatio += absorbResult.ratio;
            if (MTConfig.debug) {
                GTLog.logger.info("  do {} special armor module damage", moduleDamage);
            }
            if (absorbResult.module instanceof IDurabilityModule) {
                damage -= ((IDurabilityModule) absorbResult.module).damage(entityLivingBase, itemStack, absorbResult.moduleData, damageSource, moduleDamage, entityEquipmentSlot);
            } else {
                damage -= moduleDamage;
            }
        }

        if (armorModules.size() > 0) {
            properties.AbsorbMax = Integer.MAX_VALUE;
            float ta = 0, tt = 0;
            for (AbsorbResult absorbResult : armorModules) {
                ta += absorbResult.armor;
                tt += absorbResult.toughness;
            }
            if (armorModules.size() > 1) {
                float factor = (float) getFactor(armorModules.size());
                ta /= armorModules.size();
                tt /= armorModules.size();
                ta *= factor;
                tt *= factor;
            }
            ta *= 4;
            float moduleDamage = (float) (damage - getDamageAfterAbsorb((float) damage, ta, tt));
            moduleDamage /= armorModules.size();
            if (MTConfig.debug) {
                GTLog.logger.info("  do {} armor module damage * {}", moduleDamage, armorModules.size());
            }
            for (AbsorbResult absorbResult : armorModules) {
                if (absorbResult.module instanceof IDurabilityModule) {
                    damage -= ((IDurabilityModule) absorbResult.module).damage(entityLivingBase, itemStack, absorbResult.moduleData, damageSource, moduleDamage, entityEquipmentSlot);
                } else {
                    damage -= moduleDamage;
                }
            }
        }
        damage = Math.max(damage, 0);
        properties.AbsorbRatio = 1 - (damage / originalDamage);
        if (MTConfig.debug) {
            GTLog.logger.info("  damage left {}, ratio {}", damage, properties.AbsorbRatio);
        }
        return properties;
    }

    private float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {
        float f = 2.0F + toughnessAttribute / 4.0F;
        float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
        return damage * (1.0F - f1 / 25.0F);
    }

    private double getFactor(int modules) {
        if (modules <= 1)
            return 1;
        if (modules <= 5)
            return modules * (1 - (modules - 1) / 10.0);
        return 3 + modules * 0.1;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        Collection<IModule> modules = getModulesOf(itemStack);
        NBTTagCompound nbt = getArmorData(itemStack);
        for (IModule module : modules) {
            if (world.isRemote) {
                module.onClientTick(world, player, itemStack, nbt);
            } else {
                module.onServerTick(world, player, itemStack, nbt);
            }
        }
        setArmorData(itemStack, nbt);
    }

    public void onUnequip(World world, EntityLivingBase player, ItemStack modularArmorPiece, ItemStack newStack) {
        Collection<IModule> modules = getModulesOf(modularArmorPiece);
        for (IModule module : modules) {
            module.onUnequip(world, player, modularArmorPiece, newStack);
        }
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armorPiece, int i) {
        List<IModule> modules = ModularArmor.getModulesOf(armorPiece);
        double armor = 0;
        for (IModule module : modules) {
            if (module instanceof IArmorModule)
                armor += (((IArmorModule) module).getArmor(slot) + 0.5);
        }
        return (int) armor;
    }

    @Override
    public void addToolComponents(ArmorMetaItem.ArmorMetaValueItem mvi) {
        mvi.addComponents(new ModularArmorStats());
        mvi.addComponents(new IItemBehaviour() {
            @Override
            public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
                if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem) {
                    ItemStack armor = player.getHeldItem(hand);
                    if (armor.getItem() instanceof ArmorMetaItem && player.inventory.armorInventory.get(slot.getIndex()).isEmpty() && !player.isSneaking()) {
                        player.inventory.armorInventory.set(slot.getIndex(), armor.copy());
                        player.setHeldItem(hand, ItemStack.EMPTY);
                        player.playSound(new SoundEvent(new ResourceLocation("item.armor.equip_generic")), 1.0F, 1.0F);
                        return ActionResult.newResult(EnumActionResult.SUCCESS, armor);
                    }
                }
                return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
            }
        });
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return slot;
    }

    public EntityEquipmentSlot getSlot() {
        return slot;
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack itemStack) {
        ImmutableMultimap.Builder<String, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
        return builder.build();
    }

    @SideOnly(Side.CLIENT)
    public void addHUDInfo(ItemStack item, List<String> hudStrings) {
        List<IModule> modules = getModulesOf(item);
        NBTTagCompound nbt = getArmorData(item);
        modules.forEach(module -> module.addHUDInfo(item, nbt, hudStrings));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        String armorTexture = "nano_muscule_suite";
        return slot != EntityEquipmentSlot.LEGS ?
                String.format("gregtech:textures/armor/%s_1.png", armorTexture) :
                String.format("gregtech:textures/armor/%s_2.png", armorTexture);
    }

    public static NBTTagCompound getArmorData(ItemStack itemStack) {
        NBTTagCompound armorNbt = itemStack.getTagCompound();
        NBTTagCompound nbt;
        if (armorNbt != null) {
            nbt = armorNbt.getCompoundTag("ModuleData");
        } else {
            armorNbt = new NBTTagCompound();
            nbt = new NBTTagCompound();
            itemStack.setTagCompound(armorNbt);
        }
        armorNbt.setTag("ModuleData", nbt);
        return nbt;
    }

    public static void setArmorData(ItemStack itemStack, NBTTagCompound nbt) {
        NBTTagCompound armorNbt = itemStack.getTagCompound();
        if (armorNbt == null) {
            armorNbt = new NBTTagCompound();
            itemStack.setTagCompound(armorNbt);
        }
        armorNbt.setTag("ModuleData", nbt);
    }

    public static long fill(ItemStack stack, long amount, int tier, boolean simulate) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        long original = amount;
        if (nbt.hasKey(BATTERIES)) {
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound batteryNbt = list.getCompoundTagAt(i);
                ItemStack batteryStack = new ItemStack(batteryNbt);
                IElectricItem electricItem = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null) {
                    amount -= electricItem.charge(amount, tier, false, simulate);
                    batteryNbt = batteryStack.serializeNBT();
                    list.set(i, batteryNbt);
                    if (amount == 0)
                        break;
                }
            }
            nbt.setTag(BATTERIES, list);
        }
        return original - amount;
    }

    public static long drain(ItemStack stack, long amount, int tier, boolean simulate) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        long original = amount;
        if (nbt.hasKey(BATTERIES)) {
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound batteryNbt = list.getCompoundTagAt(i);
                ItemStack batteryStack = new ItemStack(batteryNbt);
                IElectricItem electricItem = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null) {
                    amount -= electricItem.discharge(amount, tier, false, false, simulate);
                    batteryNbt = batteryStack.serializeNBT();
                    list.set(i, batteryNbt);
                    if (amount == 0)
                        break;
                }
            }
            nbt.setTag(BATTERIES, list);
        }
        return original - amount;
    }

    public static int getTier(ItemStack stack) {
        int tier = 0;
        List<ItemStack> batteries = getBatteries(stack);
        if (batteries.isEmpty()) {
            return GTValues.MV;
        }
        // Tier is determined by whatever battery is the lowest tier
        for (ItemStack battery : batteries) {
            if (battery.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
                IElectricItem electricItem = battery.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (tier == 0) {
                    tier = electricItem.getTier();
                }
                tier = Math.min(tier, electricItem.getTier());
            }
        }
        return tier;
    }

    public static long getTransferLimit(ItemStack stack) {
        long transferLimit = 0;
        List<ItemStack> batteries = getBatteries(stack);
        if (batteries.isEmpty()) {
            return 0;
        }
        // Transfer limit is determined by whatever battery has the lowest
        for (ItemStack battery : batteries) {
            if (battery.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
                IElectricItem electricItem = battery.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (transferLimit == 0) {
                    transferLimit = electricItem.getTransferLimit();
                }
                transferLimit = Math.min(transferLimit, electricItem.getTransferLimit());
            }
        }
        return transferLimit;
    }

    public static long getCapacity(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        long cap = 0;
        if (nbt.hasKey(BATTERIES)) {
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound batteryNbt = list.getCompoundTagAt(i);
                ItemStack batteryStack = new ItemStack(batteryNbt);
                IElectricItem electricItem = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null) {
                    cap += electricItem.getMaxCharge();
                }
            }
        }
        return cap;
    }

    public static long getEnergy(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        long cap = 0;
        if (nbt.hasKey(BATTERIES)) {
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound batteryNbt = list.getCompoundTagAt(i);
                ItemStack batteryStack = new ItemStack(batteryNbt);
                IElectricItem electricItem = batteryStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null) {
                    cap += electricItem.getCharge();
                }
            }
        }
        return cap;
    }

    public static int drainFluid(ItemStack stack, FluidStack fluid, boolean simulate) {
        if (stack.isEmpty() || fluid == null || fluid.amount <= 0) {
            return 0;
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        FluidStack toDrain = fluid.copy();
        if (nbt.hasKey(BATTERIES)) {
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound batteryNbt = list.getCompoundTagAt(i);
                ItemStack batteryStack = new ItemStack(batteryNbt);
                IFluidHandlerItem fluidHandler = batteryStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    FluidStack drained = fluidHandler.drain(toDrain, !simulate);
                    if (drained != null && drained.amount > 0) {
                        toDrain.amount -= drained.amount;
                        if (toDrain.amount <= 0) {
                            break;
                        }
                    }
                }
            }
            nbt.setTag(BATTERIES, list);
        }
        return fluid.amount - toDrain.amount;
    }

    public static int fillFluid(ItemStack stack, FluidStack fluid, boolean simulate) {
        if (stack.isEmpty() || fluid == null || fluid.amount <= 0) {
            return 0;
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        }
        FluidStack toFill = fluid.copy();
        if (nbt.hasKey(BATTERIES)) {
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound batteryNbt = list.getCompoundTagAt(i);
                ItemStack batteryStack = new ItemStack(batteryNbt);
                IFluidHandlerItem fluidHandler = batteryStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    int filled = fluidHandler.fill(toFill, !simulate);
                    if (filled > 0) {
                        toFill.amount -= filled;
                        if (toFill.amount <= 0) {
                            break;
                        }
                    }
                }
            }
            nbt.setTag(BATTERIES, list);
        }
        return fluid.amount - toFill.amount;
    }

    public static List<IFluidHandlerItem> getFluidHandlers(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (!nbt.hasKey(BATTERIES))
                return Collections.emptyList();
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            List<IFluidHandlerItem> fluidHandlers = new ArrayList<>();
            for (int i = 0; i < list.tagCount(); i++) {
                ItemStack item = new ItemStack(list.getCompoundTagAt(i));
                IFluidHandlerItem fluidHandler = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    fluidHandlers.add(fluidHandler);
                }
            }
            return fluidHandlers;
        }
        return Collections.emptyList();
    }

    public static void setBatteries(ItemStack stack, List<ItemStack> batteries) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
        }
        NBTTagList list = new NBTTagList();
        for (ItemStack battery : batteries) {
            list.appendTag(battery.serializeNBT());
        }
        nbt.setTag(BATTERIES, list);
        stack.setTagCompound(nbt);
    }

    public static List<ItemStack> getBatteries(ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (!nbt.hasKey(BATTERIES))
                return Collections.emptyList();
            List<ItemStack> batteries = new ArrayList<>();
            NBTTagList list = nbt.getTagList(BATTERIES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                batteries.add(new ItemStack(list.getCompoundTagAt(i)));
            }
            return batteries;
        }
        return Collections.emptyList();
    }

    public static List<ItemStack> getModuleStacksOf(ItemStack stack) {
        ModularArmor armor = get(stack);
        if (armor != null) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null || !nbt.hasKey(MODULES))
                return Collections.emptyList();
            List<ItemStack> modules = new ArrayList<>();
            NBTTagList modulesNbt = nbt.getTagList(MODULES, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < modulesNbt.tagCount(); i++) {
                NBTTagCompound moduleNbt = modulesNbt.getCompoundTagAt(i);
                IModule module = Modules.getModule(moduleNbt.getInteger("ID"));
                if (!moduleNbt.getBoolean("Destroyed")) {
                    ItemStack stack1 = module.getMetaValueItem().getStackForm();
                    NBTTagCompound itemNbt = module.writeExtraDataToModuleItem(moduleNbt);
                    if (itemNbt != null)
                        stack1.setTagCompound(itemNbt);
                    modules.add(stack1);
                }
            }
            return modules;
        }

        return Collections.emptyList();
    }

    public static List<IModule> getModulesOf(ItemStack stack) {
        ModularArmor armor = get(stack);
        if (armor != null) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null || !nbt.hasKey(MODULES))
                return Collections.emptyList();
            NBTTagList modulesNbt = nbt.getTagList(MODULES, Constants.NBT.TAG_COMPOUND);
            List<IModule> modules = new ArrayList<>();
            for (int i = 0; i < modulesNbt.tagCount(); i++) {
                NBTTagCompound moduleNbt = modulesNbt.getCompoundTagAt(i);
                IModule module = Modules.getModule(moduleNbt.getInteger("ID"));
                if (!moduleNbt.getBoolean("Destroyed"))
                    modules.add(module);
            }
            return modules;
        }

        return Collections.emptyList();
    }

    public static List<Pair<IModule, NBTTagCompound>> getModulesWithData(ItemStack stack) {
        ModularArmor armor = get(stack);
        if (armor != null) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null || !nbt.hasKey(MODULES))
                return Collections.emptyList();
            NBTTagList modulesNbt = nbt.getTagList(MODULES, Constants.NBT.TAG_COMPOUND);
            List<Pair<IModule, NBTTagCompound>> modules = new ArrayList<>();
            for (int i = 0; i < modulesNbt.tagCount(); i++) {
                NBTTagCompound moduleNbt = modulesNbt.getCompoundTagAt(i);
                IModule module = Modules.getModule(moduleNbt.getInteger("ID"));
                if (!moduleNbt.getBoolean("Destroyed"))
                    modules.add(Pair.of(module, moduleNbt));
            }
            return modules;
        }

        return Collections.emptyList();
    }

    public static void writeModulesTo(Collection<ItemStack> modules, ItemStack stack) {
        ModularArmor armor = get(stack);
        if (armor != null) {
            NBTTagList modulesNbt = new NBTTagList();
            for (ItemStack stack1 : modules) {
                NBTTagCompound moduleNbt = new NBTTagCompound();
                IModule module = IModule.getOf(stack1);
                if (module == null)
                    throw new NullPointerException("Module is null");
                moduleNbt.setInteger("ID", Modules.getModuleId(module));
                module.writeExtraDataToArmor(moduleNbt, stack1);
                modulesNbt.appendTag(moduleNbt);
            }
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                nbt = new NBTTagCompound();
            }
            nbt.setTag(MODULES, modulesNbt);
            stack.setTagCompound(nbt);
        }
    }

    public static void removeModules(ItemStack stack, Collection<Integer> indexes) {
        ModularArmor armor = get(stack);
        if (armor != null) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null || !nbt.hasKey(MODULES))
                return;
            NBTTagList modulesNbt = nbt.getTagList(MODULES, Constants.NBT.TAG_COMPOUND);
            NBTTagList newList = new NBTTagList();
            for (int i = 0; i < modulesNbt.tagCount(); i++) {
                if (!indexes.contains(i))
                    newList.appendTag(modulesNbt.get(i));
            }
            nbt.setTag(MODULES, newList);
        }
    }
}
