package penna.core.slf4j.mdc;

import penna.core.internals.store.StringMap;
import penna.core.internals.store.StringTreeMap;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This is the MDC Proxy between the adapter and the actual storage.
 * The purpose of this intermediary layer is to ensure a few things:
 * <br/>
 * - We don't create unnecessary garbage in the memory if no values are associated with the MDC, so we reuse the
 * {@link EmptyMdc} instance in all threads by default;
 * - We can swap the internal implementation without interfering with the adapter (given it's an external interface
 * from slf4j);
 * <br />
 * If we want to make any configuration changes, the inner {@link Control} class is the one that should be updated
 */
public sealed interface Mdc extends Map<String, String> permits EmptyMdc, MdcStorage {

    boolean isNotEmpty();

    int size();

    void remove(String key);

    void forEach(BiConsumer<? super String, ? super String> action);

    String get(String key);

    default void setContextMap(Map<String, String> contextMap) {
        if (contextMap instanceof Mdc mdcCtx) {
            Control.mdcStorage.set(mdcCtx);
        } else if (contextMap.isEmpty()) {
            Control.mdcStorage.set(Control.empty);
        } else if (contextMap instanceof StringMap arrayMap) {
            Control.mdcStorage.set(new MdcStorage(arrayMap));
        } else {
            Control.mdcStorage.set(new MdcStorage(new StringTreeMap(contextMap)));
        }
    }


    Map<String, String> getCopyOfContextMap();

    /**
     * Basic static storage to allow for control over the MDC structure;
     */
    final class Control {
        private Control() {}

        /**
         * {@link EmptyMdc} Singleton instance. Default visibility so the {@link Mdc} implementations
         * can refer to it.
         */
        static final EmptyMdc empty = new EmptyMdc();

        /**
         * The actual MDC storage for current thread
         */
        public static final ThreadLocal<Mdc> mdcStorage = ThreadLocal.withInitial(() -> empty);
    }

}