package penna.core.sink;

import penna.core.models.PennaLogEvent;

import java.io.IOException;

public sealed interface Sink permits CoreSink, InternalSink {
    void write(PennaLogEvent logEvent) throws IOException;
}