package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class LoggerProvider implements LogFieldProvider {
    private transient final String loggerFieldName;

    public LoggerProvider(String loggerFieldName) {
        this.loggerFieldName = loggerFieldName;
    }

    public LoggerProvider() {
        this("logger");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        writer.writeString(loggerFieldName, log.logger());
    }
}
