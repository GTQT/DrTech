package com.drppp.drtech.common.Blocks.Crops;

import java.util.*;

/**
 * 杂交配方注册表
 */

public class CrossBreedingRegistry {

    private static final List<CrossRecipe> RECIPES = new ArrayList<>();

    public static void register(String parent1, String parent2, String result, int weight) {
        RECIPES.add(new CrossRecipe(parent1, parent2, result, weight));
    }

    /**
     * 获取两个亲本能产出的所有杂交产物及权重
     */
    public static Map<String, Integer> getProducts(String a, String b) {
        Map<String, Integer> products = new HashMap<>();
        for (CrossRecipe recipe : RECIPES) {
            if (recipe.matches(a, b)) {
                products.merge(recipe.result, recipe.weight, Integer::sum);
            }
        }
        return products;
    }

    public static void registerDefaults() {
        // ======================================================================
        // 杂交路线图 (Tier 1 → Tier 10)
        //
        // Tier 1: wheat, potato, carrot, pumpkin, melon, beetroot, dandelion, rose
        // Tier 2: reed, cyazint, tulip
        // Tier 3: cocoa, flax, hyacinth
        // Tier 4: stickreed, blackthorn, wonderflower
        // Tier 5: ferru, shining, nether_wart_crop, sweet_flower, slimeplant
        // Tier 6: redwheat, brown_mushroom, red_mushroom
        // Tier 7: aurelia
        // Tier 8: dahlia (钻石)
        // Tier 9: green_diamond (绿宝石)
        // ======================================================================

        // === 同种杂交(属性提升) ===
        register("wheat", "wheat", "wheat", 10);
        register("potato", "potato", "potato", 10);
        register("carrot", "carrot", "carrot", 10);

        // === Tier 1 → Tier 2 ===
        register("wheat", "pumpkin", "reed", 8);
        register("potato", "carrot", "reed", 6);
        register("wheat", "melon", "cyazint", 5);
        register("dandelion", "rose", "tulip", 6);
        register("dandelion", "wheat", "cyazint", 4);

        // === Tier 1/2 → Tier 3 ===
        register("reed", "wheat", "cocoa", 4);
        register("cyazint", "wheat", "cocoa", 5);
        register("reed", "cyazint", "flax", 5);
        register("tulip", "cyazint", "hyacinth", 5);
        register("tulip", "rose", "hyacinth", 4);

        // === Tier 2/3 → Tier 4 ===
        register("reed", "reed", "stickreed", 5);
        register("reed", "cocoa", "stickreed", 6);
        register("cocoa", "rose", "blackthorn", 4);
        register("hyacinth", "cocoa", "blackthorn", 4);
        register("hyacinth", "blackthorn", "wonderflower", 3);
        register("hyacinth", "dandelion", "wonderflower", 3);

        // === Tier 3/4 → Tier 5 ===
        register("stickreed", "stickreed", "ferru", 3);
        register("cocoa", "cocoa", "shining", 3);
        register("stickreed", "wheat", "nether_wart_crop", 2);
        register("flax", "reed", "sweet_flower", 4);
        register("flax", "cocoa", "sweet_flower", 3);
        register("wonderflower", "stickreed", "slimeplant", 3);
        register("wonderflower", "shining", "slimeplant", 3);

        // === Tier 4/5 → Tier 6 ===
        register("ferru", "wheat", "redwheat", 3);
        register("ferru", "reed", "redwheat", 3);
        register("nether_wart_crop", "cocoa", "brown_mushroom", 3);
        register("nether_wart_crop", "blackthorn", "red_mushroom", 3);
        register("shining", "nether_wart_crop", "brown_mushroom", 3);

        // === Tier 5/6 → Tier 7 ===
        register("ferru", "ferru", "aurelia", 2);
        register("ferru", "redwheat", "aurelia", 2);

        // === Tier 7 → Tier 8 ===
        register("aurelia", "aurelia", "dahlia", 1);
        register("aurelia", "redwheat", "dahlia", 1);
        register("aurelia", "shining", "dahlia", 1);

        // === Tier 8 → Tier 9 ===
        register("dahlia", "aurelia", "green_diamond", 1);
        register("dahlia", "dahlia", "green_diamond", 1);

        // === 追加作物杂交路线 ===
        // 仙人掌(T2): 甘蔗系
        register("reed", "melon", "cactus", 5);
        register("reed", "pumpkin", "cactus", 4);

        // 皮革叶(T3): 亚麻+可可
        register("flax", "blackthorn", "leatherleaf", 4);
        register("flax", "cocoa", "leatherleaf", 3);

        // 碳化疣(T5): 地狱疣+黑荆棘(墨色+下界)
        register("nether_wart_crop", "blackthorn", "carbon_wart", 3);
        register("nether_wart_crop", "stickreed", "carbon_wart", 2);

        // 烈焰花(T6): 地狱疣+闪光草(下界+光)
        register("nether_wart_crop", "shining", "blazeflower", 2);
        register("nether_wart_crop", "ferru", "blazeflower", 2);

        // 末影花(T6): 奇妙花+闪光草(神秘+光)
        register("wonderflower", "shining", "enderflower", 2);
        register("wonderflower", "nether_wart_crop", "enderflower", 2);

        // 爆炸草(T6): 碳化疣+铁叶草(火药=硫磺+金属感)
        register("carbon_wart", "ferru", "explosivegrass", 2);
        register("carbon_wart", "redwheat", "explosivegrass", 2);

        // 哭泣花(T7): 烈焰花+末影花(下界高级)
        register("blazeflower", "enderflower", "tearflower", 1);
        register("blazeflower", "aurelia", "tearflower", 1);

        // 晶化叶(T7): 金叶草+铁叶草(金属结晶)
        register("aurelia", "ferru", "prismaleaf", 1);
        register("aurelia", "redwheat", "prismaleaf", 1);

        // 星之疣(T10): 哭泣花+绿金刚(最终boss级)
        register("tearflower", "green_diamond", "starwart", 1);
        register("tearflower", "dahlia", "starwart", 1);
    }

    public static class CrossRecipe {
        public final String parent1, parent2, result;
        public final int weight;

        public CrossRecipe(String p1, String p2, String r, int w) {
            parent1 = p1; parent2 = p2; result = r; weight = w;
        }

        public boolean matches(String a, String b) {
            return (parent1.equals(a) && parent2.equals(b)) ||
                    (parent1.equals(b) && parent2.equals(a));
        }
    }
}
