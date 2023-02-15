package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class TimestampProvider implements LogFieldProvider {
    private transient final String timestampFieldName;

    public TimestampProvider(String timestampFieldName) {
        this.timestampFieldName = timestampFieldName;
    }

    public TimestampProvider() {
        this("timestamp");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        writer.writeLong(timestampFieldName, log.timestamp());
    }
}
