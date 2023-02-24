package maple.core.logger.guard;

import maple.core.logger.MapleLogger;
import maple.core.logger.event.MapleLogEventBuilder;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public final class InfoLevelGuard implements LevelGuard {
    private InfoLevelGuard(){}

    private static final LevelGuard instance = new InfoLevelGuard();
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
        return false;
    }

    @Override
    public LoggingEventBuilder debug(MapleLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder info(MapleLogger logger) {
        return MapleLogEventBuilder.Factory.get(logger, Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder warn(MapleLogger logger) {
        return MapleLogEventBuilder.Factory.get(logger, Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder error(MapleLogger logger) {
        return MapleLogEventBuilder.Factory.get(logger, Level.ERROR);
    }
}
