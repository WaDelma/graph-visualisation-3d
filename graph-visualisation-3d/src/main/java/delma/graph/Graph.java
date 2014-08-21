package delma.graph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import delma.graph.Graph.Node;
import delma.graph.visualisation.App;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author delma
 * @param <N> Node label
 * @param <E> Edge label
 */
@JsonTypeInfo(use = Id.CLASS, defaultImpl = VisualisableGraph.class)
public interface Graph<N, E> extends Iterable<Node<N>> {

    void add(Node<N> node);

    void add(Edge<N, E> edge);

    void add(Edge<N, E> edge, boolean directionless);

    void add(Graph<N, E> graph);

    boolean remove(Node<N> node);

    boolean remove(Edge<N, E> edge);

    boolean remove(Graph<N, E> graph);

    Collection<Edge<N, E>> getNeighbourEdges(Node<N> node);

    Collection<Node<N>> getNeighbourNodes(Node<N> node);

    void setNodes(Collection<Node<N>> nodes);

    Collection<Node<N>> getNodes();

    @Override
    default Iterator<Node<N>> iterator() {
        return getNodes().iterator();
    }

    void setEdges(Collection<Edge<N, E>> edges);

    Collection<Edge<N, E>> getEdges();

    @JsonIgnore
    Graph<N, E> getTranspose();

    boolean contains(Node<N> node);

    int size();

    @JsonIgnore
    default boolean isEmpty() {
        return size() == 0;
    }

    void clear();

    Node<N> getRandomNode(Random rand);

    Optional<Edge<N, E>> getRandomEdge(Node<N> node, Random rand);

    @JsonIgnore
    Collection<Graph<N, E>> getSubgraphs();

    static class Node<N> {

        private final N label;
        private int hash;

        public Node(String string) {
            N temp = null;
            try {
                temp = App.MAPPER.readValue(string, (Class<N>) Object.class);
            } catch (IOException ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
            }
            label = temp;
        }

        public Node(@JsonProperty("label") N label) {
            this.label = label;
            hash = 7;
            hash = 83 * hash + Objects.hashCode(this.label);
        }

        @JsonProperty("label")
        public N getLabel() {
            return label;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass() || obj.hashCode() != hashCode()) {
                return false;
            }
            final Node<?> other = (Node<?>) obj;
            return Objects.equals(this.label, other.label);
        }

        @Override
        public String toString() {
            try {
                return App.MAPPER.writeValueAsString(label);
            } catch (JsonProcessingException ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class Edge<N, E> {

        private final Node<N> from;
        private final Node<N> to;
        private final E label;

        @JsonCreator
        public Edge(@JsonProperty("from") Node<N> from,
                @JsonProperty("to") Node<N> to,
                @JsonProperty("label") E label) {
            this.from = from;
            this.to = to;
            this.label = label;
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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.from);
            hash = 97 * hash + Objects.hashCode(this.to);
            hash = 97 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Edge<?, ?> other = (Edge<?, ?>) obj;
            if (!Objects.equals(this.from, other.from)) {
                return false;
            }
            if (!Objects.equals(this.to, other.to)) {
                return false;
            }
            return Objects.equals(this.label, other.label);
        }

        public static <N, E> Edge<N, E> flip(Edge<N, E> edge) {
            return new Edge(edge.getTo(), edge.getFrom(), edge.getLabel());
        }
    }
}
