package maple.config.yaml;

import maple.api.config.ConfigManager;
import maple.api.config.Configurable;

public class YamlConfigManager implements ConfigManager {

    private Configurable configurable;

    @Override
    public void bind(Configurable configurable) {
        this.configurable = configurable;
    }

    @Override
    public void configure() {

    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        ConfigManager.super.updateConfigs(configItems);
    }
}
