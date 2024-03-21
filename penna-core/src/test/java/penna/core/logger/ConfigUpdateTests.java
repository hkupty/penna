package penna.core.logger;

import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.slf4j.event.Level;
import penna.api.config.Config;
import penna.api.config.Manager;
import penna.core.logger.guard.*;
import penna.core.slf4j.PennaLoggerFactory;

public class ConfigUpdateTests {

    @CartesianTest
    void canSetTheConfig(@CartesianTest.Enum Level level) {
        PennaLoggerFactory factory = PennaLoggerFactory.getInstance();
        PennaLogger logger = (PennaLogger) factory.getLogger(ConfigUpdateTests.class.getName());
        Manager.Factory.initialize(factory);
        Manager manager = Manager.Factory.getInstance();
        manager.set(ConfigUpdateTests.class.getName(), () -> Config.getDefault().replaceLevel(level));
        var target = switch (level) {
            case ERROR -> ErrorLevelGuard.class;
            case WARN -> WarnLevelGuard.class;
            case INFO -> InfoLevelGuard.class;
            case DEBUG -> DebugLevelGuard.class;
            case TRACE -> TraceLevelGuard.class;
        };
        Assertions.assertInstanceOf(target, logger.levelGuard);
    }

    @CartesianTest
    void canUpdateTheConfig(@CartesianTest.Enum Level level) {
        PennaLoggerFactory factory = PennaLoggerFactory.getInstance();
        PennaLogger logger = (PennaLogger) factory.getLogger(ConfigUpdateTests.class.getName());
        Manager.Factory.initialize(factory);
        Manager manager = Manager.Factory.getInstance();
        manager.update(ConfigUpdateTests.class.getName(), (base) -> base.replaceLevel(level));
        var target = switch (level) {
            case ERROR -> ErrorLevelGuard.class;
            case WARN -> WarnLevelGuard.class;
            case INFO -> InfoLevelGuard.class;
            case DEBUG -> DebugLevelGuard.class;
            case TRACE -> TraceLevelGuard.class;
        };
        Assertions.assertInstanceOf(target, logger.levelGuard);
    }
}
