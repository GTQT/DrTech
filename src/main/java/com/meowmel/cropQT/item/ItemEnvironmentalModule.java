package com.meowmel.cropQT.item;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.BiomeDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 环境模块：往工业农场里塞一个生物群系标签。
 *
 * <p>效果只有一个——让农场在计算「喜好群」时把这个 tag 也算上。
 * <b>它不改温度、不改湿度、不改降雨</b>，源端就是这么设计的：
 * 装模块的意义是「让讨厌这里的作物觉得这里是它老家」，而不是「把沙漠变雨林」。
 *
 * <p>meta 0 是空白卡，不算任何 tag，不能进槽位。
 */
public class ItemEnvironmentalModule extends Item {

    /** 空白卡。 */
    public static final int META_BLANK = 0;

    /** 按 meta 索引的 tag，下标即 meta；下标 0 为 null（空白卡）。 */
    private static final List<BiomeDictionary.Type> TAGS = new ArrayList<>();

    /**
     * 28 个变体，顺序即 meta 1~28。
     *
     * <p>照搬源端的清单。
     */
    private static final BiomeDictionary.Type[] VARIANTS = {
            BiomeDictionary.Type.HOT, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SPARSE,
            BiomeDictionary.Type.DENSE, BiomeDictionary.Type.WET, BiomeDictionary.Type.DRY,
            BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.CONIFEROUS, BiomeDictionary.Type.JUNGLE,
            BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.LUSH,
            BiomeDictionary.Type.NETHER, BiomeDictionary.Type.END, BiomeDictionary.Type.MUSHROOM,
            BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER,
            BiomeDictionary.Type.MESA, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.PLAINS,
            BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.SWAMP,
            BiomeDictionary.Type.SANDY, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.WASTELAND,
            BiomeDictionary.Type.BEACH,
    };

    static {
        TAGS.add(null);
        for (BiomeDictionary.Type type : VARIANTS) {
            TAGS.add(type);
        }
    }

    public ItemEnvironmentalModule() {
        setTranslationKey(Tags.MODID + ".environmental_module");
        setRegistryName(Tags.MODID, "environmental_module");
        setMaxStackSize(1);
        setHasSubtypes(true);
        setCreativeTab(DrTechMain.DrTechTab);
    }

    /** 变体总数（含空白卡）。 */
    public static int getVariantCount() {
        return TAGS.size();
    }

    /** meta 对应的生物群系标签；空白卡或越界返回 {@code null}。 */
    @Nullable
    public static BiomeDictionary.Type getBiomeTag(int meta) {
        return meta >= 0 && meta < TAGS.size() ? TAGS.get(meta) : null;
    }

    /** 造一个指定标签的环境模块。 */
    public static ItemStack create(int meta) {
        return new ItemStack(com.drppp.drtech.common.items.ItemsInit.ENVIRONMENTAL_MODULE, 1, meta);
    }

    /**
     * 该 meta 用的模型名后缀。
     *
     * <p>模型是「底图 {@code blank} + 一层标签覆盖」，所以每个 meta 一套模型 ——
     * 覆盖层的图是静态的，模型里就写死了，没法像染色那样运行时换。
     * 名字与 {@link #VARIANTS} 同序，越界落回空白卡。
     */
    public static String getModelSuffix(int meta) {
        BiomeDictionary.Type tag = getBiomeTag(meta);
        return tag == null ? "blank" : tag.getName().toLowerCase();
    }

    @Override
    public @NotNull String getTranslationKey(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta == META_BLANK || getBiomeTag(meta) == null) {
            return super.getTranslationKey(stack) + ".blank";
        }
        return super.getTranslationKey(stack) + "." + getBiomeTag(meta).getName().toLowerCase();
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (int meta = META_BLANK; meta < TAGS.size(); meta++) {
            items.add(new ItemStack(this, 1, meta));
        }
    }
}
