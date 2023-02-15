package com.github.hkupty.maple.slf4j.impl;

import com.github.hkupty.maple.logger.event.*;
import com.github.hkupty.maple.sink.providers.*;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;
import org.slf4j.event.Level;

import java.util.EnumMap;

public record Config(
        Level level,
        LogFieldProvider[] providers
) {

    private static final EnumMap<Level, LoggingEventBuilderFactory> levelMapping = new EnumMap<>(Level.class);

    static {
        levelMapping.put(Level.DEBUG, DebugLoggingEventFactory.singleton());
        levelMapping.put(Level.INFO, InfoLoggingEventFactory.singleton());
        levelMapping.put(Level.WARN, WarnLoggingEventFactory.singleton());
        levelMapping.put(Level.TRACE, TraceLoggingEventFactory.singleton());
        levelMapping.put(Level.ERROR, ErrorLoggingEventFactory.singleton());
    }
    public static Config getDefault() {
        return new Config(
                Level.INFO,
                new LogFieldProvider[]{
                    // new TimestampProvider(),
                        // new LevelProvider(),
                        // new ThreadProvider(),
                        new MessageProvider(),
                       // new LoggerProvider(),
                       // new KeyValueProvider(),
                       // new ThrowableProvider()
                }
        );
    }

    public LoggingEventBuilderFactory factory() {
        return levelMapping.getOrDefault(level, NOPLoggingEventFactory.singleton());
    }
}
