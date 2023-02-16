package com.github.hkupty.maple.logger.factory;

import com.github.hkupty.maple.logger.MapleLogger;
import com.github.hkupty.maple.logger.event.JsonLogEventBuilder;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

public class TraceLoggingEventFactory implements LoggingEventBuilderFactory {

    private TraceLoggingEventFactory(){}
    private static final LoggingEventBuilderFactory instance = new TraceLoggingEventFactory();
    public static LoggingEventBuilderFactory singleton() {
        return instance;
    }
    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder trace(MapleLogger logger) {
        return new JsonLogEventBuilder(logger, Level.TRACE);
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
