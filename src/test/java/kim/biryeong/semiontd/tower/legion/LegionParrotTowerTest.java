package kim.biryeong.semiontd.tower.legion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import kim.biryeong.semiontd.config.TowerBalanceConfig;
import kim.biryeong.semiontd.config.TowerBalanceRuntime;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegionParrotTowerTest {
    private TowerBalanceConfig defaults;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void applyDefaults() {
        defaults = TowerBalanceConfig.defaultConfig();
        TowerBalanceRuntime.apply(defaults);
    }

    @AfterEach
    void restoreDefaults() {
        TowerBalanceRuntime.apply(defaults);
    }

    @Test
    void attackStacksCapAndResetAtRoundBoundary() {
        LegionParrotTower parrot = tower(LegionTowers.T1_PARROT_TOWER);
        int maximumStacks = TowerBalanceRuntime.abilityInt(parrot.type().id(), "maxAttackStacks");
        double stackBonus = TowerBalanceRuntime.ability(parrot.type().id(), "attackStackBonus");

        for (int attack = 0; attack < maximumStacks + 2; attack++) {
            parrot.onAttack(null, null, parrot.type().damage(), false);
        }

        double maximumMultiplier = 1.0 + maximumStacks * stackBonus;
        assertEquals(maximumStacks, parrot.attackStacks());
        assertTrue(String.join("\n", parrot.runtimeDetailLines()).contains(
                "공격 스택 " + maximumStacks + "/" + maximumStacks
        ));
        assertEquals(parrot.type().damage() * maximumMultiplier,
                parrot.modifyAttackDamage(null, null, parrot.type().damage()), 0.0001);
        assertEquals((int) Math.ceil(parrot.type().attackIntervalTicks() / maximumMultiplier),
                parrot.adjustAttackInterval(parrot.type().attackIntervalTicks()));

        parrot.resetForRound(null);

        assertEquals(0, parrot.attackStacks());
        assertTrue(String.join("\n", parrot.runtimeDetailLines()).contains(
                "공격 스택 0/" + maximumStacks
        ));
    }

    @Test
    void currentRoundStacksDoNotTransferToUpgrade() {
        LegionParrotTower previous = tower(LegionTowers.T1_PARROT_TOWER);
        for (int attack = 0; attack < 3; attack++) {
            previous.onAttack(null, null, previous.type().damage(), false);
        }
        LegionParrotTower upgraded = tower(LegionTowers.T2_PARROT_TOWER);

        upgraded.copyFrom(previous, LegionTowers.T2_PARROT_TOWER.mineralCost());

        assertEquals(3, previous.attackStacks());
        assertEquals(0, upgraded.attackStacks());
    }

    private static LegionParrotTower tower(kim.biryeong.semiontd.tower.TowerType type) {
        return new LegionParrotTower(
                TowerBalanceRuntime.resolve(type),
                UUID.nameUUIDFromBytes(type.id().getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                TeamId.RED,
                1,
                new GridPosition(0, 64, 0)
        );
    }
}
