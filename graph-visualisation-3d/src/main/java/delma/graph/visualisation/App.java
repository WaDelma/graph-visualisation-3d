package delma.graph.visualisation;

import delma.graph.Graph;
import delma.graph.Graph.Edge;
import delma.graph.GraphGenerator;
import delma.graph.VisualisableGraph;
import delma.graph.visualisation.entity.Entity;
import delma.graph.visualisation.entity.Node;
import delma.util.FunctionalUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public class App implements Startable {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        App instance = new App();
        instance.run();
    }

    private Renderer renderer;
    private Pool<Entity> entities;
    private Graph<Object, Object> graph;
    private static final float CAM_SPEED = 0.02f;
    private final Vector3f camMove = new Vector3f();
    private Deque<Graph<Object, Object>> graphStack;
    private Map<Graph.Node, Graph.Node> childToParentMap;
    private Map<Graph.Node, Node> nodeToNodeMap;

    @Override
    public void create() {
        childToParentMap = new HashMap<>();
        nodeToNodeMap = new HashMap<>();

        graphStack = new ArrayDeque<>();

        graph = new VisualisableGraph();

        renderer = new Renderer(this);
        renderer.create();

        entities = new Pool<>();
    }

    @Override
    public void tick() {
        while (!Display.isCloseRequested()) {
            while (Keyboard.next()) {
                boolean state = Keyboard.getEventKeyState();
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_0:
                        if (state) {
                            entities.clear();
                            nodemap.clear();
                            graphStack.clear();
                            childToParentMap.clear();
                            nodeToNodeMap.clear();

                            GraphGenerator.generate(graph, true, 100, 50, n -> n, n -> n);
                            graphStack.push(graph);
                            int subGraphs = graph.getSubgraphs().size();
                            while (graph.size() > subGraphs) {
                                graph = createCoarced(graph.getSubgraphs());
                                graphStack.push(graph);
                            }
                            for (Graph.Node node : graph) {
                                Node simNode = new Node(this, graph, node);
                                nodeToNodeMap.put(node, simNode);
                                simNode.create();
                                addEntity(simNode);
                            }
                        }
                        break;
                    case Keyboard.KEY_W:
                        if (state) {
                            camMove.y += CAM_SPEED;
                        } else {
                            camMove.y -= CAM_SPEED;
                        }
                        break;
                    case Keyboard.KEY_A:
                        if (state) {
                            camMove.x -= CAM_SPEED;
                        } else {
                            camMove.x += CAM_SPEED;
                        }
                        break;
                    case Keyboard.KEY_S:
                        if (state) {
                            camMove.y -= CAM_SPEED;
                        } else {
                            camMove.y += CAM_SPEED;
                        }
                        break;
                    case Keyboard.KEY_D:
                        if (state) {
                            camMove.x += CAM_SPEED;
                        } else {
                            camMove.x -= CAM_SPEED;
                        }
                        break;
                }
            }

            if (Mouse.isButtonDown(1)) {
                renderer.getCamera().rotate(Mouse.getDX() * 0.01f, Mouse.getDY() * 0.01f, 0);
            }
            float distance = 1 + Mouse.getDWheel() / 600f;
            renderer.getCamera().scale(new Vector3f(distance, distance, distance));
            renderer.getCamera().set(camMove.x, camMove.y, camMove.z);
            entities.update();
            entities.forEach(e -> e.tick());
            entities.update();
            if (!graphStack.isEmpty()) {
                entities.stream()
                        .filter(e -> e instanceof Node)
                        .map(e -> (Node) e)
                        .filter(n -> n.isReady())
                        .findAny()
                        .orElseGet(() -> {
                            graph = graphStack.pop();
                            entities.clear();
                            nodemap.clear();
                            for (Graph.Node node : graph) {
                                Node simNode = new Node(this, graph, node);
                                nodeToNodeMap.put(node, simNode);
                                simNode.create();
                                System.out.println("Node: " + node);
                                System.out.println("Parent: " + childToParentMap.get(node));
                                System.out.println("SimNode: " + nodeToNodeMap.get(childToParentMap.get(node)));
                                simNode.getPosition().set(nodeToNodeMap.get(childToParentMap.get(node)).getPosition());
                                addEntity(simNode);
                            }
                            return null;
                        });
            }
            renderer.tick();
        }
    }

    @Override
    public void destroy() {
        renderer.destroy();
    }

    @Override
    public void handle(Exception ex) {

    }

    private final Map<Graph.Node<Object>, Node> nodemap = new HashMap<>();

    public Node getNode(Graph.Node<Object> n) {
        return nodemap.get(n);
    }

    public Collection<Node> getNodes() {
        return nodemap.values();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        FunctionalUtil.acceptIfCan(Node.class, entity, node -> nodemap.put(node.getNode(), node));
    }

    public Graph<Object, Object> getGraph() {
        return graph;
    }

    public Pool<Entity> getEntities() {
        return entities;
    }

    private Graph<Object, Object> createCoarced(Collection<Graph<Object, Object>> graphs) {
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
        Deque<Graph.Node<Object>> notUsed = new ArrayDeque<>(source.getNodes());
        List<Edge<Object, Object>> notUsedEdges = new ArrayList<>(source.getEdges());
        System.out.println("Coarsing subgraph of size: " + source.size());
        while (!notUsed.isEmpty()) {
            System.out.println("E: " + notUsedEdges.size());
            System.out.println(notUsed);
            Graph.Node<Object> node = notUsed.peek();
            Optional<Edge<Object, Object>> optional = findEdge(notUsedEdges, node);
            if (optional.isPresent()) {
                Graph.Node<Object> other = optional.get().getOther(node).get();
                if (!notUsed.contains(other)) {
                    continue;
                }
                notUsed.remove(other);
                notUsed.remove(node);
                notUsedEdges.removeAll(source.getNeighbourEdges(node));
                notUsedEdges.removeAll(source.getNeighbourEdges(other));

                Graph.Node<Object> newNode = new Graph.Node(new Combiner(node, other));
                childToParentMap.put(node, newNode);
                childToParentMap.put(other, newNode);
                result.add(newNode);
            } else {
                System.out.println("Singleton graph");
                notUsed.remove(node);
//                Graph.Node<Object> newNode = new Graph.Node(node);
//                childToParentMap.put(node, newNode);
//                result.add(newNode);
            }
        }
    }

    private Optional<Edge<Object, Object>> findEdge(List<Edge<Object, Object>> edges, Graph.Node<Object> node) {
        return edges
                .stream()
                .filter(e -> e.contains(node))
                .findAny();
    }

    private void addEdges(Graph<Object, Object> source, Graph<Object, Object> result) {
        result.forEach(n -> {
            FunctionalUtil.acceptIfCan(Combiner.class, n.getLabel(), combiner -> {
                addEdge(source, combiner.first, result);
                addEdge(source, combiner.second, result);
            });
        });
    }

    private void addEdge(Graph<Object, Object> source, Graph.Node<Object> node, Graph<Object, Object> result) {
        source.getNeighbourEdges(node).forEach(e -> {
            Graph.Node<Object> parent = childToParentMap.get(node);
            Graph.Node<Object> otherParent = childToParentMap.get(e.getOther(node).get());
            if (otherParent == null || parent == null) {
                return;
            }
            if (otherParent.equals(parent)) {
                return;
            }
            result.add(new Edge(parent, otherParent, "wut", true));
        });
    }

    private static class Combiner {

        private final Graph.Node<Object> first;
        private final Graph.Node<Object> second;

        public Combiner(Graph.Node<Object> first, Graph.Node<Object> second) {
            this.first = first;
            this.second = second;
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
            return "(" + first + "|" + second + ")";
        }
    }

}
