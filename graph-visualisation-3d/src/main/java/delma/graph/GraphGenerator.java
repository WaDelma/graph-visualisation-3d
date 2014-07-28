package delma.graph;

import delma.graph.Graph.Edge;
import delma.graph.Graph.Node;
import java.util.Random;
import java.util.function.Supplier;

/**
 * This generates randomised graphs for testing and other purposes.
 *
 * @author delma
 */
public class GraphGenerator {

    private int nodeCount = 100;
    private int edgeCount = 50;
    private int maxWeigth = 100;

    /**
     * Generates new random graph
     *
     * @param <N> node label
     * @param <E> edge label
     * @param graph Graph to which new graph is generated
     * @param directionless
     * @param nodes Approximately how many nodes there will be
     * @param edges How many vertices there will be
     * @param nodeLabels
     * @param edgeLabels
     */
    public static <N, E> void generate(Graph<N, E> graph, boolean directionless, int nodes, double edges, Supplier<N> nodeLabels, Supplier<E> edgeLabels) {
        graph.clear();
        Random rand = new Random();

        for (int i = 0; i < nodes; i++) {
            Node curNode = new Node(nodeLabels.get());
            graph.add(curNode);
            Node<N> node = graph.getRandomNode(rand);
            if (!node.equals(curNode)) {
                graph.add(new Edge(curNode, node, edgeLabels.get(), directionless));
            }
        }
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getMaxWeigth() {
        return maxWeigth;
    }
}
