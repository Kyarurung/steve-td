package kim.biryeong.semiontd.buildguide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kim.biryeong.semiontd.SemionTd;

public final class BuildGuideStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type RAW_TYPE = new TypeToken<Map<String, BuildGuide>>() {
    }.getType();

    private final Path path;
    private final Map<String, BuildGuide> guides = new LinkedHashMap<>();
    private boolean loaded;

    public BuildGuideStore(Path path) {
        this.path = path;
    }

    public synchronized Optional<BuildGuide> find(String code) {
        ensureLoaded();
        return Optional.ofNullable(guides.get(normalizeCode(code)));
    }

    public synchronized boolean contains(String code) {
        ensureLoaded();
        return guides.containsKey(normalizeCode(code));
    }

    public synchronized BuildGuide put(BuildGuide guide) {
        ensureLoaded();
        guides.put(normalizeCode(guide.code()), guide);
        save();
        return guide;
    }

    public synchronized boolean remove(String code) {
        ensureLoaded();
        boolean removed = guides.remove(normalizeCode(code)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized List<BuildGuide> publicGuides() {
        ensureLoaded();
        return guides.values().stream()
                .sorted(Comparator.comparingLong(BuildGuide::publishedAtEpochMillis).reversed())
                .toList();
    }

    static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (path == null || Files.notExists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, BuildGuide> raw = GSON.fromJson(reader, RAW_TYPE);
            if (raw == null) {
                return;
            }
            for (Map.Entry<String, BuildGuide> entry : raw.entrySet()) {
                String code = normalizeCode(entry.getKey());
                if (!code.isBlank() && entry.getValue() != null) {
                    guides.put(code, entry.getValue());
                }
            }
        } catch (IOException | RuntimeException exception) {
            SemionTd.LOGGER.warn("Failed to load build guide store {}.", path, exception);
        }
    }

    private void save() {
        if (path == null) {
            return;
        }

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(guides, RAW_TYPE, writer);
            }
        } catch (IOException exception) {
            SemionTd.LOGGER.warn("Failed to save build guide store {}.", path, exception);
        }
    }
}
