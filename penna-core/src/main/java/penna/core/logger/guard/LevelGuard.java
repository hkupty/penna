package penna.core.logger.guard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;
import penna.api.models.Config;
import penna.core.internals.LogUnitContextPool;
import penna.core.logger.LogUnitContext;
import penna.core.logger.PennaLogger;

/**
 * The Level guard is a property of the {@link PennaLogger} that ensures a log only goes through
 * if the configured log level is set.
 * This is done in order for us to avoid runtime checks for "is X-level allowed".
 * <br />
 * By introducing the {@link LevelGuard} as a thin proxy in the logger we allow better control over the behavior
 * for the log levels in the logger.
 */
public sealed interface LevelGuard permits DebugLevelGuard, ErrorLevelGuard, InfoLevelGuard, NOPGuard, TraceLevelGuard, WarnLevelGuard {

    final class Shared {
        @VisibleForTesting
        public static final LogUnitContextPool logUnits = new LogUnitContextPool();
    }

    final class FromConfig {

        private FromConfig() {
        }

        public static LevelGuard get(@NotNull Config config) {
            return switch (config.level()) {
                case ERROR -> ErrorLevelGuard.singleton();
                case WARN -> WarnLevelGuard.singleton();
                case INFO -> InfoLevelGuard.singleton();
                case DEBUG -> DebugLevelGuard.singleton();
                case TRACE -> TraceLevelGuard.singleton();
            };
        }
    }


    default LogUnitContext get(PennaLogger logger, Level level) {
        var eventBuilder = Shared.logUnits.get();
        eventBuilder.reset(logger, level);

        return eventBuilder;
    }

    Level level();

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
