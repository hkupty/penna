package maple.core.slf4j;

import maple.api.config.ConfigManager;
import maple.api.config.Configurable;
import maple.core.logger.MapleLogger;
import maple.api.config.Config;
import maple.core.logger.TreeCache;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Objects;
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

    private MapleLoggerFactory(){
        var rootLogger = new MapleLogger("", Config.getDefault());
        cache = new TreeCache(rootLogger);
    }

    @Override
    public Logger getLogger(String name) {
        String[] identifier = DOT_SPLIT.split(name);
        var logger = cache.find(identifier);
        if (Objects.isNull(logger)) {
            return cache.createRecursively(
                    identifier,
                    (parent, partialIdentifier) -> {
                        var loggerName = String.join(".", partialIdentifier);
                        return new MapleLogger(loggerName, parent.getConfig());
                    });
        }
        return logger;
    }
}
