package com.meowmel.cropQT.client;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.unification.CropMaterialType;
import com.meowmel.cropQT.api.unification.material.properties.CropProperty;
import gregtech.api.GregTechAPI;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 让作物产物显示源端 {@code materialLeaf} 的逐材料彩色贴图。
 *
 * <p><b>为什么不能直接用图标集</b>：GT 的物品模型路径是
 * {@code material_sets/<图标集>/<前缀名>}，同一种前缀只有一张图；而 20 株作物的
 * 产物前缀（{@code leaf/fiber/flower/…}）是共用的。给材料换图标集倒是能区分，
 * 但那会连带改掉这种材料的方块 / 流体 / 管道贴图路径，风险远大于收益。
 *
 * <p><b>实际做法</b>：GT 的 {@code MetaPrefixItem} 已经把模型选择交给了
 * {@link ItemOverrideList}（{@code RenderItem#getItemModelWithOverrides} 每帧按物品栈问一次）。
 * 所以在 {@link ModelBakeEvent} 里把产物前缀的默认模型包一层：栈是带
 * {@link CropProperty} 的材料就换成 {@code drtech:crop_produce/<源端贴图>}，否则原样返回。
 * 这样材料本身、方块、流体一概不动。
 *
 * <p>贴图是照抄源端 {@code assets/cropsnh/textures/items/materialLeaf/} 的彩色原图，
 * 放在模型的 {@code layer1}：GT 只给 {@code tintIndex == 0} 染材料色，
 * 所以彩色原图不会被二次染色。
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public final class CropProduceModels {

    /** 材料 → 产物模型位置（只装真的有贴图的材料）。 */
    private static final Map<Material, ModelResourceLocation> ART_LOCATIONS = new IdentityHashMap<>();
    /** 材料 → 烘焙后的产物模型（{@link ModelBakeEvent} 里填）。 */
    private static final Map<Material, IBakedModel> BAKED_ART = new IdentityHashMap<>();
    /** 已经被包过的默认模型，避免同一位置包两层。 */
    private static final Set<ModelResourceLocation> WRAPPED = new HashSet<>();

    private CropProduceModels() {}

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        Set<OrePrefix> prefixes = new HashSet<>();
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            for (Material material : registry.getAllMaterials()) {
                CropProperty property = material.getProperty(CropProperty.KEY);
                if (property == null) {
                    continue;
                }
                String art = artName(property);
                if (art == null || !hasArtModel(art)) {
                    continue;
                }
                ART_LOCATIONS.put(material,
                        new ModelResourceLocation(Tags.MODID + ":crop_produce/" + art, "inventory"));
                prefixes.add(property.getMaterialType().getPrefix());
            }
        }
        if (ART_LOCATIONS.isEmpty()) {
            return;
        }
        ResourceLocation[] locations = ART_LOCATIONS.values().stream()
                .distinct()
                .toArray(ResourceLocation[]::new);
        for (MetaItem<?> item : MetaItems.ITEMS) {
            if (item instanceof MetaPrefixItem) {
                if (prefixes.contains(((MetaPrefixItem) item).getOrePrefix())) {
                    ModelLoader.registerItemVariants(item, locations);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        if (ART_LOCATIONS.isEmpty()) {
            return;
        }
        for (Map.Entry<Material, ModelResourceLocation> entry : ART_LOCATIONS.entrySet()) {
            IBakedModel baked = event.getModelRegistry().getObject(entry.getValue());
            if (baked != null) {
                BAKED_ART.put(entry.getKey(), baked);
            }
        }
        for (Map.Entry<Material, ModelResourceLocation> entry : ART_LOCATIONS.entrySet()) {
            if (!BAKED_ART.containsKey(entry.getKey())) {
                continue; // 模型没烘出来（资源缺失），别包，让原模型继续用
            }
            ModelResourceLocation fallback = defaultLocation(entry.getKey());
            if (fallback == null || !WRAPPED.add(fallback)) {
                continue;
            }
            IBakedModel original = event.getModelRegistry().getObject(fallback);
            if (original != null) {
                event.getModelRegistry().putObject(fallback, new ProduceModel(original));
            }
        }
    }

    /**
     * 源端贴图文件名。<b>规则来自 20 条 {@code CropMaterials} 声明</b>，
     * 有三处对不上“作物名_类型名”的要单列。
     */
    @Nullable
    private static String artName(@NotNull CropProperty property) {
        String render = property.getRenderTexture();
        switch (property.getMaterialType()) {
            case LEAF:
                // 锰叶在源端是带帧号的多帧图，只有第 0 帧是通用图标
                return "pyrolusium".equals(render) ? render + "_leaf.0" : render + "_leaf";
            case FIBER:
                return render + "_fiber";
            case TWIG:
                return render + "_twig";
            case FLOWER:
                return render + "_flower";
            case BERRY:
                // 油浆果的贴图就叫 oil_berry，没有后缀
                return "oil_berry".equals(render) ? render : render + "_berry";
            case ROOT:
                // 咸根同理
                return "salty_root".equals(render) ? render : render + "_root";
            case STEM:
                return render + "_stem";
            case WART:
                return render + "_wart";
            case BLOSSOM:
                return render + "_blossom";
            case MAGIC_ESSENCE:
                return render + "_essence";
            default:
                return null;
        }
    }

    /** 模型文件在不在（贴在资源里，用类路径探一下，省得运行时才炸）。 */
    private static boolean hasArtModel(@NotNull String art) {
        return CropProduceModels.class.getResource(
                "/assets/" + Tags.MODID + "/models/item/crop_produce/" + art + ".json") != null;
    }

    /** 不换模型时本该用的位置 —— 就是 GT 自己算的那条。 */
    @Nullable
    private static ModelResourceLocation defaultLocation(@NotNull Material material) {
        CropProperty property = material.getProperty(CropProperty.KEY);
        if (property == null) {
            return null;
        }
        MaterialIconType iconType = property.getMaterialType().getPrefix().materialIconType;
        if (iconType == null) {
            return null;
        }
        return new ModelResourceLocation(iconType.getItemModelPath(material.getMaterialIconSet()), "inventory");
    }

    /** 包一层，只为了替换 {@link IBakedModel#getOverrides()}。 */
    private static final class ProduceModel implements IBakedModel {

        private final IBakedModel original;
        private final ItemOverrideList overrides;

        private ProduceModel(@NotNull IBakedModel original) {
            this.original = original;
            this.overrides = new ItemOverrideList(Collections.emptyList()) {
                @Override
                public IBakedModel handleItemState(IBakedModel model, ItemStack stack,
                                                   @Nullable World world, @Nullable EntityLivingBase entity) {
                    Material material = MetaPrefixItem.tryGetMaterial(stack);
                    if (material != null) {
                        IBakedModel art = BAKED_ART.get(material);
                        if (art != null) {
                            return art;
                        }
                    }
                    return original;
                }
            };
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return original.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return original.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return original.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return original.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return original.getParticleTexture();
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return original.getItemCameraTransforms();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return overrides;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
                ItemCameraTransforms.TransformType cameraTransformType) {
            return original.handlePerspective(cameraTransformType);
        }
    }
}
