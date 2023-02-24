package maple.core.sink;

import maple.core.models.MapleLogEvent;
import maple.core.sink.impl.JacksonMapleSink;

import java.io.IOException;
import java.io.Writer;

public sealed interface SinkImpl permits JacksonMapleSink {
    void init(Writer writer) throws IOException;
    void write(MapleLogEvent logEvent) throws IOException;
}
