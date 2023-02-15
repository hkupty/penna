package com.github.hkupty.maple.slf4j;

import com.github.hkupty.maple.logger.ProxyLogger;
import com.github.hkupty.maple.logger.event.*;
import com.github.hkupty.maple.slf4j.impl.Config;
import com.github.hkupty.maple.slf4j.impl.TreeCache;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Objects;
import java.util.regex.Pattern;

public class LoggerFactory implements ILoggerFactory {
    private static final Pattern DOT_SPLIT = Pattern.compile("\\.");
    private transient final TreeCache cache;
    private transient Config config;

    public LoggerFactory(){
        config = Config.getDefault();
        var rootLogger = new ProxyLogger("", config);
        cache = new TreeCache(rootLogger);
    }

    @Override
    public Logger getLogger(String name) {
        String[] identifier = DOT_SPLIT.split(name);
        var logger = cache.find(identifier);
        if (Objects.isNull(logger)) {
            return cache.createRecursively(
                    identifier,
                    (parent, partialIdentifier) -> {
                        var loggerName = String.join(".", partialIdentifier);
                        return createLogger(loggerName, parent.getEventBuilderFactory());
                    });
        }
        return logger;
    }

    public void updateLogLevel(String name, Level level) {
        config = new Config(level, config.providers());
        String[] identifier = name.isEmpty() ? new String[]{} : DOT_SPLIT.split(name);
        cache.updateLoggerEventFactory(identifier, config.factory());
    }

    private ProxyLogger createLogger(String loggerName, LoggingEventBuilderFactory factory) {
        return new ProxyLogger(loggerName, config.providers(), factory);
    }
}
