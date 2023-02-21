package maple.core.sink;

import maple.api.models.MapleLogEvent;

public interface SinkQueue {
    void enqueue(MapleLogEvent logData);
}
