package delma.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author delma
 */
public class VisualisableGraph implements Graph<Object, Object> {

    private final Map<Node<Object>, List<Edge<Object, Object>>> map;
    private final Map<Node<Object>, List<Edge<Object, Object>>> transpose;
    private final Transpose transposeGraph = new Transpose();

    public VisualisableGraph() {
        map = new HashMap<>();
        transpose = new HashMap<>();
    }

    @Override
    public void add(Node<Object> node) {
        map.computeIfAbsent(node, n -> new ArrayList<>());
        transpose.computeIfAbsent(node, n -> new ArrayList<>());
    }

    @Override
    public void add(Edge<Object, Object> edge) {
        Node<Object> from = edge.getFrom();
        Node<Object> to = edge.getTo();
        add(from);
        add(to);
        map.get(from).add(edge);
        transpose.get(to).add(edge);
        if (edge.isDirectionless()) {
            map.get(to).add(edge);
            transpose.get(from).add(edge);
        }
    }

    @Override
    public void add(Graph<Object, Object> graph) {
        graph.getNodes().forEach(this::add);
        graph.getEdges().forEach(this::add);
    }

    @Override
    public boolean remove(Node<Object> node) {
        if (!map.containsKey(node)) {
            return false;
        }
        map.get(node).forEach(e -> {
            e.getOther(node).ifPresent(n -> transpose.get(n).remove(e));
        });
        transpose.get(node).forEach(e -> {
            e.getOther(node).ifPresent(n -> map.get(n).remove(e));
        });
        map.remove(node);
        transpose.remove(node);
        return true;
    }

    @Override
    public boolean remove(Edge<Object, Object> edge) {
        Node<Object> from = edge.getFrom();
        Node<Object> to = edge.getTo();
        boolean flag = map.get(from).remove(edge);
        if (edge.isDirectionless()) {
            map.get(to).remove(edge);
            transpose.get(from).remove(edge);
        }
        transpose.get(to).remove(edge);
        return flag;
    }

    @Override
    public boolean remove(Graph<Object, Object> graph) {
        int size = size();
        graph.getEdges().forEach(this::remove);
        return size() != size;
    }

    @Override
    public Collection<Edge<Object, Object>> getNeighbourEdges(Node<Object> node) {
        return map.getOrDefault(node, Collections.emptyList());
    }

    @Override
    public Collection<Node<Object>> getNeighbourNodes(Node<Object> node) {
        return mapToNodes(this::getNeighbourEdges, node);
    }

    private Collection<Node<Object>> mapToNodes(Function<Node<Object>, Collection<Edge<Object, Object>>> c, Node<Object> node) {
        return c.apply(node)
                .stream()
                .map(e -> e.getOther(node))
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Graph<Object, Object>> getSubgraphs() {
        List<Node<Object>> nodesRemaining = new ArrayList<>(map.keySet());
        Deque<Node<Object>> stack = new ArrayDeque();
        List<Graph<Object, Object>> result = new ArrayList<>();
        while (!nodesRemaining.isEmpty()) {
            stack.push(nodesRemaining.get(0));
            Graph<Object, Object> curGraph = new VisualisableGraph();
            result.add(curGraph);
            while (!stack.isEmpty()) {
                Node<Object> cur = stack.pop();
                if (!nodesRemaining.contains(cur)) {
                    continue;
                }
                nodesRemaining.remove(cur);
                curGraph.add(cur);
                getNeighbourEdges(cur).forEach(curGraph::add);
                getNeighbourEdges(cur)
                        .stream()
                        .map(e -> e.getOther(cur).get())
                        .forEach(stack::push);
            }
        }
        return result;
    }

    @Override
    public Graph<Object, Object> getTranspose() {
        return transposeGraph;
    }

    @Override
    public boolean contains(Node<Object> node) {
        return map.containsKey(node);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
        transpose.clear();
    }

    @Override
    public Collection<Node<Object>> getNodes() {
        return map.keySet();
    }

    @Override
    public Collection<Edge<Object, Object>> getEdges() {
        Set<Edge<Object, Object>> result = new HashSet<>();
        map.values().forEach(result::addAll);
        return result;
    }

    @Override
    public Node<Object> getRandomNode(Random rand) {
        return map.keySet().toArray(new Node[size()])[rand.nextInt(size())];
    }

    @Override
    public Optional<Edge<Object, Object>> getRandomEdge(Node<Object> node, Random rand) {
        List<Edge<Object, Object>> edges = map.get(node);
        if (edges.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(edges.get(rand.nextInt(edges.size())));
    }

    @Override
    public void setNodes(Collection<Node<Object>> nodes) {
        nodes.forEach(this::add);
    }

    @Override
    public void setEdges(Collection<Edge<Object, Object>> edges) {
        edges.forEach(this::add);
    }

    private class Transpose implements Graph<Object, Object> {

        @Override
        public void add(Node<Object> node) {
            VisualisableGraph.this.add(node);
        }

        @Override
        public void add(Edge<Object, Object> edge) {
            VisualisableGraph.this.add(Edge.flip(edge));
        }

        @Override
        public void add(Graph<Object, Object> graph) {
            graph.getEdges().forEach(this::add);
        }

        @Override
        public boolean remove(Node<Object> node) {
            return VisualisableGraph.this.remove(node);
        }

        @Override
        public boolean remove(Edge<Object, Object> edge) {
            return VisualisableGraph.this.remove(Edge.flip(edge));
        }

        @Override
        public boolean remove(Graph<Object, Object> graph) {
            int size = size();
            graph.getEdges().forEach(this::remove);
            return size() != size;
        }

        @Override
        public Collection<Edge<Object, Object>> getNeighbourEdges(Node<Object> node) {
            return transpose.getOrDefault(node, Collections.emptyList());
        }

        @Override
        public Collection<Node<Object>> getNeighbourNodes(Node<Object> node) {
            return VisualisableGraph.this.mapToNodes(this::getNeighbourEdges, node);
        }

        @Override
        public Collection<Node<Object>> getNodes() {
            return VisualisableGraph.this.getNodes();
        }

        @Override
        public Collection<Edge<Object, Object>> getEdges() {
            List<Edge<Object, Object>> result = new ArrayList<>();
            transpose.values().forEach(result::addAll);
            return result;
        }

        @Override
        public Graph<Object, Object> getTranspose() {
            return VisualisableGraph.this;
        }

        @Override
        public boolean contains(Node<Object> node) {
            return VisualisableGraph.this.contains(node);
        }

        @Override
        public int size() {
            return VisualisableGraph.this.size();
        }

        @Override
        public void clear() {
            VisualisableGraph.this.clear();
        }

        @Override
        public Node<Object> getRandomNode(Random rand) {
            return VisualisableGraph.this.getRandomNode(rand);
        }

        @Override
        public Optional<Edge<Object, Object>> getRandomEdge(Node<Object> node, Random rand) {
            List<Edge<Object, Object>> edges = transpose.get(node);
            if (edges.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(edges.get(rand.nextInt(edges.size())));
        }

        @Override
        public Collection<Graph<Object, Object>> getSubgraphs() {
            return VisualisableGraph.this.getSubgraphs();
        }

        @Override
        public void setNodes(Collection<Node<Object>> nodes) {
            nodes.forEach(this::add);
        }

        @Override
        public void setEdges(Collection<Edge<Object, Object>> edges) {
            edges.forEach(this::add);
        }
    }

}
