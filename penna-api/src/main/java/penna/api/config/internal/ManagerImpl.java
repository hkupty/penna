package penna.api.config.internal;

import penna.api.config.ConfigToLogger;
import penna.api.config.Manager;
import penna.api.config.Provider;
import penna.api.config.Storage;
import penna.api.models.Config;

import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This is the central piece of the configuration management.
 * It should not be created manually, but instead through {@link Manager#create(Storage)}.
 */
public final class ManagerImpl implements Manager {
    /**
     * The service loader for Providers
     */
    public static final ServiceLoader<Provider> loader = ServiceLoader.load(Provider.class);

    private final Storage storage;

    /**
     * Initializes the Manager with an instance of {@link Storage}
     * @param storage The component that stores loggers and the respective configuration
     */
    public ManagerImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void set(ConfigToLogger... configs) {
        storage.apply(configs);
    }

    @Override
    public void set(String logger, Supplier<Config> action) {
        Config config = action.get();
        ConfigToLogger item = switch (logger) {
            case String path when path.isEmpty() -> new ConfigToLogger.RootLoggerConfigItem(config);
            case String path -> new ConfigToLogger.NamedLoggerConfigItem(path, config);
        };

        storage.apply(item);
    }

    @Override
    public void update(String logger, Function<Config, Config> action) {
        var current = storage.get(logger);
        Config config = action.apply(current);
        ConfigToLogger item = switch (logger) {
            case String path when path.isEmpty() -> new ConfigToLogger.RootLoggerConfigItem(config);
            case String path -> new ConfigToLogger.NamedLoggerConfigItem(path, config);
        };

        storage.apply(item);
    }
}
