# 특성 시스템 구현 현황과 콘텐츠 추가 순서

> **기준일:** 2026-07-17  
> **상태:** 시스템 기반 구현 완료, 실전 특성 기획 대기  
> **관련 문서:** [특성 선택 뼈대 기획안](./2026-06-10-trait-selection-foundation.ko.md), [특성 콘텐츠 기획](./2026-07-18-trait-content-design.ko.md), [직업 통계](../job-statistics.ko.md), [설정 파일](../config-reference.ko.md)

## 현재 운영 상태

특성 선택, 경기 스냅샷, 결과 저장, 통계 집계와 인게임 조회까지 공통 기반이 연결돼 있다. 현재 등록된 특성 콘텐츠는 효과가 없는 `semion-td:none`뿐이다.

- `traits.json`의 기본값은 `enabled: true`, 선택 제한 시간은 45초다.
- `none` 외의 특성이 없으면 선택 단계를 자동으로 건너뛰고 기존 5초 경기 시작 카운트다운으로 진행한다.
- 첫 실전 특성을 등록하면 별도 기능 플래그 수정 없이 참가자 확정 후 선택 단계가 열린다.
- 운영 중 전체 특성을 끄려면 `traits.json`의 `enabled`를 `false`로 바꾸고 `/semiontd reload`를 실행한다.
- 비활성 경기와 이전 경기 기록은 모두 `(none v0, none v0)`으로 남는다.

## 완성된 공통 기반

### 선택 흐름

```text
로비에서 미리 선택 가능
→ /semiontd start
→ 참가자, 팀, 라인 확정
→ 주특성과 부특성 최종 선택
→ 전원 완료 또는 제한 시간 종료
→ 미선택 슬롯을 none으로 확정
→ 참가자별 ID와 버전 스냅샷 잠금
→ 5초 카운트다운
→ 경기 시작
```

- 선택 대상은 실제 참가자뿐이며 관전자는 완료 판정에 포함하지 않는다.
- 같은 `none`이 아닌 특성을 주특성과 부특성에 동시에 넣을 수 없다.
- 제한 시간이 끝나면 미선택 슬롯만 `none`으로 채운다.
- 카운트다운 이후에는 선택을 바꿀 수 없다.
- 선택 중 재접속한 참가자에게 현재 선택과 남은 시간을 다시 표시한다.
- `enabled: false`이거나 선택 가능한 실전 특성이 없으면 선택 단계를 만들지 않는다.

### 결과 스냅샷

각 `MatchParticipantResult`에 다음 값을 고정해 저장한다.

```text
jobId
primaryTraitId
primaryTraitVersion
secondaryTraitId
secondaryTraitVersion
finalTowerComposition[]
attemptedRounds[]
clearedRounds[]
finalRound
placement
winner
```

특성 이름이나 현재 코드를 나중에 다시 읽어 과거 경기를 해석하지 않는다. 같은 특성의 효과를 변경하면 버전을 올려 과거 기록과 분리한다.

`finalTowerComposition`은 경기 종료 직전 살아 있는 타워를 다음 형태로 묶는다.

```text
towerTypeId
tier
count
```

패배 팀은 타워를 제거하기 직전에 구성을 보존하고, 승리 팀은 결과 생성 시점의 살아 있는 타워를 읽는다. 설치, 업그레이드, 판매 순서 전체는 첫 버전 범위에 포함하지 않는다.

### 조합 통계

기본 조합 키는 다음과 같다.

```text
(jobId, primaryTraitId, primaryTraitVersion,
        secondaryTraitId, secondaryTraitVersion)
```

조합마다 다음 값을 계산한다.

- 선택 수와 선택률, 승리 수와 승률
- 평균 순위와 평균 최종 라운드
- R1~R40 시도 수, 통과 수와 통과율
- 최종 타워 종류와 티어별 등장 참가자 수, 총수량과 경기당 평균 수량

`job_stat_participant_facts`에 특성 ID와 버전을 저장하고, 최종 타워는 `job_stat_participant_towers`에 저장한다. 조합 통계는 이 사실 테이블을 SQL `GROUP BY`로 조회하므로 별도의 중복 집계 테이블이 없다. 같은 경기 결과를 다시 처리해도 `(match_id, player_id)` 기본 키로 중복되지 않는다.

`/semiontd job stats <빌더>` 상세 화면에서 표본이 많은 특성 조합 8개와 주요 최종 타워를 확인할 수 있다.

## 실전 특성 추가 순서

특성 기획이 확정되면 특성 하나마다 다음 작업만 수행한다.

1. 고유 ID와 버전, 표시 이름, 설명을 가진 `SemionTrait` 구현을 추가한다.
2. `BuiltInTraits.register()`에서 해당 구현을 등록한다.
3. 필요한 기존 훅만 재정의한다.
4. 주특성 100%, 부특성 50% 적용값과 중첩 순서를 단위 테스트로 고정한다.
5. 전체 단위 테스트, Fabric GameTest와 `remapJar`를 실행한다.

현재 공통 훅은 시작 다이아, 에메랄드, 인컴, 초당 에메랄드 보정과 라운드 시작 및 종료 이벤트다. 기획된 효과가 이 범위를 벗어날 때만 그 효과에 필요한 최소 훅을 공통 기반에 추가한다. 예정된 특성을 위해 전투, 타워 AI, 투사체, 팀 버프 훅을 미리 만들지 않는다.

특성의 실제 효과가 바뀌어 기존 경기와 직접 비교하면 안 되는 경우 `SemionTrait`의 버전을 올린다. ID는 같은 개념의 특성인 동안 유지한다.

## 콘텐츠 추가 전 확인 목록

- 표시 설명과 실제 계산이 같은 수치를 쓰는가
- 주특성과 부특성의 적용 비율이 테스트로 고정됐는가
- `none`과 조합했을 때 부작용이 없는가
- 같은 특성을 두 슬롯에 넣을 수 없는가
- 특성 버전이 결과와 조합 통계에 분리돼 보이는가
- 비활성 설정에서 기존 경기 시작 흐름이 유지되는가

## 검증 명령

```bash
./gradlew test --console=plain
./gradlew runGameTest --console=plain
./gradlew remapJar --console=plain
```

## 아직 포함하지 않는 범위

- 실제 효과가 있는 정식 특성 콘텐츠
- 설치, 업그레이드, 판매 순서 전체 이벤트 로그
- 라운드별 타워 구성 체크포인트
- 특성별 자동 밸런스 조정
- 다중 서버 통계 병합
- 외부 웹 통계 API

이 항목들은 현재 선택·저장·통계 기반을 완성하는 데 필요하지 않으며, 확정된 특성 기획이나 실제 운영 질문이 생겼을 때 추가한다.
