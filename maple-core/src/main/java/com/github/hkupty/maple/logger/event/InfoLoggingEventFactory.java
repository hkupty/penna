package com.github.hkupty.maple.logger.event;

import com.github.hkupty.maple.logger.BaseLogger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public class InfoLoggingEventFactory implements LoggingEventBuilderFactory {
    private InfoLoggingEventFactory(){}

    private static final LoggingEventBuilderFactory SINGLETON = new InfoLoggingEventFactory();
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
        return true;
    }

    @Override
    public LoggingEventBuilder info(BaseLogger logger) {
        return new JsonLogEventBuilder(logger, Level.INFO);
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
