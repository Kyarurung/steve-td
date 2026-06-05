# 벌 타워 무리 빌더 리워크 기획안

**목표:** 주석 처리된 BEE 타워를 단순 독/DOT 타워가 아니라, 무리 빌더의 핵심 정체성인 “혼자 약하고 같이 강한 군집 압박”에 맞는 시너지 타워로 재설계한다.

**현재 문제:** 기존 벌 타워는 공격 대상에게 독침/독 스택을 누적하고, 같은 벌 타워 수에 따라 독 피해와 최대 스택이 증가하는 구조였다. 이는 소재상 벌에는 맞지만, 무리 빌더의 플레이 감각보다는 독/디버프 빌더 또는 안티탱커 타워에 가깝다.

---

## 1. 최종 컨셉

벌 타워는 직접 딜이 강한 타워가 아니라, 주변 무리 빌더 타워와 분신들이 만들어내는 공격 횟수를 하나의 벌집 신호로 묶는 **군집 시너지 타워**로 간다.

한 줄 요약:

> 벌 타워는 혼자 약하지만, 주변 아군 무리 타워가 많을수록 공격 반응이 잦아지고 강해지는 벌집 코어다.

무리 빌더 내 역할 분담:

| 타워 | 역할 |
|---|---|
| 닭 | 저가 분신, 분기 업그레이드 |
| 슬라임 | 분신 + 재생, 끈질긴 물량 |
| 펭귄 | 다수 분신 + 스플래시 |
| 앵무 | 공격할수록 자기 강화 |
| 환술 | 사망 시 전역 분신 폭발 |
| 벌 | 주변 무리 전체의 공격을 증폭하는 군집 시너지 |

---

## 2. 핵심 메커니즘

### 벌집 표식

벌 타워가 공격한 대상에게 `벌집 표식`을 남긴다.

### 지원 침

`벌집 표식`이 찍힌 대상이 아군 무리 타워에게 피격되면, 벌 타워가 추가 피해를 준다.

중요한 점은 독처럼 시간이 지나면 자동으로 피해가 들어가는 것이 아니라는 점이다.

> 아군 무리가 때릴수록 벌떼가 같이 몰려든다.

이 구조는 닭/슬라임/펭귄 분신처럼 공격 개체 수가 늘어나는 무리 빌더 타워와 자연스럽게 맞물린다.

### 군집 밀도

주변 일정 범위 내 아군 무리 타워 수를 센다.

```text
군집 스택 = 주변 아군 무리 타워 수, 최대 N
```

군집 스택에 따라 지원 침 피해가 증가하고 발동 쿨다운이 감소한다.

---

## 3. 티어별 설계

## T1: 정찰 벌 타워

### 역할

초반 저가 시너지 타워. 자체 딜은 낮지만, 아군 무리 타워가 같은 대상을 공격할 때 작은 추가 피해를 제공한다.

### 능력

- 공격 시 대상에게 벌집 표식 부여
- 표식 지속시간: 3~4초
- 표식 대상이 아군 무리 타워에게 피격되면 지원 침 피해
- 대상별 발동 쿨다운 존재

### 밸런스 초안

```text
cost: 80
health: 28
range: 7
damage: 3
attackIntervalTicks: 18
markDurationTicks: 80
assistDamage: 2.0
assistCooldownTicks: 12
nearbyRadius: 5.0
maxNearbySwarmTowers: 3
```

### 설명문 초안

```text
<gray>혼자서는 약하지만, 무리의 공격 신호를 따라 벌떼가 달려듭니다.</gray>
<green>공격 시 대상에게 {ability.markDurationTicks:seconds} 동안 벌집 표식을 남깁니다.</green>
<green>표식 대상이 아군 무리 타워에게 피격되면 지원 침 피해를 줍니다.</green>
```

---

## T2: 벌집 타워

### 역할

미드게임 군집 시너지 코어. 주변 무리 타워 수가 많을수록 지원 침이 강해지고 자주 발동한다.

### 능력 추가

- 주변 아군 무리 타워 수에 따라 지원 침 피해 증가
- 주변 아군 무리 타워 수에 따라 발동 쿨다운 감소

### 밸런스 초안

```text
cost: 160
health: 36
range: 7
damage: 5
attackIntervalTicks: 17
markDurationTicks: 100
assistDamage: 3.5
assistDamagePerNearbyTower: 0.75
assistCooldownTicks: 12
cooldownReductionPerNearbyTower: 1
minAssistCooldownTicks: 7
nearbyRadius: 5.5
maxNearbySwarmTowers: 5
```

### 설명문 초안

```text
<gray>주변 무리 타워의 공격 신호를 모아 벌떼를 더 빠르게 출격시킵니다.</gray>
<green>표식 대상이 아군 무리 타워에게 피격되면 지원 침 피해를 줍니다.</green>
<green>주변 아군 무리 타워마다 지원 침 피해가 증가하고 발동 간격이 감소합니다.</green>
```

---

## T3: 여왕벌 타워

### 역할

후반 군집 코어. 무리가 집중 공격하는 대상을 벌떼의 사냥감으로 지정한다.

### 능력 추가: 여왕의 호출

표식 대상이 짧은 시간 안에 여러 번 아군 무리 타워에게 피격되면 `벌떼 집중` 상태가 발동한다.

`벌떼 집중` 중에는:

- 지원 침 피해 증가
- 지원 침 발동 쿨다운 감소
- 표식 지속시간 갱신
- 대상별 재발동 쿨다운 적용

### 밸런스 초안

```text
cost: 310
health: 48
range: 8
damage: 7
attackIntervalTicks: 16
markDurationTicks: 120
assistDamage: 5.0
assistDamagePerNearbyTower: 1.0
assistCooldownTicks: 10
cooldownReductionPerNearbyTower: 1
minAssistCooldownTicks: 5
nearbyRadius: 6.0
maxNearbySwarmTowers: 6
focusTriggerHits: 5
focusDurationTicks: 60
focusDamageMultiplier: 1.5
focusTargetCooldownTicks: 160
```

### 설명문 초안

```text
<gray>여왕벌은 무리가 집중 공격하는 대상을 벌떼의 사냥감으로 지정합니다.</gray>
<green>표식 대상이 여러 번 피격되면 벌떼 집중이 발동합니다.</green>
<green>벌떼 집중 중 지원 침 피해와 발동 빈도가 증가합니다.</green>
```

---

## 4. 기존 구현에서 버릴 것과 살릴 것

### 버릴 것

기존 독/DOT 중심 키는 제거하거나 더 이상 핵심 능력으로 쓰지 않는다.

```text
poisonDamagePerStack
maxPoisonStacks
poisonDurationTicks
poisonTickIntervalTicks
```

이 구조는 벌 소재에는 맞지만, 무리 빌더의 “공격 개체 수와 상호작용하는 재미”를 만들기 어렵다.

### 살릴 수 있는 방향

기존 `swarm stack` 아이디어는 유지하되, 의미와 이름을 바꾼다.

```text
maxSwarmStacks              -> maxNearbySwarmTowers
poisonDamagePerSwarmStack   -> assistDamagePerNearbyTower
poisonStacksPerSwarmStack   -> cooldownReductionPerNearbyTower
```

권장 config 키:

```text
markDurationTicks
baseAssistDamage
assistDamagePerNearbyTower
baseAssistCooldownTicks
cooldownReductionPerNearbyTower
minAssistCooldownTicks
nearbyRadius
maxNearbySwarmTowers
focusTriggerHits
focusDurationTicks
focusDamageMultiplier
focusTargetCooldownTicks
```

---

## 5. 구현 권장 방식

### 추천: 타워 공격 이벤트 훅 추가

`BeeTower`가 정확히 “아군 무리 타워가 표식 대상을 때렸는지” 알아야 한다. 단순 tick 감시로 대상 체력 변화를 관찰하면 자연 웨이브 피해, 다른 빌더 피해, 기존 DOT 피해까지 섞일 수 있다.

따라서 모든 타워 공격 후에 통지되는 작은 이벤트 훅을 추가하는 것이 좋다.

예시:

```java
TowerAttackEvents.afterDamage(lane, attackerTower, target, damageAmount);
```

벌 타워는 같은 lane의 공격 이벤트를 받아 다음을 확인한다.

```text
1. 대상이 내가 표식 찍은 몬스터인가?
2. 공격자가 내 소유자의 무리 타워인가?
3. 공격자가 벌 타워 자신은 아닌가?
4. 내부 쿨다운이 끝났는가?
5. 조건을 만족하면 지원 침 발동
```

장점:

- 컨셉과 로직이 정확함
- 다른 빌더나 자연 피해에 오발동하지 않음
- JVM 테스트와 GameTest가 명확해짐
- 향후 공격 반응형 서포트 타워에도 재사용 가능

---

## 6. MVP 범위

1차 구현은 아래까지만 권장한다.

```text
- 독 DOT 제거
- 벌집 표식 추가
- 표식 대상이 아군 무리 타워에게 피격될 때 지원 침 발동
- 주변 아군 무리 타워 수로 피해/쿨다운 보정
- T3는 여왕의 호출까지 넣거나, 일정상 T2 강화형으로 시작
```

1차에서 제외할 것:

```text
- 실제 벌 분신 엔티티 소환
- 체인 라이트닝식 다중 대상 튐
- 전역 오라
- 이동속도 감소/방어 감소/중독 같은 범용 디버프
```

---

## 7. 테스트 포인트

### JVM 테스트

- 벌집 표식이 공격 시 생성되고 지속시간이 갱신되는지
- 표식 대상이 아군 무리 타워에게 피격될 때만 지원 침이 발동하는지
- 다른 빌더/다른 플레이어/자기 자신 공격에는 발동하지 않는지
- 주변 무리 타워 수에 따라 피해와 쿨다운이 config 값대로 계산되는지
- T3 벌떼 집중이 hit count와 재발동 쿨다운을 지키는지
- description/template이 config 값을 동적으로 렌더링하는지

### GameTest

- 벌 타워가 실제 build UI에 다시 노출되는지
- 벌 타워 + 닭/슬라임/펭귄 조합에서 표식 대상 피격 시 지원 침 피해가 들어가는지
- 단독 벌 타워는 지원 침이 거의 발동하지 않아 단독 DPS가 낮은지
- config override가 runtime tower 능력에 반영되는지

---

## 8. 최종 판단

벌 타워는 독 DOT 안티탱커보다, 무리 빌더 전용 **집단 공격 반응형 시너지 타워**가 더 잘 맞는다.

최종 라인:

```text
T1 정찰 벌 타워
T2 벌집 타워
T3 여왕벌 타워
```

최종 핵심:

```text
벌집 표식: 벌 타워가 적에게 표식을 남긴다.
지원 침: 표식 대상이 아군 무리 타워에게 피격되면 추가 피해를 준다.
군집 밀도: 주변 아군 무리 타워 수에 따라 지원 침 피해/발동 빈도가 증가한다.
여왕의 호출: T3는 같은 표식 대상이 여러 번 피격되면 짧은 강화 상태를 만든다.
```

이렇게 가면 벌 타워는 “또 하나의 독 딜러”가 아니라, 무리 전체의 공격을 한 대상에게 결집시키는 벌집 코어가 된다.
