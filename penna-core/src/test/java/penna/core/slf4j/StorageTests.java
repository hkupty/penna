package penna.core.slf4j;

import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.slf4j.event.Level;
import penna.api.models.Config;
import penna.api.config.ConfigToLogger;
import penna.api.config.Storage;

/**
 * This class allows us to test the {@link PennaLoggerFactory} from the perspective of the
 */
public class StorageTests {

    enum LoggerOption {
        Root(""),
        Simple("simple"),
        Complex("com.with.levels");

        private final String path;

        LoggerOption(String s) {
            this.path = s;
        }
    }

    @CartesianTest
    void doSomething(
            @CartesianTest.Enum Level level,
            @CartesianTest.Enum LoggerOption logger
    ) {
        Storage storage = new PennaLoggerFactory();
        Config next = Config.getDefault().replaceLevel(level);
        storage.apply(switch (logger){
            case Root -> new ConfigToLogger.RootLoggerConfigItem(next);
            case Simple, Complex -> new ConfigToLogger.NamedLoggerConfigItem(logger.path, next);
        });
        var afterUpdate = storage.get(logger.path);
        Assertions.assertEquals(level, afterUpdate.level());
    }


}
