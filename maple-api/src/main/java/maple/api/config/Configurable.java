package maple.api.config;

/**
 * Defines how {@code maple.core}'s implementation of {@link org.slf4j.LoggerFactory} will configure the loggers.
 * <br />
 * This interface exists detached from {@code maple.core} just to allow {@link ConfigManager} to define the
 * {@link ConfigManager#bind(Configurable)} method.
 */
public interface Configurable {
    /**
     * Apply given {@link maple.api.config.ConfigManager.ConfigItem}s to the respective loggers.
     * @param configItems Sequence of configurations to be applied.
     */
    void configure(ConfigManager.ConfigItem[] configItems);
}