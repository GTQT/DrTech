package com.drppp.drtech.Client.drone;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Client-only bilingual node index. Both bundled languages remain searchable regardless of the active locale. */
public final class DroneBilingualNodeSearch {
    private static volatile Map<String, String> english;
    private static volatile Map<String, String> chinese;

    private DroneBilingualNodeSearch() { }

    public static boolean matches(ResourceLocation nodeType, DroneNodeDefinition definition, String rawQuery) {
        if (nodeType == null) return false;
        String query = normalize(rawQuery);
        if (query.isEmpty()) return true;
        String category = definition == null ? "" : definition.getCategory();
        String nodeKey = definition == null ? "drtech.drone.node." + nodeType.getPath()
                : definition.getTranslationKey();
        String categoryKey = "drtech.drone.programmer.node_category." + category;
        String path = nodeType.getPath().toLowerCase(Locale.ROOT);
        return contains(path, query)
                || contains(path.replace('_', ' '), query)
                || contains(nodeType.toString(), query)
                || contains(category, query)
                || contains(I18n.format(nodeKey), query)
                || contains(I18n.format(categoryKey), query)
                || contains(language("en_us").get(nodeKey), query)
                || contains(language("zh_cn").get(nodeKey), query)
                || contains(language("en_us").get(categoryKey), query)
                || contains(language("zh_cn").get(categoryKey), query);
    }

    private static Map<String, String> language(String code) {
        Map<String, String> current = "en_us".equals(code) ? english : chinese;
        if (current != null) return current;
        synchronized (DroneBilingualNodeSearch.class) {
            current = "en_us".equals(code) ? english : chinese;
            if (current == null) {
                current = load(code);
                if ("en_us".equals(code)) english = current;
                else chinese = current;
            }
        }
        return current;
    }

    private static Map<String, String> load(String code) {
        Map<String, String> values = new HashMap<>();
        ResourceLocation location = new ResourceLocation("drtech", "lang/" + code + ".lang");
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.charAt(0) == '#') continue;
                    int separator = line.indexOf('=');
                    if (separator <= 0) continue;
                    values.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
                }
            }
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(values);
    }

    private static boolean contains(String value, String query) {
        return value != null && normalize(value).contains(query);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
