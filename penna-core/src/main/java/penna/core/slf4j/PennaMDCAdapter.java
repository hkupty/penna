package penna.core.slf4j;

import org.slf4j.spi.MDCAdapter;
import penna.core.slf4j.mdc.Mdc;

import java.util.Deque;
import java.util.Map;
import java.util.function.BiConsumer;

public final class PennaMDCAdapter implements MDCAdapter {

    public void forEach(BiConsumer<String, String> action) {
        var node = Mdc.Inner.mdcStorage.get();
        node.forEach(action);
    }

    public boolean isNotEmpty() {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        return inner.isNotEmpty();
    }

    @Override
    public void put(String key, String val) {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        inner.put(key, val);
    }

    @Override
    public String get(String key) {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        return inner.get(key);
    }

    @Override
    public void remove(String key) {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        inner.remove(key);
    }

    @Override
    public void clear() {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        inner.clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        return inner.getCopyOfContextMap();
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        Mdc inner = Mdc.Inner.mdcStorage.get();
        inner.setContextMap(contextMap);
    }

    @Override
    public void pushByKey(String key, String value) {
        // Intentionally left black, not supported at the moment
    }

    @Override
    public String popByKey(String key) {
        return null;
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        return null;
    }

    @Override
    public void clearDequeByKey(String key) {
        // Intentionally left black, not supported at the moment
    }
}
