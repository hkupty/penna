package maple.core.logger.guard;

import maple.core.logger.MapleLogger;
import maple.core.logger.event.JsonLogEventBuilder;
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
    public LoggingEventBuilder trace(MapleLogger logger) {
        return JsonLogEventBuilder.Factory.get(logger, Level.TRACE);
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
