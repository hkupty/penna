package com.github.hkupty.maple.logger.event;

import com.github.hkupty.maple.logger.BaseLogger;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventAware;
import org.slf4j.spi.LoggingEventBuilder;

public interface LoggingEventBuilderFactory {

    boolean isTraceEnabled();
    LoggingEventBuilder trace(BaseLogger logger);

    boolean isDebugEnabled();
    LoggingEventBuilder debug(BaseLogger logger);

    boolean isInfoEnabled();
    LoggingEventBuilder info(BaseLogger logger);

    boolean isWarnEnabled();
    LoggingEventBuilder warn(BaseLogger logger);

    boolean isErrorEnabled();
    LoggingEventBuilder error(BaseLogger logger);
}
