package penna.api.configv2;

import penna.api.config.Config;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface describes the set of actions a {@link Provider} can access from the {@link ManagerImpl}.
 * This allows the ManagerImpl to evolve its internal configuration while still encapsulating its behavior
 * from outside third-parties.
 */
public sealed interface Manager {
    /**
     * This is where the concrete implementation of a {@link Manager} will be created and stored
     */
    class Factory {
        private Factory() {}

        private static final ServiceLoader<Provider> loader = ServiceLoader.load(Provider.class);
        private static ManagerImpl instance;

        /**
         * Returns a concrete implementation of {@link Manager} once and if it's created, otherwise fails with an
         * exception.
         *
         * @return a concrete implementation of {@link Manager}.
         */
        public static ManagerImpl getInstance() {
            if (instance == null) throw new RuntimeException("ManagerImpl instance has not been initialized yet!");
            return instance;
        }

        /**
         * Used to initialize the {@link Manager} for a given {@link Storage} implementation.
         *
         * @param storage The concrete {@link Storage} implementation that will effectively store the configuration.
         * @return the initialized concrete {@link Manager} instance.
         */
        public static ManagerImpl initialize(Storage storage) {
            if (instance != null) throw new RuntimeException("ManagerImpl instance has already been initialized!");
            loader.reload();
            instance = new ManagerImpl(storage);
            var providers = loader.stream()
                    .map(provider -> {
                        try {
                            return provider.get();
                        } catch (Throwable ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(provider -> provider.register(instance))
                    .toList();

            instance.attachProviders(providers);

            providers.forEach(Provider::init);

            return instance;
        }
    }

    /**
     * Sets a number of configurations to the {@link Storage}
     *
     * @param configs a number of configuration items to update
     */
    void set(ConfigToLogger... configs);

    /**
     * Sets a single configuration item to a particular logger path, wrapping in a {@link ConfigToLogger} concrete
     * instance.
     *
     * @param logger The path of a logger. Use "" for the root.
     * @param action A function that produces a {@link Config} element to be applied to the supplied logger.
     */
    void set(String logger, Supplier<Config> action);

    /**
     * For a given logger path, it will produce a new configuration based on the previous one.
     *
     * @param logger The path of a logger. Use "" for the root.
     * @param action A function that transforms a {@link Config} element into a new one to be applied for the supplied logger.
     */
    void update(String logger, Function<Config, Config> action);

    /**
     * This is the central piece of the configuration management.
     * It should not be created manually, but instead through {@link Factory#initialize(Storage)} and subsequent
     * instance requests fetched through {@link Factory#getInstance()}.
     */
    final class ManagerImpl implements Manager, Closeable {
        private final Storage storage;
        private List<Provider> providers;

        ManagerImpl(Storage storage) {
            this.storage = storage;
        }

        void attachProviders(List<Provider> providers) {
            this.providers = providers;
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
            if (current != null) {
                Config config = action.apply(current);
                ConfigToLogger item = switch (logger) {
                    case String path when path.isEmpty() -> new ConfigToLogger.RootLoggerConfigItem(config);
                    case String path -> new ConfigToLogger.NamedLoggerConfigItem(path, config);
                };

                storage.apply(item);
            }
        }

        @Override
        public void close() throws IOException {
            providers.forEach(Provider::deregister);
        }
    }
}
