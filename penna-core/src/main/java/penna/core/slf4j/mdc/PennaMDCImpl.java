package penna.core.slf4j.mdc;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public sealed interface PennaMDCImpl extends PennaMDCSupport {
    /**
     * Basic static storage to allow for control over the MDC structure;
     */
    final class Control {
        private Control() {}

        /**
         * {@link EmptyMDC} Singleton instance. Default visibility so the {@link PennaMDCImpl} implementations
         * can refer to it.
         */
        public static final EmptyMDC empty = new EmptyMDC();

        /**
         * The actual MDC storage for current thread
         */
        public static final ThreadLocal<PennaMDCImpl> mdcStorage = ThreadLocal.withInitial(() -> empty);
    }

    final class EmptyMDC implements PennaMDCImpl {
        @Override
        public void put(String s, String s1) {
            var storage = new TreeMap<String, String>();
            storage.put(s, s1);
            Control.mdcStorage.set(new ActiveMDC(storage));
        }

        @Override
        public String get(String s) {
            return null;
        }

        @Override
        public void remove(String s) {}

        @Override
        public void clear() {}

        @Override
        public Map<String, String> getCopyOfContextMap() {return new TreeMap<>();}

        @Override
        public void setContextMap(Map<String, String> map) {
            Control.mdcStorage.set(new ActiveMDC(new TreeMap<>(map)));
        }

        @Override
        public void forEach(BiConsumer<String, String> action) {}
    }

    record ActiveMDC(Map<String, String> storage) implements PennaMDCImpl {
        @Override
        public void put(String key, String val) {
            storage.put(key, val);
        }

        @Override
        public String get(String key) {
            return storage.get(key);
        }

        @Override
        public void remove(String key) {
            storage.remove(key);
            if (storage.isEmpty()) {
                Control.mdcStorage.set(Control.empty);
            }
        }

        @Override
        public void clear() {
            Control.mdcStorage.set(Control.empty);
        }

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return new TreeMap<>(storage);
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
            storage.clear();
            storage.putAll(contextMap);
        }

        @Override
        public void forEach(BiConsumer<String, String> action) {
            storage.forEach(action);

        }
    }
}
