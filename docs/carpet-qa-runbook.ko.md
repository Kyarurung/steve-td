# Carpet QA Runbook

Carpet은 로컬 QA 런타임에서만 사용한다. 운영 배포물 검증 문서에는 fake player로 닫은 서버 항목과 실제 클라이언트로 봐야 하는 화면 항목을 분리해서 기록한다.

## 기본 서버 기동

```text
./gradlew runServer --console=plain
```

서버가 `Done` 상태가 된 뒤 콘솔에서 아래 명령을 실행한다. fake player spawn 직후에는 로그인 처리가 한 tick 이상 늦을 수 있으므로, `player ... spawn` 후 ready/start 명령이 참가자 없음으로 처리되면 같은 ready/start 단계만 다시 실행한다.

## NORMAL 4인 Smoke

```text
semiontd create
player qared spawn
player qablue spawn
player qagreen spawn
player qayellow spawn
execute as qared run semiontd ready
execute as qablue run semiontd ready
execute as qagreen run semiontd ready
execute as qayellow run semiontd ready
semiontd start
semiontd status
semiontd status teams
semiontd status players
execute as qared run semiontd spectate blue
player qaspec spawn
execute as qaspec run semiontd spectate red
execute as qaspec run semiontd spectate green
execute as qaspec run semiontd spectate blue
execute as qared run semiontd economy
execute as qared run semiontd summons
execute as qared run semiontd ui
execute as qaspec run semiontd ui
semiontd end
semiontd reset
```

기대 결과:

- 4명 ready 후 NORMAL start가 성공한다.
- active participant의 관전 전환은 실패한다.
- 신규 관전자는 active RED/BLUE 관전에 성공하고 inactive GREEN 관전은 실패한다.
- `economy`, `summons`, `ui`가 서버 예외 없이 응답한다.
- `end`/`reset` 후 `activeGame=false`, `arenaLoaded=false`로 복구된다.

## TEST Tower Smoke

```text
semiontd create
semiontd testmode true
player qatower1 spawn
player qatower2 spawn
execute as qatower1 run semiontd ready
execute as qatower2 run semiontd ready
semiontd start
semiontd status players
semiontd status lanes
execute as qatower1 run tp @s <towerSample x> <towerSample y> <towerSample z>
execute as qatower1 run data get entity @s Pos
execute as qatower1 run semiontd tower test
execute as qatower1 run semiontd tower upgrades
semiontd status lanes
semiontd end
semiontd status
stop
```

`towerSample`은 `semiontd status lanes`가 출력한 active lane별 `towerSample=x,y,z`를 그대로 사용한다. 2026-05-11 확인값은 `towerSample=-26,145,50`, `laneArea=-47,145,47..-5,145,53`였고, 이 좌표에서 `semiontd tower test`는 `테스트 타워를 설치했습니다: BlockPos{x=-26, y=145, z=50}`로 성공했다.

기대 결과:

- `status lanes`가 active lane별 `towerSample`, `laneArea`, `monsters`, `towers`를 출력한다.
- fake player를 `towerSample`으로 이동한 뒤 `tower test`가 성공한다.
- 직후 `status lanes`에서 해당 player lane의 `towers=1`이 출력된다.
- `end` 후 `activeGame=false`, `arenaLoaded=false`로 복구된다.

## Known Noisy Logs

- Polymer/DialogUtils resource-pack 경고(`zip END header not found`, `rootPath is null`)는 현재 GameTest와 Carpet smoke 실패 원인이 아니다.
- Carpet fake player는 cross-dimension lobby teleport 중 내부 예외를 낼 수 있다. reset/end는 해당 fake player를 disconnect 처리하고 전체 복구를 계속해야 하며, 최종 `status`로 `activeGame=false`, `arenaLoaded=false`를 확인한다.
- AvatarRenderer의 offline fake player skin lookup은 SSL handshake 경고를 남길 수 있다. `stop` 후 Gradle 세션이 남으면 콘솔 세션을 인터럽트해 닫는다.
