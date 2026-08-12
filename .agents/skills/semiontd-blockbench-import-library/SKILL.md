---
name: semiontd-blockbench-import-library
description: "Author, inspect, package, and debug Blockbench Import Library (BIL) models used by SemionTD. Use when work touches .bbmodel or .ajmodel resources, embedded model textures, BIL animation names, SemionBilModelCache, LivingEntityHolder, Polymer generated resource-pack assets, model load failures, or the pinned blockbench-import-library dependency."
---

# SemionTD Blockbench Import Library

Work from the checked-out SemionTD source and its pinned BIL version. Treat the current dependency, loader code, packaged model resource, startup log, and generated Polymer pack as the authority.

## Establish the current contract

1. Read repository instructions and inspect the working tree. Preserve unrelated changes.
2. Read `bli_version` in `gradle.properties` and the BIL dependency in `build.gradle`.
3. Read these integration points before changing a model workflow:
   - `entity/model/SemionBilModelCache.java`
   - `entity/SemionPolymerEntityDataWarmup.java`
   - `entity/tower/SemionTowerEntity.java` or `entity/monster/SemionMonsterEntity.java`
   - `entity/visual/SemionAnimationState.java`
4. Read [references/library-api.md](references/library-api.md) for the resource, loader, animation, packaging, and failure contracts.

Do not assume a newer upstream BIL API applies to the repository's pinned artifact. Inspect the local source JAR when a signature or format behavior is unclear.

## Prepare a model

1. Keep a stable lowercase resource ID such as `semion-td:tower/penguin`.
2. Store the source at exactly one matching classpath path:
   - `.bbmodel`: `src/main/resources/model/semion-td/tower/penguin.bbmodel`
   - `.ajmodel`: `src/main/resources/model/semion-td/tower/penguin.ajmodel`
3. Keep geometry inside exported Blockbench groups. Use stable bone UUIDs and names.
4. Embed every texture as a PNG data URI. The pinned BIL resource-pack generator decodes `textures[].source`; an external workstation path is not deployable data.
5. Name only the animations the runtime actually invokes. SemionTD recognizes lowercase `idle`, `walk`, `attack`, and `heal` states.
6. Validate the file before wiring it into gameplay:

```text
rtk .agents/skills/semiontd-blockbench-import-library/scripts/inspect_bbmodel.py \
  src/main/resources/model/semion-td/tower/penguin.bbmodel \
  --model-id semion-td:tower/penguin \
  --require-animation idle
```

Require `attack`, `walk`, or `heal` only when that tower or monster should visibly use it. A missing optional animation does not justify adding a parallel animation controller.

## Verify the real output

Run the narrow validation first, then exercise startup/resource generation:

```text
rtk ./gradlew runGameTest remapJar --console=plain --no-daemon
rtk proxy unzip -l build/libs/semion-td-*.jar | rtk rg 'model/semion-td/'
rtk proxy unzip -l build/run/gameTest/polymer/resource_pack.zip | rtk rg 'assets/bil/'
rtk git diff --check
```

Confirm all of the following:

- the source model is present in the remapped SemionTD JAR;
- the startup log reports the expected BIL warm count and no failure for the model ID;
- the generated Polymer pack contains BIL item models and decoded textures;
- a real runtime entity creates a BIL holder and plays the intended state;
- the client has accepted the current generated resource pack.

Restart after adding or replacing a model. `SemionBilModelCache` caches both successful loads and missing-model results for the process lifetime; `/semiontd reload` does not clear that cache.

## Diagnose failures

- If the model ID exists but `hasBilModelHolder()` is false, verify the exact classpath path, JSON, embedded PNG data, and startup warm warning.
- If the holder exists but the entity is invisible, inspect generated `assets/bil/` entries and client resource-pack acceptance.
- If geometry is visible but animation is static, compare exact lowercase animation names with `SemionAnimationState` and the runtime goal/hook that calls `playAnimation`.
- If visual size and server interaction disagree, inspect the parent entity dimensions and `EntityVisual.scale`; BIL geometry does not define SemionTD gameplay collision by itself.
- If a changed file still renders the old model, restart the server and ensure the client received a newly generated pack.

Report the model ID, source resource path, pinned BIL version, validator result, startup warm result, generated-pack evidence, and any animation or collision limitation.
