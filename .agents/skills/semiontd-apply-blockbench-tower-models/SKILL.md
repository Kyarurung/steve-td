---
name: semiontd-apply-blockbench-tower-models
description: "Apply Blockbench Import Library (BIL) models to SemionTD production towers safely. Use when adding or replacing a tower .bbmodel/.ajmodel, assigning EntityVisual.blockbenchModel, choosing visual scale and fallback entity data, synchronizing state-dependent tower models, wiring animation states, validating catalog/web export, or testing BIL tower rendering and interaction."
---

# Apply Blockbench Models to SemionTD Towers

Attach a BIL model through the existing `EntityVisual` and `SemionTowerEntity` pipeline. Do not add a tower-specific holder, resource-pack generator, or animation controller unless the shared path cannot express the required behavior.

## Required companion context

1. Read repository instructions and preserve unrelated worktree changes.
2. Read `$semiontd-builder-tower-dev` for the current builder/catalog/runtime contract.
3. Read `$semiontd-blockbench-import-library` before authoring or debugging the model resource.
4. Read [references/tower-integration.md](references/tower-integration.md) before editing a tower family or runtime visual state.

Use current source as authority. The primary paths are `TowerType`, `EntityVisual`, the target family `*Towers` and `*TowerCatalogs`, `Tower.visual()`, `SemionTowerEntity.configure/syncTowerState`, `SemionAnimationState`, and `WebCatalogExporter`.

## Apply a static tower model

1. Choose a stable ID tied to the model resource, normally `semion-td:tower/<tower_or_family>`.
2. Add and validate the model at `src/main/resources/model/semion-td/tower/<path>.bbmodel` or `.ajmodel`.
3. Reuse the family helper that constructs `TowerType`; change only its visual argument:

```java
EntityVisual.builder(byId(EntityType.SALMON))
        .blockbenchModel("semion-td:tower/penguin")
        .scale(1.0)
        .build()
```

Omit `.scale(...)` when `1.0` is correct. Keep a valid fallback entity ID even though BIL's block-display path takes visual precedence while the model ID is present.

4. Resolve the type through `TowerBalanceRuntime.resolve(...)` and verify that the model ID and scale survive catalog reload.
5. Keep the existing `ProductionTowerCatalog` factory unless behavior—not appearance—requires a custom runtime class.

## Apply a state-dependent model

Use a static `TowerType.visual()` when every instance and state shares one model. For a genuine runtime state change:

1. Override `Tower.visual()` in the existing runtime tower class.
2. Return one stable `EntityVisual` per state; do not allocate a new equivalent visual every tick.
3. Change state through the existing state transition.
4. Call `onStateChanged()` exactly when the visible state changes.
5. Let `SemionTowerEntity.syncTowerState` compare IDs, replace the holder when necessary, reapply scale, and restart `idle`.
6. Test upgrade copy, respawn/final-defense movement, and cleanup if the state can survive those paths.

Do not call `installBilModel` from tower code; it is an entity implementation detail.

## Match animation and collision behavior

- Provide `idle` for visible idle motion.
- Provide `attack` when basic attacks should animate; `TowerAttackMonsterGoal` triggers it.
- Provide `walk` only for a tower that moves toward targets or during its relevant movement path.
- Provide `heal` only when the tower invokes the shared healing animation.
- Keep names lowercase and exact. Do not infer or remap names at runtime.
- Treat `EntityVisual.scale` as both parent entity scale and BIL visual scale. Verify server hitbox/click behavior; do not compensate with `holder.setScale`.
- Treat Blockbench geometry as visual data. Do not infer tower range, placement footprint, health, or collision from the model.

## Verify the complete slice

Add the smallest tests that prove the new risk:

- family catalog/JUnit: resolved model ID, scale, starter/tier ownership, and web serialization;
- GameTest: placed runtime entity retains the model ID, creates a holder when the real resource is present, uses `BLOCK_DISPLAY`, remains clickable, and records animation state;
- stateful model: holder/model changes after `onStateChanged()` without replacing the logical tower;
- artifact: remapped JAR contains the source model and the generated Polymer pack contains its BIL assets.

Run:

```text
rtk .agents/skills/semiontd-blockbench-import-library/scripts/inspect_bbmodel.py <model-file> \
  --model-id semion-td:tower/<path> --require-animation idle
rtk ./gradlew test runGameTest remapJar --console=plain --no-daemon
rtk proxy unzip -l build/libs/semion-td-*.jar | rtk rg 'model/semion-td/tower/'
rtk proxy unzip -l build/run/gameTest/polymer/resource_pack.zip | rtk rg 'assets/bil/'
rtk git diff --check
```

For live delivery, restart the server, confirm the BIL warm log, accept the regenerated pack, place and upgrade the tower, observe idle/attack/movement states, and verify click selection and server collision.

Report the affected tower IDs, model ID/path, static or stateful wiring, scale/collision decision, exact tests, generated-pack evidence, and any missing optional animation.
