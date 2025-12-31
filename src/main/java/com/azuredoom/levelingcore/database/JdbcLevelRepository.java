package com.azuredoom.levelingcore.database;

import com.azuredoom.levelingcore.LevelingCoreException;
import com.azuredoom.levelingcore.Main;
import com.azuredoom.levelingcore.config.FormulaDescriptor;
import com.azuredoom.levelingcore.config.LevelFormulaFactory;
import com.azuredoom.levelingcore.level.formulas.LevelFormula;
import com.azuredoom.levelingcore.playerdata.PlayerLevelData;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;

/**
 * Implementation of the {@link LevelRepository} interface using JDBC for data persistence. This repository manages the
 * storage and retrieval of player level data in a relational database. The implementation provides methods for saving,
 * loading, checking the existence of, and closing resources related to player level data.
 */
public class JdbcLevelRepository implements LevelRepository {

    private final DataSource dataSource;

    public JdbcLevelRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        createTableIfNotExists();
    }

    /**
     * Ensures the existence of the "player_levels" table in the database. If the table does not already exist, it
     * creates a new table with the necessary schema. The table consists of the following columns: - `player_id`: A
     * unique identifier for the player, represented as a VARCHAR(36), and used as the primary key. - `xp`: A BIGINT
     * column to store the player's experience points (XP), which cannot be null.
     * <p>
     * This method establishes a connection to the database using the provided DataSource and executes a SQL "CREATE
     * TABLE IF NOT EXISTS" statement to create the table if it does not already exist. If any exception occurs during
     * the operation, it wraps and rethrows the exception as a {@link LevelingCoreException} to provide a clear context
     * about the failure.
     * <p>
     * Implementation Notes: - Uses a try-with-resources block to ensure the proper closure of the database connection
     * and statement. - The method is private and intended to be used internally within the class during initialization
     * to guarantee the schema's availability.
     *
     * @throws LevelingCoreException if the table creation fails due to database connectivity issues or an error in the
     *                               SQL operation.
     */
    private void createTableIfNotExists() {
        var sql = """
            CREATE TABLE IF NOT EXISTS player_levels (
                player_id VARCHAR(36) PRIMARY KEY,
                xp BIGINT NOT NULL
            )
            """;

        try (
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement()
        ) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to create player_levels table", e);
        }
    }

    /**
     * Ensures the existence of the "levelingcore_meta" table in the database. If the table does not exist, it creates
     * it with the specified schema.
     * <p>
     * The "levelingcore_meta" table is defined with the following structure: - `meta_key`: A VARCHAR(64) that serves as
     * the primary key. - `meta_value`: A VARCHAR(255) that cannot be null.
     * <p>
     * This method uses the provided data source to establish a database connection and executes the SQL statement to
     * create the table. If the table already exists, the SQL statement has no effect.
     * <p>
     * If an error occurs during table creation, a {@link LevelingCoreException} is thrown.
     *
     * @throws LevelingCoreException if a database access error occurs or table creation fails.
     */
    private void createMetaTableIfNotExists() {
        var sql = """
            CREATE TABLE IF NOT EXISTS levelingcore_meta (
                meta_key VARCHAR(64) PRIMARY KEY,
                meta_value VARCHAR(255) NOT NULL
            )
            """;
        try (var c = dataSource.getConnection(); var s = c.createStatement()) {
            s.execute(sql);
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to create levelingcore_meta table", e);
        }
    }

    /**
     * Retrieves the metadata value associated with the given key from the "levelingcore_meta" table. If the specified
     * key does not exist in the database, the method returns {@code null}.
     * <p>
     * This method establishes a connection to the database, executes a SQL query to fetch the metadata value, and
     * ensures that all resources are properly closed after use. If any database-related issue occurs, it wraps and
     * rethrows the exception as a {@link LevelingCoreException}.
     *
     * @param key The metadata key as a {@link String}. It identifies the entry to retrieve from the database.
     * @return The metadata value as a {@link String} associated with the specified key, or {@code null} if the key does
     *         not exist in the database.
     * @throws LevelingCoreException If any database operation fails, including connection issues, invalid SQL, or
     *                               errors during data retrieval.
     */
    private String metaGet(String key) {
        var sql = "SELECT meta_value FROM levelingcore_meta WHERE meta_key = ?";
        try (var c = dataSource.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to read meta key: " + key, e);
        }
    }

    /**
     * Inserts or updates a metadata key-value pair in the database. If the specified key already exists, its
     * corresponding value is updated. If the key does not exist, a new entry is inserted.
     * <p>
     * This operation ensures consistent storage of metadata in the "levelingcore_meta" table. The method uses two SQL
     * statements: 1. An "UPDATE" statement to modify the value of an existing key. 2. An "INSERT" statement to create a
     * new key-value pair if the update does not affect any rows.
     * <p>
     * Any database operation failure is wrapped and rethrown as a {@link LevelingCoreException}.
     *
     * @param key   The metadata key as a {@link String}. It identifies the entry to be inserted or updated.
     * @param value The metadata value as a {@link String}. It is stored or updated in association with the provided
     *              key.
     * @throws LevelingCoreException If any database operation fails, such as connection issues, invalid SQL, or errors
     *                               during the update or insert process.
     */
    private void metaPut(String key, String value) {
        var update = "UPDATE levelingcore_meta SET meta_value = ? WHERE meta_key = ?";
        var insert = "INSERT INTO levelingcore_meta (meta_key, meta_value) VALUES (?, ?)";
        try (var c = dataSource.getConnection()) {
            int changed;
            try (var ps = c.prepareStatement(update)) {
                ps.setString(1, value);
                ps.setString(2, key);
                changed = ps.executeUpdate();
            }
            if (changed == 0) {
                try (var ps = c.prepareStatement(insert)) {
                    ps.setString(1, key);
                    ps.setString(2, value);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to write meta key: " + key, e);
        }
    }

    /**
     * Migrates the XP data across all players in the database to align with a new leveling formula while maintaining
     * the same player levels. This operation ensures that players retain their current levels after a formula change by
     * recalculating their XP based on the provided {@link LevelFormula} and updating the database content accordingly.
     * <p>
     * The migration is performed only if the provided formula descriptor differs from the one stored in the database
     * metadata. If the existing database metadata already matches the new formula descriptor, this method exits without
     * performing the migration.
     * <p>
     * The steps involved in the migration process include: 1. Reading the current formula metadata and comparing it
     * with the new formula descriptor. 2. Re-calculating each player's XP based on the corresponding level in the new
     * formula. 3. Updating the database with the re-calculated XP for each player. 4. Storing the new formula
     * descriptor to metadata upon successful migration.
     *
     * @param newFormula The new leveling formula represented by a {@link LevelFormula}. This formula provides methods
     *                   to calculate XP for a given level and vice versa.
     * @param newDesc    The descriptor of the new formula, represented as a {@link FormulaDescriptor}. This descriptor
     *                   consists of the formula type and its parameters, which are used to identify the formula and
     *                   check for differences during migration.
     * @throws LevelingCoreException If any database operation fails, such as connection issues, invalid SQL, or errors
     *                               while performing the data migration.
     */
    public void migrateFormulaIfNeeded(LevelFormula newFormula, FormulaDescriptor newDesc) {
        createMetaTableIfNotExists();

        String oldType = metaGet("formula.type");
        String oldParams = metaGet("formula.params");

        if (oldType == null || oldParams == null) {
            metaPut("formula.type", newDesc.type());
            metaPut("formula.params", newDesc.params());
            return;
        }

        if (oldType.equalsIgnoreCase(newDesc.type()) && oldParams.equals(newDesc.params())) {
            return;
        }

        var oldFormula = LevelFormulaFactory.formulaFromDescriptor(
            new FormulaDescriptor(oldType, oldParams),
            Paths.get("./data")
        );

        var select = "SELECT player_id, xp FROM player_levels";
        var update = "UPDATE player_levels SET xp = ? WHERE player_id = ?";

        try (var c = dataSource.getConnection()) {
            c.setAutoCommit(false);

            try (
                var psSel = c.prepareStatement(select);
                var rs = psSel.executeQuery();
                var psUpd = c.prepareStatement(update)
            ) {

                var batch = 0;

                while (rs.next()) {
                    var playerId = rs.getString("player_id");
                    var oldXp = rs.getLong("xp");

                    var level = oldFormula.getLevelForXp(oldXp);
                    var newXp = newFormula.getXpForLevel(level);

                    psUpd.setLong(1, newXp);
                    psUpd.setString(2, playerId);
                    psUpd.addBatch();

                    batch++;
                    if (batch >= 1000) {
                        psUpd.executeBatch();
                        batch = 0;
                    }
                }

                if (batch > 0) {
                    psUpd.executeBatch();
                }
            }

            c.commit();
            Main.LOGGER.log(System.Logger.Level.INFO, "Migration completed");
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to migrate XP to preserve levels", e);
        }

        metaPut("formula.type", newDesc.type());
        metaPut("formula.params", newDesc.params());
    }

    /**
     * Saves the level-related data for a player. This method updates the player's experience points (XP) in the
     * database if an entry for the player already exists. If no entry exists, a new row is created.
     * <p>
     * The method uses two SQL statements: 1. An "UPDATE" statement to modify the player's existing XP value if the
     * player ID is found. 2. An "INSERT" statement to create a new entry with the player ID and XP if the update
     * operation did not affect any rows.
     * <p>
     * This method ensures that player XP data is consistently stored in the database and handles scenarios where a
     * player record needs to be either updated or inserted.
     *
     * @param data The {@link PlayerLevelData} instance containing the player's unique identifier and XP value. The
     *             unique identifier is used to check for existing database entries, and the XP value is saved or
     *             updated accordingly.
     * @throws LevelingCoreException if any database operation fails, such as connection issues or invalid SQL.
     */
    @Override
    public void save(PlayerLevelData data) {
        var updateSql = "UPDATE player_levels SET xp = ? WHERE player_id = ?";
        var insertSql = "INSERT INTO player_levels (player_id, xp) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            int updated;
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setLong(1, data.getXp());
                ps.setString(2, data.getPlayerId().toString());
                updated = ps.executeUpdate();
            }

            if (updated == 0) {
                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setString(1, data.getPlayerId().toString());
                    ps.setLong(2, data.getXp());
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to save player level data", e);
        }
    }

    /**
     * Loads the level-related data for a player identified by their unique UUID. This method retrieves the player's
     * experience points (XP) from the database and creates a {@link PlayerLevelData} instance with the retrieved
     * values. If no data exists for the given UUID, the method returns null. If any database-related issue occurs, it
     * wraps and rethrows the exception as a {@link LevelingCoreException}.
     *
     * @param id The unique identifier of the player as a {@link UUID}.
     * @return A {@link PlayerLevelData} instance containing the player's XP and unique identifier, or null if no data
     *         exists for the given UUID.
     * @throws LevelingCoreException if any database operation fails, such as connection issues or invalid SQL.
     */
    @Override
    public PlayerLevelData load(UUID id) {
        var sql = "SELECT xp FROM player_levels WHERE player_id = ?";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, id.toString());

            var rs = ps.executeQuery();
            if (rs.next()) {
                var data = new PlayerLevelData(id);
                data.setXp(rs.getLong("xp"));
                return data;
            }
            return null;
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load player level data", e);
        }
    }

    /**
     * Checks if a record exists in the "player_levels" database table for the given player UUID. This method executes a
     * SQL query using the provided UUID to determine if an entry exists.
     *
     * @param id The unique identifier of the player as a {@link UUID}.
     * @return {@code true} if a record exists for the given UUID; {@code false} otherwise.
     * @throws LevelingCoreException if any database operation fails, such as connection issues or invalid SQL.
     */
    @Override
    public boolean exists(UUID id) {
        var sql = "SELECT 1 FROM player_levels WHERE player_id = ?";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, id.toString());
            var rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            throw new LevelingCoreException("exists() failed", e);
        }
    }

    @Override
    public void close() {
        try {
            if (dataSource instanceof AutoCloseable c) {
                c.close();
            }
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to close JDBC datasource", e);
        }
    }
}
