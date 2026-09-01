package kim.biryeong.semiontd.tower.resonance;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.monster.SemionMonsterEntity;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.trait.BuiltInTraits;
import kim.biryeong.semiontd.trait.TraitLoadout;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class ResonanceCombatGameTest extends ResonanceGameTestSupport {
    @GameTest
    public void frostDebuffActivatesSharedDamageAura(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("resonance-frost-aura");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
        BlockPos center = BuilderIntegrationGameTestSupport.primaryPosition(lane);
        ResonanceTower frost = addTower(lane, owner, ResonanceTowers.FROST_CORE, center);
        ResonanceTower focus = addTower(lane, owner, ResonanceTowers.FOCUS_CRYSTAL, center.offset(1, 0, 0));
        addTower(lane, owner, ResonanceTowers.FOCUS_PRISM, center.offset(-1, 0, 0));
        addTower(lane, owner, ResonanceTowers.WAVE_CRYSTAL, center.offset(0, 0, 1));
        addTower(lane, owner, ResonanceTowers.WAVE_PRISM, center.offset(0, 0, -1));
        addTower(lane, owner, ResonanceTowers.AMPLIFY_CRYSTAL, center.offset(1, 0, 1));
        addTower(lane, owner, ResonanceTowers.AMPLIFY_PRISM, center.offset(-1, 0, -1));
        ResonanceController.refresh(lane.towers());
        BuilderIntegrationGameTestSupport.require(frost.resonanceLevel() == 3,
                "Frost should reach resonance level three.");
        requireClose(1.5, focus.auraDamageVsSlowedBonus(),
                "Nearby Moobloom should receive the level-three frost aura.");
        SemionTowerEntity frostEntity = BuilderIntegrationGameTestSupport.towerEntity(lane, frost);
        SemionMonsterEntity target = spawnRoleMonster(
                context,
                "resonance-frost-aura-target",
                frostEntity.position().add(0.0, 0.0, 1.0),
                100.0,
                0.0,
                0.0
        );
        requireClose(100.0, focus.modifyAttackDamage(null, target, 100.0),
                "Frost aura should require an active debuff.");
        frost.onAttack(frostEntity, target, 20.0, false);
        requireClose(0.4, target.activeTimedEffectMagnitude(TimedEffectType.MONSTER_MOVE_SPEED_REDUCTION),
                "Frost should reduce movement speed.");
        requireClose(0.4, target.activeTimedEffectMagnitude(TimedEffectType.MONSTER_ATTACK_SPEED_REDUCTION),
                "Frost should reduce attack speed.");
        requireClose(250.0, focus.modifyAttackDamage(null, target, 100.0),
                "Frost aura should amplify damage against a debuffed target.");
        context.succeed();
    }

    @GameTest
    public void focusWaveAndFrostAbilitiesUseMagicDamageWithoutIgnite(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("resonance-magic-combat");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = BuilderIntegrationGameTestSupport.lane(game, owner);
        lane.assignTraitLoadout(new TraitLoadout(BuiltInTraits.IGNITE_ID, BuiltInTraits.NONE_ID));
        BlockPos center = BuilderIntegrationGameTestSupport.primaryPosition(lane);
        ResonanceTower focus = addTower(lane, owner, ResonanceTowers.FOCUS_CORE, center);
        ResonanceTower wave = addTower(lane, owner, ResonanceTowers.WAVE_CORE, center);
        ResonanceTower frost = addTower(lane, owner, ResonanceTowers.FROST_CORE, center);
        addTower(lane, owner, ResonanceTowers.FOCUS_CRYSTAL, center);
        addTower(lane, owner, ResonanceTowers.FOCUS_PRISM, center);
        addTower(lane, owner, ResonanceTowers.WAVE_CRYSTAL, center);
        addTower(lane, owner, ResonanceTowers.WAVE_PRISM, center);
        addTower(lane, owner, ResonanceTowers.FROST_CRYSTAL, center);
        addTower(lane, owner, ResonanceTowers.FROST_PRISM, center);
        ResonanceController.refresh(lane.towers());
        verifyFocusMagic(context, lane, focus);
        verifyWaveMagic(context, lane, wave);
        verifyFrostMagic(context, lane, frost);
        context.succeed();
    }

    private static void verifyFocusMagic(GameTestHelper context, PlayerLane lane, ResonanceTower tower) {
        SemionTowerEntity source = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
        SemionMonsterEntity target = spawnRoleMonster(
                context, "resonance-focus-target", source.position().add(4.0, 0.0, 0.0), 5_000.0, 100.0, 0.0
        );
        int every = TowerBalanceRuntime.abilityInt(tower.type().id(), "focusStrikeEveryAttacks");
        for (int attack = 1; attack < every; attack++) {
            tower.onAttack(source, target, tower.type().damage(), false);
        }
        double before = target.getHealth();
        tower.onAttack(source, target, tower.type().damage(), false);
        double expected = tower.resolveOutgoingDamage(
                source,
                target,
                tower.type().damage() * TowerBalanceRuntime.ability(tower.type().id(), "focusStrikeDamageRatio")
        );
        requireClose(before - expected, target.getHealth(), "Focus strike should use magic mitigation.");
        requireNoIgnite(target, "Focus strike");
        target.discard();
    }

    private static void verifyWaveMagic(GameTestHelper context, PlayerLane lane, ResonanceTower tower) {
        SemionTowerEntity source = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
        SemionMonsterEntity primary = spawnRoleMonster(
                context, "resonance-wave-primary", source.position().add(4.0, 0.0, 0.0), 5_000.0, 0.0, 0.0
        );
        SemionMonsterEntity secondary = spawnRoleMonster(
                context, "resonance-wave-secondary", primary.position().add(1.0, 0.0, 0.0), 5_000.0, 100.0, 0.0
        );
        double before = secondary.getHealth();
        double expected = tower.resolveOutgoingDamage(
                source,
                secondary,
                tower.type().damage() * TowerBalanceRuntime.ability(tower.type().id(), "waveLevel3SplashDamageRatio")
        );
        tower.onAttack(source, primary, tower.type().damage(), false);
        requireClose(before - expected, secondary.getHealth(), "Wave splash should use magic mitigation.");
        requireNoIgnite(secondary, "Wave splash");
        primary.discard();
        secondary.discard();
    }

    private static void verifyFrostMagic(GameTestHelper context, PlayerLane lane, ResonanceTower tower) {
        SemionTowerEntity source = BuilderIntegrationGameTestSupport.towerEntity(lane, tower);
        SemionMonsterEntity target = spawnRoleMonster(
                context, "resonance-frost-target", source.position().add(4.0, 0.0, 0.0), 5_000.0, 100.0, 0.0
        );
        int every = TowerBalanceRuntime.abilityInt(tower.type().id(), "frostPulseEveryAttacks");
        for (int attack = 1; attack < every; attack++) {
            tower.onAttack(source, target, tower.type().damage(), false);
        }
        double before = target.getHealth();
        tower.onAttack(source, target, tower.type().damage(), false);
        double expected = tower.resolveOutgoingDamage(
                source,
                target,
                tower.type().damage() * TowerBalanceRuntime.ability(tower.type().id(), "frostPulseDamageRatio")
        );
        requireClose(before - expected, target.getHealth(), "Frost pulse should use magic mitigation.");
        requireNoIgnite(target, "Frost pulse");
    }

    private static void requireNoIgnite(SemionMonsterEntity target, String ability) {
        BuilderIntegrationGameTestSupport.require(
                target.activeTimedEffectTicks(TimedEffectType.MONSTER_IGNITED) == 0,
                ability + " should not apply the ignite trait."
        );
    }
}
