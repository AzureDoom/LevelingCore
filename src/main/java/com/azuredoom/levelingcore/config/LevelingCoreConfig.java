package com.azuredoom.levelingcore.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration settings for the LevelingCore system. This configuration includes database connection
 * settings and level progress formula details.
 */
public class LevelingCoreConfig {

    public Database database = new Database();

    public Formula formula = new Formula();

    public static class Database {

        public String jdbcUrl = "jdbc:h2:file:./data/plugins/levelingcore/levelingcore;MODE=PostgreSQL";

        public String username = "";

        public String password = "";

        public int maxPoolSize = 10;
    }

    public static class Formula {

        public String type = "EXPONENTIAL";

        public Boolean migrateXP = true;

        public Exponential exponential = new Exponential();

        public Linear linear = new Linear();

        public Table table = new Table();

        public Custom custom = new Custom();
    }

    public static class Exponential {

        public double baseXp = 100.0;

        public double exponent = 1.7;
    }

    public static class Linear {

        public long xpPerLevel = 100;
    }

    public static class Table {

        public String file = "levels.csv";
    }

    public static class Custom {

        public String xpForLevel = "";

        public Map<String, Double> constants = new HashMap<>();

        public int maxLevel = 100000;
    }
}
