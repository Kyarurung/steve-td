# Semion TD

Semion TD는 Minecraft `1.21.8` Fabric 서버에서 실행하는 서버 전용 타워 디펜스 미니게임입니다. 플레이어는 빌더를 고르고, 라인에 타워를 배치하며, 웨이브와 인컴 유닛을 버팁니다.

## 요구 환경

기준 버전은 `gradle.properties`와 `fabric.mod.json`입니다.

- Minecraft `1.21.8`
- Java `21` 이상 (`25.0.2`에서 검증)
- Fabric Loader `0.19.2` 이상
- Fabric API
- [APEL](https://modrinth.com/mod/apel/version/mc1.21.8-0.1.5) `0.1.5+1.21.8` (서버 필수, 클라이언트 선택)
- Polymer: `polymer-core`, `polymer-networking`, `polymer-resource-pack`, `polymer-virtual-entity`
- BIL, Placeholder API, Sidebar API, FactoryTools
- Friends & Foes, Flowery Mooblooms, 각 Polymer patch

개발용 빌드는 Gradle Loom을 사용합니다. 배포 전에는 단위 테스트, GameTest, remap JAR 생성을 한 번에 실행합니다.

```bash
./gradlew test runGameTest remapJar
```

배포 파일은 `build/libs/semion-td-1.0-SNAPSHOT+1.21.8.jar`입니다.

## 빠른 길잡이

- [서버 유지보수 인수인계](docs/next-session-handoff.ko.md): 빌드, 배포, 백업, 복구, 장애 확인 순서입니다.
- [서비스 준비 체크리스트](docs/service-readiness-checklist.ko.md): 운영 전 서버 및 실클라이언트 확인 항목입니다.
- [빌더와 타워](docs/builders-and-towers.ko.md): 현재 등록된 빌더와 계열별 타워 흐름입니다.
- [설정 파일](docs/config-reference.ko.md): `config/semion-td/*.json` 자동 생성 파일과 운영 데이터 구분입니다.
- [타워 수치 설정](docs/tower-balance-reference.ko.md): `tower_balance.json`의 공통 수치, 업그레이드 가격, 고유 능력값입니다.
- [명령어](docs/command-reference.ko.md): 플레이어용, 관리자용, 빌드 기록용, 내부/디버그용 명령어입니다.
- [직업 통계](docs/job-statistics.ko.md): 직업별 선택률, 승률, 1~40라운드 통과율, 전투·인컴·라인 지표와 SQLite 조회 기준입니다.
- [프로덕션 타워 카탈로그](docs/production-tower-catalog.ko.md): 새 타워를 코드에 등록할 때 보는 개발 문서입니다.
- [범위 효과 API](docs/area-effect-api.ko.md): 애드온에서 Semion 몬스터·타워 대상 범위 효과와 VFX 스타일을 추가하는 방법입니다.

## 운영 흐름

관리자가 먼저 볼 명령어는 다음 순서입니다.

1. `/semiontd create`: 로비와 아레나를 생성합니다.
2. `/semiontd ready`: 플레이어가 참가 준비를 표시합니다. 한국어 alias는 `/준비`입니다.
3. `/semiontd start`: 준비된 플레이어로 게임을 시작합니다.
4. `/semiontd reset`: 진행 중인 게임을 리셋하고 로비로 돌립니다.
5. `/semiontd reload`: 설정 파일과 타워 카탈로그를 다시 불러옵니다.
6. `/semiontd rating softreset`: ELO 데이터를 백업한 뒤 소프트 리셋합니다. 같은 관리자가 30초 안에 두 번 입력해야 실행됩니다.

## 설정 위치

서버 실행 후 설정 파일은 `config/semion-td/` 아래에 생성됩니다. `economy.json`, `wave.json`, `map.json`, `progression.json`, `rating.json`, `persistence.json`, `tower_balance.json`, `summons.json`, `leader_targeting.json`, `income_lane_routing.json`, `monster_scaling.json`, `vfx.json`, `tips.json`이 코드 기준 설정 파일입니다. 개인 스카이박스 PNG는 `config/semion-td/skyboxes/`에 추가합니다.

`cosmetics.json`, `profiles.json`, `build_guides.json`, `semiontd.db`, `job-statistics.db`는 운영 데이터입니다. 치장 상품은 주 손 아이템을 든 뒤 `/semiontd cosmetic add <id> <price> [head|offhand]`로 등록합니다. 슬롯을 생략하면 `head`를 사용합니다. 운영 데이터는 직접 수정하기보다 명령어와 서버 백업으로 관리합니다.

## 운영 배포

현재 서버 루트는 `~/Desktop/SemionTd`입니다. `start.sh`는 서버 종료 뒤 5초 후 Java 프로세스를 다시 실행하므로, 유지보수할 때는 Java 자식 프로세스와 함께 실행 스크립트도 중지합니다.

1. 서버를 중지하고 `config/semion-td/`와 기존 mod JAR을 백업합니다.
2. `./gradlew test runGameTest remapJar`를 통과시킵니다.
3. 생성된 JAR을 `~/Desktop/SemionTd/mods/semion-td-1.0-SNAPSHOT+1.21.8.jar`로 복사합니다.
4. 서버를 시작한 뒤 `Semion TD initialized.`와 Polymer 리소스팩 생성 성공 로그를 확인합니다.
5. `semiontd status`, `semiontd create`, `semiontd reset`으로 기본 복구 흐름을 확인합니다.

세부 절차와 롤백 방법은 [서버 유지보수 인수인계](docs/next-session-handoff.ko.md)를 따릅니다.

## 비공개 리소스

`src/main/resources/assets/semion-td/`에는 별도 구매한 모델과 텍스처가 들어가며 Git에서 제외합니다. 새 작업 환경에는 소유자가 제공한 원본을 같은 경로에 따로 배치해야 합니다. 이 디렉터리를 강제 추가하거나 공개 저장소, CI artifact, 릴리스 첨부 파일에 올리지 않습니다.

## 문서 기준

이 README는 현재 코드 기준으로 작성했습니다. 실행 예시는 `run/config/semion-td/`의 생성 파일을 참고할 수 있지만, 필드와 기본값 판단은 `SemionConfigLoader`, config record, command registration, job/tower catalog를 기준으로 합니다.
