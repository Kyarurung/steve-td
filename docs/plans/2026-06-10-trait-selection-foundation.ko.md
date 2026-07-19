# 세미온 TD 특성 선택 뼈대 구현 기획안

> **현재 작업 순서:** 뼈대 구현 이후의 재활성화와 통계 확장 순서는 [특성 시스템 재활성화 작업 순서](./2026-07-17-trait-system-reactivation-roadmap.ko.md)를 따른다.
>
> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** 세미온 TD 경기 시작 전에 참가자가 `주특성`과 `부특성`을 선택하고, 주특성은 100%, 부특성은 50% 효과로 적용되는 확장 가능한 특성 시스템의 뼈대를 만든다.

**Architecture:** 현재 `/semiontd start`가 참가자 선발 계획을 만들고 5초 카운트다운 뒤 `SemionGame.start(...)`를 호출하는 구조 사이에 `특성 선택 단계`를 삽입한다. 특성 자체는 `TraitRegistry`/`SemionTrait`/`TraitLoadout` 도메인으로 분리하고, 실제 경기 시작 시 선택 스냅샷을 `SemionPlayer`에 복사해 이후 밸런스·통계·평점 분석에서 일관되게 참조한다.

**Tech Stack:** Java 25, Fabric 1.21.8, Brigadier commands, Minecraft Dialog UI, existing `SemionGame`/`SemionGameManager` lifecycle, Fabric GameTest, JUnit.

---

## 0. 현재 코드 기준 확인 사항

현재 `master` 기준으로 확인한 구조:

- 명령 시작점: `src/main/java/kim/biryeong/semiontd/command/SemionCommands.java`
  - `/semiontd start` → `startGame(...)`
  - `buildSelectionPlan(...)`로 ready player 기반 `ParticipantSelectionPlan` 생성
  - `gameManager.scheduleStart(server, plan)` 호출
- 시작 지연/확정: `src/main/java/kim/biryeong/semiontd/game/SemionGameManager.java`
  - `pendingStartPlan`, `startCountdownTicks`
  - `scheduleStart(...)`가 현재 5초 카운트다운 시작
  - `tickStartCountdown(...)`가 시간이 끝나면 `activeGame.start(server, plan)` 호출
- 실제 게임 시작: `src/main/java/kim/biryeong/semiontd/game/SemionGame.java`
  - `start(server, plan)`에서 팀 활성화, `activateParticipant(...)`, 보스 생성, 플레이어 배치, `startPreparePhase(...)` 수행
  - `activateParticipant(...)`에서 `SemionPlayer` 생성 후 선택된 `SemionJob` 적용
- 직업 선택 유사 UI:
  - `SemionDialogService.showJobSelection(...)`
  - `/semiontd job select <id>`
  - `/직업` alias
- 기존 확장점:
  - `SemionJob`은 시작 자원, 소환 비용/수입, 킬 보상, 라운드 이벤트 등 일부 경제/전투 훅을 이미 보유
  - `Tower`/`SemionTowerEntity`는 타워별 공격력/피해 훅을 보유

---

## 1. 용어와 정책

### 1.1 TraitSlot

```java
public enum TraitSlot {
    PRIMARY(1.0),
    SECONDARY(0.5);

    private final double effectScale;
}
```

- `PRIMARY` = 주특성, 효과 100%
- `SECONDARY` = 부특성, 효과 50%

### 1.2 TraitLoadout

```java
public record TraitLoadout(
        ResourceLocation primaryTraitId,
        ResourceLocation secondaryTraitId
) {
    public boolean complete() { ... }
    public boolean hasDuplicateNonNoneTrait() { ... }
}
```

정책 초안:

- 주특성과 부특성은 서로 다른 특성만 허용한다.
  - 같은 특성을 주/부에 동시에 넣어 150% 중첩하는 것은 금지한다.
  - 나중에 “같은 특성 중복 가능”이 필요하면 config로 열 수 있게만 고려한다.
- 선택 시간이 끝났는데 미선택 슬롯이 있으면 `semion-td:none`으로 채운다.
- `semion-td:none`은 효과 없는 기본 특성이다.
- “모두 선택”의 기준은 **active participant 전원이 주특성+부특성을 모두 선택**한 상태다. 관전자는 포함하지 않는다.

### 1.3 선택 시간

기본값:

```text
traitSelectionDurationTicks = 90 * 20
```

- 사용자 요구가 “약 1-2분”이므로 기본 90초가 적당하다.
- 추후 config에서 60/90/120초로 조정 가능하게 둔다.

---

## 2. UX 흐름

### 2.1 시작 전 자유 선택

로비가 열린 상태에서 플레이어는 언제든지 특성 UI를 열 수 있다.

명령:

```text
/semiontd trait
/semiontd trait ui
/semiontd trait select primary <id>
/semiontd trait select secondary <id>
/semiontd trait current
/특성
```

초기 구현은 `trait` 영문 하위 명령과 `/특성` 한국어 alias만 추가한다.

### 2.2 `/semiontd start` 이후 특성 선택 단계

기존 흐름:

```text
/semiontd start
→ ready player 선발
→ 5초 카운트다운
→ SemionGame.start(...)
```

변경 후 흐름:

```text
/semiontd start
→ ready player 선발
→ 선발된 active participants 확정 후보로 trait selection session 생성
→ active participants에게 특성 선택 UI 표시
→ 90초 대기 또는 전원 선택 완료
→ 짧은 5초 시작 카운트다운 또는 즉시 start
→ SemionGame.start(..., TraitSelectionSnapshot)
```

권장 MVP:

- 기존 5초 카운트다운은 유지하되, 그 앞에 90초 특성 선택 세션을 둔다.
- 전원 선택 완료 시 바로 5초 카운트다운으로 넘어간다.
- 90초가 지나면 미선택 슬롯을 `none`으로 채우고 5초 카운트다운으로 넘어간다.

### 2.3 UI 구성

특성 선택 UI 제목:

```text
세미온 TD 특성 선택
```

본문:

```text
주특성: <현재 주특성 이름 또는 미선택>
부특성: <현재 부특성 이름 또는 미선택>
남은 시간: NN초

주특성은 100%, 부특성은 50% 효과로 적용됩니다.
```

버튼 구성 MVP:

- 버튼 행 1: `주특성: <trait>` 선택 버튼들
- 버튼 행 2: `부특성: <trait>` 선택 버튼들
- 버튼 툴팁: 효과 설명 + “주특성 100% / 부특성 50%”

특성 수가 늘어나면 pagination을 추가한다. 뼈대 단계에서는 `none` + 테스트용 placeholder trait 1~2개 정도만 있어도 된다.

---

## 3. 도메인 설계

### 3.1 새 패키지

```text
src/main/java/kim/biryeong/semiontd/trait/
```

생성 예정 파일:

```text
SemionTrait.java
TraitContext.java
TraitRegistry.java
TraitSlot.java
TraitLoadout.java
TraitSelectionState.java
TraitSelectionConfig.java
TraitSelectionResult.java
BuiltInTraits.java
```

### 3.2 SemionTrait

직업 시스템과 비슷하지만, 직업을 대체하지 않는다. 특성은 직업과 독립적인 경기 전 modifier layer다.

초기 인터페이스 초안:

```java
public abstract class SemionTrait {
    private final ResourceLocation id;
    private final Component displayName;
    private final List<Component> description;

    public long modifyStartingDiamond(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifyStartingEmerald(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifyStartingIncome(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifyStartingEmeraldPerSec(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifySummonGasCost(TraitContext context, TraitSlot slot, SummonMonsterType summonType, long value) {
        return value;
    }

    public long modifySummonIncomeGain(TraitContext context, TraitSlot slot, SummonMonsterType summonType, long value) {
        return value;
    }

    public long modifyKillDiamondReward(TraitContext context, TraitSlot slot, Monster monster, long value) {
        return value;
    }

    public void onRoundStarted(TraitContext context, TraitSlot slot, int round) {
    }

    public void onRoundEnded(TraitContext context, TraitSlot slot, int round) {
    }
}
```

MVP에서는 경제/소환/라운드 훅까지만 먼저 연결한다.

전투 직접 훅, 타워 스탯 훅, 인컴 유닛 스폰 훅은 뼈대 파일에 TODO로 남기되 바로 연결하지 않는다. 이유는 타워/몬스터 엔티티 런타임 경로가 넓어서 첫 패치에서 위험도가 커진다.

### 3.3 효과 스케일링 규칙

특성 구현자가 직접 `slot.effectScale()`을 적용하는 방식은 실수 가능성이 크다. 따라서 공통 helper를 제공한다.

```java
public final class TraitScaling {
    public static long scaleDelta(long base, long modifiedAtFullPower, TraitSlot slot) {
        long delta = modifiedAtFullPower - base;
        return base + Math.round(delta * slot.effectScale());
    }

    public static double scaleDelta(double base, double modifiedAtFullPower, TraitSlot slot) {
        double delta = modifiedAtFullPower - base;
        return base + delta * slot.effectScale();
    }
}
```

예시:

```text
기본 시작 다이아 200
특성 100% 효과: +100
주특성 적용: 300
부특성 적용: 250
```

### 3.4 적용 순서

권장 순서:

```text
base config value
→ job modifier
→ primary trait modifier 100%
→ secondary trait modifier 50%
→ clamp / non-negative guard
```

이유:

- 기존 직업 밸런스를 먼저 유지한다.
- 특성은 경기 전 추가 레이어로 해석한다.
- 동일한 훅에서 적용 순서를 항상 고정해 재현성을 유지한다.

---

## 4. 라이프사이클 설계

### 4.1 SemionGameManager에 TraitSelectionSession 추가

새 필드 초안:

```java
private TraitSelectionSession pendingTraitSelection;
private ParticipantSelectionPlan pendingStartPlan;
```

혹은 더 명확하게:

```java
private TraitSelectionSession pendingTraitSelection;
private StartCountdownSession pendingStartCountdown;
```

하지만 MVP에서는 기존 `pendingStartPlan`을 크게 갈아엎지 말고 아래처럼 최소 변경한다.

```java
private TraitSelectionSession pendingTraitSelection;
private ParticipantSelectionPlan pendingStartPlan;
private int startCountdownTicks;
```

### 4.2 새 상태 흐름

```text
WAITING lobby
  └─ /semiontd start
      └─ scheduleTraitSelection(plan)
          ├─ pendingTraitSelection != null
          ├─ selected active participants locked for selection window
          ├─ selected spectators assigned spectator team as before
          └─ UI broadcast/open

TRAIT_SELECTION pending
  ├─ tickTraitSelection()
  │   ├─ all complete → finishTraitSelectionAndScheduleCountdown()
  │   └─ timeout → fill missing none → finishTraitSelectionAndScheduleCountdown()
  └─ /semiontd trait select ... allowed only for selected active participants

START_COUNTDOWN pending
  └─ existing tickStartCountdown()
      └─ SemionGame.start(server, plan, traitSnapshot)
```

### 4.3 선택 스냅샷

`TraitSelectionSession`은 timeout/complete 시 다음 스냅샷을 만든다.

```java
public record TraitSelectionSnapshot(
        Map<UUID, TraitLoadout> loadouts
) {
    public TraitLoadout loadoutOrDefault(UUID playerId) { ... }
}
```

`SemionGame.start(...)`는 snapshot을 받아 `activateParticipant(...)` 중 `SemionPlayer`에 복사한다.

```java
public boolean start(MinecraftServer server, ParticipantSelectionPlan plan, TraitSelectionSnapshot traits)
```

기존 테스트/샌드박스/호출부 호환을 위해 overload 유지:

```java
public boolean start(MinecraftServer server, ParticipantSelectionPlan plan) {
    return start(server, plan, TraitSelectionSnapshot.empty());
}
```

### 4.4 SemionPlayer에 loadout 저장

```java
private TraitLoadout traitLoadout = TraitLoadout.none();
```

접근자:

```java
public TraitLoadout traitLoadout()
public void assignTraitLoadout(TraitLoadout loadout)
```

---

## 5. Rating / telemetry 주의사항

특성이 경제/방어/인컴에 영향을 주면 나중에 ELO/기여도 분석이 왜곡될 수 있다.

따라서 첫 뼈대부터 다음을 남긴다.

- `MatchParticipantResult` 또는 `MatchStatsSnapshot`에 trait id snapshot을 포함할 수 있는 확장 지점 확보
- 최소한 `MatchResult` 생성 시 참가자별 `primaryTraitId`, `secondaryTraitId`를 직렬화할 계획을 문서화
- 실제 rating 계산은 여전히 placement/win-loss 중심으로 유지
- trait-aware contribution normalization은 후속 작업으로 분리

MVP에서는 rating 점수 보정은 하지 않는다.

---

## 6. 구현 태스크 계획

### Task 1: Trait 도메인 모델 추가

**Objective:** 특성/슬롯/로드아웃의 순수 도메인 모델을 만든다.

**Files:**

- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitSlot.java`
- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitLoadout.java`
- Create: `src/main/java/kim/biryeong/semiontd/trait/SemionTrait.java`
- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitContext.java`
- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitScaling.java`
- Test: `src/test/java/kim/biryeong/semiontd/trait/TraitLoadoutTest.java`
- Test: `src/test/java/kim/biryeong/semiontd/trait/TraitScalingTest.java`

**Acceptance criteria:**

- `PRIMARY.effectScale() == 1.0`
- `SECONDARY.effectScale() == 0.5`
- 중복 non-none trait loadout 감지 가능
- scale helper가 정수/실수 delta를 100%/50%로 정확히 적용

**Verification:**

```bash
./gradlew test --tests 'kim.biryeong.semiontd.trait.*' --console=plain
```

---

### Task 2: TraitRegistry와 기본 none 특성 추가

**Objective:** 특성을 등록/조회할 registry와 기본 `none` 특성을 만든다.

**Files:**

- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitRegistry.java`
- Create: `src/main/java/kim/biryeong/semiontd/trait/BuiltInTraits.java`
- Modify: `src/main/java/kim/biryeong/semiontd/SemionTd.java`
- Test: `src/test/java/kim/biryeong/semiontd/trait/TraitRegistryTest.java`

**Design notes:**

- `semion-td:none`은 항상 존재해야 한다.
- 개발/테스트용 placeholder trait 하나를 둘 수 있다.
  - 예: `semion-td:miner_training`
  - 효과는 테스트용 시작 다이아 +100 정도
  - 실제 정식 특성으로 노출할지는 후속 결정

**Acceptance criteria:**

- `TraitRegistry.find(NONE_ID)` 항상 present
- 중복 ID 등록 방지
- `BuiltInTraits.register()` idempotent

---

### Task 3: SemionPlayer에 TraitLoadout 스냅샷 추가

**Objective:** 경기 시작 후 플레이어가 어떤 주/부특성을 들고 시작했는지 보존한다.

**Files:**

- Modify: `src/main/java/kim/biryeong/semiontd/game/SemionPlayer.java`
- Test: existing SemionPlayer-related JVM test가 있으면 추가, 없으면 `src/test/java/kim/biryeong/semiontd/game/SemionPlayerTraitTest.java` 생성

**Acceptance criteria:**

- 기본값은 `TraitLoadout.none()`
- `assignTraitLoadout(...)` 후 `traitLoadout()`이 같은 값을 반환
- null 입력은 none으로 처리하거나 명시적으로 reject. 권장: none 처리

---

### Task 4: TraitSelectionSession 도입

**Objective:** `/semiontd start` 이후 90초 선택 창을 관리하는 세션 모델을 만든다.

**Files:**

- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitSelectionSession.java`
- Create: `src/main/java/kim/biryeong/semiontd/trait/TraitSelectionSnapshot.java`
- Test: `src/test/java/kim/biryeong/semiontd/trait/TraitSelectionSessionTest.java`

**Behavior:**

- 생성 시 `ParticipantSelectionPlan.activeParticipants()`만 선택 대상
- `select(playerId, PRIMARY, traitId)`
- `select(playerId, SECONDARY, traitId)`
- 관전자/비선발자는 선택 불가
- duplicate non-none trait 선택 불가
- `complete()`은 active participant 전원이 주/부특성 선택 시 true
- timeout 시 미선택 슬롯은 `none`

**Acceptance criteria:**

- active participant 2명이 모두 선택해야 complete
- 한 명이 부특성만 미선택이면 incomplete
- timeout snapshot은 missing slot을 none으로 채움
- spectator는 선택 불가

---

### Task 5: SemionGameManager 시작 흐름에 trait selection 삽입

**Objective:** 기존 start countdown 앞에 trait selection phase를 넣는다.

**Files:**

- Modify: `src/main/java/kim/biryeong/semiontd/game/SemionGameManager.java`
- Modify: `src/main/java/kim/biryeong/semiontd/command/SemionCommands.java`
- Test: `src/gametest/java/kim/biryeong/semiontd/gametest/SemionTraitSelectionGameTest.java`

**Implementation outline:**

- `scheduleStart(...)`를 내부적으로 `scheduleTraitSelection(...)`로 변경하거나, 새 `StartCountdownResult` 상태를 추가한다.
- `pendingTraitSelection != null`이면 기존 start 중복 방지와 동일하게 취급한다.
- `tick(...)`에서 `tickTraitSelection(server)`를 `tickStartCountdown(server)`보다 먼저 처리한다.
- 선택 완료/timeout 시 기존 `pendingStartPlan`에 plan을 넣고 5초 countdown 시작.

**Acceptance criteria:**

- `/semiontd start` 후 즉시 `SemionGame.start(...)`가 호출되지 않는다.
- trait selection timeout 전에는 `activeGame.rosterLocked() == false`
- timeout 후에는 기존처럼 `SemionGame.start(...)`가 호출된다.
- 모든 active participants가 선택하면 timeout 전이라도 countdown으로 넘어간다.

---

### Task 6: 특성 선택 명령 추가

**Objective:** 플레이어가 주특성/부특성을 명령으로 선택할 수 있게 한다.

**Files:**

- Modify: `src/main/java/kim/biryeong/semiontd/command/SemionCommands.java`
- Test: `src/gametest/java/kim/biryeong/semiontd/gametest/SemionTraitSelectionGameTest.java`

**Commands:**

```text
/semiontd trait
/semiontd trait ui
/semiontd trait current
/semiontd trait select primary <id>
/semiontd trait select secondary <id>
/특성
```

**Korean aliases inside command:**

초기 MVP에서는 root alias `/특성`만 필수로 한다. `primary/secondary`의 한국어 alias `주특성/부특성`은 후속으로 추가해도 된다.

**Acceptance criteria:**

- `/semiontd trait select primary none` parse/execute 가능
- `/semiontd trait select secondary none` parse/execute 가능
- `/특성`이 UI 명령으로 parse 가능
- selection phase가 아닐 때도 waiting lobby에서는 preselect 가능
- active game 시작 후에는 변경 불가

---

### Task 7: Dialog UI 추가

**Objective:** 기존 직업 선택 UI와 비슷한 특성 선택 UI를 제공한다.

**Files:**

- Modify: `src/main/java/kim/biryeong/semiontd/ui/SemionDialogService.java`
- Modify: `src/main/java/kim/biryeong/semiontd/command/SemionCommands.java`

**Implementation outline:**

- `showTraitSelection(ServerPlayer player, SemionGame game, TraitSelectionSession session)` 추가
- waiting lobby에서 preselect만 할 때는 session 없이 `game.pendingTraitSelectionFor(player)` 또는 manager state를 참조
- 버튼은 command action 사용:

```text
/semiontd trait select primary <id>
/semiontd trait select secondary <id>
```

**Acceptance criteria:**

- 현재 주/부특성 표시
- 주특성 100%, 부특성 50% 설명 표시
- 선택 가능한 trait 버튼 표시
- 선택 완료 후 다시 열면 선택 상태가 반영됨

---

### Task 8: Trait 효과를 시작 자원에 연결

**Objective:** 첫 실제 효과 연결은 가장 안전한 시작 자원 modifier부터 한다.

**Files:**

- Modify: `src/main/java/kim/biryeong/semiontd/game/SemionGame.java`
- Modify: `src/main/java/kim/biryeong/semiontd/game/SemionPlayer.java`
- Test: `src/test/java/kim/biryeong/semiontd/trait/TraitEconomyModifierTest.java`
- GameTest: `src/gametest/java/kim/biryeong/semiontd/gametest/SemionTraitSelectionGameTest.java`

**Integration point:**

Existing `applyJobStartingEconomy(...)` 이후:

```text
base config
→ job starting economy modifier
→ trait starting economy modifier
```

**Acceptance criteria:**

- 주특성 +100 diamond trait = +100
- 부특성 +100 diamond trait = +50
- 주+부 서로 다른 trait가 순서대로 적용됨
- none trait는 변경 없음

---

### Task 9: Trait snapshot을 MatchResult 확장 지점에 반영

**Objective:** 향후 rating/기여도 분석을 위해 참가자 trait 선택을 결과 데이터에 남긴다.

**Files:**

- Modify: `src/main/java/kim/biryeong/semiontd/progression/MatchParticipantResult.java` 또는 현재 result model 위치
- Modify: `src/main/java/kim/biryeong/semiontd/game/SemionGame.java`의 `matchResult()`
- Test: 관련 JVM/GameTest result test

**Acceptance criteria:**

- match result participant에 primary/secondary trait id가 포함됨
- 기존 저장소 JSON은 backfill 가능해야 함
- 과거 결과에 trait field가 없어도 로드 실패하지 않음

**주의:** 이 task는 저장소 호환성 영향이 있으므로 별도 커밋으로 분리한다.

---

### Task 10: GameTest 통합 검증

**Objective:** 실제 Fabric runtime에서 명령/선택/timeout/시작 흐름을 검증한다.

**Files:**

- Create: `src/gametest/java/kim/biryeong/semiontd/gametest/SemionTraitSelectionGameTest.java`
- Modify if needed: `src/gametest/resources/fabric.mod.json`

**Required GameTests:**

1. `traitSelectionDelaysMatchStartUntilTimeout`
   - `/semiontd start` 이후 바로 roster lock 되지 않음
   - timeout tick 후 start 됨
2. `traitSelectionStartsCountdownEarlyWhenAllActivePlayersComplete`
   - active participants 모두 주/부특성 선택
   - timeout 전 countdown으로 전환
3. `traitSelectionIgnoresSpectatorsForCompletion`
   - spectator 미선택이어도 active participants만 완료하면 진행
4. `primaryAndSecondaryTraitScaleStartingEconomy`
   - 주특성 100%, 부특성 50% 확인
5. `traitSelectionRejectsDuplicateNonNoneTraits`
   - 같은 특성을 주/부에 선택 시 실패
6. `traitSelectionCommandTreeRegistersKoreanAlias`
   - `/특성` parse 확인

**Verification:**

```bash
./gradlew test --tests 'kim.biryeong.semiontd.trait.*' compileGametestJava --console=plain
JAVA_TOOL_OPTIONS='-Dfabric-api.gametest.filter=semion-td-gametest:semion_trait_selection_game_test_* -Dfabric-api.gametest.report-file=/tmp/semion-trait-selection.xml' ./gradlew runGameTest --console=plain
/usr/bin/python3 - <<'PY'
import xml.etree.ElementTree as ET
root=ET.parse('/tmp/semion-trait-selection.xml').getroot()
print('tests', sum(1 for _ in root.iter('testcase')), 'failures', sum(1 for tc in root.iter('testcase') for _ in tc.findall('failure')))
PY
```

---

## 7. MVP 범위와 비범위

### MVP에 포함

- Trait domain/registry/loadout
- 주특성 100%, 부특성 50% scaling 규칙
- 경기 시작 전 선택 session
- timeout 또는 전원 선택 시 진행
- 명령 + Dialog UI 뼈대
- 시작 자원 modifier 하나 이상 연결
- trait snapshot 저장 확장 지점
- JVM + GameTest coverage

### MVP에 포함하지 않음

- 정식 특성 여러 개 추가
- 전투/타워/인컴 유닛 전체 modifier 연결
- trait 기반 rating 보정
- trait unlock/progression
- trait persistence across sessions
- 복잡한 추천/자동 선택 UI

---

## 8. 첫 구현에서 추천하는 테스트용 특성

정식 밸런스 특성이 아니라 뼈대 검증용으로만 둔다.

```text
semion-td:none
- 이름: 선택 안 함
- 효과: 없음

semion-td:starter_mineral_training
- 이름: 채굴 훈련
- 100% 효과: 시작 다이아 +100
- 50% 효과: 시작 다이아 +50
```

이 특성 하나만으로 주/부특성 scaling, 중복 선택 금지, none fallback, 시작 자원 적용을 모두 검증할 수 있다.

---

## 9. 예상 리스크

1. **시작 흐름 복잡도 증가**
   - 기존 `pendingStartPlan`/`startCountdownTicks`에 trait selection state가 섞이면 버그가 나기 쉽다.
   - 해결: `TraitSelectionSession`을 별도 객체로 분리하고, 완료 후 기존 countdown으로 넘긴다.

2. **선발된 active participant와 UI 대상 불일치**
   - ready player 전체가 아니라 실제 selected active participants만 완료 조건에 포함해야 한다.
   - 관전자는 선택하지 않아도 진행되어야 한다.

3. **timeout fallback 누락**
   - 미선택 플레이어 때문에 게임이 영원히 시작하지 않는 상황을 막아야 한다.
   - timeout 시 `none` 자동 채움 필수.

4. **rating/기여도 왜곡**
   - trait가 경제/방어에 영향을 주면 match stats 해석이 달라진다.
   - 첫 구현부터 trait snapshot을 결과에 남길 준비를 해야 한다.

5. **Dialog UI와 command drift**
   - UI 버튼 command와 Brigadier command tree가 어긋나면 클릭이 실패한다.
   - GameTest에서 실제 command parse/execute를 검증한다.

---

## 10. 권장 커밋 분리

```text
feat(trait): add trait domain and registry
feat(trait): add pre-match trait selection session
feat(command): add trait selection commands and ui
feat(trait): apply trait loadouts to starting economy
test(trait): add trait selection gametests
```

저장소/MatchResult 호환성 변경이 들어가면 별도 커밋:

```text
feat(result): snapshot trait loadouts in match results
```

---

## 11. 구현 전 결정이 필요한 질문

1. 같은 특성을 주특성/부특성에 동시에 선택하는 것을 영구 금지할지, config로 허용할지?
   - 기획안 권장: MVP에서는 금지.
2. 선택 시간 기본값을 90초로 할지 120초로 할지?
   - 기획안 권장: 90초.
3. timeout 시 미선택 슬롯을 `none`으로 할지, 랜덤 추천 특성으로 할지?
   - 기획안 권장: `none`.
4. trait 선택을 프로필에 저장해서 다음 로비에도 자동 적용할지?
   - 기획안 권장: MVP에서는 경기별 선택만. persistence는 후속.
5. 첫 정식 trait를 경제형으로 할지, 타워형으로 할지?
   - 기획안 권장: 뼈대 검증 후 별도 밸런스 기획에서 결정.
