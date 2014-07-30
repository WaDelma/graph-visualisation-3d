package delma.graph;

import delma.graph.Graph.Node;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 *
 * @author delma
 * @param <N> Node label
 * @param <E> Edge label
 */
public interface Graph<N, E> extends Iterable<Node<N>> {

    void add(Node<N> node);

    void add(Edge<N, E> edge);

    void add(Graph<N, E> graph);

    boolean remove(Node<N> node);

    boolean remove(Edge<N, E> edge);

    boolean remove(Graph<N, E> graph);

    Collection<Edge<N, E>> getNeighbourEdges(Node<N> node);

    Collection<Node<N>> getNeighbourNodes(Node<N> node);

    Collection<Node<N>> getNodes();

    @Override
    default Iterator<Node<N>> iterator() {
        return getNodes().iterator();
    }

    Collection<Edge<N, E>> getEdges();

    Graph<N, E> getTranspose();

    boolean contains(Node<N> node);

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    void clear();

    Node<N> getRandomNode(Random rand);

    Optional<Edge<N, E>> getRandomEdge(Node<N> node, Random rand);

    Collection<Graph<N, E>> getSubgraphs();

    static class Node<N> {

        private final N label;

        public Node(N label) {
            this.label = label;
        }

        public N getLabel() {
            return label;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Node<?> other = (Node<?>) obj;
            return Objects.equals(this.label, other.label);
        }

        @Override
        public String toString() {
            return "(" + label.toString() + ")";
        }

    }

    static class Edge<N, E> {

        private final Node<N> from;
        private final Node<N> to;
        private final E label;
        private final boolean directionless;

        public Edge(Node<N> from, Node<N> to, E label) {
            this(from, to, label, true);
        }

        public Edge(Node<N> from, Node<N> to, E label, boolean directionless) {
            this.from = from;
            this.to = to;
            this.label = label;
            this.directionless = directionless;
        }

        public Node<N> getFrom() {
            return from;
        }

        public Node<N> getTo() {
            return to;
        }

        public boolean contains(Node<N> node) {
            return from.equals(node) || to.equals(node);
        }

        public Optional<Node<N>> getOther(Node<N> node) {
            if (from.equals(node)) {
                return Optional.of(to);
            }
            if (to.equals(node)) {
                return Optional.of(from);
            }
            return Optional.empty();
        }

        public E getLabel() {
            return label;
        }

        public boolean isDirectionless() {
            return directionless;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.from);
            hash = 97 * hash + Objects.hashCode(this.to);
            if (directionless) {
                hash += Objects.hashCode(this.label);
            } else {
                hash = 97 * hash + Objects.hashCode(this.label);
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Edge<?, ?> other = (Edge<?, ?>) obj;
            if (!Objects.equals(this.from, other.from)) {
                if (directionless) {
                    return Objects.equals(this.from, other.to)
                            && Objects.equals(this.to, other.from);
                }
                return false;
            }
            if (!Objects.equals(this.to, other.to)) {
                return false;
            }
            return Objects.equals(this.label, other.label);
        }

        @Override
        public String toString() {
            return "E" + label.toString();
        }

        public static <N, E> Edge<N, E> flip(Edge<N, E> edge) {
            if (edge.isDirectionless()) {
                return edge;
            }
            return new Edge(edge.getTo(), edge.getFrom(), edge.getLabel(), false);
        }
    }
}
