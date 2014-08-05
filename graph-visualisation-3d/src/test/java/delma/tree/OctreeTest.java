package delma.tree;

import delma.tree.Octree.Node;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public class OctreeTest {

    private Octree tree;

    @Before
    public void setUp() {
        tree = new Octree();
    }

    @Test()
    public void addingBodyWorks() {
        tree.addBody("A", new Vector3f(1, 1, 0), 10);

        Node rightNode = tree.getRoot();
        assertEquals(10, rightNode.getMass(), 0);
        assertEquals(new Vector3f(1, 1, 0), rightNode.getMassCenter());
        assertEquals("A", rightNode.getData().get(0));
    }

    @Test()
    public void addingMultipleBodiesWork() {
        tree.addBody("A", new Vector3f(1, 1, 1), 10);
        tree.addBody("B", new Vector3f(-1, -1, 1), 8);

        Node rightNode = tree.getRoot().getSubNode(1, 1, 1);
        assertEquals(10, rightNode.getMass(), 0);
        assertEquals(new Vector3f(1, 1, 1), rightNode.getMassCenter());
        assertEquals("A", rightNode.getData().get(0));

        rightNode = tree.getRoot().getSubNode(0, 0, 1);
        assertEquals(8, rightNode.getMass(), 0);
        assertEquals(new Vector3f(-1, -1, 1), rightNode.getMassCenter());
        assertEquals("B", rightNode.getData().get(0));
    }

    @Test()
    public void dividingInQuadrantsWorks() {
        tree.addBody("A", new Vector3f(1, 1, 1), 10);
        tree.addBody("B", new Vector3f(Float.MAX_VALUE / 2 + 1, 1, 1), 5);

        Node rightNode = tree.getRoot().getSubNode(1, 1, 1).getSubNode(0, 0, 0);
        assertEquals(10, rightNode.getMass(), 0);
        assertEquals(new Vector3f(1, 1, 1), rightNode.getMassCenter());
        assertEquals("A", rightNode.getData().get(0));

        rightNode = tree.getRoot().getSubNode(1, 1, 1).getSubNode(1, 0, 0);
        assertEquals(5, rightNode.getMass(), 0);
        assertEquals(new Vector3f(Float.MAX_VALUE / 2 + 1, 1, 1), rightNode.getMassCenter());
        assertEquals("B", rightNode.getData().get(0));
    }

    @Test()
    public void calculatingMassAndMassCenterWorks() {
        tree.addBody("A", new Vector3f(1, 1, 0), 10);
        tree.addBody("B", new Vector3f(2, 5, 0), 5);

        Node rightNode = tree.getRoot();
        assertEquals(15, rightNode.getMass(), 0);
        Vector3f center = rightNode.getMassCenter();
        assertEquals((1 * 10 + 2 * 5) / 15.0f, center.x, 0.0001f);
        assertEquals((1 * 10 + 5 * 5) / 15.0f, center.y, 0.0001f);
        assertEquals(0, center.z, 0.0001f);
    }

//    @Test()
//    public void addingSameIsNotAllowed() {
//        tree.addBody("A", new Vector3f(-4100.0f, 1408.0f, 0), 10);
//        assertFalse(tree.addBody("B", new Vector3f(-4100.0f, 1408.0f, 0), 5));
//
//        Node rightNode = tree.getRoot();
//        assertEquals("A", rightNode.getData().get(0));
//        //emptyTree.addBody("C", new Vector(-4291.0, 1791.0), 10);
//        //emptyTree.addBody("D", new Vector(-4291.0, 1790.0), 5);
//
//        //emptyTree.addBody("A", new Vector(-4291.0, 1408.0), 10);
//        //emptyTree.addBody("D", new Vector(-4291.0, 1791.0), 5);
//        //emptyTree.addBody("A", new Vector(-4291.0, 1493.0), 10);
//        //emptyTree.addBody("B", new Vector(-4291.0, 1602.0), 5);
//    }
}
