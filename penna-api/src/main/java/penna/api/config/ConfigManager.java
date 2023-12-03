package penna.api.config;

import java.util.function.UnaryOperator;

/**
 * The config manager is a class that binds to a {@link Configurable}, most notably the PennaLoggerFactory,
 * and can configure it. It is mandatory that {@link ConfigManager#configure()} is implemented correctly,
 * so than at least the initial set up of the logger factory has the adequate levels and fields to log.
 * <br />
 * Also, {@link ConfigManager#bind(Configurable)} is required so then penna.core's implementation of
 * {@link org.slf4j.spi.SLF4JServiceProvider} can bind the ConfigManager instance to the LoggerFactory.
 * <br />
 * Finally, event though a convenience method, it is nice that {@link ConfigManager#updateConfigs(ConfigItem...)}
 * is implemented, because that would allow for runtime reconfiguration of a logger. It uses {@link ConfigItem} instead
 * of {@link ConfigItem} because the former uses the {@link FunctionalInterface} {@link ConfigurationChange}, ensuring we can
 * partially update a value if we want to.
 * <br />
 * For example, if one just wants to change the log level without changing the fields to be logged:
 * <pre>
 * myConfigManager.updateConfigs(RootConfigItem(oldConfig -> oldConfig.copy(Level.DEBUG)));
 * </pre>
 * Another case would be adding or removing fields from a specific logger.
 * Note that the order of the fields matter, since this is the order they'll be rendered.
 */
public interface ConfigManager {

    /**
     * The Configuration change {@link FunctionalInterface} allows us to define a lambda or extract a function
     * reference that directly applies a new {@link Config}, optionally using the existing value.
     */
    @FunctionalInterface
    interface ConfigurationChange extends UnaryOperator<Config> {
    }

    /**
     * Simple tuple between a loggerPath (String[] of logger name components) and a {@link ConfigurationChange}.
     * Note that the logger path will be used as a starting point for configuration update, and it will cascade down
     * the whole hierarchy.
     */
    sealed interface ConfigItem {
        /**
         * The string that represents the path/prefix for a configuration to be applied. Can point to a full logger name
         * or just a portion of the name for the configuration to be hierarchically applied to all loggers matching the
         * prefix.
         */
        String loggerPath();

        /**
         * The record or class implementing this interface should return here a function reference that conforms to
         * {@link ConfigurationChange} that will be applied for the point in the log hierarchy described at
         * {@link #loggerPath()}.
         * <br />
         *
         * @return A {@link ConfigurationChange} function be used to create or update the config
         * for this {@link #loggerPath()}.
         */
        ConfigurationChange updateFn();

        /**
         * Convenience record that takes the logger name instead and breaks it into the required {@link #loggerPath()}.
         *
         * @param loggerName Name of the logger being configured.
         * @param updateFn   Function that will update (or overwrite) the existing configuration.
         */
        record LoggerConfigItem(CharSequence loggerName, ConfigurationChange updateFn) implements ConfigItem {

            @Override
            public String loggerPath() {
                return loggerName.toString();
            }
        }

        /**
         * Convenience record for configuring the root level of the hierarchy.
         *
         * @param updateFn Config function that will be applied over all the levels of the hierarchy.
         */
        record RootConfigItem(ConfigurationChange updateFn) implements ConfigItem {

            @Override
            public String loggerPath() {
                return "";
            }
        }
    }


    /**
     * Binds the config manager to the {@link Configurable}. This method exists to allow the configuration
     * to happen at a different point in time than the creation of the instance.
     *
     * @param configurable Typically, the class implementing {@link org.slf4j.LoggerFactory} in {@code penna.core}.
     */
    void bind(Configurable configurable);

    /**
     * Given the {@link Configurable} is bound, applies the loaded (or default) configurations.
     */
    void configure();

    /**
     * Utility method to allow for runtime config updates.
     * <br />
     * When this method is implemented, consumers can get hold of the {@link ConfigManager} instance and change
     * values in runtime without having access to {@code penna.core} internals.
     *
     * @param configItems Sequence of {@link ConfigItem} to be applied.
     */
    void updateConfigs(ConfigItem... configItems);
}
