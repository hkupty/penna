package penna.core.slf4j;

import org.slf4j.spi.MDCAdapter;
import penna.core.slf4j.mdc.MDCNode;

import java.util.*;
import java.util.function.BiConsumer;

public final class PennaMDCAdapter implements MDCAdapter {

    private final ThreadLocal<MDCNode> threadLocalTree = ThreadLocal.withInitial(MDCNode::create);

    public void forEach(BiConsumer<String, String> action) {
        var node = threadLocalTree.get();
        node.forEach(action);
    }

    public boolean isNotEmpty() {
        return threadLocalTree.get().isNotEmpty();
    }

    @Override
    public void put(String key, String val) {
        var node = threadLocalTree.get();
        node.put(key, val);
    }

    @Override
    public String get(String key) {
        var node = threadLocalTree.get();
        return node.get(key);
    }

    @Override
    public void remove(String key) {
        var node = threadLocalTree.get();
        node.remove(key);
    }

    @Override
    public void clear() {
        var node = threadLocalTree.get();
        node.clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        var node = threadLocalTree.get();
        return node.getCopyOfContextMap();
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        var node = threadLocalTree.get();
        node.setContextMap(contextMap);
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
