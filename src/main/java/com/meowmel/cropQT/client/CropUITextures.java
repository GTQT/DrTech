package com.meowmel.cropQT.client;

import com.cleanroommc.modularui.drawable.UITexture;
import com.drppp.drtech.Tags;

/**
 * cropQT 的界面贴图。
 *
 * <p>贴图全部来自 CropsNH（{@code assets/cropsnh/textures/gui/...}），逐字节复制到
 * {@code assets/drtech/textures/gui/...}——这里只做同样的 {@link UITexture} 声明，
 * 让作物管理机的槽位底纹和四个开关按钮与源端长得一样。
 *
 * <p>{@code UITexture} 是惰性加载的（{@code location} 还没带 {@code textures/} 与
 * {@code .png} 时由它自己补），所以这里不需要 {@code init()} 时机，静态常量即可。
 */
public final class CropUITextures {

    private CropUITextures() {}

    /** 除草剂槽底纹（CropsNH {@code OVERLAY_SLOT_WEED_EX_STANDARD}）。 */
    public static final UITexture OVERLAY_SLOT_WEED_EX = UITexture.builder()
            .location(Tags.MODID, "gui/overlay_slot/weed_ex")
            .imageSize(18, 18)
            .adaptable(1)
            .canApplyTheme()
            .build();

    /** 肥料槽底纹（CropsNH {@code OVERLAY_SLOT_FERTILIZER_STANDARD}）。 */
    public static final UITexture OVERLAY_SLOT_FERTILIZER = UITexture.builder()
            .location(Tags.MODID, "gui/overlay_slot/fertilizer")
            .imageSize(18, 18)
            .adaptable(1)
            .canApplyTheme()
            .build();

    /** 浇水开关。 */
    public static final UITexture BUTTON_TOGGLE_WATER = UITexture.builder()
            .location(Tags.MODID, "gui/overlay_button/water_toggle")
            .fullImage()
            .canApplyTheme()
            .build();

    /** 除草剂开关。 */
    public static final UITexture BUTTON_TOGGLE_WEED_EX = UITexture.builder()
            .location(Tags.MODID, "gui/overlay_button/weed_ex_toggle")
            .fullImage()
            .canApplyTheme()
            .build();

    /** 施肥开关。 */
    public static final UITexture BUTTON_TOGGLE_FERTILIZER = UITexture.builder()
            .location(Tags.MODID, "gui/overlay_button/fertilizer_toggle")
            .fullImage()
            .canApplyTheme()
            .build();

    /** 收割开关。 */
    public static final UITexture BUTTON_TOGGLE_HARVEST = UITexture.builder()
            .location(Tags.MODID, "gui/overlay_button/harvest_toggle")
            .fullImage()
            .canApplyTheme()
            .build();
}
