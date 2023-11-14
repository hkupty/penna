package penna.core.slf4j.mdc;

import org.slf4j.spi.MDCAdapter;
import penna.core.internals.MapWrapperRingBuffer;
import penna.core.internals.MapWrapperTicket;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public sealed interface Mdc extends MDCAdapter {

    boolean isNotEmpty();
    int size();
    void forEach(BiConsumer<String, String> action);

    final class Inner {
        private Inner() {}
        private static final EmptyMdc empty = new EmptyMdc();
        public static final ThreadLocal<Mdc> mdcStorage = ThreadLocal.withInitial(() -> empty);

    }

    final class EmptyMdc implements Mdc {
        private static final Map<String, String> empty = Map.of();

        @Override
        public void put(String key, String val) {
            Inner.mdcStorage.set(new MdcStorage(key, val));
        }

        @Override
        public String get(String key) {
            return null;
        }

        @Override
        public void remove(String key) {}

        @Override
        public void clear() {}

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return empty;
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
            Inner.mdcStorage.set(new MdcStorage(contextMap));
        }

        @Override
        public void pushByKey(String key, String value) {
            // Swap
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
        public void clearDequeByKey(String key) {}

        @Override
        public boolean isNotEmpty() {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void forEach(BiConsumer<String, String> action) {}
    }
    final class MdcStorage implements Mdc {
        final MapWrapperRingBuffer ringBuffer = new MapWrapperRingBuffer();

        MdcStorage(String key, String val) {
            ringBuffer.put(new HashMap<>());
            ringBuffer.get().inner().put(key, val);
        }

        MdcStorage(Map<String, String> context) {
            ringBuffer.put(context);
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
            Inner.mdcStorage.set(Inner.empty);
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
        public boolean isNotEmpty() {
            return !ringBuffer.get().isEmpty();
        }

        @Override
        public int size() {
            return ringBuffer.get().size();
        }

        @Override
        public void forEach(BiConsumer<String, String> action) {
            var map = ringBuffer.get();
            for (var key : map.keySet()) {
                action.accept(key, map.get(key));
            }
        }
    }
}
