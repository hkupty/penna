package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.EntryDataFrame;
import org.slf4j.MDC;
import org.slf4j.event.LoggingEvent;

public class MDCProvider implements DataFrameProvider<EntryDataFrame> {

    @Override
    public LogField field() {
        return LogField.MDC;
    }

    @Override
    public EntryDataFrame get(LoggingEvent event) {
        return new EntryDataFrame(field().name(),  MDC.getCopyOfContextMap());
    }
}
