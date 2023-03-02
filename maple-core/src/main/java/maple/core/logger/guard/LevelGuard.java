package maple.core.logger.guard;

import maple.api.config.Config;
import maple.core.logger.MapleLogger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.EnumMap;

/**
 * The Level guard is a property of the {@link MapleLogger} that proxies the
 * {@link maple.core.logger.event.MapleLogEventBuilder} creation.
 * This is done in order for us to avoid runtime checks for "is X-level allowed".
 * <br />
 * By introducing the {@link LevelGuard} as a thin proxy in the logger we allow better control over the behavior
 * for the log levels in the logger.
 */
public sealed interface LevelGuard permits
        NOPGuard,
        TraceLevelGuard,
        DebugLevelGuard,
        InfoLevelGuard,
        WarnLevelGuard,
        ErrorLevelGuard
{

    final class FromConfig {

        private FromConfig() {}

        private static final EnumMap<Level, LevelGuard> levelMapping = new EnumMap<>(Level.class);

        static {
            levelMapping.put(Level.DEBUG, DebugLevelGuard.singleton());
            levelMapping.put(Level.INFO, InfoLevelGuard.singleton());
            levelMapping.put(Level.WARN, WarnLevelGuard.singleton());
            levelMapping.put(Level.TRACE, TraceLevelGuard.singleton());
            levelMapping.put(Level.ERROR, ErrorLevelGuard.singleton());
        }

        public static LevelGuard get(Config config) {
            return levelMapping.getOrDefault(config.level(), NOPGuard.singleton());
        }

    }

    boolean isTraceEnabled();
    LoggingEventBuilder trace(MapleLogger logger);

    boolean isDebugEnabled();
    LoggingEventBuilder debug(MapleLogger logger);

    boolean isInfoEnabled();
    LoggingEventBuilder info(MapleLogger logger);

    boolean isWarnEnabled();
    LoggingEventBuilder warn(MapleLogger logger);

    boolean isErrorEnabled();
    LoggingEventBuilder error(MapleLogger logger);
}
