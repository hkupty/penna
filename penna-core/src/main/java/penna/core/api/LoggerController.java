package penna.core.api;

import org.slf4j.event.Level;
import penna.api.config.Manager;
import penna.api.config.Provider;
import penna.core.internals.ManagerHolder;

/**
 * This is class provides the runtime with a façade for simple runtime control over the loggers.
 * For a finer level of control, resort to implementing a custom {@link Provider} instead.
 */
public class LoggerController {

    private LoggerController() {}

    /**
     * Convenience runtime function for changing the logger level.
     * For a finer level of control, resort to implementing a custom {@link Provider}
     * @param loggerName Name of the logger (or prefix of the logger) for the level change
     * @param level The target level for supplied logger
     */
    public static void changeLoggerLevel(String loggerName, Level level) {
        Manager instance;
        if ((instance = ManagerHolder.getInstance()) != null) {
            instance.update(loggerName, config -> config.replaceLevel(level));
        }
    }

    /**
     * Convenience runtime function for changing the logger level.
     * For a finer level of control, resort to implementing a custom {@link Provider}
     * @param klass The class whose name is assigned to the logger
     * @param level The target level for supplied logger
     */
    public static void changeLoggerLevel(Class<?> klass, Level level) { changeLoggerLevel(klass.getName(), level);}
}
