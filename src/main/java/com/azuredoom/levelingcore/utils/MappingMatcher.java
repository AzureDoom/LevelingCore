package com.azuredoom.levelingcore.utils;

public final class MappingMatcher {

    private MappingMatcher() {}

    public static boolean wildcardMatch(String pattern, String value) {
        if (pattern.equals("*") || pattern.equals("default")) {
            return true;
        }

        if (pattern.endsWith("*")) {
            var prefix = pattern.substring(0, pattern.length() - 1);
            return value.startsWith(prefix);
        }

        return value.equals(pattern);
    }

    public static int specificity(String pattern) {
        return pattern.replace("*", "").length();
    }
}
