package com.drppp.drtech.drone.program.registry;

import com.drppp.drtech.drone.program.model.DroneNodeDefinition;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry-backed category pages used by the visual programmer node library. */
public final class DroneNodeLibraryIndex {

    private final List<ResourceLocation> allNodes;
    private final List<Page> pages;

    public DroneNodeLibraryIndex(DroneNodeRegistry registry, int pageSize) {
        if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be positive");
        Map<String, List<ResourceLocation>> categories = new LinkedHashMap<>();
        List<ResourceLocation> registered = new ArrayList<>();
        for (DroneNodeDefinition definition : registry.values()) {
            registered.add(definition.getId());
            categories.computeIfAbsent(definition.getCategory(), ignored -> new ArrayList<>())
                    .add(definition.getId());
        }

        List<Page> builtPages = new ArrayList<>();
        for (Map.Entry<String, List<ResourceLocation>> category : categories.entrySet()) {
            List<ResourceLocation> nodes = category.getValue();
            int categoryPageCount = Math.max(1, (nodes.size() + pageSize - 1) / pageSize);
            for (int page = 0; page < categoryPageCount; page++) {
                int from = page * pageSize;
                int to = Math.min(nodes.size(), from + pageSize);
                builtPages.add(new Page(category.getKey(), page, categoryPageCount,
                        nodes.subList(from, to)));
            }
        }
        this.allNodes = Collections.unmodifiableList(registered);
        this.pages = Collections.unmodifiableList(builtPages);
    }

    public List<ResourceLocation> getAllNodes() {
        return allNodes;
    }

    public List<Page> getPages() {
        return pages;
    }

    public static final class Page {
        private final String category;
        private final int categoryPage;
        private final int categoryPageCount;
        private final List<ResourceLocation> nodes;

        private Page(String category, int categoryPage, int categoryPageCount, List<ResourceLocation> nodes) {
            this.category = category;
            this.categoryPage = categoryPage;
            this.categoryPageCount = categoryPageCount;
            this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        }

        public String getCategory() {
            return category;
        }

        public int getCategoryPage() {
            return categoryPage;
        }

        public int getCategoryPageCount() {
            return categoryPageCount;
        }

        public List<ResourceLocation> getNodes() {
            return nodes;
        }
    }
}
