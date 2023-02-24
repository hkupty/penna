package maple.core.sink;

import maple.core.models.MapleLogEvent;

public sealed interface SinkProxy permits SinkService.DirectSinkWrapper {
    void write(MapleLogEvent logData);
}
