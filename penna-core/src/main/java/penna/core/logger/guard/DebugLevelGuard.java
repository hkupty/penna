package penna.core.logger.guard;

import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import penna.core.logger.PennaLogger;

public final class DebugLevelGuard implements LevelGuard {
    private DebugLevelGuard() {
    }

    private static final LevelGuard instance = new DebugLevelGuard();

    public static LevelGuard singleton() {
        return instance;
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
