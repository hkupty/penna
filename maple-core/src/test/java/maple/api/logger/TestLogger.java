package maple.api.logger;

import maple.api.models.MapleLogEvent;
import maple.api.models.Config;
import maple.core.logger.MapleLogger;

import java.util.ArrayDeque;
import java.util.Queue;


public class TestLogger extends MapleLogger {
    private final Queue<MapleLogEvent> buffer;

    public TestLogger(String name, Config config) {
        super(name, config);
        buffer = new ArrayDeque<>();
    }

    public TestLogger(String name) {
        this(name, Config.getDefault());
    }



    public Queue<MapleLogEvent> getBuffer() { return buffer; }
}
