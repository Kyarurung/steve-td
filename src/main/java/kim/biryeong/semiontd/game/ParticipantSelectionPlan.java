package kim.biryeong.semiontd.game;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ParticipantSelectionPlan(
        MatchMode mode,
        List<AssignedParticipant> activeParticipants,
        Set<UUID> spectatorIds,
        int activeTeamCount
) {
    public ParticipantSelectionPlan {
        activeParticipants = List.copyOf(activeParticipants);
        spectatorIds = Set.copyOf(spectatorIds);
    }

    public int activePlayerCount() {
        return activeParticipants.size();
    }

    public int spectatorCount() {
        return spectatorIds.size();
    }

    public Map<TeamId, Integer> teamSizes() {
        Map<TeamId, Integer> sizes = new EnumMap<>(TeamId.class);
        for (AssignedParticipant participant : activeParticipants) {
            sizes.merge(participant.teamId(), 1, Integer::sum);
        }
        return Map.copyOf(sizes);
    }

    public String compositionSummary() {
        return teamSizes().values().stream()
                .sorted(Comparator.reverseOrder())
                .map(String::valueOf)
                .collect(Collectors.joining("/"));
    }

    public ParticipantSelectionPlan withFifthLanePreference(UUID playerId) {
        AssignedParticipant preferred = activeParticipants.stream()
                .filter(participant -> participant.uuid().equals(playerId))
                .findFirst()
                .orElse(null);
        if (preferred == null || preferred.laneId() == 5 || teamSizes().getOrDefault(preferred.teamId(), 0) != 5) {
            return this;
        }

        List<AssignedParticipant> reassigned = activeParticipants.stream()
                .map(participant -> {
                    int laneId = participant.laneId();
                    if (participant.uuid().equals(playerId)) {
                        laneId = 5;
                    } else if (participant.teamId() == preferred.teamId() && participant.laneId() == 5) {
                        laneId = preferred.laneId();
                    }
                    return laneId == participant.laneId()
                            ? participant
                            : new AssignedParticipant(
                                    participant.uuid(),
                                    participant.name(),
                                    participant.teamId(),
                                    laneId,
                                    participant.displayElo()
                            );
                })
                .toList();
        return new ParticipantSelectionPlan(mode, reassigned, spectatorIds, activeTeamCount);
    }
}
