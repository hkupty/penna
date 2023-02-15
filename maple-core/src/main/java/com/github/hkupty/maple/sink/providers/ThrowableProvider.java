package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class ThrowableProvider implements LogFieldProvider {
    private final String throwableFieldName;

    public ThrowableProvider(String throwableFieldName) {
        this.throwableFieldName = throwableFieldName;
    }

    public ThrowableProvider() {
        this("throwable");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        if (log.throwable() != null) {
            var stack = log.throwable().getStackTrace();
            writer.startObject(throwableFieldName);
            writer.writeString("message", log.throwable().getMessage());
            writer.startArray("stacktrace");
            for (int i = 0; i < stack.length; i++) {
                var frame = stack[i];
                writer.writeString(frame.toString());
            }
            writer.endArray();
            writer.endObject();
        }

    }
}
