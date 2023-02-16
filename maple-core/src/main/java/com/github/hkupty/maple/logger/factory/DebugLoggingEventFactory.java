package com.github.hkupty.maple.logger.factory;

import com.github.hkupty.maple.logger.MapleLogger;
import com.github.hkupty.maple.logger.event.JsonLogEventBuilder;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public class DebugLoggingEventFactory implements LoggingEventBuilderFactory {
    private DebugLoggingEventFactory(){}

    private static final LoggingEventBuilderFactory instance = new DebugLoggingEventFactory();
    public static LoggingEventBuilderFactory singleton() {
        return instance;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder trace(MapleLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder debug(MapleLogger logger) {
        return new JsonLogEventBuilder(logger, Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder info(MapleLogger logger) {
        return new JsonLogEventBuilder(logger, Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder warn(MapleLogger logger) {
        return new JsonLogEventBuilder(logger, Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder error(MapleLogger logger) {
        return new JsonLogEventBuilder(logger, Level.ERROR);
    }
}
