package penna.core.slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PennaMDCAdapterTest {

    @Test
    void canStoreInTheMdc() {
        var adapter = new PennaMDCAdapter();

        adapter.put("Key", "value");
        Assertions.assertEquals("value", adapter.get("Key"));
    }

    @Test
    void canOverwriteAValueInMdc() {
        var adapter = new PennaMDCAdapter();

        adapter.put("Key", "value");
        Assertions.assertEquals("value", adapter.get("Key"));
        adapter.put("Key", "other");
        Assertions.assertEquals("other", adapter.get("Key"));
    }

    @Test
    void canReplaceTheContextMap() {
        var adapter = new PennaMDCAdapter();

        // Sets initial value
        adapter.put("Key", "value");
        Assertions.assertEquals("value", adapter.get("Key"));

        // Stores a copy
        var previous = adapter.getCopyOfContextMap();

        // Overwrites the MDC
        adapter.put("Key", "other");
        Assertions.assertEquals("other", adapter.get("Key"));
        Assertions.assertEquals("value", previous.get("Key"));

        // Restores from previous copy
        adapter.setContextMap(previous);
        Assertions.assertEquals("value", adapter.get("Key"));

        // Overwrites after restore
        adapter.put("Key", "other");
        Assertions.assertEquals("other", adapter.get("Key"));
    }

    @Test
    void canUpdateContextOutsideMdc() {
        var adapter = new PennaMDCAdapter();

        // Sets initial value
        adapter.put("Key", "value");
        Assertions.assertEquals("value", adapter.get("Key"));

        // Stores a copy
        var previous = adapter.getCopyOfContextMap();

        // Overwrites the MDC
        adapter.put("Key", "other");
        Assertions.assertEquals("other", adapter.get("Key"));
        Assertions.assertEquals("value", previous.get("Key"));

        previous.put("Key", "third");

        // Restores from previous copy
        adapter.setContextMap(previous);
        Assertions.assertEquals("third", adapter.get("Key"));

        // Overwrites after restore
        adapter.put("Key", "other");
        Assertions.assertEquals("other", adapter.get("Key"));
    }


}