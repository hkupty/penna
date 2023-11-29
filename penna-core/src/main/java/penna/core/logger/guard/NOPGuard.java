package penna.core.logger.guard;

import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;
import penna.core.logger.PennaLogger;

public record NOPGuard() implements LevelGuard {

    private static final LevelGuard instance = new NOPGuard();

    public static LevelGuard singleton() {
        return instance;
    }

    @Override
    public Level level() {
        return null;
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
    public LoggingEventBuilder error(PennaLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

}
