package com.github.hkupty.maple.logger.event;

import com.github.hkupty.maple.logger.BaseLogger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public class WarnLoggingEventFactory implements LoggingEventBuilderFactory {

    private WarnLoggingEventFactory(){}

    private static final LoggingEventBuilderFactory SINGLETON = new WarnLoggingEventFactory();
    public static LoggingEventBuilderFactory singleton() {
        return SINGLETON;
    }
    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder trace(BaseLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder debug(BaseLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public LoggingEventBuilder info(BaseLogger logger) {
        return NOPLoggingEventBuilder.singleton();
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder warn(BaseLogger logger) {
        return new JsonLogEventBuilder(logger, Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public LoggingEventBuilder error(BaseLogger logger) {
        return new JsonLogEventBuilder(logger, Level.ERROR);
    }
}
