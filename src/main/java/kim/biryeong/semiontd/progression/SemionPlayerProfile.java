package kim.biryeong.semiontd.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record SemionPlayerProfile(
        String lastKnownName,
        int gamesPlayed,
        int wins,
        int losses,
        long cosmeticCurrency,
        String selectedJobId
) {
    public static final Codec<SemionPlayerProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("lastKnownName", "").forGetter(SemionPlayerProfile::lastKnownName),
            Codec.INT.optionalFieldOf("gamesPlayed", 0).forGetter(SemionPlayerProfile::gamesPlayed),
            Codec.INT.optionalFieldOf("wins", 0).forGetter(SemionPlayerProfile::wins),
            Codec.INT.optionalFieldOf("losses", 0).forGetter(SemionPlayerProfile::losses),
            Codec.LONG.optionalFieldOf("cosmeticCurrency", 0L).forGetter(SemionPlayerProfile::cosmeticCurrency),
            Codec.STRING.optionalFieldOf("selectedJobId", "").forGetter(SemionPlayerProfile::selectedJobId)
    ).apply(instance, SemionPlayerProfile::new));

    public SemionPlayerProfile {
        if (lastKnownName == null) {
            lastKnownName = "";
        }
        if (selectedJobId == null) {
            selectedJobId = "";
        }
        if (gamesPlayed < 0 || wins < 0 || losses < 0 || cosmeticCurrency < 0) {
            throw new IllegalArgumentException("Profile values cannot be negative.");
        }
    }

    public static SemionPlayerProfile fresh(String playerName) {
        return new SemionPlayerProfile(playerName == null ? "" : playerName, 0, 0, 0, 0, "");
    }

    public SemionPlayerProfile updateName(String playerName) {
        String normalized = playerName == null ? "" : playerName;
        if (normalized.equals(lastKnownName)) {
            return this;
        }
        return new SemionPlayerProfile(normalized, gamesPlayed, wins, losses, cosmeticCurrency, selectedJobId);
    }

    public SemionPlayerProfile updateSelectedJob(String playerName, ResourceLocation jobId) {
        String normalized = playerName == null ? "" : playerName;
        String jobIdText = jobId == null ? "" : jobId.toString();
        if (normalized.equals(lastKnownName) && jobIdText.equals(selectedJobId)) {
            return this;
        }
        return new SemionPlayerProfile(normalized, gamesPlayed, wins, losses, cosmeticCurrency, jobIdText);
    }

    public Optional<ResourceLocation> selectedJobResource() {
        if (selectedJobId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(selectedJobId));
    }

    public SemionPlayerProfile recordMatch(String playerName, boolean winner, long reward) {
        String normalized = playerName == null ? "" : playerName;
        return new SemionPlayerProfile(
                normalized,
                gamesPlayed + 1,
                wins + (winner ? 1 : 0),
                losses + (winner ? 0 : 1),
                cosmeticCurrency + Math.max(0L, reward),
                selectedJobId
        );
    }
}
