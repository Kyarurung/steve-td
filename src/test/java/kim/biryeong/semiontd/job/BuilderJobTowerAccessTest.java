package kim.biryeong.semiontd.job;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import kim.biryeong.semiontd.tower.TowerType;
import kim.biryeong.semiontd.tower.animal.AnimalTowers;
import kim.biryeong.semiontd.tower.end.EndTowers;
import kim.biryeong.semiontd.tower.legion.LegionTowers;
import kim.biryeong.semiontd.tower.undead.UndeadTowers;
import kim.biryeong.semiontd.tower.villager.VillagerTowers;
import kim.biryeong.semiontd.tower.warlock.WarlockTowers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BuilderJobTowerAccessTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @ParameterizedTest
    @MethodSource("builderTowers")
    void catalogOwnershipDoesNotRequireRuntimeContext(SemionJob job, TowerType ownedTower) {
        assertTrue(job.includesTowerInCatalog(ownedTower));
        assertTrue(job.canUseTower(null, ownedTower));
    }

    @ParameterizedTest
    @MethodSource("builders")
    void rejectsTowersOwnedByAnotherBuilder(SemionJob job) {
        assertFalse(job.includesTowerInCatalog(EndTowers.BASE_END_TOWER));
        assertFalse(job.canUseTower(null, EndTowers.BASE_END_TOWER));
    }

    private static Stream<Arguments> builderTowers() {
        return Stream.of(
                Arguments.of(new VillagerTowerJob(), VillagerTowers.T1_SPLASH_TOWER),
                Arguments.of(new UndeadTowerJob(), UndeadTowers.T1_ZOMBIE_TOWER),
                Arguments.of(new AnimalTowerJob(), AnimalTowers.T1_PIG_TOWER),
                Arguments.of(new LegionTowerJob(), LegionTowers.ILLUSION_TOWER),
                Arguments.of(new WarlockTowerJob(), WarlockTowers.BASE_WARLOCK_TOWER)
        );
    }

    private static Stream<SemionJob> builders() {
        return Stream.of(
                new VillagerTowerJob(),
                new UndeadTowerJob(),
                new AnimalTowerJob(),
                new LegionTowerJob(),
                new WarlockTowerJob()
        );
    }
}
