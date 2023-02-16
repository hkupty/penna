package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.StringFrame;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.EnumMap;

public class LevelProvider implements DataFrameProvider<StringFrame> {

    private static final EnumMap<Level, StringFrame> cache;
    static {
        cache = new EnumMap<>(Level.class);
        for (var level : Level.values()) {
            cache.put(level, new StringFrame(LogField.Level.name(), level.name()));
        }
    }


    @Override
    public LogField field() {
        return LogField.Level;
    }

    @Override
    public StringFrame get(LoggingEvent event) {
        return cache.get(event.getLevel());
    }

}
