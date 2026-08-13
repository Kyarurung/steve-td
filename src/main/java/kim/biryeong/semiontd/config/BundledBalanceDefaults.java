package kim.biryeong.semiontd.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BundledBalanceDefaults {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String RESOURCE_ROOT = "/semiontd/balance-defaults/";

    private BundledBalanceDefaults() {
    }

    public static <T> T load(String fileName, Class<T> type, T fallback) {
        try (InputStream input = open(fileName);
             Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            T value = GSON.fromJson(reader, type);
            return value == null ? fallback : value;
        } catch (IOException exception) {
            return fallback;
        }
    }

    public static void copyIfMissing(String fileName, Path target) throws IOException {
        if (target == null || Files.exists(target)) {
            return;
        }
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (InputStream input = open(fileName)) {
            Files.copy(input, target);
        } catch (FileAlreadyExistsException ignored) {
            // Another startup path created the same config first.
        }
    }

    private static InputStream open(String fileName) throws IOException {
        InputStream input = BundledBalanceDefaults.class.getResourceAsStream(RESOURCE_ROOT + fileName);
        if (input == null) {
            throw new IOException("Missing bundled balance config: " + fileName);
        }
        return input;
    }
}
