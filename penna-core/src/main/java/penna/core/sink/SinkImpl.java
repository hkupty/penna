package penna.core.sink;

import penna.core.models.PennaLogEvent;
import penna.core.sink.impl.*;

import java.io.IOException;
import java.io.Writer;

public sealed interface SinkImpl permits GsonPennaSink, JacksonPennaSink, JakartaPennaSink, NOPSink, DummySink {
    void init(Writer writer) throws IOException;
    void write(PennaLogEvent logEvent) throws IOException;
}
