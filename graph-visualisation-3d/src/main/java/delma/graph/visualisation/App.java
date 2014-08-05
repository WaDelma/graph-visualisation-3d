package delma.graph.visualisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import delma.graph.Graph;
import delma.graph.GraphGenerator;
import delma.graph.VisualisableGraph;
import delma.graph.visualisation.entity.Entity;
import delma.graph.visualisation.entity.Node;
import delma.tree.Octree;
import delma.util.FunctionalUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    private static final float CAM_SPEED = 2f;
    private final Vector3f camMove = new Vector3f();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Octree<Entity> octree;
    private GraphCoarcer coarcer;
    private float delta;
    private long lastTime;

    @Override
    public void create() {
        graph = new VisualisableGraph();
        renderer = new Renderer(this);
        renderer.create();
        entities = new Pool<>();
        coarcer = new GraphCoarcer(this);
    }

    @Override
    public void tick() {
        while (!Display.isCloseRequested()) {
            delta = calcDelta();
            octree = Octree.create(entities, 0.001);
            handleInput();
            entities.update();
            entities.forEach(e -> e.tick());
            entities.update();
            if (!coarcer.ready()) {
                entities.stream()
                        .filter(e -> e instanceof Node)
                        .map(e -> (Node) e)
                        .filter(n -> !n.isReady())
                        .findAny()
                        .orElseGet(() -> {
                            entities.clear();
                            nodemap.clear();
                            graph = coarcer.uncoarce();
                            return null;
                        });
            }
            renderer.tick();
        }
    }

    private void handleInput() {
        while (Keyboard.next()) {
            boolean state = Keyboard.getEventKeyState();
            switch (Keyboard.getEventKey()) {
                case Keyboard.KEY_0:
                    if (state) {
                        entities.clear();
                        nodemap.clear();
                        GraphGenerator.generate(graph, true, 10000, 5000, n -> n, n -> n);
                        coarcer.coarce(graph);
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
        float wheel = Mouse.getDWheel();
        float distance = 1 + wheel / 600;
        renderer.getCamera().scale(new Vector3f(distance, distance, distance));
        renderer.getCamera().set(camMove);
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

    public Octree<Entity> getOctree() {
        return octree;
    }

    private float calcDelta() {
        long curTime = System.nanoTime();
        float result = (float) ((curTime - lastTime) / 1000000000d);
        lastTime = curTime;
        return result;
    }

    public float getDelta() {
        return delta;
    }

}
