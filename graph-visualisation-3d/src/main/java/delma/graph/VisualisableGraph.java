package delma.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author delma
 */
public class VisualisableGraph implements Graph<Integer, Integer> {

    private final Map<Node<Integer>, List<Edge<Integer, Integer>>> map;
    private final Map<Node<Integer>, List<Edge<Integer, Integer>>> transpose;
    private final Transpose transposeGraph = new Transpose();

    public VisualisableGraph() {
        map = new HashMap<>();
        transpose = new HashMap<>();
    }

    @Override
    public void add(Node<Integer> node) {
        map.computeIfAbsent(node, n -> new ArrayList<>());
        transpose.computeIfAbsent(node, n -> new ArrayList<>());
    }

    @Override
    public void add(Edge<Integer, Integer> edge) {
        Node<Integer> from = edge.getFrom();
        Node<Integer> to = edge.getTo();
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
    public void add(Graph<Integer, Integer> graph) {
        graph.getEdges().forEach(this::add);
    }

    @Override
    public boolean remove(Node<Integer> node) {
        map.get(node).forEach(e -> {
            e.getOther(node).ifPresent(n -> transpose.get(n).remove(e));
        });
        transpose.get(node).forEach(e -> {
            e.getOther(node).ifPresent(n -> map.get(n).remove(e));
        });
        if (map.remove(node) != null) {
            transpose.remove(node);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Edge<Integer, Integer> edge) {
        Node<Integer> from = edge.getFrom();
        Node<Integer> to = edge.getTo();
        boolean flag = map.get(from).remove(edge);
        flag |= map.get(to).remove(edge);
        transpose.get(from).remove(edge);
        transpose.get(to).remove(edge);
        return flag;
    }

    @Override
    public boolean remove(Graph<Integer, Integer> graph) {
        int size = size();
        graph.getEdges().forEach(this::remove);
        return size() != size;
    }

    @Override
    public Collection<Edge<Integer, Integer>> getNeighbourEdges(Node<Integer> node) {
        return map.getOrDefault(node, Collections.emptyList());
    }

    @Override
    public Collection<Node<Integer>> getNeighbourNodes(Node<Integer> node) {
        return mapToNodes(this::getNeighbourEdges, node);
    }

    private Collection<Node<Integer>> mapToNodes(Function<Node<Integer>, Collection<Edge<Integer, Integer>>> c, Node<Integer> node) {
        return c.apply(node)
                .stream()
                .map(e -> e.getOther(node))
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(Collectors.toList());
    }

    @Override
    public Graph<Integer, Integer> getTranspose() {
        return transposeGraph;
    }

    @Override
    public boolean contains(Node<Integer> node) {
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
    public Collection<Node<Integer>> getNodes() {
        return map.keySet();
    }

    @Override
    public Collection<Edge<Integer, Integer>> getEdges() {
        List<Edge<Integer, Integer>> result = new ArrayList<>();
        map.values().forEach(result::addAll);
        return result;
    }

    @Override
    public Node<Integer> getRandomNode(Random rand) {
        return map.keySet().toArray(new Node[size()])[rand.nextInt(size())];
    }

    private class Transpose implements Graph<Integer, Integer> {

        @Override
        public void add(Node<Integer> node) {
            VisualisableGraph.this.add(node);
        }

        @Override
        public void add(Edge<Integer, Integer> edge) {
            VisualisableGraph.this.add(Edge.flip(edge));
        }

        @Override
        public void add(Graph<Integer, Integer> graph) {
            graph.getEdges().forEach(this::add);
        }

        @Override
        public boolean remove(Node<Integer> node) {
            return VisualisableGraph.this.remove(node);
        }

        @Override
        public boolean remove(Edge<Integer, Integer> edge) {
            return VisualisableGraph.this.remove(Edge.flip(edge));
        }

        @Override
        public boolean remove(Graph<Integer, Integer> graph) {
            int size = size();
            graph.getEdges().forEach(this::remove);
            return size() != size;
        }

        @Override
        public Collection<Edge<Integer, Integer>> getNeighbourEdges(Node<Integer> node) {
            return VisualisableGraph.this.transpose.getOrDefault(node, Collections.emptyList());
        }

        @Override
        public Collection<Node<Integer>> getNeighbourNodes(Node<Integer> node) {
            return VisualisableGraph.this.mapToNodes(this::getNeighbourEdges, node);
        }

        @Override
        public Collection<Node<Integer>> getNodes() {
            return VisualisableGraph.this.getNodes();
        }

        @Override
        public Collection<Edge<Integer, Integer>> getEdges() {
            List<Edge<Integer, Integer>> result = new ArrayList<>();
            VisualisableGraph.this.transpose.values().forEach(result::addAll);
            return result;
        }

        @Override
        public Graph<Integer, Integer> getTranspose() {
            return VisualisableGraph.this;
        }

        @Override
        public boolean contains(Node<Integer> node) {
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
        public Node<Integer> getRandomNode(Random rand) {
            return null;
        }
    }

}
