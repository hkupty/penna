package maple.core.logger.guard;

import maple.api.models.Config;
import maple.core.logger.MapleLogger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.EnumMap;

public sealed interface LevelGuard permits
        NOPGuard,
        TraceLevelGuard,
        DebugLevelGuard,
        InfoLevelGuard,
        WarnLevelGuard,
        ErrorLevelGuard
{

    class FromConfig {
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
