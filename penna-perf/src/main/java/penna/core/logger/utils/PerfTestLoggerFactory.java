package penna.core.logger.utils;

import ch.qos.logback.classic.spi.LogbackServiceProvider;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import penna.api.config.Config;
import penna.core.logger.LoggerStorage;
import penna.core.logger.TreeCache;
import penna.core.sink.CoreSink;
import penna.core.sink.SinkManager;
import penna.perf.misc.IfBasedLogger;

import java.io.Closeable;
import java.io.IOException;

public sealed interface PerfTestLoggerFactory extends Closeable {

    final class Factory {
        private Factory() {
        }

        public static PerfTestLoggerFactory get(Implementation implementation) {
            return switch (implementation) {
                case Penna -> new PennaFactory();
                case IfBasedLogger -> new IfBasedLoggerFactory();
                case Logback -> new LogbackFactory();
                case Log4j -> new Log4JFactory();
            };
        }

    }

    void setup(Blackhole bh);

    org.slf4j.Logger getLogger(String name);

    enum Implementation {
        Penna,
        IfBasedLogger,
        Logback,
        Log4j
    }

    final class PennaFactory implements PerfTestLoggerFactory {
        public LoggerStorage storage = new LoggerStorage();
        public TreeCache cache = new TreeCache(Config.getDefault());

        @Override
        public void setup(Blackhole bh) {
            SinkManager.Instance.replace(() -> new CoreSink(new BlackholeChannel(bh)));
        }

        @Override
        public Logger getLogger(String name) {
            return storage.getOrCreate(name);
        }

        @Override
        public void close() throws IOException {
        }
    }

    final class IfBasedLoggerFactory implements PerfTestLoggerFactory {

        @Override
        public void setup(Blackhole bh) {
            SinkManager.Instance.replace(() -> new CoreSink(new BlackholeChannel(bh)));
        }

        @Override
        public Logger getLogger(String name) {
            return new IfBasedLogger(name, Config.getDefault());
        }

        @Override
        public void close() throws IOException {
        }
    }

    final class Log4JFactory implements PerfTestLoggerFactory {

        Log4jLoggerFactory loggerFactory;

        @Override
        public void setup(Blackhole bh) {
            System.setProperty("log4j.configurationFile", "log4j2.xml");
            var markerFactory = new Log4jMarkerFactory();
            loggerFactory = new Log4jLoggerFactory(markerFactory);
        }

        @Override
        public Logger getLogger(String name) {
            return loggerFactory.getLogger(name);
        }

        @Override
        public void close() throws IOException {
            loggerFactory.close();
        }
    }

    final class LogbackFactory implements PerfTestLoggerFactory {

        LogbackServiceProvider serviceProvider;
        ILoggerFactory loggerFactory;

        @Override
        public void setup(Blackhole bh) {
            LogbackBlackholeAppender.bh = bh;
            System.setProperty("logback.configurationFile", "logback.xml");
            serviceProvider = new LogbackServiceProvider();

            serviceProvider.initialize();

            loggerFactory = serviceProvider.getLoggerFactory();
        }

        @Override
        public Logger getLogger(String name) {
            return loggerFactory.getLogger(name);
        }

        @Override
        public void close() throws IOException {
        }
    }


}
