package kim.biryeong.semiontd.tower.developer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerType;
import net.minecraft.util.RandomSource;

/**
 * Every preparation-phase operation the 개발자 builder can perform.
 *
 * <p>All of it is deliberately gated to the preparation phase by the caller; nothing here fires
 * mid-wave. The builder's entire decision surface is "what do I change before the wave runs", and
 * the wave itself is where the player finds out whether the call was right.
 */
public final class DeveloperPatchService {

    /** Outcome of an operation, with a message the dialog and the command share. */
    public record Result(boolean success, String message, DeveloperBug spawnedBug) {
        public static Result fail(String message) {
            return new Result(false, message, null);
        }

        public static Result ok(String message) {
            return new Result(true, message, null);
        }

        public static Result ok(String message, DeveloperBug bug) {
            return new Result(true, message, bug);
        }
    }

    private DeveloperPatchService() {
    }

    // ------------------------------------------------------------------ 능력 해금 집계

    /**
     * Recomputes what the player's ability towers currently unlock.
     *
     * <p>Derived from the lane every time rather than tracked incrementally, so selling an ability
     * tower removes its budget immediately. Patch slots and hotfixes do not add across a line — the
     * highest owned tier wins — because 작업대 upgrades into 배포 서버 rather than sitting beside it.
     *
     * <p>Bug visibility, debugging and 재현 are granted by <em>any</em> tower at or past the
     * relevant point on the 검수 line. Upgrading a 테스터 into a 디버거 consumes the tester, and a
     * player who upgraded should not lose the ability to read their own defects.
     */
    public static DeveloperStates.Capacity capacityFor(PlayerLane lane, UUID playerId) {
        if (lane == null || playerId == null) {
            return DeveloperStates.Capacity.none();
        }
        int patchSlots = 0;
        int hotfixes = 0;
        int combatTowers = 0;
        boolean maintenance = false;
        boolean tested = false;
        boolean debugger = false;
        boolean developer = false;

        for (Tower tower : lane.towers()) {
            if (!playerId.equals(tower.ownerPlayer())) {
                continue;
            }
            TowerType type = tower.type();
            if (!DeveloperTowers.isAbilityTower(type)) {
                if (DeveloperTowers.isGrowthTower(type)) {
                    combatTowers++;
                }
                continue;
            }
            patchSlots = Math.max(patchSlots, DeveloperBalance.patchSlots(type));
            hotfixes = Math.max(hotfixes, DeveloperBalance.hotfixesPerRound(type));
            String id = type.id();
            if (DeveloperTowers.OPS_CENTER.id().equals(id)) {
                maintenance = true;
            }
            if (DeveloperTowers.TESTER.id().equals(id)) {
                tested = true;
            }
            if (DeveloperTowers.DEBUGGER.id().equals(id)) {
                debugger = true;
            }
            if (DeveloperTowers.DEVELOPER.id().equals(id)) {
                developer = true;
            }
        }

        boolean bugsVisible = tested || debugger || developer;
        boolean canDebug = debugger || developer;
        // Base slots make the builder playable before a 작업대 exists; the per-tower term keeps the
        // per-tower patch count flat as the lane grows toward the 23 slot ceiling.
        int resolvedSlots = DeveloperBalance.basePatchSlots()
                + patchSlots
                + combatTowers / DeveloperBalance.patchSlotsPerTowers();
        return new DeveloperStates.Capacity(
                resolvedSlots,
                hotfixes,
                maintenance,
                bugsVisible,
                canDebug ? DeveloperBalance.debugRemovalsPerRound() : 0,
                developer,
                developer ? DeveloperBalance.versionPinSlots() : 0
        );
    }

    /** Refreshes the cached capacity without rolling the per-round budgets. */
    public static void refreshCapacity(PlayerLane lane, UUID playerId) {
        if (playerId == null) {
            return;
        }
        DeveloperStates.of(playerId).refreshCapacity(capacityFor(lane, playerId));
    }

    // ------------------------------------------------------------------ 패치

    public static Result applyPatch(PlayerLane lane, DeveloperTower tower, DeveloperPatch patch, boolean hotfix) {
        if (tower == null || patch == null) {
            return Result.fail("대상을 찾을 수 없습니다.");
        }
        if (!DeveloperTowers.isGrowthTower(tower.type())) {
            return Result.fail("능력 타워에는 패치를 걸 수 없습니다.");
        }
        if (DeveloperTowerData.isPinned(tower)) {
            return Result.fail("버전이 고정된 타워입니다.");
        }
        if (!hotfix && tower.hasBug(DeveloperBug.ROLLBACK_FAILURE)) {
            return Result.fail("롤백 실패 버그로 정식 패치가 걸리지 않습니다. 핫픽스만 가능합니다.");
        }

        DeveloperStates.PlayerState state = DeveloperStates.of(tower.ownerPlayer());
        if (hotfix ? !state.consumeHotfix() : !state.consumePatch()) {
            return Result.fail(hotfix ? "이번 라운드 핫픽스를 모두 사용했습니다." : "이번 라운드 패치 슬롯이 없습니다.");
        }

        double step = patch.stepAmount(DeveloperTowerData.effectiveCount(tower, patch));
        double amount = step * tower.patchEfficiency(lane);
        if (hotfix) {
            amount *= DeveloperBalance.hotfixScale(tower.type());
        }

        if (hotfix) {
            DeveloperTowerData.addActivePatch(tower, patch, amount);
            DeveloperTowerData.addInstability(tower, 1);
        } else {
            DeveloperTowerData.addPendingPatch(tower, patch, amount);
        }

        DeveloperBug spawned = rollBug(tower, hotfix);

        // Sync after the roll, not before: aggro is cached on the entity, so a defect that moves it
        // (어그로 폭주, 은신) would otherwise not take effect until something else forced a resync.
        if (patch == DeveloperPatch.HEALTH && hotfix) {
            tower.resyncHealth(lane, true);
        } else {
            tower.onStateChanged(lane);
        }

        String base = hotfix
                ? patch.displayName() + " 핫픽스를 즉시 적용했습니다."
                : patch.displayName() + " 패치를 예약했습니다. 다음 라운드부터 적용됩니다.";
        if (spawned == null) {
            return Result.ok(base);
        }
        return Result.ok(base + " 버그가 발생했습니다.", spawned);
    }

    /**
     * Rolls whether this patch left a defect, and picks which one.
     *
     * <p>Hotfixes always leave one regardless of tier: skipping review is the thing that causes
     * defects, and letting 정식판 absorb a hotfix cleanly would remove the only reason not to spam
     * them. Reviewed patches use the tower's own chance, which is zero for 정식판 and LTS.
     */
    private static DeveloperBug rollBug(DeveloperTower tower, boolean hotfix) {
        double chance = hotfix ? DeveloperBalance.HOTFIX_BUG_CHANCE : DeveloperBalance.bugChance(tower.type());
        if (chance <= 0.0 || tower.random().nextDouble() >= chance) {
            return null;
        }
        return spawnRandomBug(tower);
    }

    private static DeveloperBug spawnRandomBug(DeveloperTower tower) {
        Set<DeveloperBug> existing = DeveloperTowerData.bugs(tower);
        if (existing.size() >= DeveloperBalance.maxBugsPerTower()) {
            return null;
        }
        List<DeveloperBug> pool = new ArrayList<>(DeveloperBug.values().length);
        for (DeveloperBug bug : DeveloperBug.values()) {
            if (!existing.contains(bug)) {
                pool.add(bug);
            }
        }
        if (pool.isEmpty()) {
            return null;
        }
        RandomSource random = tower.random();
        DeveloperBug chosen = pool.get(random.nextInt(pool.size()));
        return DeveloperTowerData.addBug(tower, chosen) ? chosen : null;
    }

    // ------------------------------------------------------------------ 긴급 점검

    public static Result applyMaintenance(PlayerLane lane, DeveloperTower tower, int round) {
        if (tower == null) {
            return Result.fail("대상을 찾을 수 없습니다.");
        }
        if (!DeveloperTowers.isGrowthTower(tower.type())) {
            return Result.fail("능력 타워는 점검할 수 없습니다.");
        }
        DeveloperStates.PlayerState state = DeveloperStates.of(tower.ownerPlayer());
        if (!state.consumeMaintenance()) {
            return Result.fail("운영 센터가 없거나 이번 라운드 점검을 이미 사용했습니다.");
        }
        DeveloperTowerData.scheduleMaintenance(tower, round);
        tower.resyncHealth(lane, true);
        tower.syncHealth(tower.currentMaxHealth());
        tower.onStateChanged(lane);
        return Result.ok("이번 라운드를 쉬고 다음 라운드에 강화되어 돌아옵니다.");
    }

    // ------------------------------------------------------------------ 최적화

    public static Result applyOptimization(
            PlayerLane lane,
            DeveloperTower tower,
            DeveloperOptimization optimization
    ) {
        if (tower == null || optimization == null) {
            return Result.fail("대상을 찾을 수 없습니다.");
        }
        if (!DeveloperTowers.isGrowthTower(tower.type())) {
            return Result.fail("능력 타워는 최적화할 수 없습니다.");
        }
        if (tower.hasBug(DeveloperBug.READ_ONLY)) {
            return Result.fail("읽기 전용 버그가 있어 최적화를 걸 수 없습니다.");
        }
        if (tower.hasOptimization(optimization)) {
            return Result.fail("이미 적용된 최적화입니다.");
        }
        DeveloperStates.PlayerState state = DeveloperStates.of(tower.ownerPlayer());
        if (state.optimizationsRemaining() <= 0) {
            return Result.fail("이번 매치의 최적화를 모두 사용했습니다.");
        }
        if (!DeveloperTowerData.addOptimization(tower, optimization)) {
            return Result.fail("최적화를 적용하지 못했습니다.");
        }
        state.consumeOptimization();
        tower.resyncHealth(lane, false);
        return Result.ok(optimization.displayName() + "를 적용했습니다. 되돌릴 수 없습니다.");
    }

    /**
     * Whether this optimisation would largely cancel one the tower already has.
     *
     * <p>The dialog uses this to warn rather than to block. Burning two of three match-wide charges
     * on a pair that nets out to nothing is a mistake worth flagging, but it is still the player's
     * call.
     */
    public static boolean wouldConflict(DeveloperTower tower, DeveloperOptimization optimization) {
        if (tower == null || optimization == null) {
            return false;
        }
        for (DeveloperOptimization existing : DeveloperTowerData.optimizations(tower)) {
            if (existing.conflictsWith(optimization)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ 디버그와 재현

    public static Result removeBug(PlayerLane lane, DeveloperTower tower, DeveloperBug bug) {
        if (tower == null || bug == null) {
            return Result.fail("대상을 찾을 수 없습니다.");
        }
        DeveloperStates.PlayerState state = DeveloperStates.of(tower.ownerPlayer());
        if (!state.consumeDebugRemoval()) {
            return Result.fail("디버거가 없거나 이번 라운드 제거를 이미 사용했습니다.");
        }
        if (!DeveloperTowerData.removeBug(tower, bug)) {
            return Result.fail("그 버그가 없습니다.");
        }
        tower.resyncHealth(lane, false);
        return Result.ok(bug.displayName() + " 버그를 제거했습니다.");
    }

    public static Result reproduceBug(
            PlayerLane lane,
            DeveloperTower source,
            DeveloperTower target,
            DeveloperBug bug
    ) {
        if (source == null || target == null || bug == null) {
            return Result.fail("대상을 찾을 수 없습니다.");
        }
        if (source == target) {
            return Result.fail("같은 타워에는 재현할 수 없습니다.");
        }
        if (!DeveloperTowers.isGrowthTower(target.type())) {
            return Result.fail("능력 타워에는 재현할 수 없습니다.");
        }
        if (DeveloperTowerData.isPinned(target)) {
            return Result.fail("버전이 고정된 타워입니다.");
        }
        if (!source.hasBug(bug)) {
            return Result.fail("원본 타워에 그 버그가 없습니다.");
        }
        DeveloperStates.PlayerState state = DeveloperStates.of(target.ownerPlayer());
        if (!state.consumeReproduction()) {
            return Result.fail("개발자가 없거나 이번 라운드 재현을 이미 사용했습니다.");
        }
        if (!DeveloperTowerData.addBug(target, bug)) {
            return Result.fail("대상 타워가 이미 버그로 가득 찼습니다.");
        }
        target.resyncHealth(lane, false);
        return Result.ok(bug.displayName() + " 버그를 재현했습니다.", bug);
    }

    // ------------------------------------------------------------------ 버전 고정

    public static Result setPinned(PlayerLane lane, DeveloperTower tower, boolean pinned) {
        if (tower == null) {
            return Result.fail("대상을 찾을 수 없습니다.");
        }
        if (!DeveloperTowers.isGrowthTower(tower.type())) {
            return Result.fail("능력 타워는 고정할 수 없습니다.");
        }
        DeveloperStates.PlayerState state = DeveloperStates.of(tower.ownerPlayer());
        if (pinned) {
            int slots = state.versionPinSlots();
            if (slots <= 0) {
                return Result.fail("개발자 타워가 없어 버전을 고정할 수 없습니다.");
            }
            if (pinnedCount(lane, tower.ownerPlayer()) >= slots) {
                return Result.fail("고정 슬롯을 모두 사용했습니다.");
            }
        }
        DeveloperTowerData.setPinned(tower, pinned);
        tower.onStateChanged(lane);
        return Result.ok(pinned ? "버전을 고정했습니다." : "버전 고정을 해제했습니다.");
    }

    public static int pinnedCount(PlayerLane lane, UUID playerId) {
        if (lane == null || playerId == null) {
            return 0;
        }
        int count = 0;
        for (Tower tower : lane.towers()) {
            if (playerId.equals(tower.ownerPlayer()) && DeveloperTowerData.isPinned(tower)) {
                count++;
            }
        }
        return count;
    }
}
