package kim.biryeong.semiontd.tower.villager;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.effect.TimedEffectType;
import kim.biryeong.semiontd.entity.SemionEntityTypes;
import kim.biryeong.semiontd.entity.tower.SemionTowerEntity;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.game.SemionGame;
import kim.biryeong.semiontd.game.SemionPlayer;
import kim.biryeong.semiontd.game.TeamId;
import kim.biryeong.semiontd.gametest.BuilderIntegrationGameTestSupport;
import kim.biryeong.semiontd.test.tower.TestTower;
import kim.biryeong.semiontd.tower.TowerCategory;
import kim.biryeong.semiontd.tower.TowerType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class VillagerAdvEffectsGameTest extends VillagerAdvGameTestSupport {
    @GameTest
    public void advMarkerHalvesExistingVillagerSurvivalBonuses(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-survival-effects");
        SemionGame game = startedGame(context, owner);
        PlayerLane lane = lane(game, owner);
        SemionPlayer player = game.players().get(owner);
        BlockPos base = BuilderIntegrationGameTestSupport.primaryPosition(lane);
        VillagerSplashTower librarian = new VillagerSplashTower(
                VillagerTowers.ADV_T2_LIBRARIAN_TOWER,
                owner,
                TeamId.RED,
                1,
                GridPosition.from(base)
        );
        VillagerThornTower golem = new VillagerThornTower(
                VillagerTowers.ADV_T2_GOLEM_TOWER,
                owner,
                TeamId.RED,
                1,
                GridPosition.from(base.offset(1, 0, 0))
        );
        lane.addTower(librarian);
        lane.addTower(golem);
        librarian.setData(VillagerAdvStates.EXPERIENCE, 12.0);
        VillagerAdvEffectController.refresh(player, lane, librarian);
        VillagerAdvEffectController.refresh(player, lane, golem);
        lane.resetForRound();
        requireClose(102.5, librarian.modifyAttackDamage(null, null, 100.0),
                "ADV librarian survival damage bonus should remain halved.");
        requireClose(VillagerTowers.ADV_T2_GOLEM_TOWER.maxHealth() * 1.05, golem.currentMaxHealth(),
                "ADV golem survival health bonus should remain halved.");
        context.succeed();
    }

    @GameTest
    public void configDrivenTimedEffectsPreserveMaxHealthHealingAndLargeMagnitudes(GameTestHelper context) {
        UUID owner = BuilderIntegrationGameTestSupport.stableUuid("villager-adv-timed-effects");
        TowerType type = new TowerType(
                "villager_adv_effect_probe",
                "Villager ADV Effect Probe",
                TowerCategory.DIRECT,
                0,
                100.0,
                4.0,
                10.0,
                20,
                0,
                List.of()
        );
        TestTower tower = new TestTower(type, owner, TeamId.RED, 1, GridPosition.from(context.absolutePos(BlockPos.ZERO)));
        tower.syncHealth(50.0);
        SemionTowerEntity entity = new SemionTowerEntity(SemionEntityTypes.TOWER, context.getLevel());
        entity.configure(tower, null);
        context.getLevel().addFreshEntity(entity);
        entity.applyTimedEffect(TimedEffectType.TOWER_MAX_HEALTH_BONUS, 0.50, 72000);
        requireClose(150.0, tower.currentMaxHealth(), "Maximum-health effect should update runtime maximum health.");
        requireClose(100.0, tower.health(), "Maximum-health increase should heal its added health delta.");
        entity.applyTimedEffect(TimedEffectType.TOWER_DAMAGE_BONUS, 2.50, 72000);
        requireClose(2.50, entity.activeTimedEffectMagnitude(TimedEffectType.TOWER_DAMAGE_BONUS),
                "Config-driven effect magnitudes should not be clamped.");
        context.succeed();
    }
}
