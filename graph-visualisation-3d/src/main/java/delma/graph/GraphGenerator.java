package delma.graph;

import delma.graph.Graph.Edge;
import delma.graph.Graph.Node;
import java.util.Random;
import java.util.function.Function;

/**
 * This generates randomised graphs for testing and other purposes.
 *
 * @author delma
 */
public enum GraphGenerator {

    INSTANCE;

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
    public static <N, E> void generate(Graph<N, E> graph, boolean directionless, int nodes, double edges, Function<Integer, N> nodeLabels, Function<Integer, E> edgeLabels) {
        graph.clear();
        Random rand = new Random();

        int e = 0;
        for (int i = 0; i < nodes; i++) {
            Node curNode = new Node(nodeLabels.apply(i));
            graph.add(curNode);
            Node<N> node = graph.getRandomNode(rand);
            if (!node.equals(curNode)) {
                graph.add(new Edge(curNode, node, edgeLabels.apply(e)), directionless);
                e++;
            }
        }
    }
}
