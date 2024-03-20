package penna.core.logger;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import penna.api.config.Config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LoggerStorageTests {

    @Test
    public void getOrCreateCreatesALogger() {
        var cache = new LoggerStorage();
        var logger = cache.getOrCreate("com.for.testing");
        assertEquals(logger.name, "com.for.testing");
    }


    @Test
    public void callingGetOrCreateTwiceDoesNotCreateDuplicates() {
        var cache = new LoggerStorage();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing");

        assertSame(logger1, logger2);
    }


    @Test
    public void storingAHierarchicalPathCreatesTheNodesCorrectly() {
        var cache = new LoggerStorage();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");
        var logger3 = cache.getOrCreate("com.for.something.else");

        //           com
        //            |
        //           for
        //            |
        //         testing
        //        /   |
        // something  |
        //     |    other
        //   else

        var comNode = cache.root.children[2];
        assertEquals("com", comNode.component);

        var forNode = comNode.children[1];
        assertEquals("for", forNode.component);

        var testingNode = forNode.children[1];
        assertEquals("testing", testingNode.component);
        assertEquals(logger1, testingNode.loggerRef);

        var otherNode = testingNode.children[1];
        assertEquals("other", otherNode.component);
        assertEquals(logger2, otherNode.loggerRef);

        var somethingNode = forNode.children[1].children[0];
        assertEquals("something", somethingNode.component);

        var elseNode = somethingNode.children[1];
        assertEquals("else", elseNode.component);
        assertEquals(logger3, elseNode.loggerRef);

    }

    @Test
    public void replaceTheWholeTreeAffectsLeafObjects() {
        var cache = new LoggerStorage();
        var defaults = Config.getDefault();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");

        assertEquals(defaults.level(), logger1.levelGuard.level());
        assertEquals(defaults.level(), logger2.levelGuard.level());

        cache.replaceConfig(Config.withFields(Level.DEBUG));

        assertEquals(Level.DEBUG, cache.getOrCreate("com.for.testing").levelGuard.level());
        assertEquals(Level.DEBUG, cache.getOrCreate("com.for.testing.other").levelGuard.level());
    }


    @Test
    public void replacePrefixAffectsOnlyDescendants() {
        var cache = new LoggerStorage();
        var defaults = Config.getDefault();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");
        var logger3 = cache.getOrCreate("com.for.unrelated");

        assertEquals(defaults.level(), logger1.levelGuard.level());
        assertEquals(defaults.level(), logger2.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());

        cache.replaceConfig("com.for.testing", Config.withFields(Level.DEBUG));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());
        cache.replaceConfig("com.for.unrelated", Config.withFields(Level.WARN));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(Level.WARN, logger3.levelGuard.level());
    }

    @Test
    public void updatePrefixAffectsOnlyDescendants() {
        var cache = new LoggerStorage();
        var defaults = Config.getDefault();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");
        var logger3 = cache.getOrCreate("com.for.unrelated");
        var logger4 = cache.getOrCreate("com.more.unrelated");
        var logger5 = cache.getOrCreate("io.completely.unrelated");

        assertEquals(defaults.level(), logger1.levelGuard.level());
        assertEquals(defaults.level(), logger2.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());

        cache.replaceConfig("com.for.testing", defaults.replaceLevel(Level.DEBUG));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());

        cache.replaceConfig("com.for.unrelated", defaults.replaceLevel(Level.WARN));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(Level.WARN, logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());
        cache.replaceConfig("com.for", defaults.replaceLevel(Level.TRACE));

        assertEquals(Level.TRACE, logger2.levelGuard.level());
        assertEquals(Level.TRACE, logger1.levelGuard.level());
        assertEquals(Level.TRACE, logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());

        cache.replaceConfig("io", defaults.replaceLevel(Level.ERROR));

        assertEquals(Level.TRACE, logger2.levelGuard.level());
        assertEquals(Level.TRACE, logger1.levelGuard.level());
        assertEquals(Level.TRACE, logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(Level.ERROR, logger5.levelGuard.level());
    }

    @Test
    public void weAlwaysGetTheRightLogger() {
        var cache = new LoggerStorage();
        var loggers = List.of(
                "com.AAA.AAA", "com.AAA.AAA", "io.aaa.zzz.AAA", "io.aaa.zzz"
        );

        for (var logger : loggers) {
            assertEquals(logger, cache.getOrCreate(logger).name);
        }
    }
}
