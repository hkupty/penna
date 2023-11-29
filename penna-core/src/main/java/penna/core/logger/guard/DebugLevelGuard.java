package penna.core.logger.guard;

import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import penna.core.logger.PennaLogger;

public record DebugLevelGuard() implements LevelGuard {
    private static final LevelGuard instance = new DebugLevelGuard();

    public static LevelGuard singleton() {
        return instance;
    }

    @Override
    public Level level() {
        return Level.DEBUG;
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
