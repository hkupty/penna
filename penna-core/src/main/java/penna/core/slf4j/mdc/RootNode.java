package penna.core.slf4j.mdc;

import java.util.*;
import java.util.function.BiConsumer;

public record RootNode(Map<String, String> mdcValues) implements MDCNode {

    public static RootNode create() {
        return new RootNode(new HashMap<>());
    }
    @Override
    public void put(String key, String val) {
        mdcValues.put(key, val);
    }

    @Override
    public String get(String key) {
        return mdcValues.get(key);
    }

    @Override
    public void remove(String key) {
        mdcValues.remove(key);
    }

    @Override
    public void clear() {
        mdcValues.clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return Map.copyOf(mdcValues);
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        for (var key : mdcValues.keySet()) {
            String newVal;
            if ((newVal = contextMap.get(key)) != null) {
                mdcValues.put(key, newVal);
            } else {
                mdcValues.remove(key);
            }
        }
    }

    @Override
    public void pushByKey(String key, String value) {

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

    }

    @Override
    public void forEach(BiConsumer<String, String> action, Collection<String> printedKeys) {
        for (var key : mdcValues.keySet()) {
            if(!printedKeys.contains(key)) {
                action.accept(key, mdcValues.get(key));
            }
        }
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        for (var key : mdcValues.keySet()) {
            action.accept(key, mdcValues.get(key));
        }
    }

    @Override
    public int size() {
        return mdcValues().size();
    }

    @Override
    public boolean isNotEmpty() {
        return !mdcValues.isEmpty();
    }
}
