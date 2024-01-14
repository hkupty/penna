package penna.core.internals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringNavigatorTest {

    @Test
    void canFindSameString() {
        StringNavigator sn = new StringNavigator(StringNavigatorTest.class.getName());

        Assertions.assertTrue(sn.hasNext);
        StringNavigator.StringView view = sn.next();
        Assertions.assertEquals("penna", view.toString());
        Assertions.assertEquals(1, view.indexCompare("penna"));

        Assertions.assertTrue(sn.hasNext);
        view = sn.next();
        Assertions.assertEquals("core", view.toString());
        Assertions.assertEquals(1, view.indexCompare("core"));

        Assertions.assertTrue(sn.hasNext);
        view = sn.next();
        Assertions.assertEquals("internals", view.toString());
        Assertions.assertEquals(1, view.indexCompare("internals"));

        Assertions.assertTrue(sn.hasNext);
        view = sn.next();
        Assertions.assertEquals("StringNavigatorTest", view.toString());
        Assertions.assertEquals(1, view.indexCompare("StringNavigatorTest"));

        Assertions.assertFalse(sn.hasNext);
    }

}