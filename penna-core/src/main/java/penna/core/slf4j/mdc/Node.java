package penna.core.slf4j.mdc;

import java.util.*;
import java.util.function.BiConsumer;

public record Node(Map<String, String> mdcValues, MDCNode parent) implements MDCNode{
    public static Node create(MDCNode parent) {
        return new Node(new HashMap<>(), parent);
    }

    @Override
    public void forEach(BiConsumer<String, String> action, Collection<String> printedKeys) {
        var keys = mdcValues.keySet();
        for (var key : keys) {
            if(!printedKeys.contains(key)) {
                printedKeys.add(key);
                action.accept(key, mdcValues.get(key));
            }
        }
        parent.forEach(action, printedKeys);
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        forEach(action, new ArrayList<>());
    }

    @Override
    public int size() {
        return mdcValues().size() + parent.size();
    }

    @Override
    public void put(String key, String val) {
        mdcValues.put(key, val);
    }

    @Override
    public String get(String key) {
        String result;
        if((result = mdcValues.get(key)) != null) {
            return result;
        }
        return parent.get(key);
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

    public boolean isNotEmpty() {
        return !mdcValues.isEmpty();
    }
}
