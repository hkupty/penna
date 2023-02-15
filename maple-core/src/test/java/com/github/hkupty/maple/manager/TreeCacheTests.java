package com.github.hkupty.maple.manager;

import com.github.hkupty.maple.logger.ProxyLogger;
import com.github.hkupty.maple.logger.TestLogger;
import com.github.hkupty.maple.logger.event.InfoLoggingEventFactory;
import com.github.hkupty.maple.logger.event.WarnLoggingEventFactory;
import com.github.hkupty.maple.slf4j.impl.Config;
import com.github.hkupty.maple.slf4j.impl.TreeCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TreeCacheTests {

    @Test
    void canGetRootLogger() {
        var cache = new TreeCache(new TestLogger(""));
        assertNotNull(cache.find(new String[]{}));
    }

    @Test
    void canCreateAChildLogger() {
        var cache = new TreeCache(new TestLogger(""));
        cache.createRecursively(new String[]{"com", "github", "hkupty", "maple", "LeLogger"},
                (baseLogger, partialIdentifier) ->
                        new TestLogger(String.join(".", partialIdentifier))
        );
        assertNotNull(cache.find(new String[]{}));
        assertNotNull(cache.find(new String[]{"com"}));
        assertNotNull(cache.find(new String[]{"com", "github"}));
        assertNotNull(cache.find(new String[]{"com", "github", "hkupty"}));
        assertNotNull(cache.find(new String[]{"com", "github", "hkupty", "maple"}));
        var finalLogger = cache.find(new String[]{"com", "github", "hkupty", "maple", "LeLogger"});
        assertNotNull(finalLogger);
        assertEquals(InfoLoggingEventFactory.singleton(), ((TestLogger)finalLogger).getEventBuilderFactory());;
    }

    @Test
    void canRecursivelyUpdateChildren() {
        var cache = new TreeCache(new TestLogger(""));
        cache.createRecursively(new String[]{"com", "github", "hkupty", "maple", "LeLogger"},
                (baseLogger, partialIdentifier) ->
                        new TestLogger(String.join(".", partialIdentifier))
        );
        cache.updateLoggerEventFactory(new String[]{"com", "github"}, WarnLoggingEventFactory.singleton());

        var nodeLogger = cache.find(new String[]{"com"});
        assertNotNull(nodeLogger);
        assertEquals(InfoLoggingEventFactory.singleton(), ((TestLogger)nodeLogger).getEventBuilderFactory());;

        var finalLogger = cache.find(new String[]{"com", "github", "hkupty", "maple", "LeLogger"});
        assertNotNull(finalLogger);
        assertEquals(WarnLoggingEventFactory.singleton(), ((TestLogger)finalLogger).getEventBuilderFactory());;
    }
    @Test
    void canRecursivelyUpdateAllChildren() {
        var cache = new TreeCache(new TestLogger(""));
        cache.createRecursively(new String[]{"com", "github", "hkupty", "maple", "LeLogger"},
                (baseLogger, partialIdentifier) ->
                        new TestLogger(String.join(".", partialIdentifier))
        );
        cache.updateLoggerEventFactory(new String[]{}, WarnLoggingEventFactory.singleton());

        var rootLogger = cache.find(new String[]{"com"});
        assertNotNull(rootLogger);
        assertEquals(WarnLoggingEventFactory.singleton(), ((TestLogger)rootLogger).getEventBuilderFactory());;

        var nodeLogger = cache.find(new String[]{"com"});
        assertNotNull(nodeLogger);
        assertEquals(WarnLoggingEventFactory.singleton(), ((TestLogger)nodeLogger).getEventBuilderFactory());;

        var finalLogger = cache.find(new String[]{"com", "github", "hkupty", "maple", "LeLogger"});
        assertNotNull(finalLogger);
        assertEquals(WarnLoggingEventFactory.singleton(), ((TestLogger)finalLogger).getEventBuilderFactory());;
    }

    @Test
    void testLog() {
        var logger = new ProxyLogger( "", Config.getDefault() );
        logger.atInfo().log("stuff");
    }

}
