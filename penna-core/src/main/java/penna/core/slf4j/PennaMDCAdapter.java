package penna.core.slf4j;

import org.slf4j.spi.MDCAdapter;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class PennaMDCAdapter implements MDCAdapter {
    private record Node(HashMap<String, String> data, Node parent) implements MDCAdapter{

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
    }

    private final InheritableThreadLocal<Node> threadLocalTree = new InheritableThreadLocal<>() {

        @Override
        protected Node initialValue() {
            return new Node(new HashMap<>(), null);
        }

        @Override
        protected Node childValue(Node parentValue) {
            return new Node(new HashMap<>(), parentValue);
        }
    };

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
