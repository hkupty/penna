package penna.core.sink.impl;

import penna.core.models.PennaLogEvent;
import penna.core.sink.SinkImpl;

import java.io.IOException;
import java.io.Writer;

public final class NOPSink implements SinkImpl {

    private NOPSink() {}

    private static final NOPSink singleton = new NOPSink();

    public static NOPSink getInstance() { return singleton; }
    @Override
    public void init(Writer writer) throws IOException {
        // this method should, intentionally, do nothing.
        // this class only exists to fill in a compliant instance
        // in case no implementation is found
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        // this method should, intentionally, do nothing.
        // this class only exists to fill in a compliant instance
        // in case no implementation is found
    }
}
