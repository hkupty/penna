package penna.core.logger.guard;

import penna.core.logger.PennaLogger;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public final class NOPGuard implements LevelGuard {

    private NOPGuard(){}
    private static final LevelGuard instance = new NOPGuard();
    public static LevelGuard singleton() {
        return instance;
    }
    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder trace(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder debug(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder info(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder warn(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder error(PennaLogger logger) { return NOPLoggingEventBuilder.singleton(); }
}
