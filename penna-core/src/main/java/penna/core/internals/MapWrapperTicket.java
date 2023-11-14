package penna.core.internals;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public record MapWrapperTicket(int ticket, Map<String, String> inner) implements Map<String, String> {

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return inner.get(key);
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("MDC Context map should not be modified");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("MDC Context map should not be modified");
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("MDC Context map should not be modified");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("MDC Context map should not be modified");
    }

    @Override
    public @NotNull Set<String> keySet() {
        return inner.keySet();
    }

    @Override
    public @NotNull Collection<String> values() {
        return inner.values();
    }

    @Override
    public @NotNull Set<Entry<String, String>> entrySet() {
        return inner.entrySet();
    }
}
