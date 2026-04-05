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
     * 核心杂交计算
     */
    public static CrossResult tryCrossBreed(List<String> parentCrops, List<CropStats> parentStats, Random rand) {
        if (parentCrops.size() < 2) return null;

        Map<String, Integer> candidateWeights = new HashMap<>();

        for (int i = 0; i < parentCrops.size(); i++) {
            for (int j = i + 1; j < parentCrops.size(); j++) {
                String a = parentCrops.get(i);
                String b = parentCrops.get(j);

                for (CrossRecipe recipe : RECIPES) {
                    if (recipe.matches(a, b)) {
                        candidateWeights.merge(recipe.result, recipe.weight, Integer::sum);
                    }
                }
                // 同种杂交 -> 属性提升
                if (a.equals(b)) {
                    candidateWeights.merge(a, 10, Integer::sum);
                }
            }
        }

        if (candidateWeights.isEmpty()) {
            if (rand.nextInt(100) < 15) {
                CropStats weedStats = new CropStats(
                    rand.nextInt(10) + 5, rand.nextInt(5) + 1, rand.nextInt(5) + 1);
                return new CrossResult("weed", weedStats);
            }
            return null;
        }

        // 成功率计算
        int baseChance = 30 + (parentCrops.size() - 2) * 10;
        int avgGrowth = parentStats.stream().mapToInt(CropStats::getGrowth).sum() / parentStats.size();
        baseChance += avgGrowth / 4;

        if (rand.nextInt(100) >= Math.min(baseChance, 90)) {
            if (rand.nextInt(100) < 10) {
                return new CrossResult("weed", new CropStats(rand.nextInt(8) + 3, 1, 1));
            }
            return null;
        }

        // 权重选择
        String result = selectByWeight(candidateWeights, rand);
        if (result == null) return null;

        // Tier检查
        CropType resultType = CropRegistry.get(result);
        if (resultType != null) {
            int resultTier = resultType.getTier();
            int maxParentTier = parentCrops.stream()
                .map(CropRegistry::get)
                .filter(Objects::nonNull)
                .mapToInt(CropType::getTier)
                .max().orElse(1);
            if (resultTier > maxParentTier + 1) {
                return null;
            }
        }

        CropStats childStats = CropStats.inheritMultiple(
            parentStats.toArray(new CropStats[0]), rand);
        return new CrossResult(result, childStats);
    }

    private static String selectByWeight(Map<String, Integer> weights, Random rand) {
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) return null;
        int roll = rand.nextInt(total);
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) return entry.getKey();
        }
        return null;
    }

    public static void registerDefaults() {
        // Tier 1 -> Tier 2
        register("wheat", "wheat", "wheat", 10);
        register("wheat", "pumpkin", "reed", 8);
        register("wheat", "melon", "cyazint", 5);
        register("potato", "carrot", "reed", 6);
        // Tier 2 -> Tier 3
        register("reed", "reed", "stickreed", 5);
        register("reed", "wheat", "cocoa", 4);
        register("cyazint", "wheat", "cocoa", 5);
        // Tier 3 -> Tier 4
        register("reed", "cocoa", "stickreed", 6);
        register("cocoa", "cocoa", "shining", 3);
        // Tier 4 -> Tier 5
        register("stickreed", "stickreed", "ferru", 3);
        register("stickreed", "wheat", "nether_wart_crop", 2);
        register("shining", "cocoa", "ferru", 3);
        // Tier 5 -> Tier 6
        register("ferru", "wheat", "redwheat", 3);
        register("ferru", "reed", "redwheat", 3);
        // Tier 6 -> Tier 7
        register("ferru", "ferru", "aurelia", 2);
        register("ferru", "redwheat", "aurelia", 2);
    }

    public static class CrossRecipe {
        public final String parent1, parent2, result;
        public final int weight;

        public CrossRecipe(String parent1, String parent2, String result, int weight) {
            this.parent1 = parent1;
            this.parent2 = parent2;
            this.result = result;
            this.weight = weight;
        }

        public boolean matches(String a, String b) {
            return (parent1.equals(a) && parent2.equals(b)) ||
                   (parent1.equals(b) && parent2.equals(a));
        }
    }

    public static class CrossResult {
        public final String cropId;
        public final CropStats stats;

        public CrossResult(String cropId, CropStats stats) {
            this.cropId = cropId;
            this.stats = stats;
        }
    }
}
