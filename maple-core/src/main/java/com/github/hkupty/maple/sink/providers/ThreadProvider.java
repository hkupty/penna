package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class ThreadProvider implements LogFieldProvider {
    private final String threadFieldName;

    public ThreadProvider(String threadFieldName) {
        this.threadFieldName = threadFieldName;
    }

    public ThreadProvider() {
        this("threadName");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        writer.writeString(threadFieldName, log.threadName());
    }
}
