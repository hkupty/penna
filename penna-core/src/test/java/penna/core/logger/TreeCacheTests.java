package penna.core.logger;

import penna.api.config.Config;
import penna.core.logger.guard.DebugLevelGuard;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeCacheTests {

    @Test
    public void empty_cache_has_depth_1(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        AtomicInteger depth = new AtomicInteger();
        cache.traverse(cache.ROOT, ignored -> depth.getAndIncrement());

        assertEquals(1, depth.get());
    }

    @Test
    public void empty_cache_has_no_loggers_created(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        AtomicInteger depth = new AtomicInteger();
        cache.traverse(cache.ROOT, data -> {
            if(data.logger != null) {
                depth.getAndIncrement();
            }
        });

        assertEquals(0, depth.get());
    }


    @Test
    public void requesting_a_logger_from_cache_creates_entries_recursively(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        cache.getOrCreate(new String[]{
                "com",
                "for",
                "testing"
        });
        AtomicInteger depth = new AtomicInteger();
        cache.traverse(cache.ROOT, ignored -> depth.getAndIncrement());

        assertEquals(4, depth.get());
    }

    @Test
    public void requesting_a_logger_from_cache_creates_only_one_logger(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        cache.getLoggerAt(new String[]{
                "com",
                "for",
                "testing"
        });
        AtomicInteger depth = new AtomicInteger();
        cache.traverse(cache.ROOT, data -> {
            if(data.logger != null) {
                depth.getAndIncrement();
            }
        });

        assertEquals(1, depth.get());
    }
    @Test
    public void calling_getOrCreate_twice_does_not_create_duplicates(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);

        cache.getOrCreate(new String[]{
                "com",
                "for",
                "testing"
        });

        cache.getOrCreate(new String[]{
                "com",
                "for",
                "testing"
        });

        AtomicInteger depth = new AtomicInteger();
        cache.traverse(cache.ROOT, ignored -> depth.getAndIncrement());

        assertEquals(4, depth.get());

        AtomicInteger loggers = new AtomicInteger();
        cache.traverse(cache.ROOT, data -> {
            if(data.logger != null) {
                loggers.getAndIncrement();
            }
        });

        assertEquals(0, loggers.get());
    }

    @Test
    public void updating_uninitialized_parent_cascades_down_to_materialized_logger(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        var logger = cache.getLoggerAt(new String[]{
                "com",
                "for",
                "testing"
        });
        cache.updateConfig(new String[]{"com", "for"}, cfg -> cfg.replaceLevel(Level.DEBUG));
        assertEquals(DebugLevelGuard.singleton(), logger.levelGuard);
    }

    @Test
    public void updating_uninitialized_parent_does_not_materialize_logger(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        cache.getLoggerAt(new String[]{
                "com",
                "for",
                "testing"
        });
        cache.updateConfig(new String[]{"com", "for"}, cfg -> cfg.replaceLevel(Level.DEBUG));
        AtomicInteger loggers = new AtomicInteger();
        cache.traverse(cache.ROOT, data -> {
            if(data.logger != null) {
                loggers.getAndIncrement();
            }
        });

        assertEquals(1, loggers.get());
    }

    @Test
    public void updating_uninitialized_config_will_recursively_create_the_entries_but_not_loggers(){
        var config = Config.getDefault();
        var cache = new TreeCache(config);
        cache.updateConfig(new String[]{"com", "for", "testing"}, cfg -> cfg.replaceLevel(Level.DEBUG));
        AtomicInteger depth = new AtomicInteger();
        cache.traverse(cache.ROOT, ignored -> depth.getAndIncrement());

        assertEquals(4, depth.get());

        AtomicInteger loggers = new AtomicInteger();
        cache.traverse(cache.ROOT, data -> {
            if(data.logger != null) {
                loggers.getAndIncrement();
            }
        });

        assertEquals(0, loggers.get());
    }

}
