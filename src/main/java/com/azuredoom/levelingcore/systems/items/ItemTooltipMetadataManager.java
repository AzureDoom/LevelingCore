package com.azuredoom.levelingcore.systems.items;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.ItemDisplayMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.itemlevellock.ItemStatRequirement;

public class ItemTooltipMetadataManager {

    private volatile boolean ready = false;

    private static boolean registered = false;

    private static final Timer SCAN_TIMER = new Timer("levelingcore-item-display", true);

    private static final Pattern CRAWL_PATTERN = Pattern.compile(
        "(?:\"|'|\\b)(Physical|Magical|Void|True|Poison|Fire|Ice|Wind|Earth|Water|Lightning|Elemental)(?:\"|'|\\b)\\s*[:=]\\s*(\\d+)",
        Pattern.CASE_INSENSITIVE
    );

    public static final ItemTooltipMetadataManager INSTANCE = new ItemTooltipMetadataManager();

    private final Set<String> processedItems = ConcurrentHashMap.newKeySet();

    private final Map<Class<?>, Field> bufferFieldCache = new ConcurrentHashMap<>();

    private final Set<Class<?>> noBufferFieldCache = ConcurrentHashMap.newKeySet();

    private final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>();

    private final Map<String, String> displayDescriptionAdditions = new ConcurrentHashMap<>();

    private ItemTooltipMetadataManager() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        SCAN_TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                ItemTooltipMetadataManager.INSTANCE.applyAll();
            }
        }, 10000L);
    }

    public boolean isReady() {
        return ready;
    }

    private void applyAll() {
        try {
            if (LevelingCore.getConfig().get().isEnableItemLevelRestriction()) {
                for (var entry : LevelingCore.itemLevelMapping.entrySet()) {
                    appendDescriptionText(
                        entry.getKey(),
                        "\nRequired Level: " + entry.getValue()
                    );
                }
            }

            if (LevelingCore.getConfig().get().isEnableItemStatRequirement()) {
                for (var entry : LevelingCore.itemStatRequirements.entrySet()) {
                    appendDescriptionText(entry.getKey(), buildStatTooltip(entry.getValue()));
                }
            }

            scanForWeapons();
            ready = true;
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("Failed to apply LevelingCore item display metadata");
        }
    }

    public void addTooltipLine(String itemId, String line) {
        if (itemId == null || itemId.isBlank() || line == null || line.isBlank()) {
            return;
        }

        appendDescriptionText(itemId, "\n" + line.strip());
    }

    private void scanForWeapons() {
        try {
            for (var item : Item.getAssetMap().getAssetMap().values()) {
                var itemId = item.getId();

                if (itemId == null || processedItems.contains(itemId)) {
                    continue;
                }

                var damagesByType = this.getDamagesByTypeFromBuffer(item);

                if (!damagesByType.isEmpty()) {
                    var tooltip = buildWeaponTooltip(item, damagesByType);
                    if (tooltip != null && !tooltip.isBlank()) {
                        appendDescriptionText(itemId, tooltip);
                    }
                }

                this.processedItems.add(itemId);
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("LevelingCore weapon display scan failed");
        }
    }

    public ItemStack applyDisplayMetadata(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.getItem().getId() == null) {
            return stack;
        }

        var item = stack.getItem();
        var addition = displayDescriptionAdditions.get(item.getId());

        if (addition == null || addition.isBlank()) {
            return stack;
        }

        var displayMeta = new ItemDisplayMetadata(
            null,
            Message.raw(addition.stripLeading()).color("#b5a077")
        );

        return stack.withMetadata(ItemDisplayMetadata.KEYED_CODEC, displayMeta);
    }

    public String getAddition(String itemId) {
        return displayDescriptionAdditions.get(itemId);
    }

    private void appendDescriptionText(String itemId, String textToAppend) {
        if (itemId == null || textToAppend == null || textToAppend.isBlank()) {
            return;
        }

        displayDescriptionAdditions.merge(itemId, textToAppend, (oldValue, newValue) -> {
            if (oldValue.contains(newValue.trim())) {
                return oldValue;
            }
            return oldValue + newValue;
        });
    }

    private Field findField(Class<?> clazz) {
        while (clazz != null) {
            try {
                var f = clazz.getDeclaredField("buffer");
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private static String buildStatTooltip(ItemStatRequirement req) {
        var sb = new StringBuilder();

        sb.append("\nRequired Stats:");

        appendStat(sb, "STR", req.str());
        appendStat(sb, "AGI", req.agi());
        appendStat(sb, "PER", req.per());
        appendStat(sb, "VIT", req.vit());
        appendStat(sb, "INT", req.intelligence());
        appendStat(sb, "CON", req.con());

        return sb.toString();
    }

    private static void appendStat(StringBuilder sb, String name, int value) {
        if (value <= 0) {
            return;
        }

        sb.append("\n")
            .append(name)
            .append(": ")
            .append(value);
    }

    private Map<String, List<Integer>> getDamagesByTypeFromBuffer(Item item) {
        Map<String, List<Integer>> damagesByType = new LinkedHashMap<>();
        var bufferDump = new StringBuilder();

        try {
            this.crawlBufferText(item, bufferDump, 0);
            var m = CRAWL_PATTERN.matcher(bufferDump.toString());

            while (m.find()) {
                try {
                    var damageType = this.normalizeDamageType(m.group(1));
                    var val = Integer.parseInt(m.group(2));
                    if (val > 0) {
                        damagesByType.computeIfAbsent(damageType, _ -> new ArrayList<>()).add(val);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("Failed to extract weapon damage from item " + item.getId());
        }

        for (var values : damagesByType.values()) {
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
        if (obj == null || depth > 8) {
            return;
        }

        try {
            if (isSimpleValue(obj.getClass())) {
                return;
            }

            var bufField = this.getField(obj.getClass());
            if (bufField != null) {
                var buf = bufField.get(obj);
                if (buf instanceof byte[]) {
                    sb.append(new String((byte[]) buf, StandardCharsets.UTF_8)).append(" ");
                } else if (buf instanceof char[]) {
                    sb.append(new String((char[]) buf)).append(" ");
                }
            }

            if (obj instanceof Map) {
                for (var val : ((Map<?, ?>) obj).values()) {
                    this.crawlBufferText(val, sb, depth + 1);
                }
            } else if (obj instanceof Iterable) {
                for (var val : (Iterable<?>) obj) {
                    this.crawlBufferText(val, sb, depth + 1);
                }
            } else if (obj.getClass().isArray()) {
                int len = Array.getLength(obj);
                for (var i = 0; i < len; i++) {
                    this.crawlBufferText(Array.get(obj, i), sb, depth + 1);
                }
            } else if (this.isComplex(obj.getClass())) {
                for (var f : getDeclaredFields(obj.getClass())) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        this.crawlBufferText(f.get(obj), sb, depth + 1);
                    }
                }
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("Failed reflection crawl while extracting item damage text");
        }
    }

    private boolean isSimpleValue(Class<?> c) {
        return c == String.class || Number.class.isAssignableFrom(c) || c == Boolean.class || c == Character.class
            || c == Class.class;
    }

    private boolean isComplex(Class<?> c) {
        return !c.isPrimitive() && !c.getName().startsWith("java.") && !c.isEnum();
    }

    private Field getField(Class<?> clazz) {
        var cachedField = bufferFieldCache.get(clazz);
        if (cachedField != null) {
            return cachedField;
        }
        if (noBufferFieldCache.contains(clazz)) {
            return null;
        }

        var foundField = findField(clazz);
        if (foundField != null) {
            bufferFieldCache.put(clazz, foundField);
            return foundField;
        }

        noBufferFieldCache.add(clazz);
        return null;
    }

    private Field[] getDeclaredFields(Class<?> clazz) {
        return declaredFieldsCache.computeIfAbsent(clazz, c -> {
            var fields = c.getDeclaredFields();
            for (var field : fields) {
                field.setAccessible(true);
            }
            return fields;
        });
    }

    private static String buildWeaponTooltip(Item item, Map<String, List<Integer>> damagesByType) {
        if (item == null || damagesByType == null || damagesByType.isEmpty()) {
            return null;
        }

        var text = new StringBuilder();

        text.append("\nWeapon Level: ")
            .append(item.getItemLevel());

        for (var entry : damagesByType.entrySet()) {
            var damageType = entry.getKey();
            var values = entry.getValue();

            if (damageType == null || values == null || values.isEmpty()) {
                continue;
            }

            var min = values.getFirst();
            var max = values.getLast();

            text.append("\n")
                .append(damageType)
                .append(": ")
                .append(min)
                .append(" - ")
                .append(max);
        }

        return text.toString();
    }
}
