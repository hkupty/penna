package com.github.hkupty.maple.sink;

import com.github.hkupty.maple.minilog.MiniLogger;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public class SinkFactory {

    private static Supplier<Sink> factoryFunction;
    private static ClassLoader cl;

    private static void initCl() {
        if(cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
    }

    private static Supplier<Sink> getJacksonSink() {
        initCl();
        try {
            Class.forName("com.fasterxml.jackson.core.JsonGenerator", false, cl);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return JacksonSink::new;
    }

    private static Supplier<Sink> getJakartaSink() {
        initCl();
        try {
            Class.forName("jakarta.json.stream.JsonGenerator", false, cl);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return JakartaSink::new;
    }

    public static synchronized Supplier<Sink> getSinkClass() {
        if (factoryFunction == null) {
            factoryFunction = getJacksonSink();
        }

        if (factoryFunction == null) {
            factoryFunction = getJakartaSink();
        }

        return factoryFunction;
    }

    public static Sink getSink() {
        if (factoryFunction == null) {
            getSinkClass();
        }
        return factoryFunction.get();
    }
}
