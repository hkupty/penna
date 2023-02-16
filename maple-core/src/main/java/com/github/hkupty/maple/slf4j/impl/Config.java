package com.github.hkupty.maple.slf4j.impl;

import com.github.hkupty.maple.logger.factory.*;
import com.github.hkupty.maple.logger.provider.DataFrameProvider;
import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.sink.providers.*;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;
import org.slf4j.event.Level;

import java.util.EnumMap;

public record Config(
        Level level,
        LogField[] fields
) {
    private static final LogField[] defaultFields = new LogField[]{
            LogField.Message
    };


    public Config copy(Level level) {
        return new Config(level, this.fields);
    }

    public Config copy(LogField[] fields) {
        return new Config(this.level, fields);
    }

    private static final EnumMap<Level, LoggingEventBuilderFactory> levelMapping = new EnumMap<>(Level.class);

    static {
        levelMapping.put(Level.DEBUG, DebugLoggingEventFactory.singleton());
        levelMapping.put(Level.INFO, InfoLoggingEventFactory.singleton());
        levelMapping.put(Level.WARN, WarnLoggingEventFactory.singleton());
        levelMapping.put(Level.TRACE, TraceLoggingEventFactory.singleton());
        levelMapping.put(Level.ERROR, ErrorLoggingEventFactory.singleton());
    }
    public static Config getDefault() {
        return new Config(Level.INFO, defaultFields);
    }

    public LoggingEventBuilderFactory factory() {
        return levelMapping.getOrDefault(level, NOPLoggingEventFactory.singleton());
    }
}
