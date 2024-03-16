package penna.api.configv2;

import java.util.function.Function;

/**
 * This interface allows for additional configuration providers to interact with the logger setup.
 * One such example is the yaml configuration project.
 * <br />
 * A custom provider could also be implemented, for example, to reduce or increase the log level
 * based on heuristics or custom behavior
 */
public interface Provider {
    /**
     * Register the {@link Manager} instance so the provider can update configuration
     * through {@link Manager#set(ConfigToLogger...)} or {@link Manager#update(String, Function)}
     *
     * @param manager The manager instance;
     * @return Returns whether the provider successfully registered
     */
    boolean register(Manager manager);

    /**
     * This method should be called when and if the {@link Manager} is closed,
     * allowing the provider to perform any clean-up activity.
     * <br />
     * As the instance was likely initiated by {@link Manager.Factory} upon creation,
     * this is a chance to close any threads created at register/startup or release any
     * pending resources.
     */
    void deregister();
}
