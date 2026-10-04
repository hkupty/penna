package penna.core.slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

class PennaSLF4JTest {

    @Test
    void canLogWithoutExceptions() {
        LoggerFactory
          .getLogger("test")
          .atLevel(Level.INFO)
          .setMessage("Hello, world!")
          .log();
    }

}
