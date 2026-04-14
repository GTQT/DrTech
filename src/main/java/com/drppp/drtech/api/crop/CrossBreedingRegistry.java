package com.drppp.drtech.api.crop;

import java.util.*;

/**
 * 杂交配方注册表
 */

public class CrossBreedingRegistry {

    private static final List<CrossRecipe> RECIPES = new ArrayList<>();

    public static void register(String parent1, String parent2, String result, int weight) {
        RECIPES.add(new CrossRecipe(parent1, parent2, result, weight));
    }

    public static List<CrossRecipe> getAllRecipes() {
        return Collections.unmodifiableList(RECIPES);
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
        // 杂交路线图
        // 新机制: 所有参与杂交的作物的产物池相加, 同种产物权重相加
        //         某作物杂交出自身的权重固定500
        //         移除了同种×同种=同种的无用配方(自身500权重已覆盖)
        // ======================================================================

        // === Tier 1 → Tier 2 ===
        register("wheat", "pumpkin", "reed", 80);
        register("potato", "carrot", "reed", 60);
        register("wheat", "melon", "cyazint", 50);
        register("dandelion", "rose", "tulip", 60);
        register("dandelion", "wheat", "cyazint", 40);
        register("reed", "melon", "cactus", 50);
        register("reed", "pumpkin", "cactus", 40);
        register("wheat", "dandelion", "xp_berry", 30);
        register("melon", "slimeplant", "gel_grass", 30);
        register("lotus_leaf", "reed", "gel_grass", 40);

        // === Tier 1/2 → Tier 3 ===
        register("reed", "wheat", "cocoa", 40);
        register("cyazint", "wheat", "cocoa", 50);
        register("reed", "cyazint", "flax", 50);
        register("tulip", "cyazint", "hyacinth", 50);
        register("tulip", "rose", "hyacinth", 40);
        register("flax", "blackthorn", "leatherleaf", 40);
        register("flax", "cocoa", "leatherleaf", 30);
        register("wheat", "melon", "milk_wart", 25);
        register("potato", "wheat", "egg_berry", 25);
        register("reed", "flax", "rubber_grass", 30);
        register("cyazint", "shining", "mercury_flower", 25);
        register("reed", "flax", "papyrus", 35);
        register("potato", "beetroot", "meat_berry", 25);
        register("lotus_leaf", "dandelion", "lotus_leaf", 20);

        // === Tier 2/3 → Tier 4 ===
        register("reed", "cocoa", "stickreed", 60);
        register("cocoa", "rose", "blackthorn", 40);
        register("hyacinth", "cocoa", "blackthorn", 40);
        register("hyacinth", "blackthorn", "wonderflower", 30);
        register("hyacinth", "dandelion", "wonderflower", 30);
        register("dandelion", "cyazint", "rapeseed", 35);
        register("tulip", "dandelion", "snapdragon", 35);
        register("cocoa", "wheat", "fertilizer_grass", 30);
        register("blackthorn", "nether_wart_crop", "corpse_wart", 25);
        register("flax", "blackthorn", "spider_bite", 30);
        register("reed", "papyrus", "fishing_rod_crop", 20);

        // 神秘莓
        register("wonderflower", "hyacinth", "mystical_order", 20);
        register("wonderflower", "blackthorn", "mystical_chaos", 20);
        register("cyazint", "wonderflower", "mystical_wind", 20);
        register("rose", "wonderflower", "mystical_fire", 20);
        register("hyacinth", "tulip", "mystical_water", 20);
        register("cocoa", "wonderflower", "mystical_earth", 20);

        // 16色神秘花(从对应神秘莓+花卉杂交)
        register("mystical_order", "dandelion", "mystic_flower_white", 15);
        register("mystical_chaos", "blackthorn", "mystic_flower_black", 15);
        register("mystical_fire", "rose", "mystic_flower_red", 15);
        register("mystical_earth", "cyazint", "mystic_flower_green", 15);
        register("mystical_earth", "cocoa", "mystic_flower_brown", 15);
        register("mystical_fire", "tulip", "mystic_flower_orange", 15);
        register("mystical_chaos", "wonderflower", "mystic_flower_magenta", 15);
        register("mystical_water", "cyazint", "mystic_flower_light_blue", 15);
        register("mystical_order", "rapeseed", "mystic_flower_yellow", 15);
        register("mystical_wind", "rapeseed", "mystic_flower_lime", 15);
        register("mystical_order", "rose", "mystic_flower_pink", 15);
        register("mystical_chaos", "cyazint", "mystic_flower_gray", 15);
        register("mystical_wind", "dandelion", "mystic_flower_silver", 15);
        register("mystical_water", "hyacinth", "mystic_flower_cyan", 15);
        register("mystical_water", "wonderflower", "mystic_flower_blue", 15);
        register("mystical_chaos", "wonderflower", "mystic_flower_purple", 15);

        // 石莲系列
        register("lotus_leaf", "redwheat", "redstone_lotus", 25);
        register("lotus_leaf", "shining", "white_lotus", 25);
        register("lotus_leaf", "ferru", "gray_lotus", 25);
        register("lotus_leaf", "carbon_wart", "black_lotus_small", 25);
        register("lotus_leaf", "sweet_flower", "yellow_lotus", 25);
        register("lotus_leaf", "nether_wart_crop", "nether_lotus", 25);

        // 活木/活石盆栽
        register("bonsai", "wonderflower", "livingwood_bonsai", 20);
        register("bonsai", "ferru", "livingrock_bonsai", 20);

        // === Tier 3/4 → Tier 5 ===
        register("stickreed", "stickreed", "ferru", 30);
        register("cocoa", "cocoa", "shining", 30);
        register("stickreed", "wheat", "nether_wart_crop", 20);
        register("flax", "reed", "sweet_flower", 40);
        register("wonderflower", "stickreed", "slimeplant", 30);
        register("nether_wart_crop", "blackthorn", "carbon_wart", 30);
        register("rapeseed", "blackthorn", "oil_berry", 20);
        register("corpse_wart", "nether_wart_crop", "corruption_wart", 20);
        register("ferru", "blackthorn", "lead_leaf", 20);
        register("cocoa", "cactus", "salt_root", 25);
        register("ferru", "shining", "mica_grass", 20);
        register("wonderflower", "xp_berry", "ascend_berry", 15);
        register("lotus_leaf", "cactus", "arid_lotus", 20);

        // === Tier 4/5 → Tier 6 ===
        register("ferru", "wheat", "redwheat", 30);
        register("nether_wart_crop", "cocoa", "brown_mushroom", 30);
        register("nether_wart_crop", "blackthorn", "red_mushroom", 30);
        register("nether_wart_crop", "shining", "blazeflower", 20);
        register("wonderflower", "shining", "enderflower", 20);
        register("carbon_wart", "ferru", "explosivegrass", 20);
        register("nether_wart_crop", "ferru", "demon_claw", 20);
        register("mica_grass", "shining", "whale_claw", 15);
        register("ferru", "copper_leaf", "tin_leaf", 20);
        register("ferru", "shining", "copper_leaf", 25);
        register("ferru", "mica_grass", "titanium_leaf", 15);

        // === Tier 5/6 → Tier 7 ===
        register("ferru", "ferru", "aurelia", 20);
        register("ferru", "redwheat", "aurelia", 20);
        register("blazeflower", "enderflower", "tearflower", 10);
        register("aurelia", "ferru", "prismaleaf", 10);
        register("aurelia", "copper_leaf", "silver_leaf", 10);
        register("ferru", "tin_leaf", "aluminum_leaf", 10);
        register("copper_leaf", "lead_leaf", "zinc_leaf", 10);
        register("ferru", "aurelia", "nickel_leaf", 10);

        // === Tier 7 → Tier 8 ===
        register("aurelia", "aurelia", "dahlia", 10);
        register("aurelia", "shining", "dahlia", 10);
        register("enderflower", "corruption_wart", "black_lotus", 8);
        register("aurelia", "explosivegrass", "thunder_grass", 8);
        register("nickel_leaf", "ferru", "manganese_leaf", 8);

        // === Tier 8 → Tier 9 ===
        register("dahlia", "aurelia", "green_diamond", 8);
        register("dahlia", "dahlia", "green_diamond", 8);
        register("wonderflower", "green_diamond", "lucky_clover", 5);
        register("black_lotus", "starwart", "wither_flower", 3);

        // === Tier 9 → Tier 10 ===
        register("tearflower", "green_diamond", "starwart", 5);
        register("thunder_grass", "dahlia", "nuclear_grass", 3);
        register("dahlia", "green_diamond", "bedrock_heart", 2);

        // === Tier 10 → Tier 11 ===
        register("starwart", "enderflower", "space_berry", 2);
        register("aurelia", "dahlia", "platinum_leaf", 2);
        register("manganese_leaf", "dahlia", "tungsten_leaf", 2);
        register("aurelia", "green_diamond", "money_grass", 2);
        register("nuclear_grass", "dahlia", "mech_brain", 1);
        // heartfruit: canBeBreedResult=false, 不会出现在杂交产物中
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
