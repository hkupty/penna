package penna.core.logger.guard;

import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import penna.core.logger.PennaLogger;

public final class TraceLevelGuard implements LevelGuard {

    private TraceLevelGuard() {
    }

    private static final LevelGuard instance = new TraceLevelGuard();

    public static LevelGuard singleton() {
        return instance;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder trace(PennaLogger logger) {
        return get(logger, Level.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder debug(PennaLogger logger) {
        return get(logger, Level.DEBUG);
    }

}
