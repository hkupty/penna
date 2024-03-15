package penna.core.internals.store;

import java.util.Map;

/**
 * The internal MDC implementation assumes a {@link Map} of Strings to String, so this interface
 * exists to ensure we can experiment and replace implementations without breaking SLF4J's binding implementation
 */
public sealed interface StringMap extends Map<String, String> permits StringTreeMap {
    StringMap copy();
}
