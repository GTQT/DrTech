package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JEI 动态配方注册插件（照 JustEnoughDrops-1.1.1 同名类移植，去掉 JER 依赖）：
 * - 惰性构建：首次被 JEI 查询时遍历全部已注册 EntityLiving，逐只分析战利品表生成配方；
 * - 支持"带实体 NBT 的刷怪蛋"（EntityTag.id 或顶层 id）反向定位到对应生物页。
 */
public class MobInfoRecipeRegistryPlugin implements IRecipeRegistryPlugin {

    private static final Logger LOGGER = LogManager.getLogger("drtech.mob_info");

    private List<MobInfoWrapper> allWrappers;
    private Map<String, List<MobInfoWrapper>> wrapperMap;

    private void ensureWrappersLoaded() {
        if (allWrappers != null) return;

        allWrappers = new ArrayList<>();
        wrapperMap = new HashMap<>();

        for (EntityEntry entry : GameRegistry.findRegistry(EntityEntry.class)) {
            Class<?> entityClass = entry.getEntityClass();
            if (entityClass == null
                    || !EntityLiving.class.isAssignableFrom(entityClass)
                    || MobEntityHelper.shouldSkip(entry)) {
                continue;
            }
            ResourceLocation registryName = entry.getRegistryName();
            if (registryName == null) continue;
            String entityId = registryName.toString();

            @SuppressWarnings("unchecked")
            EntityLiving entity = MobEntityHelper.instantiate((Class<? extends EntityLiving>) entityClass);
            if (entity == null) {
                LOGGER.debug("Skipping entity {}, cannot instantiate", entityId);
                continue;
            }

            ResourceLocation lootTable = MobEntityHelper.getLootTable(entity, registryName);
            List<LootDropInfo> drops = MobLootAnalyzer.analyze(lootTable);
            boolean unrecognized = drops.isEmpty();

            ItemStack spawnEgg = MobEntityHelper.buildSpawnEgg(entityId);
            String name = MobEntityHelper.getDisplayName(entity);
            int xp = MobEntityHelper.getXp(entity);
            @SuppressWarnings("unchecked")
            List<String> biomes = MobEntityHelper.getSpawnBiomes((Class<? extends EntityLiving>) entityClass);

            MobInfoEntry mobInfo = new MobInfoEntry(entity, entityId, name, xp,
                    spawnEgg, drops, biomes, unrecognized);
            MobInfoWrapper wrapper = new MobInfoWrapper(mobInfo, spawnEgg);

            allWrappers.add(wrapper);
            wrapperMap.computeIfAbsent(entityId, k -> new ArrayList<>()).add(wrapper);
        }
        LOGGER.info("Loaded {} total mob wrappers, mapped {} unique entity IDs.",
                allWrappers.size(), wrapperMap.size());
    }

    // ---------------- NBT 实体 id 提取（刷怪蛋） ----------------

    @Nullable
    private static String extractEntityId(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey("EntityTag", 10)) {
            NBTTagCompound entityTag = tag.getCompoundTag("EntityTag");
            if (entityTag.hasKey("id", 8)) return entityTag.getString("id");
        }
        if (tag.hasKey("id", 8)) return tag.getString("id");
        return null;
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if (focus == null) return Collections.emptyList();
        Object value = focus.getValue();
        if (value instanceof ItemStack) {
            String entityId = extractEntityId((ItemStack) value);
            if (entityId != null) {
                return Collections.singletonList(MobInfoJeiPlugin.UID);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory,
                                                                    IFocus<V> focus) {
        if (!MobInfoJeiPlugin.UID.equals(recipeCategory.getUid())) {
            return Collections.emptyList();
        }
        ensureWrappersLoaded();
        if (focus == null || focus.getMode() == IFocus.Mode.INPUT) {
            // 作为输入方（点击某配方输入侧）或浏览分类：列出全部生物
            return new ArrayList<>((List<T>) (List<?>) allWrappers);
        }
        Object value = focus.getValue();
        if (!(value instanceof ItemStack)) {
            return Collections.emptyList();
        }
        String entityId = extractEntityId((ItemStack) value);
        if (entityId == null) return Collections.emptyList();
        List<MobInfoWrapper> wrappers = wrapperMap.get(entityId);
        return wrappers == null || wrappers.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>((List<T>) (List<?>) wrappers);
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        if (!MobInfoJeiPlugin.UID.equals(recipeCategory.getUid())) {
            return Collections.emptyList();
        }
        ensureWrappersLoaded();
        return new ArrayList<>((List<T>) (List<?>) allWrappers);
    }
}
