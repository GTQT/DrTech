package com.meowmel.cropQT.client;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.unification.material.properties.CropProperty;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 把作物的生长贴图塞进方块图集。
 *
 * <h2>为什么需要这个</h2>
 * MC 1.12.2 的图集<b>只拼接被模型引用过的贴图</b>（见 {@code ModelBakery.registerSprites}），
 * 而作物贴图只被 {@link CropStickTESR} 在运行时按名字取用、没有任何模型引用它们 ——
 * 不在这里手动登记的话，{@code getAtlasSprite} 一律返回 missing，
 * 结果就是<b>作物架里永远空空如也</b>。
 *
 * <h2>为什么不能直接问 CropRegistry</h2>
 * 拼接发生在 preInit 的 {@code ModelRegistryEvent} 之后，而 {@code CropRegistry} 要到
 * FML init 才装填（材料作物要等 {@code OreDictUnifier.init()}）。所以这里绕开注册表：
 * <ul>
 *     <li>材料驱动 —— 直接读材料的 {@link CropProperty#getRenderTexture()}，
 *         那些属性在 {@code PostMaterialEvent} 就写好了，来得及</li>
 *     <li>手写作物 —— 读 {@link CropRegistry#VANILLA_CROP_IDS} 这张静态表</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
public final class CropTextureStitcher {

    /** 一个目录最多探多少帧，与 {@link CropStickTESR} 保持一致。 */
    private static final int MAX_FRAMES = 16;

    private CropTextureStitcher() {}

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        // 只认方块图集（作物贴图都在 textures/blocks 下）
        if (!"textures".equals(map.getBasePath())) {
            return;
        }
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
        for (String dir : collectTextureDirs()) {
            registerFrames(map, rm, dir);
        }
    }

    /** 需要拼接贴图的目录名（材料作物 + 手写作物），去重。 */
    private static Set<String> collectTextureDirs() {
        Set<String> dirs = new LinkedHashSet<>();
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            for (Material material : registry.getAllMaterials()) {
                CropProperty property = material.getProperty(CropProperty.KEY);
                if (property != null && property.getRenderTexture() != null) {
                    dirs.add(property.getRenderTexture());
                }
            }
        }
        dirs.addAll(java.util.Arrays.asList(CropRegistry.VANILLA_CROP_IDS));
        return dirs;
    }

    /**
     * 把一个目录里的帧登记进图集。
     *
     * <p>先查 `stage_N`，没有再查老命名的 `N`；<b>两种都先确认文件存在再登记</b> ——
     * 登记不存在的贴图会让图集拼接刷一屏 "Unable to load texture" 警告。
     */
    private static void registerFrames(TextureMap map, IResourceManager rm, String dir) {
        String base = "blocks/crop/" + dir + "/";
        for (int i = 0; i < MAX_FRAMES; i++) {
            ResourceLocation sprite = firstExisting(rm, base + "stage_" + i, base + i);
            if (sprite == null) {
                // 不能 break：老命名从 1 开始，第 0 帧本来就缺
                continue;
            }
            map.registerSprite(sprite);
        }
    }

    /** 两个候选名里取第一个真实存在的；都没有返回 {@code null}。 */
    private static ResourceLocation firstExisting(IResourceManager rm, String... paths) {
        for (String path : paths) {
            ResourceLocation sprite = new ResourceLocation(Tags.MODID, path);
            ResourceLocation file = new ResourceLocation(Tags.MODID, "textures/" + path + ".png");
            if (resourceExists(rm, file)) {
                return sprite;
            }
        }
        return null;
    }

    private static boolean resourceExists(IResourceManager rm, ResourceLocation file) {
        try (IResource ignored = rm.getResource(file)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
