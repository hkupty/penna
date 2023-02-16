package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.ThrowableDataFrame;
import org.slf4j.event.LoggingEvent;

public class ThrowableProvider implements DataFrameProvider<ThrowableDataFrame> {
    @Override
    public LogField field() {
        return LogField.Throwable;
    }

    @Override
    public ThrowableDataFrame get(LoggingEvent event) {
        Throwable throwable;
        if ((throwable = event.getThrowable()) != null) {
            return new ThrowableDataFrame(field().name(), event.getThrowable());
        }
        return null;
    }
}
