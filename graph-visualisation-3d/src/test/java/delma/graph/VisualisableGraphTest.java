package delma.graph;

import delma.graph.Graph.Edge;
import delma.graph.Graph.Node;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author delma
 */
public class VisualisableGraphTest {

    private VisualisableGraph graph;

    @Before
    public void setUp() {
        graph = new VisualisableGraph();
    }

    /**
     * Test of add method, of class VisualisableGraph.
     */
    @Test
    public void testAdd_GraphNode() {
        Node node = new Node("test");
        graph.add(node);
        Collection<Node<Object>> nodes = graph.getNodes();
        assertEquals(1, nodes.size());
        assertEquals(node, nodes.iterator().next());
    }

    /**
     * Test of add method, of class VisualisableGraph.
     */
    @Test
    public void testAdd_GraphEdge() {
        Node node1 = new Node("test1");
        Node node2 = new Node("test2");
        Graph.Edge<Object, Object> edge = new Edge(node1, node2, "test");
        graph.add(edge);

        Collection<Node<Object>> nodes = graph.getNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(node1));
        assertTrue(nodes.contains(node2));

        Collection<Edge<Object, Object>> edges = graph.getEdges();
        assertEquals(1, edges.size());
        assertEquals(edge, edges.iterator().next());
    }

    /**
     * Test of add method, of class VisualisableGraph.
     */
    @Test
    public void testAdd_Graph() {
        Node node1 = new Node("test");
        Node node2 = new Node("test1");
        Node node3 = new Node("test2");
        graph.add(node1);
        graph.add(node2);
        Graph<Object, Object> added = new VisualisableGraph();
        added.add(node2);
        added.add(node3);
        graph.add(added);

        Collection<Node<Object>> nodes = graph.getNodes();
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(node1));
        assertTrue(nodes.contains(node2));
        assertTrue(nodes.contains(node3));
    }

    /**
     * Test of remove method, of class VisualisableGraph.
     */
    @Test
    public void testRemove_GraphNode() {
        Node node = new Node("test");
        Node node2 = new Node("test1");

        graph.add(node);
        assertEquals(1, graph.getNodes().size());
        assertTrue(graph.remove(node));
        assertEquals(0, graph.getNodes().size());

        graph.add(node);
        assertEquals(1, graph.getNodes().size());
        assertFalse(graph.remove(node2));
        assertEquals(1, graph.getNodes().size());
    }

    /**
     * Test of remove method, of class VisualisableGraph.
     */
    @Test
    public void testRemove_GraphEdge() {
        Node node1 = new Node("test1");
        Node node2 = new Node("test2");
        Graph.Edge<Object, Object> edge = new Edge(node1, node2, "test", false);
        Graph.Edge<Object, Object> edge2 = new Edge(node2, node1, "test", false);

        graph.add(edge);
        assertEquals(1, graph.getEdges().size());
        assertTrue(graph.remove(edge));
        assertEquals(0, graph.getEdges().size());

        graph.add(edge);
        assertEquals(1, graph.getEdges().size());
        assertFalse(graph.remove(edge2));
        assertEquals(1, graph.getEdges().size());
    }

    /**
     * Test of remove method, of class VisualisableGraph.
     */
    @Test
    public void testRemove_Graph() {

    }

    /**
     * Test of getNeighbourEdges method, of class VisualisableGraph.
     */
    @Test
    public void testGetNeighbourEdges() {

    }

    /**
     * Test of getNeighbourNodes method, of class VisualisableGraph.
     */
    @Test
    public void testGetNeighbourNodes() {

    }

    /**
     * Test of getSubgraphs method, of class VisualisableGraph.
     */
    @Test
    public void testGetSubgraphs() {

    }

    /**
     * Test of getTranspose method, of class VisualisableGraph.
     */
    @Test
    public void testGetTranspose() {

    }

    /**
     * Test of contains method, of class VisualisableGraph.
     */
    @Test
    public void testContains() {

    }

    /**
     * Test of size method, of class VisualisableGraph.
     */
    @Test
    public void testSize() {

    }

    /**
     * Test of clear method, of class VisualisableGraph.
     */
    @Test
    public void testClear() {

    }

    /**
     * Test of getNodes method, of class VisualisableGraph.
     */
    @Test
    public void testGetNodes() {

    }

    /**
     * Test of getEdges method, of class VisualisableGraph.
     */
    @Test
    public void testGetEdges() {

    }

    /**
     * Test of getRandomNode method, of class VisualisableGraph.
     */
    @Test
    public void testGetRandomNode() {

    }

    /**
     * Test of getRandomEdge method, of class VisualisableGraph.
     */
    @Test
    public void testGetRandomEdge() {

    }

}
