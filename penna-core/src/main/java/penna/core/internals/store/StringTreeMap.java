package penna.core.internals.store;

import java.io.Serial;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple wrapper over {@link TreeMap} just to provide two basic guarantees:
 * - We control the copying of the map;
 * - We control the initialization;
 * <br />
 * This is not strictly necessary, but it is a very performant implementation of {@link StringMap},
 * which is our goal.
 */
public final class StringTreeMap extends TreeMap<String, String> implements StringMap {

    @Serial
    private static final long serialVersionUID = 23827L;

    public StringTreeMap() {
        super();
    }

    public StringTreeMap(Map<? extends String, ? extends String> m) {
        super();
        super.putAll(m);
    }

    @Override
    public StringMap copy() {
        return new StringTreeMap(this);
    }
}
