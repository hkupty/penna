package penna.core.slf4j.mdc;

import org.slf4j.spi.MDCAdapter;
import penna.core.internals.MapWrapperRingBuffer;
import penna.core.internals.MapWrapperTicket;

import java.util.*;
import java.util.function.BiConsumer;

public final class MDCNode implements MDCAdapter{
    final MapWrapperRingBuffer ringBuffer = new MapWrapperRingBuffer();

    public MDCNode(Map<String, String> mdcValues) {
        ringBuffer.put(mdcValues);
    }

    public static MDCNode create() {
        return new MDCNode(new HashMap<>());
    }

    public void forEach(BiConsumer<String, String> action, Collection<String> printedKeys) {
        var map = ringBuffer.get();
        var keys = map.keySet();
        keys.removeAll(printedKeys);
        for (var key : keys) {
            action.accept(key, map.get(key));
        }
    }

    public void forEach(BiConsumer<String, String> action) {
        var map = ringBuffer.get();
        for (var key : map.keySet()) {
            action.accept(key, map.get(key));
        }
    }

    public int size() { return ringBuffer.get().size(); }

    public boolean isNotEmpty() {
        return !ringBuffer.get().isEmpty();
    }


    @Override
    public void put(String key, String val) {
        ringBuffer.get().inner().put(key, val);
    }

    @Override
    public String get(String key) {
        return ringBuffer.get().get(key);
    }

    @Override
    public void remove(String key) {
        ringBuffer.get().inner().remove(key);
    }

    @Override
    public void clear() {
        ringBuffer.get().inner().clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        var wrapper = ringBuffer.get();
        ringBuffer.put(new HashMap<>(wrapper.inner()));

        return wrapper;
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        if (contextMap instanceof MapWrapperTicket wrapper) {
            if (ringBuffer.get(wrapper.ticket()) == wrapper) {
                ringBuffer.reset(wrapper.ticket());
            } else {
                ringBuffer.put(wrapper.inner());
            }
        } else {
            ringBuffer.put(contextMap);
        }
    }



    @Override
    public void pushByKey(String key, String value) { }

    @Override
    public String popByKey(String key) {
        return null;
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        return null;
    }

    @Override
    public void clearDequeByKey(String key) { }


}
