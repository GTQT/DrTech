package com.meowmel.cropQT.item;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;


/**
 * 种子袋物品
 *
 * NBT结构:
 *   cropId: String         - 作物类型ID
 *   statGrowth: int        - Growth属性
 *   statGain: int          - Gain属性
 *   statResistance: int    - Resistance属性
 *
 * 获取方式:
 * 1. 创造模式物品栏 (每种作物一个默认种子袋)
 * 2. 破坏已种植的作物架 (保留当前属性)
 * 3. 原版种子直接右键作物架自动转换 (不需要种子袋)
 */
public class ItemCropSeed extends Item {

    // 注意: 这个引用在ModItems.init()中赋值
    // createSeedBag需要通过这个引用创建ItemStack
    public static Item INSTANCE;

    public ItemCropSeed() {
        setTranslationKey(Tags.MODID + ".crop_seed");
        setRegistryName(Tags.MODID, "crop_seed");
        setMaxStackSize(64);
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        INSTANCE = this;
    }

    // ==================== 静态工具方法 ====================

    /**
     * 创建一个携带指定作物和属性的种子袋
     */
    public static ItemStack createSeedBag(String cropId, CropStats stats) {
        if (INSTANCE == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(INSTANCE);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("cropId", cropId);
        stats.writeToNBT(nbt);
        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * 创建默认属性(1/1/1)的种子袋
     */
    public static ItemStack createSeedBag(String cropId) {
        return createSeedBag(cropId, new CropStats(1, 1, 1));
    }

    /**
     * 从种子袋读取作物ID
     */
    public static String getCropId(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getString("cropId");
        }
        return "";
    }

    /**
     * 从种子袋读取作物属性
     */
    public static CropStats getCropStats(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return CropStats.readFromNBT(stack.getTagCompound());
        }
        return new CropStats();
    }

    // ==================== 显示 ====================

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String cropId = getCropId(stack);
        if (!cropId.isEmpty()) {
            CropType type = CropRegistry.get(cropId);
            if (type != null) {
                return type.getDisplayName() + " 种子袋";
            }
            return cropId + " 种子袋";
        }
        return "空种子袋";
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world,
                               @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
        String cropId = getCropId(stack);
        if (cropId.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + "右键作物架进行种植");
            tooltip.add(TextFormatting.GRAY + "或用原版种子直接右键作物架");
            return;
        }

        CropType type = CropRegistry.get(cropId);
        if (type != null) {
            tooltip.add(TextFormatting.GRAY + "Tier: " + type.getTier());
        }

        CropStats stats = getCropStats(stack);
        tooltip.add("");
        tooltip.add(TextFormatting.RED + "Growth: " + stats.getGrowth() +
                TextFormatting.GRAY + " (生长速度)");
        tooltip.add(TextFormatting.YELLOW + "Gain: " + stats.getGain() +
                TextFormatting.GRAY + " (产量)");
        tooltip.add(TextFormatting.BLUE + "Resistance: " + stats.getResistance() +
                TextFormatting.GRAY + " (抗性)");

    }

    /**
     * 创造模式物品栏 - 显示所有作物的默认种子袋
     */
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;

        // 确保作物已注册
        if (CropRegistry.getAll().isEmpty()) return;

        for (CropType type : CropRegistry.getAll().values()) {
            if (!type.getId().equals("weed")) {
                items.add(createSeedBag(type.getId()));
            }
        }
    }

    // ==================== 种子袋染色 ====================

    /** 根据种子袋内的作物类型，对双层白模贴图上色 */
    public static class SeedColorHandler implements net.minecraft.client.renderer.color.IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            String cropId = getCropId(stack);
            if (!cropId.isEmpty()) {
                CropType type = CropRegistry.get(cropId);
                if (type != null && type.getSeedColor() != 0xFFFFFF) {
                    return type.getSeedColor();
                }
            }
            return 0xFFFFFF; // 不着色
        }
    }

    // ==================== 种子袋贴图路由 ====================

    /**
     * 根据种子袋NBT中的cropId，动态选择物品模型贴图
     * - 若作物设定了seedTexture → 使用 drtech:crop_seed_<seedTexture值> (多个作物可共享)
     * - 否则 → 使用默认 drtech:crop_seed
     */
    public static class SeedMeshDefinition implements ItemMeshDefinition {
        public static final ModelResourceLocation DEFAULT_MODEL =
                new ModelResourceLocation(Tags.MODID + ":crop_seed", "inventory");

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack) {
            String cropId = getCropId(stack);
            if (!cropId.isEmpty()) {
                CropType type = CropRegistry.get(cropId);
                if (type != null && type.getSeedTexture() != null) {
                    return new ModelResourceLocation(
                            Tags.MODID + ":crop_seed_" + type.getSeedTexture(), "inventory");
                }
            }
            return DEFAULT_MODEL;
        }
    }
}
