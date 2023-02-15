package com.github.hkupty.maple.logger;

import com.github.hkupty.maple.slf4j.impl.Config;
import com.github.hkupty.maple.models.JsonLog;
import org.slf4j.event.DefaultLoggingEvent;
import org.slf4j.event.LoggingEvent;
import org.slf4j.event.SubstituteLoggingEvent;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

import static com.github.hkupty.maple.logger.LogEventAdapter.transform;

public class TestLogger extends ProxyLogger {
    private final Queue<JsonLog> buffer;

    public TestLogger(String name, Config config) {
        super(name, config);
        buffer = new ArrayDeque<>();
    }

    public TestLogger(String name) {
        this(name, Config.getDefault());
    }

    @Override
    public void log(LoggingEvent event) {
        // TODO Fix the thread name that is missing
        // HACK The LoggingEventBuilder doesn't set nor allows one to set the
        // timestamp for an event, so we hack it.
        long now = Instant.now().toEpochMilli();
        if (event instanceof DefaultLoggingEvent def) {
            def.setTimeStamp(now);
        } else if (event instanceof SubstituteLoggingEvent subs) {
            subs.setTimeStamp(now);
        }
        buffer.add(transform(event));
    }

    public Queue<JsonLog> getBuffer() { return buffer; }
}
