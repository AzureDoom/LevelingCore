package com.azuredoom.levelingcore.compat.dynamictooltips;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import org.herolias.tooltips.api.DynamicTooltipsApiProvider;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.azuredoom.levelingcore.LevelingCore;

public class DynamicTooltipsLibCompat {

    private static boolean registered = false;

    private static final int MAX_CRAWL_DEPTH = 8;

    private static final Timer SCAN_TIMER = new Timer("levelingcore-dynamic-tooltips", true);

    private static final Pattern CRAWL_PATTERN = Pattern.compile(
        "(?:\"|'|\\b)(Physical|Magical|Void|True|Poison|Fire|Ice|Wind|Earth|Water|Lightning|Elemental)(?:\"|'|\\b)\\s*[:=]\\s*(\\d+)",
        Pattern.CASE_INSENSITIVE
    );

    private final Set<String> processedItems = ConcurrentHashMap.newKeySet();

    private final Map<Class<?>, Field> bufferFieldCache = new ConcurrentHashMap<>();

    private final Set<Class<?>> noBufferFieldCache = ConcurrentHashMap.newKeySet();

    private final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>();

    public static final DynamicTooltipsLibCompat INSTANCE = new DynamicTooltipsLibCompat();

    private DynamicTooltipsLibCompat() {}

    /**
     * Registers dynamic tooltip translations for items based on their required levels. This method ensures the
     * registration happens only once per runtime by checking the `registered` flag.
     */
    public static void register() {
        if (registered)
            return;
        registered = true;

        var api = DynamicTooltipsApiProvider.get();
        if (api == null)
            return;

        // Wait for 10 seconds before scanning to ensure all items are loaded, then add tooltips for required levels & weapon damages
        SCAN_TIMER.schedule(new TimerTask() {

            {
                Objects.requireNonNull(DynamicTooltipsLibCompat.INSTANCE);
            }

            public void run() {
                DynamicTooltipsLibCompat.INSTANCE.scanForWeapons();
                for (var entry : LevelingCore.itemLevelMapping.entrySet()) {
                    var itemId = entry.getKey();
                    var requiredLevel = entry.getValue();
                    api.addGlobalLine(
                        itemId,
                        "\n<color is=\"#b5a077\">Required Level: " + requiredLevel + " </color>"
                    );
                }
            }
        }, 10000L);
    }

    private void scanForWeapons() {
        try {
            var allItems = Item.getAssetMap().getAssetMap().values();

            for (Item item : allItems) {
                String itemId = item.getId();
                if (itemId == null || this.processedItems.contains(itemId)) {
                    continue;
                }

                if (!this.hasWeaponTag(item)) {
                    this.processedItems.add(itemId);
                    continue;
                }

                Map<String, List<Integer>> damagesByType = this.getDamagesByTypeFromBuffer(item);
                if (damagesByType.isEmpty() || this.processItem(item, damagesByType)) {
                    this.processedItems.add(itemId);
                }
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .withCause(e)
                .log("Dynamic tooltips weapon scan failed");
        }
    }

    private boolean processItem(Item item, Map<String, List<Integer>> damagesByType) {
        var api = DynamicTooltipsApiProvider.get();
        if (api == null)
            return false;

        try {
            if (!damagesByType.isEmpty()) {
                StringBuilder text = new StringBuilder();

                text.append(String.format("<color is=\"#b5a077\">Weapon Level: %d</color>\n", item.getItemLevel()));
                int i = 0;
                for (Map.Entry<String, List<Integer>> entry : damagesByType.entrySet()) {
                    List<Integer> values = entry.getValue();
                    if (values.isEmpty()) {
                        continue;
                    }

                    int min = values.get(0);
                    int max = values.get(values.size() - 1);
                    if (i++ > 0) {
                        text.append("\n");
                    }
                    text.append(String.format("<color is=\"#b5a077\">%s: %d - %d</color>", entry.getKey(), min, max));
                }
                api.addGlobalLine(item.getId(), text.toString());
            }
            return true;
        } catch (Exception e) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .withCause(e)
                .log("Failed to process tooltip for item " + item.getId());
            return false;
        }
    }

    private Map<String, List<Integer>> getDamagesByTypeFromBuffer(Item item) {
        Map<String, List<Integer>> damagesByType = new LinkedHashMap<>();
        StringBuilder bufferDump = new StringBuilder();

        try {
            this.crawlBufferText(item, bufferDump, 0);
            Matcher m = CRAWL_PATTERN.matcher(bufferDump.toString());

            while (m.find()) {
                try {
                    String damageType = this.normalizeDamageType(m.group(1));
                    int val = Integer.parseInt(m.group(2));
                    if (val > 0) {
                        damagesByType.computeIfAbsent(damageType, key -> new ArrayList<>()).add(val);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .withCause(e)
                .log("Failed to extract weapon damage from item " + item.getId());
        }

        for (List<Integer> values : damagesByType.values()) {
            values.sort(Integer::compareTo);
        }

        return damagesByType;
    }

    private String normalizeDamageType(String rawType) {
        if (rawType == null || rawType.isEmpty()) {
            return "Unknown";
        }

        return rawType.substring(0, 1).toUpperCase(Locale.ROOT) + rawType.substring(1).toLowerCase(Locale.ROOT);
    }

    private void crawlBufferText(Object obj, StringBuilder sb, int depth) {
        if (obj != null && depth <= MAX_CRAWL_DEPTH) {
            try {
                if (isSimpleValue(obj.getClass())) {
                    return;
                }

                Field bufField = this.getField(obj.getClass(), "buffer");
                if (bufField != null) {
                    Object buf = bufField.get(obj);
                    if (buf != null) {
                        if (buf instanceof byte[]) {
                            sb.append(new String((byte[]) buf, StandardCharsets.UTF_8)).append(" ");
                        } else if (buf instanceof char[]) {
                            sb.append(new String((char[]) buf)).append(" ");
                        }
                    }
                }

                if (obj instanceof Map) {
                    for (Object val : ((Map<?, ?>) obj).values()) {
                        this.crawlBufferText(val, sb, depth + 1);
                    }
                } else if (obj instanceof Iterable) {
                    for (Object val : (Iterable<?>) obj) {
                        this.crawlBufferText(val, sb, depth + 1);
                    }
                } else if (obj.getClass().isArray()) {
                    int len = Array.getLength(obj);

                    for (int i = 0; i < len; ++i) {
                        this.crawlBufferText(Array.get(obj, i), sb, depth + 1);
                    }
                } else if (this.isComplex(obj.getClass())) {
                    for (Field f : getDeclaredFields(obj.getClass())) {
                        if (!Modifier.isStatic(f.getModifiers())) {
                            this.crawlBufferText(f.get(obj), sb, depth + 1);
                        }
                    }
                }
            } catch (Exception e) {
                LevelingCore.LOGGER.at(Level.WARNING)
                    .withCause(e)
                    .log("Failed reflection crawl while extracting item damage text");
            }
        }
    }

    private boolean isSimpleValue(Class<?> c) {
        return c == String.class || Number.class.isAssignableFrom(c) || c == Boolean.class || c == Character.class
            || c == Class.class;
    }

    private boolean isComplex(Class<?> c) {
        return !c.isPrimitive() && !c.getName().startsWith("java.") && !c.isEnum();
    }

    private boolean hasWeaponTag(Item item) {
        if (item.getWeapon() != null) {
            return true;
        } else {
            String id = item.getId().toLowerCase();
            return id.contains("sword") || id.contains("axe") || id.contains("staff") || id.contains("bow") || id
                .contains("hammer") || id.contains("dagger");
        }
    }

    private Field getField(Class<?> clazz, String name) {
        Field cachedField = bufferFieldCache.get(clazz);
        if (cachedField != null) {
            return cachedField;
        }
        if (noBufferFieldCache.contains(clazz)) {
            return null;
        }

        Field foundField = findField(clazz, name);
        if (foundField != null) {
            bufferFieldCache.put(clazz, foundField);
            return foundField;
        }

        noBufferFieldCache.add(clazz);
        return null;
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private Field[] getDeclaredFields(Class<?> clazz) {
        return declaredFieldsCache.computeIfAbsent(clazz, c -> {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            return fields;
        });
    }

}
