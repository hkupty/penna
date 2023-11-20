package penna.core.logger.utils;

import ch.qos.logback.classic.spi.LogbackServiceProvider;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import penna.api.config.Config;
import penna.core.logger.TreeCache;
import penna.core.sink.CoreSink;
import penna.core.sink.SinkManager;

import java.io.*;
import java.util.regex.Pattern;

public sealed interface PerfTestLoggerFactory extends Closeable {

    final class Factory {
        private Factory() {
        }

        public static PerfTestLoggerFactory get(Implementation implementation) {
            return switch (implementation) {
                case Penna -> new PennaFactory();
                case Logback -> new LogbackFactory();
                case Log4j -> new Log4JFactory();
            };
        }

    }

    void setup();

    org.slf4j.Logger getLogger(String name);

    enum Implementation {
        Penna,
        Logback,
        Log4j
    }

    final class PennaFactory implements PerfTestLoggerFactory {
        private FileOutputStream fos;
        TreeCache cache = new TreeCache(Config.getDefault());
        private static final Pattern DOT_SPLIT = Pattern.compile("\\.");

        @Override
        public void setup() {
            var devnull = new File("/dev/null");
            try {
                fos = new FileOutputStream(devnull);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Dev null doesn't exist!", e);
            }

            SinkManager.Instance.replace(() -> new CoreSink(fos));
        }

        @Override
        public Logger getLogger(String name) {
            return cache.getLoggerAt(DOT_SPLIT.split(name));
        }

        @Override
        public void close() throws IOException {
            fos.close();
        }
    }

    final class Log4JFactory implements PerfTestLoggerFactory {

        Log4jLoggerFactory loggerFactory;

        @Override
        public void setup() {
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
        public void setup() {
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
