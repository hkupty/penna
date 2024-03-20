package penna.api.config;

/**
 * Class exists to contextually bind a configuration object to a logger by its name.
 */
public sealed interface ConfigToLogger {

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
    record RootLoggerConfigItem(Config config) implements ConfigToLogger {}
}
