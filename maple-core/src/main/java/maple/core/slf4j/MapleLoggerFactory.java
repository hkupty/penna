package maple.core.slf4j;

import maple.core.logger.MapleLogger;
import maple.api.models.Config;
import maple.core.logger.TreeCache;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class MapleLoggerFactory implements ILoggerFactory {
    private static final Pattern DOT_SPLIT = Pattern.compile("\\.");
    private static final String[] rootPath = new String[] {};
    private transient final TreeCache cache;
    private transient Config defaultConfig;

    public MapleLoggerFactory(){
        this(Config.getDefault());
    }

    public MapleLoggerFactory(Config config) {
        this.defaultConfig = config;
        var rootLogger = new MapleLogger("", defaultConfig);
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

    public void updateConfig(Config config) {
        defaultConfig = config;
        cache.updateConfig(rootPath, config);
    }
    public void updateConfig(Function<Config, Config> configUpdateFn) {
        defaultConfig = configUpdateFn.apply(defaultConfig);
        cache.updateConfig(rootPath, configUpdateFn);
    }
    public void updateConfig(String baseLogger, Config config) {
        var path = DOT_SPLIT.split(baseLogger);
        if (path.length == 1 && path[0].isEmpty()) {
            updateConfig(config);
        } else {
            cache.updateConfig(path, config);
        }
    }

    public void updateConfig(String baseLogger, Function<Config, Config> updateConfigFn) {
        var path = DOT_SPLIT.split(baseLogger);
        if (path.length == 1 && path[0].isEmpty()) {
            updateConfig(updateConfigFn);
        } else {
            cache.updateConfig(path, updateConfigFn);
        }
    }
}
