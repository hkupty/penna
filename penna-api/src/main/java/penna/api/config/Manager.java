package penna.api.config;

import penna.api.config.internal.ManagerImpl;
import penna.api.models.Config;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface describes the set of actions a {@link Provider} can access from the {@link ManagerImpl}.
 * This allows the ManagerImpl to evolve its internal configuration while still encapsulating its behavior
 * from outside third-parties.
 */
public sealed interface Manager permits ManagerImpl {


    /**
     * This method initializes and creates a manager, but does not replace the existing one.
     * @param storage The implementation of the logger storage that holds the configurations
     * @return a new initialized instance of the manager that initialized {@link Provider}s
     * and registered itself with them
     */
    static Manager create(Storage storage) {
        ManagerImpl.loader.reload();
        var instance = new ManagerImpl(storage);
        var providers = ManagerImpl.loader.stream()
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

        providers.forEach(Provider::init);

        return instance;
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

}
