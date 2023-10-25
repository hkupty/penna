package penna.core.internals;

import java.util.HashMap;
import java.util.Map;

public final class MapWrapperRingBuffer {
    final MapWrapperTicket[] buffer = new MapWrapperTicket[32];
    int index;
    int max;

    public MapWrapperRingBuffer() {
        buffer[index] = new MapWrapperTicket(index, new HashMap<>());
    }

    public MapWrapperTicket get() {
        return buffer[index];
    }

    public MapWrapperTicket get(int ticket) {
        return buffer[ticket];
    }

    public void reset(int ticket) {
        index = ticket;
    }

    public void put(final Map<String, String> next) {
        index = ++max & 0x1f;
        buffer[index] = new MapWrapperTicket(index, next);
    }
}
