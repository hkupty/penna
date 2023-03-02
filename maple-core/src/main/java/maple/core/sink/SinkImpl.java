package maple.core.sink;

import maple.core.models.MapleLogEvent;
import maple.core.sink.impl.GsonMapleSink;
import maple.core.sink.impl.JacksonMapleSink;
import maple.core.sink.impl.JakartaMapleSink;

import java.io.IOException;
import java.io.Writer;

public sealed interface SinkImpl permits GsonMapleSink, JacksonMapleSink, JakartaMapleSink {
    void init(Writer writer) throws IOException;
    void write(MapleLogEvent logEvent) throws IOException;
}
