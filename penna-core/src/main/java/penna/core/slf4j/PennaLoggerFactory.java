package penna.core.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import penna.api.config.Config;
import penna.api.configv2.ConfigToLogger;
import penna.api.configv2.Storage;
import penna.core.logger.LoggerStorage;


public final class PennaLoggerFactory implements ILoggerFactory, Storage {
    private static final PennaLoggerFactory singleton = new PennaLoggerFactory();
    private transient final LoggerStorage cache;

    public static PennaLoggerFactory getInstance() {
        return singleton;
    }

    private PennaLoggerFactory() {
        cache = new LoggerStorage();
    }

    @Override
    public Logger getLogger(String name) {
        return cache.getOrCreate(name);
    }

    // Config V2 interface
    @Override
    public void apply(ConfigToLogger... configs) {
        for (var config : configs) {
            switch (config) {
                case ConfigToLogger.RootLoggerConfigItem root -> cache.replaceConfig(root.config());
                case ConfigToLogger.NamedLoggerConfigItem named -> cache.replaceConfig(named.logger(), named.config());
            }

        }
    }

    @Override
    public Config get(String logger) {
        return cache.getConfig(logger);
    }
}
