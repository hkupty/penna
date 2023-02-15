package com.github.hkupty.maple;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.ConsoleAppender;
import com.github.hkupty.maple.slf4j.LoggerFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;

import java.io.*;


public class Reference {

    @State(Scope.Thread)
    public static class BenchmarkState {
        Logger mapleLogger;
        ch.qos.logback.classic.Logger logbackLogger;

        public BenchmarkState() {
            mapleLogger = new LoggerFactory().getLogger("jmh.logger");
            var context = new LoggerContext();
            logbackLogger = context.getLogger("jmh.logger");
            logbackLogger.setLevel(Level.INFO);
            var appender = new ConsoleAppender<ILoggingEvent>();
            appender.setTarget("System.err");
            var layout = new JsonLayout();
            appender.setContext(context);
            layout.setContext(context);
            appender.setLayout(layout);
            appender.start();
            logbackLogger.addAppender(appender);
        }
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    public void aMapleEvent(BenchmarkState state){
        state.mapleLogger.atWarn().log("maple logging event....");
    }
//
//    @Fork(value = 1, warmups = 1)
//    @Warmup(time = 5, iterations = 2)
//    @Measurement(time = 5, iterations = 2)
//    @Benchmark
//    public void mapleLog(BenchmarkState state){
//        state.mapleLogger.warn("maple logging message...");
//    }
//
//    @Fork(value = 1, warmups = 1)
//    @Warmup(time = 5, iterations = 2)
//    @Measurement(time = 5, iterations = 2)
//    @Benchmark
//    public void logbackLog(BenchmarkState state, Blackhole bh){
//        state.logbackLogger.warn("logback logging message");
//    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    public void logbackEvent(BenchmarkState state, Blackhole bh){ state.logbackLogger.atWarn().log("logback logging event.."); }
}
