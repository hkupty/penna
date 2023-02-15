package com.github.hkupty.maple.logger.event;

import com.github.hkupty.maple.logger.BaseLogger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.NOPLoggingEventBuilder;

public class ErrorLoggingEventFactory implements LoggingEventBuilderFactory {

    private ErrorLoggingEventFactory(){}

    private static final LoggingEventBuilderFactory instance = new ErrorLoggingEventFactory();
    public static LoggingEventBuilderFactory singleton() {
        return instance;
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
        return false;
    }

    @Override
    public LoggingEventBuilder warn(BaseLogger logger) {
        return NOPLoggingEventBuilder.singleton();
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
