package penna.dev.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import penna.api.config.ConfigManager;
import penna.api.config.Configurable;

import java.util.ServiceLoader;

public class DevConfigManager implements ConfigManager {
    final ConfigManager proxied;

    public DevConfigManager() {
        ServiceLoader<ConfigManager> serviceLoader = ServiceLoader.load(ConfigManager.class);
        var other = serviceLoader.stream()
                .filter(configManagerProvider -> configManagerProvider.type() != DevConfigManager.class)
                .findFirst()
                .map(ServiceLoader.Provider::get);

        if (other.isPresent()) {
            proxied = new ProxyDevConfigManager(other.get());
        } else {
            proxied = new StandalondeDevConfigManager();
        }
    }

    @Override
    public void bind(Configurable configurable) {
        proxied.bind(configurable);
    }

    @Override
    public void configure() {
        proxied.configure();
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        proxied.updateConfigs(configItems);
    }
}
