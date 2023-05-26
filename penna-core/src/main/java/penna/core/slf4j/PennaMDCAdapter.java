package penna.core.slf4j;

import org.slf4j.spi.MDCAdapter;

import java.util.*;
import java.util.function.BiConsumer;

public final class PennaMDCAdapter implements MDCAdapter {
    private record Node(Map<String, String> data, Node parent, Set<String> printedKeys) implements MDCAdapter{

        private void forEach(BiConsumer<String, String> action, Set<String> printedKeys) {
            if (parent != null) {
                parent.forEach(action, printedKeys);
            }

            var keys = data.keySet();
            keys.removeAll(printedKeys);

            for (var key : keys) {
                action.accept(key, data.get(key));
            }

            printedKeys.addAll(keys);
        }

        public void forEach(BiConsumer<String, String> action) {
            printedKeys.clear();
            forEach(action, printedKeys);
        }

        @Override
        public void put(String key, String val) {
            data.put(key, val);
        }

        @Override
        public String get(String key) {
            String ret;
            if((ret = data.get(key)) != null) {
                return ret;
            } else if (parent != null && (ret = parent().get(key)) != null) {
                return ret;
            }
            return null;
        }

        @Override
        public void remove(String key) {
            data.remove(key);
        }

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public Map<String, String> getCopyOfContextMap() {
            if (parent == null) {
                return new HashMap<>(data);
            }

            var contextMap = parent.getCopyOfContextMap();
            contextMap.putAll(data);

            return contextMap;
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
            data.clear();
            data.putAll(contextMap);
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
            return (!data.isEmpty()) || (parent != null && parent().isNotEmpty());
        }
    }

    private final InheritableThreadLocal<Node> threadLocalTree = new InheritableThreadLocal<>() {

        @Override
        protected Node initialValue() {
            return new Node(new HashMap<>(), null, new HashSet<>());
        }

        @Override
        protected Node childValue(Node parentValue) {
            return new Node(new HashMap<>(), parentValue, new HashSet<>());
        }
    };

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
