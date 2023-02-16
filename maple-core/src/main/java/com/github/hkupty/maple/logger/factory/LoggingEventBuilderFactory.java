package com.github.hkupty.maple.logger.factory;

import com.github.hkupty.maple.logger.MapleLogger;
import org.slf4j.spi.LoggingEventBuilder;

public interface LoggingEventBuilderFactory {

    boolean isTraceEnabled();
    LoggingEventBuilder trace(MapleLogger logger);

    boolean isDebugEnabled();
    LoggingEventBuilder debug(MapleLogger logger);

    boolean isInfoEnabled();
    LoggingEventBuilder info(MapleLogger logger);

    boolean isWarnEnabled();
    LoggingEventBuilder warn(MapleLogger logger);

    boolean isErrorEnabled();
    LoggingEventBuilder error(MapleLogger logger);
}
