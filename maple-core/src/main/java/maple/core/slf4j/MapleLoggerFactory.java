package maple.core.slf4j;

import maple.api.config.ConfigManager;
import maple.api.config.Configurable;
import maple.api.config.Config;
import maple.core.logger.TreeCache;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.regex.Pattern;

public final class MapleLoggerFactory implements ILoggerFactory, Configurable {
    private static final Pattern DOT_SPLIT = Pattern.compile("\\.");
    private static final MapleLoggerFactory singleton = new MapleLoggerFactory();
    private transient final TreeCache cache;

    public static MapleLoggerFactory getInstance(){
        return singleton;
    }

    @Override
    public void configure(ConfigManager.ConfigItem[] configItems) {
        for (int i = 0; i < configItems.length; i++) {
            var config = configItems[i];
            cache.updateConfig(config.loggerPath(), config.config());
        }
    }

    @Override
    public void reconfigure(ConfigManager.Reconfiguration[] reconfigurations) {
        for (int i = 0; i < reconfigurations.length; i++) {
            var reconfig = reconfigurations[i];
            cache.updateConfig(reconfig.loggerPath(), reconfig.updateFn());
        }

    }

    private MapleLoggerFactory(){
        cache = new TreeCache(Config.getDefault());
    }

    @Override
    public Logger getLogger(String name) {
        return cache.getLoggerAt(DOT_SPLIT.split(name));
    }
}
