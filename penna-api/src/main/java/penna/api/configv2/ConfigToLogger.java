package penna.api.configv2;

import penna.api.config.Config;

/**
 * Class exists to contextually bind a configuration object to a logger by its name.
 */
public sealed interface ConfigToLogger {

    String logger();

    Config config();

    /**
     * Applies the configuration to a specific logger or to a partial name for all the descendant
     * loggers in that hierarchy.
     *
     * @param logger The name of the logger
     * @param config The configuration object to be applied to that logger
     */
    record NamedLoggerConfigItem(String logger, Config config) implements ConfigToLogger {}

    /**
     * Applies the config for the root element.
     *
     * @param config The configuration object to be applied
     */
    record RootLoggerConfigItem(Config config) implements ConfigToLogger {
        @Override
        public String logger() {
            return "";
        }
    }
}
