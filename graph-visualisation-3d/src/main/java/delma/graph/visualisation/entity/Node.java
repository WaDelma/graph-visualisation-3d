package delma.graph.visualisation.entity;

import delma.graph.Graph;
import delma.graph.visualisation.Model;
import delma.graph.visualisation.Renderer;
import delma.graph.visualisation.Vertex;
import delma.util.MathUtil;
import java.util.Random;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author delma
 */
public class Node implements Entity {

    private static final Model model;

    static {
        int slices = 180;
        byte[] indices = new byte[slices * 3 + 3];
        Vertex[] vertices = new Vertex[slices + 3];

        vertices[0] = new Vertex().setCoord(0, 0, 0).setColor(1, 1, 1);
        vertices[1] = new Vertex().setCoord(0, 1, 0).setColor(0, 0, 0);
        for (int n = 0; n < slices + 1; n++) {
            double angle = MathUtil.TAU * n / slices;
            angle += MathUtil.TAU / 360f;
            int index = n * 3;
            float x = (float) Math.sin(angle);
            float y = (float) Math.cos(angle);

            indices[index + 0] = (byte) 0;
            indices[index + 1] = (byte) (n + 1);
            indices[index + 2] = (byte) (n + 2);
            vertices[n + 2] = new Vertex().setCoord(x, y, 0).setColor(0, 0, 0);
        }
        indices[indices.length - 1] = 1;
//        Vertex[] vertices = {
//            new Vertex().setCoord(-0.5f, 0.5f, 0f).setColor(1, 0, 0),
//            new Vertex().setCoord(-0.5f, -0.5f, 0f).setColor(0, 1, 0),
//            new Vertex().setCoord(0.5f, -0.5f, 0f).setColor(0, 0, 1),
//            new Vertex().setCoord(0.5f, 0.5f, 0f).setColor(1, 1, 1)
//        };

//        byte[] indices = {
//            0, 1, 2,
//            2, 3, 0
//        };
        model = Renderer.createModel(vertices, indices);
    }
    private Vector4f pos;
    private Random rand;
    private int tick;
    private final int label;

    public Node() {
        label = -1;
    }

    public Node(Graph.Node<Integer> node) {
        label = node.getLabel();
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Vector4f getPosition() {
        return pos;
    }

    @Override
    public void create() {
        pos = new Vector4f(0, 0, 0, 1);
        rand = new Random();
        tick = 0;
    }

    @Override
    public void tick() {
        pos.x += (rand.nextDouble() - 0.5f) * 0.01f;
        pos.y += 0.001f;
        pos.z += (rand.nextDouble() - 0.5f) * 0.01f;
        tick++;

    }

    @Override
    public void destroy() {
    }

    @Override
    public void handle(Exception ex) {
    }
}
