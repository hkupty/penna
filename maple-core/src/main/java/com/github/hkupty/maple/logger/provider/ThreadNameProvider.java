package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.StringFrame;
import org.slf4j.event.LoggingEvent;

public class ThreadNameProvider implements DataFrameProvider<StringFrame> {

    private ThreadLocal<StringFrame> stringFrameFactory;

    private static StringFrame newThreadNameFrame() {
        String thread = Thread.currentThread().getName();
        return new StringFrame(LogField.ThreadName.name(), thread);
    }

    public ThreadNameProvider() {
        this.stringFrameFactory = ThreadLocal.withInitial(ThreadNameProvider::newThreadNameFrame);
    }


    @Override
    public LogField field() {
        return LogField.ThreadName;
    }

    @Override
    public StringFrame get(LoggingEvent event) {
        return stringFrameFactory.get();
    }
}
