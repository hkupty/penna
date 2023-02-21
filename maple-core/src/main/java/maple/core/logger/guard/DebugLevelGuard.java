package maple.core.logger.guard;

import maple.core.logger.event.JsonLogEventBuilder;
import maple.core.logger.MapleLogger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public final class DebugLevelGuard implements LevelGuard {
    private DebugLevelGuard(){}

    private static final LevelGuard instance = new DebugLevelGuard();
    public static LevelGuard singleton() {
        return instance;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder trace(MapleLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder debug(MapleLogger logger) {
        return JsonLogEventBuilder.Factory.get(logger, Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder info(MapleLogger logger) {
        return JsonLogEventBuilder.Factory.get(logger, Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder warn(MapleLogger logger) {
        return JsonLogEventBuilder.Factory.get(logger, Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder error(MapleLogger logger) {
        return JsonLogEventBuilder.Factory.get(logger, Level.ERROR);
    }
}
