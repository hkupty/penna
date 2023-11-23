package penna.core.logger.guard;

import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;
import penna.api.config.Config;
import penna.core.internals.LogUnitContextPool;
import penna.core.logger.LogUnitContext;
import penna.core.logger.PennaLogger;

import java.util.EnumMap;

/**
 * The Level guard is a property of the {@link PennaLogger} that ensures a log only goes through
 * if the configured log level is set.
 * This is done in order for us to avoid runtime checks for "is X-level allowed".
 * <br />
 * By introducing the {@link LevelGuard} as a thin proxy in the logger we allow better control over the behavior
 * for the log levels in the logger.
 */
public sealed interface LevelGuard permits
        NOPGuard,
        TraceLevelGuard,
        DebugLevelGuard,
        InfoLevelGuard,
        WarnLevelGuard,
        ErrorLevelGuard {

    final class Shared {
        private static final LogUnitContextPool logUnits = new LogUnitContextPool();
    }

    final class FromConfig {

        private FromConfig() {
        }

        private static final EnumMap<Level, LevelGuard> levelMapping = new EnumMap<>(Level.class);

        static {
            levelMapping.put(Level.TRACE, TraceLevelGuard.singleton());
            levelMapping.put(Level.DEBUG, DebugLevelGuard.singleton());
            levelMapping.put(Level.INFO, InfoLevelGuard.singleton());
            levelMapping.put(Level.WARN, WarnLevelGuard.singleton());
            levelMapping.put(Level.ERROR, ErrorLevelGuard.singleton());
        }

        public static LevelGuard get(Config config) {
            return levelMapping.getOrDefault(config.level(), NOPGuard.singleton());
        }
    }


    default LogUnitContext get(PennaLogger logger, Level level) {
        var eventBuilder = Shared.logUnits.get();
        eventBuilder.reset(logger, level);

        return eventBuilder;
    }


    default boolean isTraceEnabled() {
        return false;
    }

    default LoggingEventBuilder trace(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    default boolean isDebugEnabled() {
        return false;
    }

    default LoggingEventBuilder debug(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    default boolean isInfoEnabled() {
        return true;
    }

    default LoggingEventBuilder info(PennaLogger logger) {
        return get(logger, Level.INFO);
    }

    default boolean isWarnEnabled() {
        return true;
    }

    default LoggingEventBuilder warn(PennaLogger logger) {
        return get(logger, Level.WARN);
    }

    default boolean isErrorEnabled() {
        return true;
    }

    default LoggingEventBuilder error(PennaLogger logger) {
        return get(logger, Level.ERROR);
    }
}
