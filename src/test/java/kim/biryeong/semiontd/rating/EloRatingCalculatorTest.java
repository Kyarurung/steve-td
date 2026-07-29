package kim.biryeong.semiontd.rating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import kim.biryeong.semiontd.game.MatchId;
import kim.biryeong.semiontd.game.PlayerMatchStatsSnapshot;
import kim.biryeong.semiontd.game.TeamId;
import org.junit.jupiter.api.Test;

final class EloRatingCalculatorTest {
    @Test
    void equalRatedWinnerGainsAndLoserLosesSixteenElo() {
        UUID winnerId = UUID.nameUUIDFromBytes("winner".getBytes());
        UUID loserId = UUID.nameUUIDFromBytes("loser".getBytes());
        RatingMatchInput input = new RatingMatchInput(
                new MatchId(1L),
                1000L,
                List.of(
                        new RatingParticipant(
                                winnerId,
                                "winner",
                                TeamId.RED,
                                true,
                                PlayerRatingProfile.initial(winnerId, "winner")
                        ),
                        new RatingParticipant(
                                loserId,
                                "loser",
                                TeamId.BLUE,
                                false,
                                PlayerRatingProfile.initial(loserId, "loser")
                        )
                )
        );

        RatingMatchResult result = new EloRatingCalculator().calculate(input);

        RatingAdjustment winner = result.adjustments().get(0);
        RatingAdjustment loser = result.adjustments().get(1);
        assertEquals(16, winner.displayEloDelta());
        assertEquals(-16, loser.displayEloDelta());
        assertEquals(1, winner.after().gamesPlayed());
        assertEquals(1, winner.after().wins());
        assertEquals(1, loser.after().losses());
    }

    @Test
    void underdogWinProducesLargerPositiveDeltaThanFavoriteWin() {
        UUID underdogId = UUID.nameUUIDFromBytes("underdog".getBytes());
        UUID favoriteId = UUID.nameUUIDFromBytes("favorite".getBytes());
        PlayerRatingProfile underdog = profile(underdogId, "underdog", 1200.0);
        PlayerRatingProfile favorite = profile(favoriteId, "favorite", 1800.0);
        RatingMatchResult result = new EloRatingCalculator().calculate(new RatingMatchInput(
                new MatchId(2L),
                1000L,
                List.of(
                        new RatingParticipant(underdogId, "underdog", TeamId.RED, true, underdog),
                        new RatingParticipant(favoriteId, "favorite", TeamId.BLUE, false, favorite)
                )
        ));

        assertTrue(result.adjustments().get(0).displayEloDelta() > 30);
        assertTrue(result.adjustments().get(1).displayEloDelta() < -30);
    }

    @Test
    void unevenTeamSizesDoNotInflateTotalDelta() {
        RatingMatchResult result = new EloRatingCalculator().calculate(new RatingMatchInput(
                new MatchId(3L),
                1000L,
                List.of(
                        participant("winner-a", TeamId.RED, true),
                        participant("winner-b", TeamId.RED, true),
                        participant("winner-c", TeamId.RED, true),
                        participant("loser-a", TeamId.BLUE, false),
                        participant("loser-b", TeamId.BLUE, false)
                )
        ));

        double winnerTotal = result.adjustments().stream()
                .filter(RatingAdjustment::winner)
                .mapToDouble(RatingAdjustment::muDelta)
                .sum();
        double loserTotal = result.adjustments().stream()
                .filter(adjustment -> !adjustment.winner())
                .mapToDouble(RatingAdjustment::muDelta)
                .sum();

        assertEquals(32.0, winnerTotal, 0.000001);
        assertEquals(-32.0, loserTotal, 0.000001);
    }

    @Test
    void fourTeamPlacementScoresProduceTopHalfGainsAndBottomHalfLosses() {
        RatingMatchResult result = new EloRatingCalculator().calculate(new RatingMatchInput(
                new MatchId(4L),
                1000L,
                List.of(
                        participant("first", TeamId.RED, true, 1.0),
                        participant("second", TeamId.BLUE, false, 0.9),
                        participant("third", TeamId.GREEN, false, 0.1),
                        participant("fourth", TeamId.YELLOW, false, 0.0)
                )
        ));

        assertEquals(List.of(16, 13, -13, -16), result.adjustments().stream()
                .map(RatingAdjustment::displayEloDelta)
                .toList());
    }

    @Test
    void fiveTeamPlacementScoresKeepMiddlePlacementNeutral() {
        RatingMatchResult result = new EloRatingCalculator().calculate(new RatingMatchInput(
                new MatchId(5L),
                1000L,
                List.of(
                        participant("first", TeamId.RED, true, 1.0),
                        participant("second", TeamId.BLUE, false, 0.9),
                        participant("third", TeamId.GREEN, false, 0.5),
                        participant("fourth", TeamId.YELLOW, false, 0.1),
                        participant("fifth", TeamId.PURPLE, false, 0.0)
                )
        ));

        assertEquals(List.of(16, 13, 0, -13, -16), result.adjustments().stream()
                .map(RatingAdjustment::displayEloDelta)
                .toList());
    }

    @Test
    void weakContributionDoesNotIncreaseLossAndPerfectDefenseReducesIt() {
        RatingMatchResult weakLoss = new EloRatingCalculator().calculate(new RatingMatchInput(
                new MatchId(6L),
                1000L,
                List.of(
                        participant("perfect-winner", TeamId.RED, true, defenseStats(100.0, 0.0)),
                        participant("weak-loser", TeamId.BLUE, false, defenseStats(100.0, 100.0))
                )
        ));
        RatingMatchResult perfectLoss = new EloRatingCalculator().calculate(new RatingMatchInput(
                new MatchId(7L),
                1000L,
                List.of(
                        participant("weak-winner", TeamId.RED, true, defenseStats(100.0, 100.0)),
                        participant("perfect-loser", TeamId.BLUE, false, defenseStats(100.0, 0.0))
                )
        ));

        assertEquals(-16.0, weakLoss.adjustments().get(1).muDelta(), 0.000001);
        assertEquals(-10.2, perfectLoss.adjustments().get(1).muDelta(), 0.000001);
    }

    private static RatingParticipant participant(String name, TeamId teamId, boolean winner) {
        UUID playerId = UUID.nameUUIDFromBytes(name.getBytes());
        return new RatingParticipant(playerId, name, teamId, winner, PlayerRatingProfile.initial(playerId, name));
    }

    private static RatingParticipant participant(String name, TeamId teamId, boolean winner, double placementScore) {
        UUID playerId = UUID.nameUUIDFromBytes(name.getBytes());
        return new RatingParticipant(playerId, name, teamId, winner, PlayerRatingProfile.initial(playerId, name), placementScore);
    }

    private static RatingParticipant participant(
            String name,
            TeamId teamId,
            boolean winner,
            PlayerMatchStatsSnapshot stats
    ) {
        UUID playerId = UUID.nameUUIDFromBytes(name.getBytes());
        return new RatingParticipant(
                playerId,
                name,
                teamId,
                winner,
                PlayerRatingProfile.initial(playerId, name),
                stats
        );
    }

    private static PlayerMatchStatsSnapshot defenseStats(double incomingThreat, double leakedThreat) {
        return new PlayerMatchStatsSnapshot(
                0,
                0,
                0,
                0,
                incomingThreat,
                leakedThreat,
                0.0,
                0.0,
                0,
                0,
                0,
                0.0,
                0.0
        );
    }

    private static PlayerRatingProfile profile(UUID playerId, String name, double mu) {
        return new PlayerRatingProfile(
                playerId,
                name,
                RatingSystemId.ELO,
                0,
                0,
                0,
                0,
                mu,
                PlayerRatingProfile.INITIAL_SIGMA,
                (int) Math.round(mu),
                null,
                0L
        );
    }
}
