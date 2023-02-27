package maple.api.config;

public interface Configurable {
    void configure(ConfigManager.ConfigItem[] configItems);
    void reconfigure(ConfigManager.Reconfiguration[] reconfigurations);
}
