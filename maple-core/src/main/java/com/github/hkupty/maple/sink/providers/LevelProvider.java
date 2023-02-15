package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class LevelProvider implements LogFieldProvider {
    private transient final String levelFieldName;

    public LevelProvider(String levelFieldName) {
        this.levelFieldName = levelFieldName;
    }

    public LevelProvider() {
        this("level");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        writer.writeString(levelFieldName, log.level());
    }
}
