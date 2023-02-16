package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.StringFrame;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;


public class MessageProvider implements DataFrameProvider<StringFrame> {
    @Override
    public LogField field() {
        return LogField.Message;
    }

    @Override
    public StringFrame get(LoggingEvent event) {
        String message;
        Object[] arguments;
        if ((message = event.getMessage()) != null) {
            if ((arguments = event.getArgumentArray())!= null && arguments.length > 0) {
                return new StringFrame(field().name(),
                        MessageFormatter.basicArrayFormat(
                                message,
                                arguments
                        ));
            }
            return new StringFrame(field().name(), message);
        }
        return null;
    }
}
