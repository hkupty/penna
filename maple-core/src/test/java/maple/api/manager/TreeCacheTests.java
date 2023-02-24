package maple.api.manager;

import maple.core.logger.TreeCache;
import maple.core.logger.guard.WarnLevelGuard;
import maple.core.logger.guard.InfoLevelGuard;
import maple.core.slf4j.MapleLoggerFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TreeCacheTests {

//    @Test
//    void canGetRootLogger() {
//        var cache = new TreeCache(new TestLogger(""));
//        assertNotNull(cache.find(new String[]{}));
//    }
//
//    @Test
//    void canCreateAChildLogger() {
//        var cache = new TreeCache(new TestLogger(""));
//        cache.createRecursively(new String[]{"com", "github", "hkupty", "maple", "LeLogger"},
//                (baseLogger, partialIdentifier) ->
//                        new TestLogger(String.join(".", partialIdentifier))
//        );
//        assertNotNull(cache.find(new String[]{}));
//        assertNotNull(cache.find(new String[]{"com"}));
//        assertNotNull(cache.find(new String[]{"com", "github"}));
//        assertNotNull(cache.find(new String[]{"com", "github", "hkupty"}));
//        assertNotNull(cache.find(new String[]{"com", "github", "hkupty", "maple"}));
//        var finalLogger = cache.find(new String[]{"com", "github", "hkupty", "maple", "LeLogger"});
//        assertNotNull(finalLogger);
//        assertEquals(InfoLevelGuard.singleton(), ((TestLogger)finalLogger).getEventBuilderFactory());
//    }
//
//    @Test
//    void canRecursivelyUpdateChildren() {
//        var cache = new TreeCache(new TestLogger(""));
//        cache.createRecursively(new String[]{"com", "github", "hkupty", "maple", "LeLogger"},
//                (baseLogger, partialIdentifier) ->
//                        new TestLogger(String.join(".", partialIdentifier))
//        );
//        cache.updateConfig(new String[]{"com", "github"}, old -> old.copy(Level.WARN));
//
//        var nodeLogger = cache.find(new String[]{"com"});
//        assertNotNull(nodeLogger);
//        assertEquals(InfoLevelGuard.singleton(), ((TestLogger)nodeLogger).getEventBuilderFactory());
//
//        var finalLogger = cache.find(new String[]{"com", "github", "hkupty", "maple", "LeLogger"});
//        assertNotNull(finalLogger);
//        assertEquals(WarnLevelGuard.singleton(), ((TestLogger)finalLogger).getEventBuilderFactory());
//    }
//    @Test
//    void canRecursivelyUpdateAllChildren() {
//        var cache = new TreeCache(new TestLogger(""));
//        cache.createRecursively(new String[]{"com", "github", "hkupty", "maple", "LeLogger"},
//                (baseLogger, partialIdentifier) ->
//                        new TestLogger(String.join(".", partialIdentifier))
//        );
//        cache.updateConfig(new String[]{}, old -> old.copy(Level.WARN));
//
//        var rootLogger = cache.find(new String[]{"com"});
//        assertNotNull(rootLogger);
//        assertEquals(WarnLevelGuard.singleton(), ((TestLogger)rootLogger).getEventBuilderFactory());
//
//        var nodeLogger = cache.find(new String[]{"com"});
//        assertNotNull(nodeLogger);
//        assertEquals(WarnLevelGuard.singleton(), ((TestLogger)nodeLogger).getEventBuilderFactory());
//
//        var finalLogger = cache.find(new String[]{"com", "github", "hkupty", "maple", "LeLogger"});
//        assertNotNull(finalLogger);
//        assertEquals(WarnLevelGuard.singleton(), ((TestLogger)finalLogger).getEventBuilderFactory());
//    }

    @Test
    void testLog() {
        var logger = MapleLoggerFactory.getInstance().getLogger("test");
        var nested = new HashMap<>();
        nested.put("nested", "map");
        var map = new HashMap<>();
        map.put("Something", nested);
        map.put("other", new Object[]{
                "thing"
        });
        logger.atInfo()
                .addKeyValue("something", map)
                .addKeyValue("directly", new Object[]{
                        "here",
                        1,
                        2.3
                })
                .log("stuff");
        logger.atInfo().log("stuff");
        logger.atInfo().log("stuff");
        logger.atInfo().log("stuff");
        logger.atInfo().log("stuff");
    }

}
