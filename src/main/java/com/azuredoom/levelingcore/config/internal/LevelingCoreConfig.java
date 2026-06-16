package com.azuredoom.levelingcore.config.internal;

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

        public String jdbcUrl = "jdbc:h2:file:./data/levelingcore/levelingcore;MODE=PostgreSQL";

        public String username = "";

        public String password = "";

        public int maxPoolSize = 10;
    }

    public static class Formula {

        public String type = "EXPONENTIAL";

        public Boolean migrateXP = true;

        public Exponential exponential = new Exponential();

        public Linear linear = new Linear();

        public Polynomial polynomial = new Polynomial();

        public Table table = new Table();

        public Custom custom = new Custom();
    }

    public static class Exponential {

        public double baseXp = 100.0;

        public double exponent = 1.7;

        public int maxLevel = 100000;
    }

    public static class Linear {

        public long xpPerLevel = 100;

        public int maxLevel = 100000;
    }

    public static class Polynomial {

        /**
         * Polynomial coefficients ordered from degree 0 to degree N. The XP required for a given level is computed as:
         *
         * <pre>
         * xp = sum(coefficients[i] * (level - 1) ^ i)
         * </pre>
         *
         * Defaults to a basic quadratic curve: {@code 100 * (level - 1)^2}.
         */
        public double[] coefficients = { 0.0, 0.0, 100.0 };

        public int maxLevel = 100000;
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
