package penna.core.logger.guard;

import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;
import penna.core.logger.PennaLogger;

public final class ErrorLevelGuard implements LevelGuard {

    private ErrorLevelGuard() {
    }

    private static final LevelGuard instance = new ErrorLevelGuard();

    public static LevelGuard singleton() {
        return instance;
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

}
