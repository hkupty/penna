package penna.core.slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import penna.api.models.Config;
import penna.api.config.ConfigToLogger;
import penna.api.config.Storage;
import penna.core.logger.LoggerStorage;


public final class PennaLoggerFactory implements ILoggerFactory, Storage {
    private static final PennaLoggerFactory singleton = new PennaLoggerFactory();
    private transient final LoggerStorage cache;

    public static PennaLoggerFactory getInstance() {
        return singleton;
    }

    @VisibleForTesting
    PennaLoggerFactory() {
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
    public @NotNull Config get(@NotNull String logger) {
        return cache.getConfig(logger);
    }
}
