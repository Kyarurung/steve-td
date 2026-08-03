package kim.biryeong.semiontd.config;

public record WebIntegrationConfig(boolean enabled) {
    public static WebIntegrationConfig defaultConfig() {
        return new WebIntegrationConfig(false);
    }
}
