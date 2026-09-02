package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * mob_info（怪物信息）分类入口：点任意刷怪蛋即可查看该生物的掉落、
 * 生成群系与经验信息。分类 uid: drtech.mob_info。
 * 数据自研（战利品表确定性分析），不依赖 JER。
 */
@JEIPlugin
public class MobInfoJeiPlugin implements IModPlugin {

    public static final String UID = Tags.MODID + ".mob_info";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new MobInfoCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        // 动态插件：点击任意带实体 NBT 的刷怪蛋可反向定位到生物页
        registry.addRecipeRegistryPlugin(new MobInfoRecipeRegistryPlugin());
        // 催化剂：刷怪蛋（点开即浏览全部生物）
        registry.addRecipeCatalyst(new ItemStack(Items.SPAWN_EGG), UID);
    }
}
