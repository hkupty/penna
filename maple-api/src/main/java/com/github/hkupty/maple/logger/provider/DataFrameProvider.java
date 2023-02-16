package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.DataFrame;
import org.slf4j.event.LoggingEvent;

public interface DataFrameProvider<Frame extends DataFrame<?>> {

    public LogField field();
    public Frame get(LoggingEvent event);
}
