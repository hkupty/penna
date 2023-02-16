package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;

public final class StringFrame extends DataFrame<String> {
    public StringFrame(String key, String value) {
        super(key, value);
    }

    @Override
    public void write(Sink.SinkWriter writer) {
        writer.writeString(key, value);
    }
}
