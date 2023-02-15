package com.github.hkupty.maple.sink;

import com.github.hkupty.maple.minilog.MiniLogger;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;

import java.io.IOException;
import java.util.function.Function;

public class SinkFactory {

    private static Function<LogFieldProvider[], Sink> sinkFactory;
    private static ClassLoader cl;

    private static void initCl() {
        if(cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
    }

    private static Function<LogFieldProvider[], Sink> getJacksonSink() {
        initCl();
        try {
            Class.forName("com.fasterxml.jackson.core.JsonGenerator", false, cl);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return (init) -> {
            try {
                return new JacksonSink(init);
            } catch (IOException e) {
                MiniLogger.error("Unable to create logger", e);
            }
            return null;
        };
    }

    private static Function<LogFieldProvider[], Sink> getJakartaSink() {
        initCl();
        try {
            Class.forName("jakarta.json.stream.JsonGenerator", false, cl);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return (init) -> new JakartaSink(init);
    }

    public static synchronized void getSinkClass() {
        if (sinkFactory == null) {
            sinkFactory = getJacksonSink();
        }
        if (sinkFactory == null) {
            sinkFactory = getJakartaSink();
        }
    }

    public static Sink getSink(LogFieldProvider[] providers) {
        if (sinkFactory == null) {
            getSinkClass();
        }
        return sinkFactory.apply(providers);
    }
}
