package penna.core.slf4j.mdc;

import org.jetbrains.annotations.NotNull;
import penna.core.internals.store.StringTreeMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public final class EmptyMdc implements Mdc {
    @Override
    public String put(String key, String val) {
        var inner = new StringTreeMap();
        inner.put(key, val);
        Control.mdcStorage.set(new MdcStorage(inner));
        return key;
    }

    @Override
    public String remove(Object key) {
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        Control.mdcStorage.set(new MdcStorage(new StringTreeMap(m)));
    }

    @Override
    public void clear() {}

    @NotNull
    @Override
    public Set<String> keySet() {
        return Set.of();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return List.of();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return Set.of();
    }

    @Override
    public boolean isNotEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void remove(String key) {}

    @Override
    public boolean isEmpty() {return true;}

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public String get(Object key) {return null;}

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {}

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return this;
    }
}
