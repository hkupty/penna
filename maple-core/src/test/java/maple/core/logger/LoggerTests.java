package maple.core.logger;

import maple.api.config.ConfigManager;
import maple.api.config.ConfigManager.ConfigItem.LoggerConfigItem;
import maple.core.logger.guard.*;
import maple.core.slf4j.MapleLoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

public class LoggerTests {

    @Test
    public void log_levels_are_respected() {
        var factory = MapleLoggerFactory.getInstance();

        var logger = factory.getLogger("test");
        if (!(logger instanceof MapleLogger mapleLogger)) {
            Assertions.fail("MapleLoggerFactory is not returning an MapleLogger");
            return;
        }

        // null-level, no logs
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(null)));
        Assertions.assertEquals(NOPGuard.singleton(), mapleLogger.levelGuard);

        // Trace
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.TRACE)));
        Assertions.assertEquals(TraceLevelGuard.singleton(), mapleLogger.levelGuard);

        // Debug
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.DEBUG)));
        Assertions.assertEquals(DebugLevelGuard.singleton(), mapleLogger.levelGuard);

        // Info
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.INFO)));
        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);

        // Warn
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.WARN)));
        Assertions.assertEquals(WarnLevelGuard.singleton(), mapleLogger.levelGuard);

        // Error
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.ERROR)));
        Assertions.assertEquals(ErrorLevelGuard.singleton(), mapleLogger.levelGuard);
    }

    @Test
    public void can_write_log_messages() {

    }
}
