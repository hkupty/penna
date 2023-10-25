package penna.core.sink;

import penna.core.models.PennaLogEvent;

import java.io.IOException;

public sealed interface Sink permits CoreSink, ProxySink, NonStandardSink {
    void write(PennaLogEvent logEvent) throws IOException;
}