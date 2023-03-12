package penna.core.slf4j;

import penna.api.config.ConfigManager;
import penna.api.config.Configurable;
import penna.api.config.Config;
import penna.core.logger.TreeCache;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.regex.Pattern;

public final class PennaLoggerFactory implements ILoggerFactory, Configurable {
    private static final Pattern DOT_SPLIT = Pattern.compile("\\.");
    private static final PennaLoggerFactory singleton = new PennaLoggerFactory();
    private transient final TreeCache cache;

    public static PennaLoggerFactory getInstance(){
        return singleton;
    }


    @Override
    public void configure(ConfigManager.ConfigItem... configItems) {
        for (ConfigManager.ConfigItem configItem : configItems) {
            cache.updateConfig(configItem.loggerPath(), configItem.updateFn());
        }
    }

    private PennaLoggerFactory(){
        cache = new TreeCache(Config.getDefault());
    }

    @Override
    public Logger getLogger(String name) {
        return cache.getLoggerAt(DOT_SPLIT.split(name));
    }
}
