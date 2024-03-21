package penna.core.slf4j;

import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.slf4j.event.Level;
import penna.api.config.Config;
import penna.api.config.ConfigToLogger;
import penna.api.config.Storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Assertions.assertNull(storage.get(logger.path));
        Config next = Config.getDefault().replaceLevel(level);
        storage.apply(switch (logger){
            case Root -> new ConfigToLogger.RootLoggerConfigItem(next);
            case Simple, Complex -> new ConfigToLogger.NamedLoggerConfigItem(logger.path, next);
        });

        // Shouldn't be able to get Root logger
        if (logger != LoggerOption.Root) {
            var afterUpdate = storage.get(logger.path);
            Assertions.assertNotNull(afterUpdate);
            Assertions.assertEquals(level, afterUpdate.level());
        } else {
            Assertions.assertNull(storage.get(logger.path));
        }
    }


}
