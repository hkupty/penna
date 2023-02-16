package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;

public final class LongFrame extends DataFrame<Long> {
    public LongFrame(String key, Long value) {
        super(key, value);
    }

    @Override
    public void write(Sink.SinkWriter writer) {
        writer.writeLong(key, value);
    }
}
