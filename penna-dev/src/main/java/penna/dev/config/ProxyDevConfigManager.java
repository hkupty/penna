package penna.dev.config;

import penna.api.config.ConfigManager;
import penna.api.config.Configurable;

public class ProxyDevConfigManager implements ConfigManager {

    private final ConfigManager underlying;

    public ProxyDevConfigManager(ConfigManager underlying) {
        this.underlying = underlying;
    }

    @Override
    public void bind(Configurable configurable) {
        underlying.bind(configurable);
    }

    @Override
    public void configure() {
        underlying.updateConfigs(StandardConfigs.DevOptimized);
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        underlying.updateConfigs(configItems);
    }
}
