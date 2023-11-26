package penna.core.config;

import penna.api.config.ConfigManager;
import penna.api.config.Configurable;
import penna.core.minilog.MiniLogger;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class ConfigManagerFactory {

    private ConfigManagerFactory() {
    }

    private static ConfigManager instance;
    static ServiceLoader<ConfigManager> loader = ServiceLoader.load(ConfigManager.class);

    private static Iterator<ConfigManager> findConfigManagers() {
        loader.reload();
        return loader.iterator();
    }

    private static ConfigManager inFlightConfigManager() {
        return new ConfigManager() {
            private Configurable configurable;

            @Override
            public void bind(Configurable configurable) {
                this.configurable = configurable;
            }

            @Override
            public void configure() {
                // Do nothing, PennaLoggerFactory already initialized with the defaultConfig
            }

            @Override
            public void updateConfigs(ConfigItem... configItems) {
                configurable.configure(configItems);
            }
        };
    }

    public static ConfigManager getConfigManager() {
        if (instance == null) {
            Iterator<ConfigManager> managerCandidates = findConfigManagers();
            if (!managerCandidates.hasNext()) {
                instance = inFlightConfigManager();
            } else {
                do {
                    try {
                        if ((instance = managerCandidates.next()) != null) break;
                    } catch (Exception ex) {
                        MiniLogger.error("Unable to start manager due to exception", ex);
                    }
                } while (managerCandidates.hasNext());
            }
        }

        return instance;
    }
}
