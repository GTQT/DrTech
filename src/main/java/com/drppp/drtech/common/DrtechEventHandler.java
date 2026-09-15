package com.drppp.drtech.common;

import com.drppp.drtech.Tags;
import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.meowmel.cropQT.api.unification.CropMaterialType;
import com.meowmel.cropQT.api.unification.material.CropMaterialScanner;
import com.meowmel.cropQT.api.unification.material.CropMaterials;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.drppp.drtech.Tags.MODID;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechEventHandler {
    public static int ctrlflag = 0;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(MaterialEvent event) {
        DrtechMaterials.init();
        // 作物前缀必须在这里登记：GT 的 MetaItems.init() 会遍历前缀表建物品，晚了就漏
        CropMaterialType.registerOrePrefixes();
    }

    /**
     * 全表齐了之后的收尾。
     *
     * <p><b>这是唯一能给材料补 flag 的时机</b>：GT 在这个事件之后立刻
     * {@code freezeRegistries()}，再调 {@code Material.addFlags} 会抛
     * {@code IllegalStateException}。而 {@code OreDictUnifier.init()} 在那之后才跑，
     * 所以这里补的 flag 赶得上产物物品的生成。
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerPostMaterials(PostMaterialEvent event) {
        // 先声明（别的模组的材料这时也都齐了），再补 flag
        // 建作物要等 OreDictUnifier.init()，那在 FML init，见 CropInitHandler
        CropMaterials.declare();
        CropMaterialScanner.applyFlags();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(MODID);
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void createMaterialRegistry(MaterialRegistryEvent event) {
        GregTechAPI.materialManager.createRegistry(MODID);
    }

}
