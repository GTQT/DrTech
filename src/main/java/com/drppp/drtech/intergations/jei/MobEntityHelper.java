package com.drppp.drtech.intergations.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体装配工具：实例化已注册生物、取战利品表 RL、构造刷怪蛋、扫描生成群系、生成显示名。
 *
 * 参考：JustEnoughDrops-1.1.1 的 fallback 装配路线（去掉 JER 依赖后的自写版本）。
 * 世界来源：优先当前客户端世界；未进世界时用内置的极简假世界兜底（模式同 JER 的 FakeClientWorld，
 * 已按 "Don't Be a Jerk" 许可引用其结构思路）。
 */
public class MobEntityHelper {

    private static final Map<Class<?>, List<String>> BIOME_CACHE = new ConcurrentHashMap<>();

    /** 16 种羊毛色英文名 -> 中文（显示名前缀用；默认实例为白色） */
    private static final Map<String, String> COLOR_ZH = new ConcurrentHashMap<>();

    static {
        COLOR_ZH.put("white", "白色");
        COLOR_ZH.put("orange", "橙色");
        COLOR_ZH.put("magenta", "品红色");
        COLOR_ZH.put("light_blue", "淡蓝色");
        COLOR_ZH.put("yellow", "黄色");
        COLOR_ZH.put("lime", "黄绿色");
        COLOR_ZH.put("pink", "粉红色");
        COLOR_ZH.put("gray", "灰色");
        COLOR_ZH.put("silver", "银灰色");
        COLOR_ZH.put("cyan", "青色");
        COLOR_ZH.put("purple", "紫色");
        COLOR_ZH.put("blue", "蓝色");
        COLOR_ZH.put("brown", "棕色");
        COLOR_ZH.put("green", "绿色");
        COLOR_ZH.put("red", "红色");
        COLOR_ZH.put("black", "黑色");
    }

    private MobEntityHelper() {
    }

    // ---------------- 实例化 ----------------

    /** 当前可用世界：客户端世界，否则懒构建的假世界 */
    public static World worldForEntities() {
        World world = Minecraft.getMinecraft().world;
        return world != null ? world : FakeWorldHolder.get();
    }

    /** 反射构造实体实例；失败返回 null（调用方跳过该实体） */
    @Nullable
    public static EntityLiving instantiate(Class<? extends EntityLiving> clazz) {
        try {
            Constructor<? extends EntityLiving> ctor = clazz.getConstructor(World.class);
            return ctor.newInstance(worldForEntities());
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------- 战利品表 RL ----------------

    private static Method lootTableMethod;

    static {
        try {
            lootTableMethod = EntityLiving.class.getDeclaredMethod("getLootTable");
            lootTableMethod.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
    }

    /**
     * 反射取实体的战利品表定位（原版 EntityLiving 的 protected getLootTable()，
     * 会正确处理 sheep/witch 等覆写）；失败回退到注册名猜 "entities/&lt;path&gt;"。
     */
    public static ResourceLocation getLootTable(EntityLiving entity, ResourceLocation registryName) {
        if (lootTableMethod != null) {
            try {
                ResourceLocation rl = (ResourceLocation) lootTableMethod.invoke(entity);
                if (rl != null) return rl;
            } catch (Exception ignored) {
            }
        }
        if (registryName != null) {
            return new ResourceLocation(registryName.getNamespace(), "entities/" + registryName.getPath());
        }
        return null;
    }

    // ---------------- 刷怪蛋 ----------------

    public static ItemStack buildSpawnEgg(String entityId) {
        ItemStack egg = new ItemStack(Items.SPAWN_EGG);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound entityTag = new NBTTagCompound();
        entityTag.setString("id", entityId);
        tag.setTag("EntityTag", entityTag);
        egg.setTagCompound(tag);
        return egg;
    }

    // ---------------- 显示名 ----------------

    public static String getDisplayName(EntityLiving entity) {
        String raw;
        try {
            raw = entity.getName();
        } catch (Exception e) {
            return "";
        }
        if (raw == null || raw.isEmpty()) return "";
        if (entity instanceof EntitySheep) {
            String dyeName = ((EntitySheep) entity).getFleeceColor().getName(); // 下划线形式: light_blue
            String zh = COLOR_ZH.getOrDefault(dyeName, dyeName.replace("_", " "));
            raw = zh + " " + raw;
        }
        // 首字母大写化（JED 同款；中文无效果）
        String[] words = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String s : words) {
            if (s.isEmpty()) continue;
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1).toLowerCase()).append(' ');
        }
        return sb.toString().trim();
    }

    // ---------------- 经验值 ----------------

    /** 读取 EntityLiving.experienceValue（protected 字段，按名反射） */
    public static int getXp(EntityLiving entity) {
        try {
            Field f = EntityLiving.class.getDeclaredField("experienceValue");
            f.setAccessible(true);
            return f.getInt(entity);
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------------- 生成群系 ----------------

    /**
     * 扫描全部已注册群系的生成列表，找出会自然生成该实体的群系。
     * 结果按实体类缓存；空列表 = 未知。
     */
    public static List<String> getSpawnBiomes(Class<? extends EntityLiving> clazz) {
        List<String> cached = BIOME_CACHE.get(clazz);
        if (cached != null) return cached;
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (Biome biome : GameRegistry.findRegistry(Biome.class)) {
            if (biome == null) continue;
            for (EnumCreatureType type : EnumCreatureType.values()) {
                List<Biome.SpawnListEntry> list = biome.getSpawnableList(type);
                if (list == null) continue;
                for (Biome.SpawnListEntry entry : list) {
                    if (entry.entityClass == clazz) {
                        String biomeName = translateBiomeName(biome);
                        if (biomeName != null) names.add(biomeName);
                        break;
                    }
                }
            }
        }
        List<String> result = Collections.unmodifiableList(new ArrayList<>(names));
        BIOME_CACHE.put(clazz, result);
        return result;
    }

    @Nullable
    private static String translateBiomeName(Biome biome) {
        try {
            ResourceLocation key = biome.getRegistryName();
            if (key == null) return null;
            String langKey = "biome." + key.getNamespace() + "." + key.getPath() + ".name";
            if (I18n.hasKey(langKey)) return I18n.format(langKey);
            return key.getPath();
        } catch (Exception e) {
            return null;
        }
    }

    /** 判定实体是否应被整体跳过（JED 曾对 Ancient Warfare 实体特判） */
    public static boolean shouldSkip(EntityEntry entry) {
        String className = entry.getEntityClass().getName();
        return className.startsWith("net.shadowmage.ancientwarfare");
    }

    public static boolean isDyeSheep(EntityLiving entity) {
        return entity instanceof EntitySheep;
    }

    // ---------------- 假世界兜底 ----------------

    private static final class FakeWorldHolder {
        static final World INSTANCE = new FakeWorld();

        static World get() {
            return INSTANCE;
        }
    }

    /** 极简假世界：不提供任何 chunk/存档能力，仅用于实体实例化 */
    private static final class FakeWorld extends World {

        FakeWorld() {
            super(new NoopSaveHandler(), fakeWorldInfo(), new FakeProvider(), new Profiler(), true);
        }

        private static WorldInfo fakeWorldInfo() {
            return new WorldInfo(new WorldSettings(0, GameType.SURVIVAL, true, false, WorldType.DEFAULT),
                    "drtech_fake_world");
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return new IChunkProvider() {
                @Override
                public Chunk getLoadedChunk(int x, int z) {
                    return null;
                }

                @Override
                public Chunk provideChunk(int x, int z) {
                    return null;
                }

                @Override
                public boolean tick() {
                    return false;
                }

                @Override
                public String makeString() {
                    return "drtech_fake";
                }

                @Override
                public boolean isChunkGeneratedAt(int x, int z) {
                    return false;
                }
            };
        }

        @Override
        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            return false;
        }
    }

    private static final class FakeProvider extends WorldProvider {
        @Override
        public DimensionType getDimensionType() {
            return DimensionType.OVERWORLD;
        }
    }

    private static final class NoopSaveHandler implements ISaveHandler {
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() {
        }

        @Override
        public net.minecraft.world.chunk.storage.IChunkLoader getChunkLoader(WorldProvider provider) {
            return null;
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
        }

        @Override
        public void saveWorldInfo(WorldInfo worldInformation) {
        }

        @Override
        public net.minecraft.world.storage.IPlayerFileData getPlayerNBTManager() {
            return null;
        }

        @Override
        public void flush() {
        }

        @Override
        public java.io.File getWorldDirectory() {
            return null;
        }

        @Override
        public java.io.File getMapFileFromName(String mapName) {
            return null;
        }

        @Override
        public net.minecraft.world.gen.structure.template.TemplateManager getStructureTemplateManager() {
            return null;
        }
    }
}
