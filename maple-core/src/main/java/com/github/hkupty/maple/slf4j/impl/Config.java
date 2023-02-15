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
    private static final LogFieldProvider[] defaultProviders = new LogFieldProvider[]{
            new MessageProvider()
    };


    private static final EnumMap<Level, LoggingEventBuilderFactory> levelMapping = new EnumMap<>(Level.class);

    static {
        levelMapping.put(Level.DEBUG, DebugLoggingEventFactory.singleton());
        levelMapping.put(Level.INFO, InfoLoggingEventFactory.singleton());
        levelMapping.put(Level.WARN, WarnLoggingEventFactory.singleton());
        levelMapping.put(Level.TRACE, TraceLoggingEventFactory.singleton());
        levelMapping.put(Level.ERROR, ErrorLoggingEventFactory.singleton());
    }
    public static Config getDefault() {
        return new Config(Level.INFO, defaultProviders);
    }

    public LoggingEventBuilderFactory factory() {
        return levelMapping.getOrDefault(level, NOPLoggingEventFactory.singleton());
    }
}
