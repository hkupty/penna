package penna.api.config;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

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
    interface ConfigurationChange {
        /**
         * Simple lambda, strictly typed for convenience, allowing the creation of a new config, optionally using
         * the existing one as parameter
         * @param original Existing config (either previously configured or inherited)
         * @return new config
         */
        Config applyUpdate(Config original);
    }

    /**
     * Simple tuple between a loggerPath (String[] of logger name components) and a {@link ConfigurationChange}.
     * Note that the logger path will be used as a starting point for configuration update, and it will cascade down
     * the whole hierarchy.
     */
    sealed interface ConfigItem {
        /**
         * Internally, for simplicity, the data structures store the loggers based on each individual component of the
         * logger name (being the components joined by `.` in the final logger name), so configuration can be inherited
         * from a parent logger `com.app` on the child `com.app.service` logger.
         * <br />
         * @return a String array of all the logger components, for the point in the hierarchy where the equivalent
         * {@link #updateFn()} function will be applied.
         */
        String[] loggerPath();

        /**
         * The record or class implementing this interface should return here a function reference that conforms to
         * {@link ConfigurationChange} that will be applied for the point in the log hierarchy described at
         * {@link #loggerPath()}.
         * <br />
         * @return A {@link ConfigurationChange} function be used to create or update the config
         * for this {@link #loggerPath()}.
         */
        ConfigurationChange updateFn();

        /**
         * This is the direct implementation of {@link ConfigItem} and doesn't do anything other than holding the values.
         * @param loggerPath Specific point in the config hierarchy where the update function will be applied.
         * @param updateFn Function that will update (or overwrite) the existing configuration.
         */
        record LoggerPathConfigItem(String[] loggerPath, ConfigurationChange updateFn) implements ConfigItem {
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof LoggerPathConfigItem other)) return false;
                return other.updateFn.equals(updateFn) && Arrays.equals(other.loggerPath, loggerPath);
            }

            @Override
            public int hashCode() {
                var hash = 31 + Objects.hashCode(updateFn);
                hash = (31 * hash) +  Arrays.hashCode(loggerPath);

                return hash;
            }

            @Override
            public String toString() {
                return "LoggerPathConfigItem{" +
                        "loggerPath=" + Arrays.toString(loggerPath) +
                        ", updateFn=" + updateFn +
                        '}';
            }
        }

        /**
         * Convenience record that takes the logger name instead and breaks it into the required {@link #loggerPath()}.
         * @param loggerName Name of the logger being configured.
         * @param updateFn Function that will update (or overwrite) the existing configuration.
         */
        record LoggerConfigItem(CharSequence loggerName, ConfigurationChange updateFn) implements ConfigItem {
            private static final Pattern DOT_SPLIT = Pattern.compile("\\.");

            @Override
            public String[] loggerPath() {
                return DOT_SPLIT.split(loggerName);
            }
        }

        /**
         * Convenience record for configuring the root level of the hierarchy.
         * @param updateFn Config function that will be applied over all the levels of the hierarchy.
         */
        record RootConfigItem(ConfigurationChange updateFn) implements ConfigItem {
            private static final String[] ROOT_PATH = new String[]{};
            @Override
            public String[] loggerPath() {
                return ROOT_PATH;
            }
        }
    }


    /**
     * Binds the config manager to the {@link Configurable}. This method exists to allow the configuration
     * to happen at a different point in time than the creation of the instance.
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
     * @param configItems Sequence of {@link ConfigItem} to be applied.
     */
    void updateConfigs(ConfigItem... configItems);
}
