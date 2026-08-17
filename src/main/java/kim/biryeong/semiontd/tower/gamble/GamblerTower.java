package kim.biryeong.semiontd.tower.gamble;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import kim.biryeong.semiontd.SemionTd;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.entity.tower.vfx.TowerVfxService;
import kim.biryeong.semiontd.entity.visual.TowerEquipmentVisual;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.tower.ProductionTower;
import kim.biryeong.semiontd.tower.Tower;
import kim.biryeong.semiontd.tower.TowerDataKey;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.TowerUpgradeOption;
import kim.biryeong.semiontd.ui.SemionText;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GamblerTower extends ProductionTower {
    static final TowerDataKey<GambleState> STATE = TowerDataKey.of(
            ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "gamble/state"), GambleState.class
    );

    private transient PlayerLane lane;
    private transient ArmorStand equipmentVisual;
    private double copiedHealthRatio = 1.0;
    private int attackCount;

    public GamblerTower(
            TowerType type, UUID ownerPlayer, TeamId teamId, int laneId,
            GridPosition originalPosition, GridPosition currentPosition
    ) {
        super(type, ownerPlayer, teamId, laneId, originalPosition, currentPosition);
    }

    @Override
    public void onPlaced(PlayerLane lane) {
        this.lane = lane;
        syncMaxHealth(state().resolvedValue(GambleStat.MAX_HEALTH, type().maxHealth()), false);
        syncHealth(currentMaxHealth() * copiedHealthRatio);
        copiedHealthRatio = 1.0;
        super.onPlaced(lane);
        syncEquipmentVisual();
    }

    @Override
    protected void configureEntityAfterSpawn(SemionTowerEntity entity, PlayerLane lane) {
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLD_INGOT));
        entity.setCustomName(Component.literal(type().displayName()));
        entity.setCustomNameVisible(true);
    }

    @Override
    public void onStateChanged(PlayerLane lane) {
        super.onStateChanged(lane);
        syncEquipmentVisual();
    }

    @Override
    public void onRemoved(PlayerLane lane) {
        TowerEquipmentVisual.remove(equipmentVisual);
        equipmentVisual = null;
        super.onRemoved(lane);
    }

    @Override
    public void tick(PlayerLane lane) {
        this.lane = lane;
        super.tick(lane);
        syncEquipmentVisual();
    }

    @Override
    public void onWaveStarted(PlayerLane lane, int currentRound) {
        this.lane = lane;
        attackCount = 0;
    }

    @Override
    protected void copyRuntimeStateFrom(Tower previousTower) {
        copiedHealthRatio = previousTower.health() / Math.max(1.0, previousTower.currentMaxHealth());
        if (previousTower instanceof GamblerTower previous) {
            attackCount = previous.attackCount;
        }
    }

    @Override
    public double effectBaseMaxHealth() {
        return state().resolvedValue(GambleStat.MAX_HEALTH, type().maxHealth());
    }

    @Override
    protected void refreshMaxHealthAfterTypeChange(PlayerLane lane) {
        syncMaxHealth(effectBaseMaxHealth(), false);
    }

    @Override
    public double adjustAttackRange(double baseRange) {
        return state().resolvedValue(GambleStat.RANGE, baseRange);
    }

    @Override
    public double modifyAttackDamage(
            SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount
    ) {
        double configuredBase = Math.max(0.001, type().damage());
        double fixedBase = state().resolvedValue(GambleStat.DAMAGE, type().damage());
        return damageAmount * fixedBase / configuredBase;
    }

    @Override
    public double modifyResolvedOutgoingDamage(
            SemionTowerEntity towerEntity, SemionMonsterEntity target, double damageAmount
    ) {
        return state().has(GambleAbility.LUCKY_STRIKE)
                ? GambleRolls.luckyStrikeDamage(
                        damageAmount, towerEntity.getRandom().nextInt(6) + 1,
                        GambleBalance.luckyStrikeMultiplier())
                : damageAmount;
    }

    @Override
    public void onAttackResolved(
            SemionTowerEntity source, SemionMonsterEntity target, double attemptedDamage,
            double resolvedOutgoingDamage, double dealtDamage, boolean killedTarget
    ) {
        attackCount++;
        if (!state().has(GambleAbility.SPREAD_BET)
                || attackCount % GambleBalance.spreadEveryAttacks() != 0
                || lane == null || target == null) {
            return;
        }
        nearestExtraTarget(target).ifPresent(extra -> {
            double outgoing = resolvedOutgoingDamage * GambleBalance.spreadDamageRatio();
            damageResolvedTargetResult(source, extra, outgoing);
            TowerVfxService.showSecondaryAttack(source, extra);
        });
    }

    @Override
    public void onUpgradeApplied(PlayerLane lane, TowerUpgradeOption option) {
        GambleBet.fromUpgradeId(option.id()).ifPresent(bet -> resolveBet(lane, bet));
    }

    @Override
    public boolean upgradeCostAddsToSaleValue(TowerUpgradeOption option) {
        return GambleBet.fromUpgradeId(option.id()).isEmpty();
    }

    @Override
    public List<String> upgradeTooltipLines(TowerUpgradeOption option) {
        return GambleBet.fromUpgradeId(option.id()).map(bet -> switch (bet) {
            case ODD -> List.of("d6이 홀수면 고정 점수 +10, 실패하면 -8", "비용은 판매 환불가에 포함되지 않습니다.");
            case EVEN -> List.of("d6이 짝수면 고정 점수 +10, 실패하면 -8", "비용은 판매 환불가에 포함되지 않습니다.");
            case TWO_DICE -> List.of("2d6 합에 따라 -40~+40점, 더블이면 수치가 2배", "비용은 판매 환불가에 포함되지 않습니다.");
        }).orElseGet(List::of);
    }

    @Override
    public List<String> runtimeDetailLines() {
        GambleState state = state();
        ArrayList<String> lines = new ArrayList<>();
        lines.add("도박 횟수: " + state.totalBets());
        lines.add("고정 최대 체력: " + signed(state.maxHealthDelta()));
        lines.add("고정 공격력: " + signed(state.damageDelta()));
        lines.add("고정 사거리: " + signed(state.rangeDelta()));
        lines.add("보유 능력: " + (state.abilities().isEmpty() ? "없음" : state.abilities().stream()
                .map(GambleAbility::displayName).collect(Collectors.joining(" / "))));
        lines.add("최근 결과: " + state.lastResult());
        return List.copyOf(lines);
    }

    GambleState state() {
        return getDataOrDefault(STATE, GambleState.EMPTY);
    }

    private void resolveBet(PlayerLane lane, GambleBet bet) {
        SemionTowerEntity source = GambleRoundEffects.towerEntity(this, lane).orElse(null);
        if (source == null) {
            return;
        }
        int first = source.getRandom().nextInt(6) + 1;
        int second = bet == GambleBet.TWO_DICE ? source.getRandom().nextInt(6) + 1 : 0;
        double score = bet == GambleBet.TWO_DICE
                ? GambleRolls.twoDiceDelta(first, second)
                : GambleRolls.oddEvenDelta(bet, first);
        GambleState before = state();
        double healthRatio = health() / Math.max(1.0, currentMaxHealth());
        String roll = bet == GambleBet.TWO_DICE ? first + "+" + second : Integer.toString(first);
        GambleState after;
        if (GambleRewards.awardsAbility(before, score, source.getRandom().nextDouble())) {
            GambleAbility ability = GambleRewards.chooseMissing(
                    before, source.getRandom().nextInt(GambleRewards.missingAbilities(before).size())
            );
            after = before.recordAbility(ability, bet.displayName() + " " + roll + " → " + ability.displayName());
        } else {
            GambleStat stat = GambleRewards.chooseStat(source.getRandom().nextInt(GambleStat.values().length));
            double delta = GambleRewards.insuredDelta(before, GambleBalance.statDelta(stat, score));
            String result = bet.displayName() + " " + roll + " → " + stat.displayName() + " " + signed(delta);
            after = before.recordStat(stat, delta, baseValue(stat), result);
        }
        setData(STATE, after);
        syncMaxHealth(effectBaseMaxHealth(), false);
        syncHealth(currentMaxHealth() * healthRatio);
        onStateChanged(lane);
        showBetResult(source, after.lastResult(), score > 0.0);
    }

    private double baseValue(GambleStat stat) {
        return switch (stat) {
            case MAX_HEALTH -> type().maxHealth();
            case DAMAGE -> type().damage();
            case RANGE -> type().range();
        };
    }

    private Optional<SemionMonsterEntity> nearestExtraTarget(SemionMonsterEntity primary) {
        double radiusSquared = GambleBalance.spreadRadius() * GambleBalance.spreadRadius();
        return lane.activeMonsters().stream()
                .filter(monster -> monster.hasMinecraftEntity())
                .map(monster -> lane.arenaWorld().getEntity(monster.minecraftEntityId()))
                .filter(SemionMonsterEntity.class::isInstance).map(SemionMonsterEntity.class::cast)
                .filter(entity -> entity != primary && entity.isAlive() && !entity.isRemoved())
                .filter(entity -> entity.position().distanceToSqr(primary.position()) <= radiusSquared)
                .min(Comparator.comparingDouble((SemionMonsterEntity entity) ->
                                entity.position().distanceToSqr(primary.position()))
                        .thenComparing(entity -> entity.getUUID().toString()));
    }

    private void showBetResult(SemionTowerEntity source, String result, boolean success) {
        if (source.level() instanceof net.minecraft.server.level.ServerLevel level) {
            level.sendParticles(success ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.WITCH,
                    source.getX(), source.getY() + 1.0, source.getZ(), 14, 0.35, 0.35, 0.35, 0.04);
        }
        if (source.getServer() != null) {
            var player = source.getServer().getPlayerList().getPlayer(ownerPlayer());
            if (player != null) {
                player.sendSystemMessage(SemionText.prefixedPlain("도박 결과: " + result));
            }
        }
    }

    private void syncEquipmentVisual() {
        equipmentVisual = TowerEquipmentVisual.sync(
                equipmentVisual, GambleRoundEffects.towerEntity(this, lane).orElse(null)
        );
    }

    private static String signed(double value) {
        return (value >= 0.0 ? "+" : "") + oneDecimal(value);
    }
}
