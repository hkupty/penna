package penna.core.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import penna.api.config.ConfigManager;
import penna.api.config.Configurable;
import penna.core.logger.LoggerStorage;


public final class PennaLoggerFactory implements ILoggerFactory, Configurable {
    private static final PennaLoggerFactory singleton = new PennaLoggerFactory();
    private transient final LoggerStorage cache;

    public static PennaLoggerFactory getInstance() {
        return singleton;
    }

    @Override
    public void configure(ConfigManager.ConfigItem... configItems) {
        for (ConfigManager.ConfigItem configItem : configItems) {
            cache.updateConfig(configItem.loggerPath(), configItem.updateFn());
        }
    }

    private PennaLoggerFactory() {
        cache = new LoggerStorage();
    }

    @Override
    public Logger getLogger(String name) {
        return cache.getOrCreate(name);
    }
}
