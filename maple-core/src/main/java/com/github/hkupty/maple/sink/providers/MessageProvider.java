package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class MessageProvider implements LogFieldProvider {
    private transient final String messageFieldName;

    public MessageProvider(String messageFieldName) {
        this.messageFieldName = messageFieldName;
    }

    public MessageProvider() {
        this("message");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        writer.writeString(messageFieldName, log.message());
    }
}
