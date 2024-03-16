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
    class Factory {
        private static final ServiceLoader<Provider> loader = ServiceLoader.load(Provider.class);
        private static ManagerImpl instance;

        public static ManagerImpl getInstance() {
            if (instance == null) throw new RuntimeException("ManagerImpl instance has not been initialized yet!");
            return instance;
        }

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

            return instance;
        }
    }

    void set(ConfigToLogger... configs);

    void set(String logger, Supplier<Config> action);

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
            storage.apply(new ConfigToLogger.NamedLoggerConfigItem(
                    logger,
                    action.get()
            ));
        }

        @Override
        public void update(String logger, Function<Config, Config> action) {
            var current = storage.get(logger);
            if (current != null) {
                storage.apply(new ConfigToLogger.NamedLoggerConfigItem(
                        logger,
                        action.apply(current)
                ));
            }
        }

        @Override
        public void close() throws IOException {
            providers.forEach(Provider::deregister);
        }
    }
}
