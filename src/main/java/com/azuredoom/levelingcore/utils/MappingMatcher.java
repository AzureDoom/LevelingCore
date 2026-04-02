package com.azuredoom.levelingcore.utils;

public final class MappingMatcher {

    private MappingMatcher() {}

    /**
     * Matches a given string value against a pattern that may include wildcards. The method supports the use of '*' as
     * a wildcard indicating any sequence of characters.
     *
     * @param pattern the pattern to match against, which may contain a wildcard '*' at the end or may be set to
     *                "default" to indicate universal matching.
     * @param value   the string value to be checked against the provided pattern.
     * @return true if the value matches the pattern, including wildcard and "default" cases; false otherwise.
     */
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

    /**
     * Calculates the specificity of the given pattern by determining the number of characters in the pattern excluding
     * wildcard characters ('*').
     *
     * @param pattern the input string pattern that may include wildcard characters ('*').
     * @return the specificity value, which is the length of the pattern after removing all '*' characters.
     */
    public static int specificity(String pattern) {
        return pattern.replace("*", "").length();
    }
}
