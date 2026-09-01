package kim.biryeong.semiontd.tower.illager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import kim.biryeong.semiontd.config.AttackKind;
import kim.biryeong.semiontd.entity.monster.Monster;
import kim.biryeong.semiontd.game.GridPosition;
import kim.biryeong.semiontd.game.TeamId;
import org.junit.jupiter.api.Test;

class IllagerMarkDomainTest {
    @Test
    void markIsActiveForOwnerAndExpiresByMonsterTick() {
        UUID owner = UUID.randomUUID();
        Monster monster = monster();

        IllagerMarkDomain.apply(monster, owner, 0.2, 2, new GridPosition(1, 64, 1), 1.0);

        Optional<IllagerMark> mark = IllagerMarkDomain.activeMark(monster, owner);
        assertTrue(mark.isPresent());
        assertEquals(0.2, mark.get().damageTakenBonus(), 0.0001);
        assertTrue(mark.get().forcesTargetFor(new GridPosition(1, 64, 2)));
        assertFalse(IllagerMarkDomain.activeMark(monster, UUID.randomUUID()).isPresent());

        monster.tickSurvivalScaling(null, 0);
        monster.tickSurvivalScaling(null, 0);
        monster.tickSurvivalScaling(null, 0);

        assertFalse(IllagerMarkDomain.activeMark(monster, owner).isPresent());
    }

    @Test
    void invalidMarkInputDoesNotCreateState() {
        UUID owner = UUID.randomUUID();
        Monster monster = monster();

        IllagerMarkDomain.apply(monster, owner, 0.2, 0, new GridPosition(1, 64, 1), 1.0);

        assertFalse(IllagerMarkDomain.activeMark(monster, owner).isPresent());
    }

    private static Monster monster() {
        return new Monster(
                "test",
                TeamId.RED,
                1,
                Optional.empty(),
                Optional.empty(),
                100,
                0,
                1,
                AttackKind.MELEE,
                "minecraft:zombie",
                1
        );
    }
}
