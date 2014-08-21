package delma.graph.visualisation;

import com.fasterxml.jackson.annotation.JsonProperty;
import delma.graph.Graph;
import delma.graph.VisualisableGraph;
import delma.graph.visualisation.entity.Node;
import delma.util.FunctionalUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author delma
 */
public class GraphCoarcer {

    private final Deque<Graph<Object, Object>> graphStack;
    private final Map<Graph.Node, Graph.Node> childToParentMap;
    private final Map<Graph.Node, Node> nodeToNodeMap;
    private final App context;

    public GraphCoarcer(App context) {
        this.context = context;
        childToParentMap = new HashMap<>();
        nodeToNodeMap = new HashMap<>();
        graphStack = new ArrayDeque<>();
    }

    public void coarce(Graph<Object, Object> graph) {
        graphStack.clear();
        childToParentMap.clear();
        nodeToNodeMap.clear();

        graphStack.push(graph);
        int subGraphs = graph.getSubgraphs().size();
        while (graphStack.peekFirst().size() > subGraphs) {
            graphStack.push(createCoarced(graphStack.peekFirst().getSubgraphs()));
        }
        graph = graphStack.pop();
        for (Graph.Node node : graph) {
            Node simNode = new Node(context, graph, node);
            simNode.create();
            simNode.setTemperature(graphStack.size());
            nodeToNodeMap.put(node, simNode);
            context.addEntity(simNode);
        }
    }

    private Graph<Object, Object> createCoarced(Collection<Graph<Object, Object>> graphs) {
//        System.out.println("COARSE");
        VisualisableGraph result = new VisualisableGraph();
        graphs.forEach(g -> result.add(createCoarcedForSub(g)));
        return result;
    }

    private Graph<Object, Object> createCoarcedForSub(Graph<Object, Object> source) {
        Graph<Object, Object> result = new VisualisableGraph();
        addNodes(source, result);
        addEdges(source, result);
        return result;
    }

    private void addNodes(Graph<Object, Object> source, Graph<Object, Object> result) {
        List<Graph.Node<Object>> notUsed = new ArrayList<>(source.getNodes());
        Set<Graph.Edge<Object, Object>> notUsedEdges = new HashSet<>(source.getEdges());
        while (!notUsed.isEmpty()) {
            Graph.Node<Object> node = notUsed.get(notUsed.size() - 1);
            Collection<Graph.Edge<Object, Object>> neighbours = source.getNeighbourEdges(node);
            Optional<Graph.Edge<Object, Object>> optional = findEdge(neighbours, notUsedEdges);
            if (optional.isPresent()) {
                Graph.Node<Object> other = optional.get().getOther(node).get();
                if (!notUsed.contains(other)) {
                    continue;
                }
                notUsed.remove(other);
                notUsed.remove(node);
                notUsedEdges.removeAll(neighbours);
                notUsedEdges.removeAll(source.getNeighbourEdges(other));

                Graph.Node<Object> newNode = new Graph.Node(new Combiner(node, other));
                childToParentMap.put(node, newNode);
                childToParentMap.put(other, newNode);
                result.add(newNode);
            } else {
                notUsed.remove(node);
                Graph.Node<Object> newNode = new Graph.Node(new Wrapper(node));
                childToParentMap.put(node, newNode);
                result.add(newNode);
            }
        }
    }

    private Optional<Graph.Edge<Object, Object>> findEdge(Collection<Graph.Edge<Object, Object>> neighbour, Set<Graph.Edge<Object, Object>> edges) {
        return neighbour
                .stream()
                .filter(edges::contains)
                .findAny();
    }

    private void addEdges(Graph<Object, Object> source, Graph<Object, Object> result) {
        result.forEach(n -> {
            FunctionalUtil.acceptIfCan(Combiner.class, n.getLabel(), combiner -> {
                addEdge(source, combiner.first, result);
                addEdge(source, combiner.second, result);
            });

            FunctionalUtil.acceptIfCan(Wrapper.class, n.getLabel(), wrapper -> {
                addEdge(source, wrapper.node, result);
            });
        });
    }

    private void addEdge(Graph<Object, Object> source, Graph.Node<Object> node, Graph<Object, Object> result) {
        source.getNeighbourEdges(node).forEach(e -> {
            Graph.Node<Object> parent = childToParentMap.get(node);
            Graph.Node<Object> otherParent = childToParentMap.get(e.getOther(node).get());
            if (otherParent.equals(parent)) {
                return;
            }
            result.add(new Graph.Edge(parent, otherParent, "wut"));
        });
    }

    public boolean ready() {
        return graphStack.isEmpty();
    }

    public Graph<Object, Object> uncoarce() {
        Graph<Object, Object> graph = graphStack.pop();
        for (Graph.Node node : graph) {
            Node simNode = new Node(context, graph, node, findParent(node).getPosition());
            simNode.create();
            simNode.setTemperature(graphStack.size());
            nodeToNodeMap.put(node, simNode);
            context.addEntity(simNode);
        }
        return graph;
    }

    private Node findParent(Graph.Node node) {
        return Objects.requireNonNull(nodeToNodeMap.get(childToParentMap.get(node)));
    }

    private static class Combiner {

        private final Graph.Node<Object> first;
        private final Graph.Node<Object> second;

        public Combiner(Graph.Node<Object> first, Graph.Node<Object> second) {
            this.first = first;
            this.second = second;
        }

        @JsonProperty("f")
        public Graph.Node<Object> getFirst() {
            return first;
        }

        @JsonProperty("s")
        public Graph.Node<Object> getSecond() {
            return second;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.first);
            hash = 89 * hash + Objects.hashCode(this.second);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Combiner other = (Combiner) obj;
            if (!Objects.equals(this.first, other.first)) {
                return false;
            }
            return Objects.equals(this.second, other.second);
        }

        @Override
        public String toString() {
            return "{" + first + "|" + second + "}";
        }
    }

    private static class Wrapper {

        private final Graph.Node<Object> node;

        public Wrapper(Graph.Node<Object> node) {
            this.node = node;
        }

        @JsonProperty("n")
        public Graph.Node<Object> getNode() {
            return node;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + Objects.hashCode(this.node);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Wrapper other = (Wrapper) obj;
            return Objects.equals(this.node, other.node);
        }

        @Override
        public String toString() {
            return "[" + node + "]";
        }
    }

}
