package maple.api.config;

import java.util.regex.Pattern;

/**
 * The config manager is a class that binds to a {@link Configurable}, most notably the MapleLoggerFactory,
 * and can configure it. It is mandatory that {@link ConfigManager#configure()} is implemented correctly,
 * so than at least the initial set up of the logger factory has the adequate levels and fields to log.
 * <br />
 * Also, {@link ConfigManager#bind(Configurable)} is required so then maple.core's implementation of
 * {@link org.slf4j.spi.SLF4JServiceProvider} can bind the ConfigManager instance to the LoggerFactory.
 * <br />
 * Finally, event though a convenience method, it is nice that {@link ConfigManager#updateConfigs(Reconfiguration...)}
 * is implemented, because that would allow for runtime reconfiguration of a logger. It uses {@link Reconfiguration} instead
 * of {@link ConfigItem} because the former uses the {@link FunctionalInterface} {@link UpdateConfigFn}, ensuring we can
 * partially update a value if we want to.
 * <br />
 * For example, if one just wants to change the log level without changing the fields to be logged:
 * <pre>
 * myConfigManager.updateConfigs(RootReconfigure(oldConfig -> oldConfig.copy(Level.DEBUG)));
 * </pre>
 * Another case would be adding or removing fields from a specific logger:
 * <pre>
 * import java.util.EnumSet
 * myConfigManager.updateConfigs(
 *   LoggerConfig("com.my.app.controller", oldConfig -> {
 *      var newFields = EnumSet.of(oldConfig.fields[0], oldConfig.fields);
 *      newFields.add(LogFields.Counter);
 *      newFields.remove(LogFields.MDC);
 *      return oldConfig.copy(newFields);
 *   }));
 * </pre>
 */
public interface ConfigManager {
    @FunctionalInterface
    interface UpdateConfigFn {
        Config applyUpdate(Config original);
    }

    /**
     * Different implementations of the ConfigManager library might opt to work with different kinds of
     * values, either directly supplying the {@link ConfigItem#loggerPath()} or reading the string directly.
     */
    sealed interface ConfigItem {
        String[] loggerPath();
        Config config();

        /**
         * Raw implementation, basically a named tuple for the configuration fields.
         * @param loggerPath String array with the components of the logger. i.e. {@code new String[]{"com", "my", "app"}}
         * @param config {@link Config} to be applied to this path
         */
        record LoggerPathConfig(String[] loggerPath, Config config) implements ConfigItem {}

        /**
         * Convenience wrapper over the internal format, allows for a simple string instead of the
         * internally used loggerPath.
         * @param logger Name of the logger. i.e. {@code "com.my.app"}
         * @param config {@link Config} to be applied to this path
         */
        record LoggerConfig(CharSequence logger, Config config) implements ConfigItem {
            private static final Pattern DOT_SPLIT = Pattern.compile("\\.");
            @Override
            public String[] loggerPath() {
                return DOT_SPLIT.split(logger);
            }
        }

        /**
         * Convenience implementation for the config used to update the entire config tree.
         * @param config {@link Config} to be applied to the root logger
         */
        record RootConfig(Config config) implements ConfigItem {
            private static final String[] ROOT_PATH = new String[]{};
            @Override
            public String[] loggerPath() {
                return ROOT_PATH;
            }
        }
    }

    sealed interface Reconfiguration {
        String[] loggerPath();
        UpdateConfigFn updateFn();

        record LoggerPathReconfigure(String[] loggerPath, UpdateConfigFn updateFn) implements Reconfiguration {}

        record LoggerReconfigure(CharSequence loggerName, UpdateConfigFn updateFn) implements Reconfiguration {
            private static final Pattern DOT_SPLIT = Pattern.compile("\\.");

            @Override
            public String[] loggerPath() {
                return DOT_SPLIT.split(loggerName);
            }
        }

        record RootReconfigure(UpdateConfigFn updateFn) implements Reconfiguration {
            private static final String[] ROOT_PATH = new String[]{};
            @Override
            public String[] loggerPath() {
                return ROOT_PATH;
            }
        }
    }


    void bind(Configurable configurable);
    void configure();
    void updateConfigs(Reconfiguration... reconfigurations);
}
