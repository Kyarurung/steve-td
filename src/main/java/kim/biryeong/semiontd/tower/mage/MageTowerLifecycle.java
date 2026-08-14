package kim.biryeong.semiontd.tower.mage;

import java.util.ArrayList;
import java.util.UUID;
import kim.biryeong.semiontd.game.PlayerLane;
import kim.biryeong.semiontd.tower.Tower;

public final class MageTowerLifecycle {
    private MageTowerLifecycle() {
    }

    public static void finishRound(PlayerLane lane, UUID owner) {
        boolean hasCore = MageTowerRuntime.hasCore(lane, owner);
        int naturalMana = hasCore ? MageBalance.coreMana() : 0;
        for (Tower tower : new ArrayList<>(lane.towers())) {
            if (!owner.equals(tower.ownerPlayer())) {
                continue;
            }
            if (tower instanceof MageWizardTower wizard) {
                naturalMana += wizard.naturalManaProduction();
                wizard.finishRound();
            } else if (tower instanceof MageProphetTower prophet) {
                naturalMana += prophet.naturalManaProduction();
            }
        }
        if (hasCore) {
            MageStates.state(owner).addMana(naturalMana);
        }
        MageTowerRuntime.restoreTemporaryTowers(lane, owner);
    }

}
