package penna.core.internals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringNavigatorTest {

    @Test
    void canFindSameString() {
        StringNavigator sn = new StringNavigator(StringNavigatorTest.class.getName());
        Assertions.assertEquals(4, sn.target);
        Assertions.assertEquals("penna", sn.chunk(0).toString());
        Assertions.assertEquals(1, sn.chunk(0).indexCompare("penna"));
        Assertions.assertEquals("core", sn.chunk(1).toString());
        Assertions.assertEquals(1, sn.chunk(1).indexCompare("core"));
        Assertions.assertEquals("internals", sn.chunk(2).toString());
        Assertions.assertEquals(1, sn.chunk(2).indexCompare("internals"));
        Assertions.assertEquals("StringNavigatorTest", sn.chunk(3).toString());
        Assertions.assertEquals(1, sn.chunk(3).indexCompare("StringNavigatorTest"));

    }

}