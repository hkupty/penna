package penna.core.slf4j.mdc;

import org.slf4j.spi.MDCAdapter;

import java.util.Collection;
import java.util.function.BiConsumer;

public sealed interface MDCNode extends MDCAdapter permits RootNode, Node {
    void forEach(BiConsumer<String, String> action, Collection<String> printedKeys);
    void forEach(BiConsumer<String, String> action);
    int size();
    boolean isNotEmpty();
}
