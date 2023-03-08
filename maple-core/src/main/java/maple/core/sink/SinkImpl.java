package maple.core.sink;

import maple.core.models.MapleLogEvent;
import maple.core.sink.impl.*;

import java.io.IOException;
import java.io.Writer;

public sealed interface SinkImpl permits GsonMapleSink, JacksonMapleSink, JakartaMapleSink, NOPSink, DummySink {
    void init(Writer writer) throws IOException;
    void write(MapleLogEvent logEvent) throws IOException;
}
