package maple.api.config;

import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * The config manager is a class that binds to a {@link Configurable}, most notably the MapleLoggerFactory,
 * and can configure it. It is mandatory that {@link ConfigManager#configure()} is implemented correctly,
 * so than at least the initial set up of the logger factory has the adequate levels and fields to log.
 * Also, {@link ConfigManager#bind(Configurable)} is required so then maple.core's implementation of
 * {@link org.slf4j.spi.SLF4JServiceProvider} can bind the ConfigManager instance to the LoggerFactory.
 * <br />
 * Additionally, the other method(s) in this interface are optional to allow for runtime configuration.
 * Note that not implementing them will throw errors to the user, which might be undesired, so, unless
 * your implementation strictly forbids changing the config in runtime, it is advisable to implement
 * those methods as well.
 */
public interface ConfigManager {
    /**
     * Different implementations of the ConfigManager library might opt to work with different kinds of
     * values, either directly supplying the {@link ConfigItem#loggerPath()} or reading the string directly.
     */
    sealed interface ConfigItem {
        String[] loggerPath();
        Config config();

        /**
         * Raw implementation, basically a named tuple for the configuration fields.
         * @param loggerPath
         * @param config
         */
        record LoggerPathConfig(String[] loggerPath, Config config) implements ConfigItem {}

        /**
         * Convenience wrapper over the internal format, allows for a simple string instead of the
         * internally used loggerPath.
         * @param logger
         * @param config
         */
        record LoggerConfig(String logger, Config config) implements ConfigItem {
            private static final Pattern DOT_SPLIT = Pattern.compile("\\.");
            @Override
            public String[] loggerPath() {
                return DOT_SPLIT.split(logger);
            }
        }

        /**
         * Convenience implementation for the config used to update the entire config tree.
         * @param config
         */
        record RootConfig(Config config) implements ConfigItem {
            @Override
            public String[] loggerPath() {
                return new String[] {};
            }
        }

        /**
         * Config record for dynamic values, coming from supply functions.
         * @param logger
         * @param configSupplier
         */
        record DynamicConfig(Supplier<String> logger, Supplier<Config> configSupplier) implements ConfigItem {
            private static final Pattern DOT_SPLIT = Pattern.compile("\\.");

            @Override
            public String[] loggerPath() {
                return DOT_SPLIT.split(logger.get());
            }

            @Override
            public Config config() {
                return configSupplier.get();
            }
        }
    }

    void bind(Configurable configurable);
    void configure();

    default void updateConfigs(ConfigItem... configItems) {
        throw new UnsupportedOperationException(
                "Current implementation of ConfigManager doesn't support updating the config in runtime"
        );
    }
}
