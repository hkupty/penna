package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.logger.MapleLogger;
import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.StringFrame;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

public class LoggerNameProvider implements DataFrameProvider<StringFrame> {

    private final StringFrame nameFrame;

    public LoggerNameProvider(MapleLogger logger) {
        nameFrame = new StringFrame(LogField.LoggerName.name(), logger.getName());

    }

    @Override
    public LogField field() {
        return LogField.LoggerName;
    }

    @Override
    public StringFrame get(LoggingEvent event) {
        return nameFrame;
    }

}
