---
title: "Player Stats"
order: 7
published: true
draft: false
---

# Overview

Players gain **Ability Points** through leveling. These points can be
spent to increase one of several core stats.\
Spending a stat immediately updates derived values such as health,
stamina, mana, and combat damage.

- Use `/showstats` or the **Tome of Skills** to allocate points.
- Use the **Reset Skills Potion** to refund all spent points.
- After spending a stat point, Health, Stamina, and Mana are recalculated and immediately restored to their new maximum values.

## Stat Effects at a Glance

- **STR** — increases melee damage
- **AGI** — increases stamina and oxygen
- **PER** — increases projectile damage
- **VIT** — increases maximum health
- **INT** — increases maximum mana and mana regeneration
- **CON** — reduces incoming damage

## Spending Ability Points

A player can spend Ability Points by using the command `/showstats` or using the new Tome of Skills, which can be crafted like so:

![tome_repeice](https://github.com/user-attachments/assets/e6adfd1b-5338-443d-9683-2c5aebac6d5b)

| Ingredient   | Amount |
|--------------|--------|
| Life Essence | 5      |

### HyUI Version

![hyui_version](https://github.com/user-attachments/assets/024896b5-cf46-45a4-a03e-fc65023db8cc)

### Vanilla UI / No HyUI

![vanilla_version](https://github.com/user-attachments/assets/704fce1f-694f-4887-bbbb-0d9946e56516)

## Resetting Ability Points

A player can reset their Ability Points by crafting the `Reset Skills Potion` found in the `Alchemy bench`

![reset_recipe](https://github.com/user-attachments/assets/2875d250-fbd6-4a36-8622-f7de7e7a2a2f)

| Ingredient    | Amount |
|---------------|--------|
| Potion Bottle | 1      |
| Water Bucket  | 1      |
| Life Essence  | 5      |
| Fire Essence  | 5      |
| Ice Essence   | 5      |
| Void Essence  | 5      |
| Water Essence | 5      |
| Apple         | 2      |

## Derived Stats

### Vitality (VIT) – Maximum Health

Increases **Maximum Health**.

```
Health Bonus = VIT × vitStatMultiplier
```

-   Default multiplier: `2.0`

### Agility (AGI) – Maximum Stamina and Oxygen

Increases **Maximum Stamina** and **Maximum Oxygen**.

```
Stamina Bonus = AGI × agiStatMultiplier
Oxygen Bonus  = AGI × agiStatMultiplier
```
-   Default multiplier: `0.25`

### Intelligence (INT) – Maximum Mana

Increases **Maximum Mana** and **Mana Regeneration**.

    Mana Bonus = INT × intStatMultiplier
    Mana Regen = max(1, floor(1 + INT × 0.25))

-   Default multiplier: `2.0`

## Combat Calculations

```mermaid
flowchart TD
    A[Damage Event Triggered] --> B{Target is Player?}
    B -- Yes --> X1[Return]
    B -- No --> C{Victim has NPCEntity ref?}

    C -- No --> X2[Return]
    C -- Yes --> D{Damage source is EntitySource?}

    D -- No --> X3[Return]
    D -- Yes --> E{Attacker ref is valid?}

    E -- No --> X4[Return]
    E -- Yes --> F{Attacker has PlayerRef?}

    F -- No --> X5[Return]
    F -- Yes --> G{Attacker has Player + Inventory?}

    G -- No --> X6[Return]
    G -- Yes --> H{Level service present?}

    H -- No --> X7[Return]
    H -- Yes --> I[Get UUID, STR, PER]

    I --> J{Incoming damage > 0?}
    J -- No --> X8[Return]
    J -- Yes --> K{Damage cause exists?}

    K -- No --> X9[Return]
    K -- Yes --> L{Is Projectile?}

    %% Projectile branch
    L -- Yes --> P1[Fetch mob damage threshold]
    P1 --> P2[Use PER stat + PER multiplier]
    P2 --> P3[Compute diminishing stat bonus]
    P3 --> P4[finalDamage = incoming * threshold * bonus]

    %% Melee branch
    L -- No --> M1[Fetch mob damage threshold]
    M1 --> M2[Use STR stat + STR multiplier]
    M2 --> M3[Compute diminishing stat bonus]
    M3 --> M4[finalDamage = incoming * threshold * bonus]

    %% Merge
    P4 --> Z[Set damage amount]
    M4 --> Z
    Z --> END[End]

    %% Classes
    classDef start fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef decision fill:#F5A623,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef lookup fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
    classDef compute fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;
    classDef final fill:#E74C3C,color:#fff,stroke:#A93226,stroke-width:2px;
    classDef exit fill:#7F8C8D,color:#fff,stroke:#626567,stroke-width:2px;

    %% Apply classes
    class A start;
    class B,C,D,E,F,G,H,J,K,L decision;
    class I,P1,P2,M1,M2 lookup;
    class P3,P4,M3,M4 compute;
    class Z,END final;
    class X1,X2,X3,X4,X5,X6,X7,X8,X9 exit;
```

### Strength (STR) – Melee Damage

Applied when the player performs a **melee attack**.

    Final Damage = Base Damage × (1 + STR × strStatMultiplier)

-   Default multiplier: `0.1`

### Perception (PER) – Ranged Damage

Applied when the player performs a **projectile attack**.

    Final Damage = Base Damage × (1 + PER × perStatMultiplier)

-   Default multiplier: `0.1`

### Constitution (CON) – Damage Taken

Applied when the player **receives damage**.

    Final Damage =
      Incoming Damage
      × conDamageMultiplier(CON)
      × Weapon Type Multiplier
      × Mob Level Multiplier

-   Applies to both melee and projectile damage
-   Lower final values mean less damage taken
-   Default base multiplier: `0.80`

```mermaid
flowchart TD
    A[Damage Event Triggered] --> B{Target is Player?}
    B -- No --> X1[Return]
    B -- Yes --> C{Victim has PlayerRef?}

    C -- No --> X2[Return]
    C -- Yes --> D{Damage source is EntitySource?}

    D -- No --> X3[Return]
    D -- Yes --> E{Attacker ref is valid?}

    E -- No --> X4[Return]
    E -- Yes --> F{Attacker has NPCEntity?}

    F -- No --> X5[Return]
    F -- Yes --> G{NPC attacker reference exists?}

    G -- No --> X6[Return]
    G -- Yes --> H{Level service present?}

    H -- No --> X7[Return]
    H -- Yes --> I{Incoming damage > 0?}

    I -- No --> X8[Return]
    I -- Yes --> J{Damage cause exists?}

    J -- No --> X9[Return]
    J -- Yes --> K{Is Projectile?}

    K --> L{NPC UUIDComponent exists?}
    L -- No --> X10[Return]
    L -- Yes --> M[Fetch mob level data]

    %% Projectile branch
    K -- Yes --> P1[Use ranged base damage]
    P1 --> P2[Use ranged level multiplier]
    P2 --> N

    %% Melee branch
    K -- No --> M1[Use melee base damage]
    M1 --> M2[Use melee level multiplier]
    M2 --> N

    %% Shared damage computation
    M --> N[Get player CON]
    N --> O[Compute mitigation factor from CON]
    O --> Q[Compute scaled damage from base damage, multiplier, and mob level]
    Q --> R[Apply incoming damage times mitigation times scaled damage times mob damage multiplier]
    R --> S[Set damage amount]
    S --> Z[End]

    %% Classes
    classDef start fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef decision fill:#F5A623,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef lookup fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
    classDef compute fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;
    classDef final fill:#E74C3C,color:#fff,stroke:#A93226,stroke-width:2px;
    classDef exit fill:#7F8C8D,color:#fff,stroke:#626567,stroke-width:2px;

    class A start;
    class B,C,D,E,F,G,H,I,J,K,L decision;
    class M,N,P1,P2,M1,M2 lookup;
    class O,Q,R compute;
    class S,Z final;
    class X1,X2,X3,X4,X5,X6,X7,X8,X9,X10 exit;
```

## Configuration Defaults

``` yaml
strStatMultiplier: 0.1
perStatMultiplier: 0.1
vitStatMultiplier: 2.0
agiStatMultiplier: 0.25
intStatMultiplier: 2.0
conStatMultiplier: 0.80
```

All multipliers are configurable for balance tuning.

## Recalculation Behavior

After a stat is spent:

-   Health, Stamina, and Mana maximums are recalculated
-   Health, Stamina, and Mana are automatically **set to their new
    maximums**
-   Mana regeneration is updated immediately
-   Stat modifiers are applied as **additive max-value modifiers**