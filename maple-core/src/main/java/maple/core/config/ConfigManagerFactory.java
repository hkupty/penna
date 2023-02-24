package maple.core.config;

import maple.api.config.Config;
import maple.api.config.ConfigManager;
import maple.api.config.Configurable;
import maple.core.minilog.MiniLogger;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class ConfigManagerFactory {

    private ConfigManagerFactory() {}

    private static ConfigManager instance;
    static ServiceLoader<ConfigManager> loader = ServiceLoader.load(ConfigManager.class);
    private static Iterator<ConfigManager> findConfigManagers(boolean refresh) {
        if (refresh) {
            loader.reload();
        }
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
                configurable.configure(new ConfigItem[]{
                        new ConfigItem.RootConfig(Config.getDefault())
                });
            }
        };
    }

    public static ConfigManager getConfigManager(){
        if (instance == null) {
            Iterator<ConfigManager> managerCandidates = findConfigManagers(true);
            if (!managerCandidates.hasNext()) {
                instance = inFlightConfigManager();
            } else {
                do {
                    try {
                        if((instance = managerCandidates.next()) != null) break;
                    } catch (Exception ex) {
                        MiniLogger.error("Unable to start manager due to exception", ex);
                    }
                } while (managerCandidates.hasNext());
            }
        }

        return instance;
    }
}
