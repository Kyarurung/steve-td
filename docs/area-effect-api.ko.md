# 범위 효과 API

Semion TD는 피해 AOE, 시체 폭발, 버프·디버프, 회복 오라를 서버 전용 API로 제공합니다. API는 별도 아티팩트가 아니라 Semion TD 모드 JAR의 `kim.biryeong.semiontd.api.area` 패키지에 들어 있습니다.

## 진입점

- `SemionTdApi.areaEffects()`: 몬스터와 타워 대상 범위 효과를 실행합니다.
- `SemionTdApi.areaVfxStyles()`: 애드온 VFX 스타일을 등록하거나 조회합니다.

범위 검색과 효과 적용은 Minecraft 서버 메인 스레드에서만 실행할 수 있습니다. 작업 스레드에서 호출하면 `IllegalStateException`이 발생합니다. 중심 좌표가 유한하지 않거나 반경이 `0` 이하이면 요청 생성 시 `IllegalArgumentException`이 발생합니다.

소스 타워의 활성 경기나 라인을 찾지 못하면 빈 `AreaEffectResult`를 반환합니다.

## 몬스터 범위 효과

`applyToMonsters(...)`는 살아 있는 Semion 몬스터 중 소스 타워가 방어하는 라인의 대상만 검색합니다. 사각형 검색 뒤 구형 거리 판정을 적용합니다.

```java
MonsterAreaEffectRequest request = new MonsterAreaEffectRequest(
        ResourceLocation.fromNamespaceAndPath("example", "fire_burst"),
        sourceTower,
        target.position(),
        2.5,
        Set.of(target.getUUID()),
        monster -> !monster.isInvulnerable(),
        AreaVfxSpec.onTrigger(AreaVfxStyles.SPLASH)
);

AreaEffectResult<SemionMonsterEntity> result = SemionTdApi.areaEffects()
        .applyToMonsters(request, monster -> {
            boolean killed = runtimeTower.damageTarget(sourceTower, monster, 12.0);
            return killed ? AreaEffectOutcome.KILLED : AreaEffectOutcome.APPLIED;
        });
```

`excludedTargetIds`는 기본 공격 대상을 스플래시에서 빼는 경우에 사용합니다. `targetFilter`는 역할, 체력, 표식처럼 능력 고유 조건을 추가합니다. `null` 필터는 모든 대상을 허용합니다.

액션은 대상별 결과를 반환해야 합니다.

| 결과 | 의미 |
|---|---|
| `UNCHANGED` | 대상 상태가 바뀌지 않았습니다. 적용 수와 VFX 적중 위치에 포함하지 않습니다. |
| `APPLIED` | 피해, 회복, 버프 또는 디버프가 실제로 적용됐습니다. |
| `KILLED` | 효과로 대상이 사망했습니다. 적용 수와 처치 수에 모두 포함합니다. |

API는 `Tower.onKill(...)`을 자동으로 호출하지 않습니다. 처치 전파가 필요한 피해는 액션 안에서 호출해야 합니다. 라클 캣 시체 폭발처럼 연쇄 처치를 막아야 하는 효과는 피해만 적용하고 `onKill(...)`을 호출하지 않습니다.

## 타워 범위 효과

`applyToTowers(...)`는 소스와 같은 소유자·팀·라인의 살아 있는 Semion 타워만 검색합니다.

| 대상 모드 | 검색 대상 |
|---|---|
| `REGISTERED` | `PlayerLane`에 등록된 타워입니다. 엔티티가 없는 런타임 타워도 포함할 수 있습니다. |
| `ENTITIES` | 월드에 존재하는 Semion 타워 엔티티입니다. 등록 타워와 환영을 모두 포함합니다. |
| `REGISTERED_AND_CLONES` | 등록 타워에 월드의 환영을 추가합니다. |

```java
TowerAreaEffectRequest request = new TowerAreaEffectRequest(
        ResourceLocation.fromNamespaceAndPath("example", "healing_aura"),
        sourceTower,
        sourceTower.position(),
        4.0,
        TowerAreaTargetMode.REGISTERED,
        false,
        target -> target.tower().health() < target.tower().maxHealth(),
        AreaVfxSpec.onChange(AreaVfxStyles.BUFF)
);

SemionTdApi.areaEffects().applyToTowers(request, target -> {
    double before = target.tower().health();
    target.tower().syncHealth(Math.min(target.tower().maxHealth(), before + 6.0));
    return target.tower().health() > before
            ? AreaEffectOutcome.APPLIED
            : AreaEffectOutcome.UNCHANGED;
});
```

`AreaTowerTarget`은 런타임 `Tower`, 선택적인 `SemionTowerEntity`, 환영 여부를 제공합니다. `includeSource`가 `false`이면 소스 타워를 제외합니다.

## 결과 스냅샷

`AreaEffectResult<T>`에는 다음 값이 들어 있습니다.

- `candidateCount`: 거리와 필터를 통과해 액션을 실행한 대상 수
- `hits`: `APPLIED` 또는 `KILLED`를 반환한 대상, 적용 전 위치, 결과
- `appliedCount`: 상태가 바뀐 대상 수
- `killedCount`: `KILLED`를 반환한 대상 수

`AreaEffectHit.position()`은 액션 실행 전 위치입니다. VFX 작업 스레드로는 이 위치와 집계값만 넘어가며 엔티티나 월드 참조는 전달하지 않습니다.

액션에서 발생한 예외는 호출자에게 그대로 전달됩니다.

## VFX 표시 정책

`AreaVfxSpec`은 스타일 ID와 표시 정책을 묶습니다.

- `AreaVfxSpec.onTrigger(style)`: 적용 대상이 없어도 능력이 발동하면 표시합니다. 피해 AOE와 시체 폭발에 사용합니다.
- `AreaVfxSpec.onChange(style)`: 액션이 `APPLIED` 또는 `KILLED`를 한 번 이상 반환할 때만 표시합니다. 버프·디버프·회복에 사용합니다.
- `AreaVfxSpec.none()`: VFX를 만들지 않습니다.

버프와 디버프에서 지속 시간만 갱신할 때는 액션이 `UNCHANGED`를 반환해야 합니다. 수치가 새로 적용되거나 달라졌을 때만 `APPLIED`를 반환하면 오라가 매 tick 반복 표시되지 않습니다.

내장 스타일:

- `AreaVfxStyles.SPLASH`: 대상 중심 범위 피해
- `AreaVfxStyles.PULSE`: 타워 중심 범위 피해
- `AreaVfxStyles.CORPSE_EXPLOSION`: 처치 위치 중심 폭발
- `AreaVfxStyles.BUFF`: 강화 또는 회복 범위
- `AreaVfxStyles.DEBUFF`: 약화 범위

내장 스타일은 밝은 바닐라 `FLASH` 파티클을 사용하지 않습니다.

## 애드온 VFX 스타일

스타일은 애드온의 `ModInitializer`에서 등록합니다. Semion TD 초기화 뒤부터 서버가 시작되기 전까지만 등록할 수 있습니다. 같은 ID를 두 번 등록하거나 서버 시작 뒤 등록하면 `IllegalStateException`이 발생합니다.

```java
ResourceLocation styleId = ResourceLocation.fromNamespaceAndPath("example", "shockwave");

SemionTdApi.areaVfxStyles().register(styleId, (context, output) -> {
    output.circle(
            context.palette().primary(),
            context.center(),
            context.radius(),
            48,
            true
    );
    output.sphere(
            context.palette().accent(),
            context.center().add(0.0, 0.25, 0.0),
            context.radius() * 0.6,
            32,
            false
    );
});
```

플래너는 불변 `AreaVfxContext`와 제한된 `AreaVfxOutput`만 받습니다. 출력 도형은 `line`, `circle`, `sphere`, `trail`입니다. 플래너에서 월드나 엔티티를 다시 조회하지 않습니다.

파티클은 `AreaVfxParticle`로 지정합니다. 일반 클라이언트용 `ParticleOptions`와 GCB 클라이언트용 파티클 ID를 함께 넣어야 합니다. 공개 API는 원시 packet 전송을 제공하지 않습니다.

잘못된 좌표, 반경, point 수를 가진 도형은 생략됩니다. 플래너 예외는 해당 VFX만 버리고 로그와 통계에 기록합니다. 애드온 스타일도 `vfx.json`의 라인 큐, point 버킷, GCB 명령 수, 수신자 제한을 그대로 적용받습니다.
