package penna.core.slf4j.mdc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import penna.core.internals.store.StringMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public record MdcStorage(StringMap inner) implements Mdc {
    @Override
    public boolean isNotEmpty() {
        return !inner.isEmpty();
    }

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
    public String get(String key) {
        return inner.get(key);
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        return inner.put(key, value);
    }

    @Override
    public String remove(Object key) {
        var ret = inner.remove(key);
        if (inner.isEmpty()) {
            Control.mdcStorage.set(Control.empty);
        }
        return ret;
    }

    @Override
    public void remove(String key) {
        inner.remove(key);
        if (inner.isEmpty()) {
            Control.mdcStorage.set(Control.empty);
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        inner.putAll(m);
    }

    @Override
    public void clear() {
        inner.clear();
        Control.mdcStorage.set(Control.empty);
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return inner.keySet();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return inner.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return inner.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
        inner.forEach(action);
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        if (this.isEmpty()) {
            return Control.empty;
        } else {
            return new penna.core.slf4j.mdc.MdcStorage(inner.copy());
        }
    }
}
