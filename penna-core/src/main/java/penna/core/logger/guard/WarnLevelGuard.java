package penna.core.logger.guard;

import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;
import penna.core.logger.PennaLogger;

public final class WarnLevelGuard implements LevelGuard {

    private WarnLevelGuard() {
    }

    private static final LevelGuard instance = new WarnLevelGuard();

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

}
