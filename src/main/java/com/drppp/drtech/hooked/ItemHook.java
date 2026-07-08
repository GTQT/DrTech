package com.drppp.drtech.hooked;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemHook extends ItemMultiVariant implements IBauble {
    public ItemHook() {
        super("hook", buildVariants());
        setMaxStackSize(1);
    }

    private static String[] buildVariants() {
        HookType[] values = HookType.values();
        String[] variants = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            variants[i] = "hook_" + values[i].name().toLowerCase(Locale.ROOT);
        }
        return variants;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        HookType type = getType(stack);
        if (type == null) {
            return;
        }
        String hookLangName = type.name().toLowerCase(Locale.ROOT);
        tooltip.add(I18n.format("tooltip.drtech.hook_" + hookLangName + ".info"));
        if (type.count > 1) {
            if (isInhibited(stack)) {
                tooltip.add(I18n.format("tooltip.drtech.hook.inhibited"));
                if (GuiScreen.isShiftKeyDown()) {
                    tooltip.add(I18n.format("tooltip.drtech.hook.inhibited.help"));
                }
            } else if (GuiScreen.isShiftKeyDown()) {
                tooltip.add(I18n.format("tooltip.drtech.hook.uninhibited.help"));
            }
        }
        if (GuiScreen.isShiftKeyDown()) {
            String controls = I18n.format("tooltip.drtech.hook_" + hookLangName + ".controls", "C");
            for (String line : controls.split("\\\\n")) {
                tooltip.add("- " + line);
            }
        } else {
            tooltip.add(I18n.format("tooltip.drtech.hook.showControls"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn.isSneaking()) {
            ItemStack current = playerIn.getHeldItem(handIn);
            HookType type = getType(current);
            if (type != null && type.count > 1) {
                ItemStack copy = current.copy();
                NBTTagCompound nbt = copy.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                }
                if (isInhibited(copy)) {
                    nbt.removeTag("inhibited");
                } else {
                    nbt.setTag("inhibited", new NBTTagByte((byte) 1));
                }
                copy.setTagCompound(nbt);
                return new ActionResult<>(EnumActionResult.SUCCESS, copy);
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.TRINKET;
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        if (!(player instanceof EntityPlayer) || !Loader.isModLoaded("baubles")) {
            return false;
        }
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) player);
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (handler.getStackInSlot(slot).getItem() == this) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    public static boolean isInhibited(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getByte("inhibited") == 1;
    }

    @Nullable
    public static HookType getType(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != HookRegistry.HOOK_ITEM) {
            return null;
        }
        HookType[] values = HookType.values();
        return values[stack.getMetadata() % values.length];
    }

    @Nullable
    public static ItemStack getItem(EntityPlayer player) {
        if (Loader.isModLoaded("baubles")) {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
            for (int slot = 0; slot < baubles.getSlots(); slot++) {
                ItemStack stack = baubles.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() == HookRegistry.HOOK_ITEM) {
                    return stack;
                }
            }
        }
        if (player.getHeldItemMainhand().getItem() == HookRegistry.HOOK_ITEM) {
            return player.getHeldItemMainhand();
        }
        if (player.getHeldItemOffhand().getItem() == HookRegistry.HOOK_ITEM) {
            return player.getHeldItemOffhand();
        }
        for (int slot = 0; slot <= 35; slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == HookRegistry.HOOK_ITEM) {
                return stack;
            }
        }
        return null;
    }
}
