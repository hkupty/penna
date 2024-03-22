package penna.core.internals;

import org.jetbrains.annotations.Nullable;
import penna.api.config.Manager;

/**
 * This class exists as a middle ground for both the concrete implementation and the {@link Manager}
 * so the creation of the Manager instance, which happens at the concrete implementation during runtime, can
 * still provide the Manager's interface with an instance for its static methods.
 */
public class ManagerHolder {
    private ManagerHolder() {}
    private static Manager instance;

    /**
     * After creating an instance, this method should be called to store it for "global" availability;
     * @param manager The concrete {@link Manager} instance.
     */
    public static void setManager(Manager manager) { instance = manager; }

    /**
     * Returns the stored Manager.
     * @return the stored Manager.
     */
    public static @Nullable Manager getInstance() { return instance; }
}
