package penna.dev.config;

import penna.api.config.ConfigManager;
import penna.api.config.Configurable;

public class DevConfigManager implements ConfigManager {
    Configurable configurable;

    public DevConfigManager() {
        PennaRuntimeConfigManager.register(this);
    }

    @Override
    public void bind(Configurable configurable) {
        this.configurable = configurable;
    }

    @Override
    public void configure() {
        configurable.configure(StandardConfigs.DevOptimized);
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        configurable.configure(configItems);
    }
}
