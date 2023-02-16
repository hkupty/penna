package com.github.hkupty.maple.logger;

import com.github.hkupty.maple.slf4j.impl.Config;
import com.github.hkupty.maple.models.JsonLog;
import java.util.ArrayDeque;
import java.util.Queue;


public class TestLogger extends MapleLogger {
    private final Queue<JsonLog> buffer;

    public TestLogger(String name, Config config) {
        super(name, config);
        buffer = new ArrayDeque<>();
    }

    public TestLogger(String name) {
        this(name, Config.getDefault());
    }



    public Queue<JsonLog> getBuffer() { return buffer; }
}
