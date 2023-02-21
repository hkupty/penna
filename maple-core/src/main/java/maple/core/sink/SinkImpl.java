package maple.core.sink;

import maple.api.models.MapleLogEvent;

import java.io.IOException;
import java.io.OutputStream;

public interface SinkImpl {
    void init(OutputStream os) throws IOException;
    void write(MapleLogEvent logEvent) throws IOException;
}
