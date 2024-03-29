package penna.api.config;

import org.jetbrains.annotations.NotNull;
import penna.api.config.internal.ManagerImpl;
import penna.api.models.Config;

/**
 * A Config Storage defines a class that stores the configurations for all logs in the hierarchy.
 * <br/>
 * Such class is never expected to be called directly by its interface, but through the
 * {@link ManagerImpl} that will receive it.
 */
public interface Storage {

    /**
     * For all supplied configurations, this method ensures that they're reflected in its
     * internal structure.
     *
     * @param configs any number of configurations to be applied to any particular loggers, and they're hierarchical
     *                descendants.
     */
    void apply(ConfigToLogger... configs);

    /**
     * Returns the current configuration associated with the requested logger path.
     *
     * @param logger The path/name of the logger
     * @return The configuration for the logger
     */
    @NotNull
    Config get(@NotNull String logger);
}
