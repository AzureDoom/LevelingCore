---
name: Internal Configuration
description: Non-standard configuration options.
author: AzureDoom
---

# Configuration File Location

LevelingCore’s configuration file is stored in the following location: `/mods/com.azuredoom_levelingcore/data/config/levelingcore.yml`

### Important Notes

- LevelingCore will only read configuration values from `levelingcore.yml` in the path above.
- Always stop the server before editing the configuration file to avoid data loss or partial writes.

# Database Configuration

The `database` section controls how LevelingCore stores player data such as UUID and experience.

### Supported JDBC URLs

| Database | Example JDBC URLs |
|---------|------------------|
| **H2 (file)** | `jdbc:h2:file:./mods/com.azuredoom_levelingcore/data/levelingcore;MODE=PostgreSQL` |
| **MySQL** | `jdbc:mysql://host:port/dbname`<br>`jdbc:mysql://host:port/dbname?user=dbuser&password=dbpass` |
| **MariaDB** | `jdbc:mariadb://host:port/dbname`<br>`jdbc:mariadb://host:port/dbname?user=dbuser&password=dbpass` |
| **PostgreSQL** | `jdbc:postgresql://host:port/dbname`<br>`jdbc:postgresql://host:port/dbname?user=dbuser&password=dbpass` |

**Notes**
- H2 typically uses an empty username and password unless explicitly configured.
- MySQL, MariaDB, and PostgreSQL require valid credentials.

### Example Configuration

```yaml
database:
  jdbcUrl: "jdbc:h2:file:./data/plugins/com.azuredoom_levelingcore/levelingcore;MODE=PostgreSQL"
  username: ""
  password: ""
  maxPoolSize: 10
```

### Options

#### `jdbcUrl`

- **Type:** String
- **Description:**  
  JDBC connection string for the database used to store plugin data.  
  Supports **H2**, **MySQL**, **MariaDB**, and **PostgreSQL**.

  External databases require a pre-existing database and valid credentials.  
  Credentials may be provided via dedicated configuration fields or JDBC URL query parameters.
- **Default:** H2 file database in the plugin data directory.

#### `username`
- **Type:** String
- **Description:** Database username used for authentication.
- **Required:** Yes for MySQL, MariaDB, and PostgreSQL unless credentials are provided in the JDBC URL.

#### `password`
- **Type:** String
- **Description:** Database password used for authentication.
- **Required:** Yes for MySQL, MariaDB, and PostgreSQL unless credentials are provided in the JDBC URL.

#### `maxPoolSize`
- **Type:** Integer
- **Description:** Maximum number of database connections in the connection pool.
- **Default:** `10`

## Migration Notes

- Switching between database types (H2, MySQL, MariaDB, PostgreSQL) does **not** support automatic data migration.
- LevelingCore will treat the new database as empty unless data is manually transferred.
- Always back up your existing database before modifying JDBC settings.

---

# Leveling Formula

The `formula` section defines how much XP is required to reach each level.

### Supported Formula Types

| Type | Description |
|---|---|
| **EXPONENTIAL** | XP increases exponentially as levels increase |
| **LINEAR** | Fixed XP increase per level |
| **TABLE** | XP values are loaded from a CSV file |
| **CUSTOM** | XP is calculated using a math expression |

### Global Options

```yaml
formula:
  type: "EXPONENTIAL"
  migrateXP: true
```

#### `type`
- **Allowed Values:** `EXPONENTIAL`, `LINEAR`, `TABLE`, `CUSTOM`

#### `migrateXP`
- **Type:** Boolean
- **Default:** `true`
- **Description:** Recalculates stored XP when the formula changes to preserve player levels.

---

## Exponential Formula

```yaml
exponential:
  baseXp: 100.0
  exponent: 1.7
  maxLevel: 100000
```

**Formula**
```
XP(level) = baseXp * (level - 1) ^ exponent
```

---

## Linear Formula

```yaml
linear:
  xpPerLevel: 100
  maxLevel: 100000
```

**Formula**
```
XP(level) = xpPerLevel * (level - 1)
```

---

## Table Formula (CSV)

The **TABLE** formula allows you to explicitly define XP floors per level using a CSV file.

### Example Configuration

```yaml
table:
  file: "levels.csv"
```

### CSV Format

- One row per level
- Two columns: `level,xp`
- Levels must start at `1`
- XP represents the **XP floor** required to reach that level

### Example `levels.csv`

```csv
level,xp
1,0
2,100
3,250
4,450
5,700
6,1000
```

### Notes

- Missing levels are **not allowed**
- Levels must be sequential
- XP values must be increasing
- This method gives full manual control over progression

---

## Custom Formula

The **CUSTOM** formula allows defining XP requirements using a mathematical expression.

### Example Configuration

```yaml
custom:
  xpForLevel: "exp(a * (level - 1)) * b"
  constants:
    a: 0.15
    b: 100
  maxLevel: 100000
```

### Expression Rules

- The expression must return the **XP floor** for a level
- Expressions are evaluated using floating-point math

### Available Variables

| Variable | Description |
|---|---|
| `level` | Current level (integer ≥ 1) |

### Constants

Constants are user-defined numeric values you can reference inside the expression.

```yaml
constants:
  a: 0.15
  b: 100
```

### Example Calculations

For the example above:

- Level 1 → `exp(0) * 100 = 100`
- Level 10 → `exp(0.15 * 9) * 100 ≈ 386`
- Level 50 → `exp(0.15 * 49) * 100 ≈ 22255`

### `maxLevel`

- **Type:** Integer
- **Description:** Maximum supported level for this formula.
- **Purpose:** Used internally for binary search and XP calculations.
- **Recommendation:** Set this higher than the maximum level your server will ever reach.

---

## Migration Notes

- Changing formula types with `migrateXP` enabled will preserve player levels.
- Disabling migration will keep raw XP values unchanged.
- Always back up your data before switching formulas.