package penna.core.slf4j.mdc;

import org.slf4j.spi.MDCAdapter;

import java.util.Deque;
import java.util.function.BiConsumer;

public interface PennaMDCSupport extends MDCAdapter {

    @Override
    default void pushByKey(String s, String s1) {}

    @Override
    default String popByKey(String s) {
        return null;
    }

    @Override
    default Deque<String> getCopyOfDequeByKey(String s) {
        return null;
    }

    @Override
    default void clearDequeByKey(String s) {}

    void forEach(BiConsumer<String, String> action);
}
