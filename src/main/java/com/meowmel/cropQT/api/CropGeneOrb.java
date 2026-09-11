package com.meowmel.cropQT.api;

import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

/**
 * 作物基因球：把「物种 / 生长 / 产量 / 抗性」四选一编码进 GT 的 {@code TOOL_DATA_ORB}。
 *
 * <p>复用 GT 的数据球而不是新造物品——它本身就是个自由 NBT 容器（GT 对它没有 schema 校验，
 * 复制时是整个 compound 原样搬运），接入成本最低。
 *
 * <p><b>数据放在 {@code cropqt_gene} 子标签里</b>，不碰装配线研究球用的
 * {@code assemblylineResearch} ——那个 key 会被 {@code AssemblyLineManager} 读走并拿去查配方表。
 *
 * <p>注意 GT 的 {@code DataItemBehavior#addInformation} 只认装配线的 key，所以本系统的球
 * <b>不会自动显示内容</b>。要显示得在 DrTechC 侧挂 tooltip 事件。
 */
public final class CropGeneOrb {

    /** 本系统专用的 NBT 子标签，与装配线的数据互不干扰。 */
    private static final String TAG = "cropqt_gene";
    private static final String KEY_KIND = "kind";
    private static final String KEY_VALUE = "value";

    /** 一个球里装的是哪一类数据。 */
    public enum Kind {
        /** 物种：值 = 作物 id 字符串。 */
        SPECIES("species"),
        /** 生长：值 = 1~31 的十进制字符串。 */
        GROWTH("growth"),
        /** 产量：值 = 1~31 的十进制字符串。 */
        GAIN("gain"),
        /** 抗性：值 = 1~31 的十进制字符串。 */
        RESISTANCE("resistance");

        private final String id;

        Kind(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Nullable
        public static Kind byId(String id) {
            for (Kind kind : values()) {
                if (kind.id.equals(id)) {
                    return kind;
                }
            }
            return null;
        }
    }

    private CropGeneOrb() {
    }

    // ==================== 写入 ====================

    /** 造一个装着指定数据的基因球。 */
    public static ItemStack create(Kind kind, String value) {
        ItemStack orb = MetaItems.TOOL_DATA_ORB.getStackForm();
        NBTTagCompound tag = orb.getOrCreateSubCompound(TAG);
        tag.setString(KEY_KIND, kind.getId());
        tag.setString(KEY_VALUE, value);
        return orb;
    }

    /** 造一个装属性值的基因球。 */
    public static ItemStack createStat(Kind kind, int stat) {
        return create(kind, Integer.toString(stat));
    }

    // ==================== 读取 ====================

    /** 是不是本系统的基因球（而不是装配线的研究球，也不是空球）。 */
    public static boolean isGeneOrb(ItemStack stack) {
        return !stack.isEmpty() && MetaItems.TOOL_DATA_ORB.isItemEqual(stack) && subTag(stack) != null;
    }

    /** 球里装的是哪一类数据；不是基因球时返回 {@code null}。 */
    @Nullable
    public static Kind getKind(ItemStack stack) {
        NBTTagCompound tag = subTag(stack);
        return tag == null ? null : Kind.byId(tag.getString(KEY_KIND));
    }

    /** 球里装的原始字符串值。 */
    @Nullable
    public static String getValue(ItemStack stack) {
        NBTTagCompound tag = subTag(stack);
        if (tag == null || !tag.hasKey(KEY_VALUE)) {
            return null;
        }
        return tag.getString(KEY_VALUE);
    }

    /**
     * 把属性球的字符串值解析成整数。
     *
     * @return 数值；不是数字时返回 {@code null}
     */
    @Nullable
    public static Integer getStat(ItemStack stack) {
        String value = getValue(stack);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private static NBTTagCompound subTag(ItemStack stack) {
        if (stack.isEmpty() || !MetaItems.TOOL_DATA_ORB.isItemEqual(stack)) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        return root == null ? null : root.getCompoundTag(TAG);
    }
}
