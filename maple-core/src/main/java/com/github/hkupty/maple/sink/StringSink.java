package com.github.hkupty.maple.sink;

import com.github.hkupty.maple.minilog.MiniLogger;
import com.github.hkupty.maple.models.JsonLog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

public class StringSink implements Sink {
    private final OutputStream os;
    private final ReentrantLock lock;

    public StringSink() {
        os = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out));
        lock = new ReentrantLock();
    }
    @Override
    public void render(JsonLog jsonLog) {
        try {
            var message = jsonLog.message().getBytes(StandardCharsets.UTF_8);
            lock.lock();
            os.write(message);
            os.write('\n');
            os.flush();
        } catch (IOException e) {
            MiniLogger.error("Unable to log", e);
        } finally {
            lock.unlock();
        }
    }
}
