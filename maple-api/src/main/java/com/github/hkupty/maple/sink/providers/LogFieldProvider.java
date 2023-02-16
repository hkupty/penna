package com.github.hkupty.maple.sink.providers;

import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

@FunctionalInterface
public interface LogFieldProvider {
    void logField(Sink.SinkWriter generator, JsonLog log);

}
