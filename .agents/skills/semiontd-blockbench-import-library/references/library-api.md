# SemionTD BIL Integration Reference

## Contents

1. [Pinned dependency and ownership](#1-pinned-dependency-and-ownership)
2. [Resource ID and loader resolution](#2-resource-id-and-loader-resolution)
3. [Blockbench authoring contract](#3-blockbench-authoring-contract)
4. [Runtime holder and animation flow](#4-runtime-holder-and-animation-flow)
5. [Generated resource-pack flow](#5-generated-resource-pack-flow)
6. [Validation and diagnostics](#6-validation-and-diagnostics)

## 1. Pinned dependency and ownership

SemionTD currently includes BIL through:

```groovy
modImplementation include("de.tomalbrc:blockbench-import-library:${project.bli_version}")
```

Read `gradle.properties` before quoting a version. At the time this reference was written, the repository used `1.7.0+1.21.6` on Minecraft `1.21.8`. Because `include(...)` nests BIL in the remapped mod JAR, do not add a second runtime JAR or duplicate its initializer.

Use these source authorities:

| Concern | SemionTD authority | Pinned BIL authority |
|---|---|---|
| model lookup/cache | `SemionBilModelCache` | `BbModelLoader`, `AjModelLoader` |
| entity rendering | `SemionTowerEntity`, `SemionMonsterEntity` | `AnimatedEntity`, `LivingEntityHolder` |
| animation IDs | `SemionAnimationState` | model animation map / animator |
| generated pack | `SemionPolymerEntityDataWarmup` | `BbResourcePackGenerator`, `ResourcePackUtil`, `BIL` initializer |

When an API is uncertain, inspect the `*-sources.jar` under Gradle's module cache for the exact pinned version.

## 2. Resource ID and loader resolution

`SemionBilModelCache.load(modelId)` performs this flow:

```text
string model ID
  -> ResourceLocation.tryParse
  -> BbModelLoader.load(id)
  -> /model/<namespace>/<path>.bbmodel
  -> on failure AjModelLoader.load(id)
  -> /model/<namespace>/<path>.ajmodel
  -> Optional<Model> cached by original normalized ID
```

For `semion-td:tower/penguin`, the valid source paths are:

```text
src/main/resources/model/semion-td/tower/penguin.bbmodel
src/main/resources/model/semion-td/tower/penguin.ajmodel
```

Prefer one format per ID. If both exist, `.bbmodel` always wins. Keep IDs lowercase and valid as Minecraft resource locations. `SemionBilModelCache.normalize` removes only null/blank values; it does not repair spaces, uppercase characters, or a wrong namespace.

The cache stores `Optional.empty()` after both loaders fail. Adding the file later in the same process does not retry the ID. Restart after resource changes.

## 3. Blockbench authoring contract

Use the repository's working `penguin.bbmodel` as a structural example, not as a universal scale template. The pinned importer supports ordinary Blockbench JSON and switches importer behavior for format version 5 or newer.

Preserve these properties:

- Put renderable cubes under exported outliner groups. BIL turns groups into bones and generates one item-model display per renderable group.
- Keep cube faces associated with valid texture entries. Untextured faces are removed during import.
- Embed each texture in `textures[].source` as `data:image/png;base64,...`. The resource generator decodes that field into `assets/bil/textures/item/...`.
- Keep PNG bytes valid and keep texture names deterministic. BIL lowercases names and strips `.png` suffixes for generated paths.
- Use stable group and bone UUIDs. Animation channels point to those UUIDs; recreating groups can disconnect keyframes.
- Name a head group with a name beginning with `head` only when head-look behavior is wanted; the importer marks those nodes specially.
- Avoid relying on model bounds for gameplay. The pinned ordinary Blockbench importer reports an internal visual size, while SemionTD owns its entity collision and scale.

The runtime states are:

| State | Model animation name | Typical source |
|---|---|---|
| idle | `idle` | configure, no target, attack cooldown |
| walk | `walk` | movement toward a target/final-defense movement |
| attack | `attack` | `TowerAttackMonsterGoal` or monster attack goal |
| heal | `heal` | explicit healing animation hook |

Use loop mode for continuous `idle`/`walk`; use an appropriate one-shot or hold behavior for action animations. SemionTD retriggers `attack` and `heal` even when already selected.

## 4. Runtime holder and animation flow

`SemionTowerEntity.configure` reads `tower.visual()`, stores the model ID, chooses the Polymer proxy, applies visual scale, installs the BIL model, and selects `idle`.

When a model loads, SemionTD creates:

```java
holder = new LivingEntityHolder<>(this, model);
holderAttachment = EntityAttachment.ofTicking(holder, this);
```

`AnimatedEntity` exposes the tower to clients as a block-display proxy while the holder owns the generated display bones and redirected interaction/collision elements. If a model ID is present but loading fails, the ID is still retained and the BIL proxy path remains selected, but no holder exists.

`playAnimation` pauses the other known SemionTD state IDs and plays the requested ID through the holder animator. It still records the SemionTD state when no holder or animation exists, so an animation-state assertion alone does not prove visible playback.

`EntityVisual.scale` is applied to the parent entity's `Attributes.SCALE`. `LivingEntityHolder` derives its visual scale from that entity, so SemionTD deliberately leaves the holder-local scale at `1.0`. Do not set both or the visual can be scaled twice.

## 5. Generated resource-pack flow

SemionTD initialization follows this order:

```text
add SemionTD mod assets
  -> load configuration
  -> rebuild production tower and summon catalogs
  -> SemionPolymerEntityDataWarmup.warm
  -> load every configured wave, summon, and tower BIL model
  -> BIL records generated model and texture bytes
  -> Polymer creates the resource pack
  -> BIL adds recorded assets after initial creation
```

Expected generated entries include:

```text
assets/bil/items/...
assets/bil/models/item/...
assets/bil/textures/item/...
```

The `.bbmodel` remains a server classpath resource in the SemionTD JAR. Clients receive generated item-model JSON and PNG data through the Polymer pack, not the original `.bbmodel`.

## 6. Validation and diagnostics

Use layered evidence:

1. Run `inspect_bbmodel.py` for JSON, exported groups, embedded PNGs, ID/path agreement, and required animation names.
2. Run GameTests so initialization invokes model warmup and Polymer pack generation.
3. Inspect the remapped JAR for `model/<namespace>/<path>.<format>`.
4. Inspect `build/run/gameTest/polymer/resource_pack.zip` for `assets/bil/` models and textures.
5. In a live server, confirm the warm log, holder creation, visible animation, scale, and click/attack behavior.

| Symptom | Likely cause | Check |
|---|---|---|
| `Failed to warm BIL model` | wrong ID/path, malformed JSON, invalid embedded texture | model path, validator, loader order |
| block-display proxy but no model | ID retained but both loaders failed | `hasBilModelHolder`, startup warning |
| white/missing texture | non-embedded or invalid `textures[].source` | PNG data URI and generated texture entry |
| no animation | missing/mismatched lowercase name or no runtime trigger | model animation list and `playAnimation` caller |
| old model after reload | process cache and/or stale client pack | restart and regenerate/reaccept pack |
| click/hitbox mismatch | visual geometry treated as gameplay dimensions | parent entity scale/dimensions and runtime smoke test |
