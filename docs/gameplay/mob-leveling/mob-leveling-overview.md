---
title: "Mob Leveling Overview"
order: 15
published: true
draft: false
---

The Mob Leveling system dynamically assigns and updates levels for NPC mobs based on configurable rules, world context, and player influence.

## Level Pipeline

```mermaid
flowchart TD
    A[Mob Spawn or Recalculation] --> B{NPC Override?}
    B -- Yes --> B1[Use Override Level]
    B -- No --> C{Boss Override?}
    C -- Yes --> C1[Use Boss Level]
    C -- No --> D{Instance Mapping Exists?}

    D -- Yes --> D1[Instance Base Level]
    D -- No --> E[Compute Modes]

    E --> F[Compute Base Levels]
    F --> G[Apply Weights]
    G --> H[Combine Levels]

    D1 --> I[Randomize]
    H --> I

    I --> L[Final Level]

    %% Classes
    classDef start fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef decision fill:#F5A623,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef override fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
    classDef compute fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;
    classDef final fill:#E74C3C,color:#fff,stroke:#A93226,stroke-width:2px;

    %% Apply classes
    class A start;
    class B,C,D decision;
    class B1,C1 override;
    class E,F,G,H,D1,I compute;
    class L final;
```
