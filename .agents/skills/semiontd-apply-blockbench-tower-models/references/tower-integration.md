# SemionTD Tower BIL Integration Reference

## Contents

1. [Current visual pipeline](#1-current-visual-pipeline)
2. [Static visual recipe](#2-static-visual-recipe)
3. [Stateful visual recipe](#3-stateful-visual-recipe)
4. [Scale, hitbox, and interaction](#4-scale-hitbox-and-interaction)
5. [Animations](#5-animations)
6. [Catalog, balance, and web ownership](#6-catalog-balance-and-web-ownership)
7. [Testing recipes](#7-testing-recipes)
8. [Delivery checklist](#8-delivery-checklist)

## 1. Current visual pipeline

The tower path is:

```text
family *Towers constant
  -> TowerType.visual
  -> ProductionTowerCatalog registration
  -> TowerBalanceRuntime.resolve
  -> runtime Tower.visual
  -> PlayerLane spawns SemionTowerEntity
  -> SemionTowerEntity.configure
  -> SemionBilModelCache.load
  -> LivingEntityHolder + ticking EntityAttachment
  -> BIL-generated Polymer resource-pack displays
```

`EntityVisual` owns:

- `entityTypeId`: vanilla/Polymer fallback visual ID;
- `blockbenchModelId`: optional BIL resource ID;
- `scale`: positive visual and parent entity scale;
- `properties`: tracked-data properties for non-BIL visuals.

When `blockbenchModelId` is present, `SemionTowerEntity.getPolymerEntityType` delegates to `AnimatedEntity` and exposes a block-display proxy. Its tracked-data path also delegates to `AnimatedEntity`, so vanilla entity properties are not the BIL model customization surface.

Use `LegionTowers.T1_PENGUIN` and `T2_PENGUIN` as the current static BIL tower example. Re-read them before copying because family helpers and balance descriptions can change.

## 2. Static visual recipe

Create the resource first:

```text
model ID: semion-td:tower/stone_guardian
source:   src/main/resources/model/semion-td/tower/stone_guardian.bbmodel
```

Assign it to the `TowerType` through the family's existing definition helper:

```java
private static final EntityVisual STONE_GUARDIAN_VISUAL =
        EntityVisual.builder(byId(EntityType.IRON_GOLEM))
                .blockbenchModel("semion-td:tower/stone_guardian")
                .build();
```

Reuse a constant only when multiple tiers intentionally share the exact immutable visual. Otherwise place the concise builder at the tower definition. Do not create a visual registry for one model.

The ordinary catalog factory remains unchanged:

```text
registerStarter(resolvedType, existing factory)
register(resolvedHigherTier, tier, existing factory)
```

Appearance alone is not a reason to subclass `ProductionTower`, override placement, or alter the catalog edge.

## 3. Stateful visual recipe

Only use runtime visual selection when a tower changes form during play. Keep the state owner where it already belongs, then override:

```java
@Override
public EntityVisual visual() {
    return awakened ? AWAKENED_VISUAL : DORMANT_VISUAL;
}
```

At the transition:

```java
if (awakened != nextAwakened) {
    awakened = nextAwakened;
    onStateChanged();
}
```

`onStateChanged()` routes the updated tower to its runtime entity. `syncTowerState` compares the new model ID with the installed ID. If different, it destroys the old attachment, loads the new model, creates a new holder, reapplies scale, and requests `idle`.

Keep these invariants:

- return stable visuals rather than rebuilding them every tick;
- call the state notification only after an actual transition;
- keep model IDs stable across balance reloads;
- copy custom state through the existing upgrade/state-copy path when required;
- verify the logical `Tower` object still backs right-click details after a visual transition.

Replacing a model file without changing its ID is not a runtime state transition. Restart the process because the model cache does not reload resources.

## 4. Scale, hitbox, and interaction

`SemionTowerEntity.applyVisualScale` writes `EntityVisual.scale` to the parent living entity's `Attributes.SCALE`, refreshes dimensions, and keeps the BIL holder-local scale at `1.0`. `LivingEntityHolder` derives the displayed geometry scale from the parent.

Consequences:

- do not call `holder.setScale` in tower code;
- do not apply the same scale in both Blockbench geometry and Java without checking the combined result;
- verify `getBbWidth()` and `getBbHeight()` after placement when scale changes;
- verify right-click selection, targeting, pathing, tower overlap, and final-defense collision;
- keep attack range and placement rules independent of model geometry.

BIL's `LivingEntityHolder` supplies redirected interaction and collision elements for the display. The authoritative server entity remains `SemionTowerEntity`; the model is not a replacement gameplay entity.

Use the smallest positive scale that matches the intended visual. Avoid adding a new configurable scale key unless live operators actually need to tune it; `EntityVisual.scale` already persists through the tower definition and web export.

## 5. Animations

SemionTD maps runtime states directly to BIL animation names:

```java
IDLE("idle")
WALK("walk")
ATTACK("attack")
HEAL("heal")
```

The tower entity pauses the other known states, then plays the selected animation. `attack` and `heal` are retriggered with higher transition priority; continuous states use the normal transition.

Runtime trigger ownership:

| Animation | Trigger |
|---|---|
| `idle` | configure, missing target, waiting for cooldown, stopped movement |
| `walk` | shared movement code while closing distance |
| `attack` | `TowerAttackMonsterGoal` when an attack fires |
| `heal` | `playHealingAnimation()` |

Do not add a custom tick that repeatedly calls `playAnimation`. Use the existing attack, movement, and healing paths. A model may omit an animation it never uses, but tests and delivery notes should state the resulting static state.

## 6. Catalog, balance, and web ownership

`TowerBalanceRuntime.resolve(type)` replaces configured numeric fields and renders descriptions while preserving the tower's visual contract. Verify the resolved type, not only the source constant.

Adding a visual does not change:

- builder ownership through `includesTowerInCatalog`;
- starter/tier registration;
- upgrade IDs or directed costs;
- placement mineral cost or abilities;
- tower factory selection.

`WebCatalogExporter` serializes `entityTypeId`, `blockbenchModelId`, `scale`, and properties. Run its existing tests or the family catalog suite so the website receives the same model ID as the game.

If tiers share one model, keep distinct stable tower IDs and reuse the model resource intentionally. If tiers need different silhouettes or animations, use different model IDs so cache identity and generated assets remain explicit.

## 7. Testing recipes

### Catalog/JUnit assertion

Resolve the tower and assert its immutable visual contract:

```java
TowerType resolved = TowerBalanceRuntime.resolve(FamilyTowers.T1_EXAMPLE);
assertEquals("semion-td:tower/example", resolved.blockbenchModelId());
assertEquals(1.0, resolved.visual().scale());
```

Also retain the family's existing ownership, starter, upgrade, description, and web-export assertions.

### GameTest assertion

Place or configure a real tower entity with a real packaged model, then assert:

```java
assertEquals("semion-td:tower/example", entity.blockbenchModelId());
assertEquals(EntityType.BLOCK_DISPLAY, entity.getPolymerEntityType(null));
assertTrue(entity.hasBilModelHolder());
```

Exercise the relevant attack or movement trigger and assert the Semion animation state. For scale changes, assert server dimensions and actual click/placement behavior, not only `visual.scale()`.

For a stateful visual, transition the logical tower, invoke the normal entity synchronization path, and assert the new model ID/holder while retaining the same logical runtime tower.

### Packaging assertion

After `runGameTest remapJar`, verify:

- `build/libs/semion-td-*.jar` contains `model/semion-td/tower/<path>.<format>`;
- startup does not log `Failed to warm BIL model <id>`;
- `build/run/gameTest/polymer/resource_pack.zip` contains generated model JSON, item definitions, and texture PNGs under `assets/bil/`.

## 8. Delivery checklist

- [ ] Model ID and classpath resource path match exactly.
- [ ] Textures are embedded valid PNG data URIs.
- [ ] Required lowercase animation names exist.
- [ ] `EntityVisual` is wired at the tower definition or legitimate runtime state override.
- [ ] No custom BIL holder/resource-pack/animation pipeline was added.
- [ ] Scale and server interaction were tested together.
- [ ] Resolved catalog and web export preserve the model ID and scale.
- [ ] Stateful forms call `onStateChanged()` and survive the required upgrade/lifecycle paths.
- [ ] Unit tests, GameTests, remapped JAR, warm log, and generated Polymer pack were verified.
- [ ] Live server was restarted and the client accepted the regenerated pack when live delivery was requested.
