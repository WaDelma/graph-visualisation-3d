package delma.set;

import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author delma
 */
public class DisjointSetTest {

    private DisjointSet<String> set;

    @Before
    public void setUp() {
        set = new DisjointSet<>();
    }

    @Test
    public void testSize() {
        assertEquals(0, set.size());
    }

    @Test
    public void testAdd() {
        set.add("a");
        assertEquals(1, set.size());
        set.add("b");
        assertEquals(2, set.size());
        set.add("b");
        assertEquals(2, set.size());
    }

    @Test
    public void testFind() {
        assertNull(set.find("a"));
        set.add("a");
        assertEquals("a", set.find("a"));
        set.add("b");
        assertEquals("b", set.find("b"));
    }

    @Test
    public void testRemove() {
        assertFalse(set.remove("a"));

        set.add("a");
        assertEquals(1, set.size());
        assertTrue(set.remove("a"));
        assertEquals(0, set.size());
        assertNull(set.find("a"));

        set.add("a");
        set.add("b");
        set.union("a", "b");
        String a = set.find("a");
        String b = set.find("a");
        if (a.equals("a")) {
            assertEquals("a", b);
        } else {
            assertEquals("b", b);
        }
        assertTrue(set.remove("a"));
        assertEquals("b", set.find("b"));
    }

    @Test
    public void testUnion() {
        set.add("a");
        set.add("b");
        set.union("a", "b");
        String a = set.find("a");
        String b = set.find("a");
        if (a.equals("a")) {
            assertEquals("a", b);
        } else {
            assertEquals("b", b);
        }
        set.add("c");
        set.union("b", "c");
        String c = set.find("c");
        if (a.equals("a")) {
            assertEquals("a", c);
        } else {
            assertEquals("b", c);
        }
    }

    @Test
    public void testIterator() {
        set.add("a");
        set.add("b");
        set.union("a", "b");
        set.add("c");
        int n = 0;
        for (Set<String> s : set) {
            if (s.contains("a") || s.contains("b")) {
                assertTrue(s.contains("a") && s.contains("b"));
            } else {
                assertTrue(s.contains("c"));
            }
            n++;
        }
        assertEquals(2, n);
    }

}
