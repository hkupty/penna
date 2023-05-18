package penna.core.logger.guard;

import penna.core.logger.PennaLogger;
import penna.core.logger.PennaLogEventBuilder;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

public final class TraceLevelGuard implements LevelGuard {

    private TraceLevelGuard(){}
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
        return PennaLogEventBuilder.Factory.get(logger, Level.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder debug(PennaLogger logger) {
        return PennaLogEventBuilder.Factory.get(logger, Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder info(PennaLogger logger) {
        return PennaLogEventBuilder.Factory.get(logger, Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder warn(PennaLogger logger) {
        return PennaLogEventBuilder.Factory.get(logger, Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder error(PennaLogger logger) {
        return PennaLogEventBuilder.Factory.get(logger, Level.ERROR);
    }
}
