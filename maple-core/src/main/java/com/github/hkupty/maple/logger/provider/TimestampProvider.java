package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.LongFrame;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

public class TimestampProvider implements DataFrameProvider<LongFrame> {

    @Override
    public LogField field() {
        return LogField.Timestamp;
    }

    @Override
    public LongFrame get(LoggingEvent event) {
        return new LongFrame(LogField.Timestamp.name(), System.currentTimeMillis());
    }
}
