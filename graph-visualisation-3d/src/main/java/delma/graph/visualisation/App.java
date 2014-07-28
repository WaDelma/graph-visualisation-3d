package delma.graph.visualisation;

import delma.graph.Graph;
import delma.graph.GraphGenerator;
import delma.graph.VisualisableGraph;
import delma.graph.visualisation.entity.Entity;
import delma.graph.visualisation.entity.Node;
import java.util.UUID;
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
    private Graph<Integer, Integer> graph;
    private static final float CAM_SPEED = 0.02f;
    private final Vector3f camMove = new Vector3f();

    @Override
    public void create() {
        graph = new VisualisableGraph();
        entities = new Pool<>();
        renderer = new Renderer(entities);
        renderer.create();
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
                            GraphGenerator.generate(graph, false, 100, 20, () -> UUID.randomUUID().hashCode(), () -> 10);
                            for (Graph.Node node : graph) {
                                entities.add(new Node(node));
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
            camMove.z = Mouse.getDWheel() / 120f;
            if (Mouse.isButtonDown(1)) {
                renderer.getCamera().rotate(Mouse.getDX() * 0.01f, Mouse.getDY() * 0.01f, 0);
            }
            renderer.getCamera().set(camMove.x, camMove.y, camMove.z);
            entities.update();
            entities.forEach(e -> e.tick());
            entities.update();
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
}
