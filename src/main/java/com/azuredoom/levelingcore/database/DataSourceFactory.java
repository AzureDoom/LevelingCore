package com.azuredoom.levelingcore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.azuredoom.levelingcore.exceptions.DataSourceConfigurationException;

/**
 * A factory class for creating and configuring database connection pools using HikariCP.
 * <p>
 * This class provides a utility method to construct a {@link HikariDataSource} instance which can be used for managing
 * database connections in a high-performance and efficient manner. It is designed for ease of use and supports
 * configuration of core connection parameters such as JDBC URL, username, and password.
 * <p>
 * This class is not meant to be instantiated and is designed to be used statically.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {}

    /**
     * Creates and returns a configured HikariCP connection pool (HikariDataSource) based on the provided inputs.
     * Ensures that the connection pool is initialized and validates connectivity with the database.
     *
     * @param jdbcUrl     The JDBC URL. May include embedded credentials (example: jdbc:mysql://user:pass@host:port/db).
     * @param username    Optional database username. If provided, overrides credentials in jdbcUrl.
     * @param password    Optional database password. Used only if a username is provided.
     * @param maxPoolSize The maximum size of the connection pool. Must be greater than or equal to 1.
     * @return A fully initialized HikariDataSource configured with the specified parameters.
     * @throws IllegalArgumentException         If any of the parameters do not meet the specified conditions.
     * @throws DataSourceConfigurationException If the connection pool initialization or validation fails.
     */
    public static HikariDataSource create(
        String jdbcUrl,
        String username,
        String password,
        int maxPoolSize
    ) {
        validateBasicConfig(jdbcUrl, username, maxPoolSize);

        var cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        if (username != null && !username.isBlank()) {
            cfg.setUsername(username);
            cfg.setPassword(password);
        }

        cfg.setDriverClassName(driverClassNameFor(jdbcUrl));

        cfg.setMaximumPoolSize(maxPoolSize);
        cfg.setMinimumIdle(1);

        cfg.setInitializationFailTimeout(10_000);
        cfg.setConnectionTimeout(10_000);
        cfg.setValidationTimeout(5_000);

        HikariDataSource ds;
        try {
            ds = new HikariDataSource(cfg);
        } catch (RuntimeException e) {
            throw DataSourceConfigurationException.from("Failed to initialize connection pool", jdbcUrl, username, e);
        }

        try (var c = ds.getConnection()) {
            c.isValid(2);
        } catch (Exception e) {
            try {
                ds.close();
            } catch (Exception ignored) {}
            throw DataSourceConfigurationException.from("Database connection test failed", jdbcUrl, username, e);
        }

        return ds;
    }

    /**
     * Checks if the provided JDBC URL contains embedded credentials in the format "username:password". Specifically,
     * this method evaluates if the JDBC URL matches the pattern commonly used for PostgreSQL, where the credentials are
     * embedded before the host part of the URL.
     *
     * @param jdbcUrl The JDBC URL to evaluate. It may be null. If specified, it is expected to use the PostgreSQL
     *                format and may include embedded credentials.
     * @return true if the JDBC URL contains embedded credentials in the format "username:password" before the host.
     *         false otherwise.
     */
    private static boolean hasUserInfoAuthority(String jdbcUrl) {
        return jdbcUrl != null
            && jdbcUrl.matches("^jdbc:postgresql://[^/@:]+:[^/@]+@.+");
    }

    /**
     * Checks if the given JDBC URL contains embedded credentials. Embedded credentials are identified either by the
     * format "username:password" in the connection URL (e.g., "jdbc:mysql://user:pass@host:port/db") or by the presence
     * of query parameters such as "user" or "username".
     *
     * @param jdbcUrl The JDBC URL to check for embedded credentials. It may be null.
     * @return {@code true} if the JDBC URL contains embedded credentials, either in the main URL or as query
     *         parameters; {@code false} otherwise.
     */
    private static boolean hasCredentialsInJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null)
            return false;

        var lower = jdbcUrl.toLowerCase();

        if (lower.matches("^jdbc:[^:]+://[^/@:]+:[^/@]+@.+")) {
            return true;
        }

        var q = lower.indexOf('?');
        if (q < 0)
            return false;

        var query = lower.substring(q + 1);

        return containsQueryParam(query, "user")
            || containsQueryParam(query, "username")
            || containsQueryParam(query, "password");
    }

    /**
     * Checks if the specified query string contains a query parameter with the given key. A query parameter is
     * identified by the format "key=value", with the key either appearing at the start of the query string or following
     * an ampersand (&).
     *
     * @param query The query string to search. It may be null or empty.
     * @param key   The key of the query parameter to look for. It must be non-null and non-empty.
     * @return true if the query string contains a parameter with the specified key, false otherwise.
     */
    private static boolean containsQueryParam(String query, String key) {
        return query.startsWith(key + "=") || query.contains("&" + key + "=");
    }

    /**
     * Checks if the provided JDBC URL corresponds to an H2 database.
     *
     * @param jdbcUrl The JDBC URL used for the database connection. It may be null. If specified, it is expected to
     *                start with the prefix "jdbc:h2:".
     * @return true if the provided JDBC URL is non-null and starts with "jdbc:h2:". false otherwise.
     */
    private static boolean isH2(String jdbcUrl) {
        return jdbcUrl != null && jdbcUrl.toLowerCase().startsWith("jdbc:h2:");
    }

    /**
     * Validates the basic configuration parameters required to establish a database connection. Ensures that the
     * provided JDBC URL, username, and maximum pool size adhere to the expected constraints. Throws an
     * IllegalArgumentException if any validation fails.
     *
     * @param jdbcUrl     The JDBC URL used to establish the database connection. Must be non-null, non-blank, and start
     *                    with "jdbc:". Example: "jdbc:postgresql://host:5432/db".
     * @param username    Optional database username. If provided, it must be non-blank. If not provided, ensures
     *                    credentials are embedded in the JDBC URL or validates compatibility with H2.
     * @param maxPoolSize The maximum size of the connection pool. Must be a positive integer (>= 1).
     * @throws IllegalArgumentException If any parameter does not meet the specified validation criteria.
     */
    private static void validateBasicConfig(String jdbcUrl, String username, int maxPoolSize) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException(
                "jdbcUrl is missing/blank (example: jdbc:postgresql://host:5432/db)"
            );
        }

        if (!jdbcUrl.toLowerCase().startsWith("jdbc:")) {
            throw new IllegalArgumentException(
                "jdbcUrl must start with 'jdbc:' (got: " + jdbcUrl + ")"
            );
        }

        if (
            jdbcUrl.toLowerCase().startsWith("jdbc:postgresql:")
                && hasUserInfoAuthority(jdbcUrl)
        ) {

            throw new IllegalArgumentException(
                """
                    PostgreSQL JDBC URLs must not use 'user:password@host' syntax. \
                    Use either:
                      - jdbc:postgresql://host:port/db with dedicated username/password, or
                      - jdbc:postgresql://host:port/db?user=...&password=..."""
            );
        }

        var hasUserArg = username != null && !username.isBlank();
        var hasUrlCreds = hasCredentialsInJdbcUrl(jdbcUrl);

        if (!isH2(jdbcUrl) && !hasUserArg && !hasUrlCreds) {
            throw new IllegalArgumentException(
                "database credentials are missing (provide username/password or embed them in jdbcUrl)"
            );
        }

        if (maxPoolSize < 1) {
            throw new IllegalArgumentException(
                "maxPoolSize must be >= 1 (got: " + maxPoolSize + ")"
            );
        }
    }

    /**
     * Determines the appropriate driver class name based on the provided JDBC URL.
     *
     * @param jdbcUrl The JDBC URL used to establish a database connection. Supports schemes: mysql, mariadb,
     *                postgresql, h2.
     * @return The fully qualified name of the JDBC driver class corresponding to the specified JDBC URL.
     * @throws IllegalArgumentException If the JDBC URL scheme is not supported.
     */
    private static String driverClassNameFor(String jdbcUrl) {
        String url = jdbcUrl.toLowerCase();

        if (url.startsWith("jdbc:mysql:")) {
            if (classExists("com.azuredoom.levelingcore.libs.mysql.cj.jdbc.Driver"))
                return "com.azuredoom.levelingcore.libs.mysql.cj.jdbc.Driver";
            return "com.mysql.cj.jdbc.Driver";
        }
        if (url.startsWith("jdbc:mariadb:")) {
            if (classExists("com.azuredoom.levelingcore.libs.mariadb.jdbc.Driver"))
                return "com.azuredoom.levelingcore.libs.mariadb.jdbc.Driver";
            return "org.mariadb.jdbc.Driver";
        }
        if (url.startsWith("jdbc:postgresql:")) {
            if (classExists("com.azuredoom.levelingcore.libs.postgresql.Driver"))
                return "com.azuredoom.levelingcore.libs.postgresql.Driver";
            return "org.postgresql.Driver";
        }
        if (url.startsWith("jdbc:h2:")) {
            if (classExists("com.azuredoom.levelingcore.libs.h2.Driver"))
                return "com.azuredoom.levelingcore.libs.h2.Driver";
            return "org.h2.Driver";
        }
        throw new IllegalArgumentException(
            "Unsupported jdbcUrl scheme. Supported: mysql, mariadb, postgresql, h2. Got: " + jdbcUrl
        );
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, DataSourceFactory.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
