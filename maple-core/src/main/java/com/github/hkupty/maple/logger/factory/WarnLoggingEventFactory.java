package com.github.hkupty.maple.logger.factory;

import com.github.hkupty.maple.logger.MapleLogger;
import com.github.hkupty.maple.logger.event.JsonLogEventBuilder;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public class WarnLoggingEventFactory implements LoggingEventBuilderFactory {

    private WarnLoggingEventFactory(){}

    private static final LoggingEventBuilderFactory instance = new WarnLoggingEventFactory();
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
        return false;
    }

    @Override
    public LoggingEventBuilder debug(MapleLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder info(MapleLogger logger) {
        return NOPLoggingEventBuilder.singleton();
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
