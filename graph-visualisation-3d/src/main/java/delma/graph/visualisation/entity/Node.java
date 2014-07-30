package delma.graph.visualisation.entity;

import delma.graph.Graph;
import delma.graph.visualisation.App;
import delma.graph.visualisation.Model;
import delma.graph.visualisation.Renderer;
import delma.graph.visualisation.Vertex;
import delma.util.MathUtil;
import java.util.Random;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public class Node implements Entity {

    private static final Model model;

    static {
        int rings = 10;
        int sectors = 10;
        Vertex[] vertices = new Vertex[rings * sectors];
        byte[] indices = new byte[rings * sectors * 6];

        float rr = 1f / (rings - 1f);
        float ss = 1f / (sectors - 1f);
        int i = 0;
        for (int r = 0; r < rings; r++) {
            for (int s = 0; s < sectors; s++) {
                float y = (float) Math.sin(-(MathUtil.TAU / 4f) + (MathUtil.TAU / 2f) * r * rr);
                float x = (float) (Math.cos(MathUtil.TAU * s * ss) * Math.sin((MathUtil.TAU / 2f) * r * rr));
                float z = (float) (Math.sin(MathUtil.TAU * s * ss) * Math.sin((MathUtil.TAU / 2f) * r * rr));
                vertices[i] = new Vertex().setCoord(x, y, z).setColor(x, y, z);

                int curRow = r * sectors;
                int nextRow = ((r + 1) % rings) * sectors;
                int nextSector = (s + 1) % sectors;

                indices[i * 6 + 0] = (byte) (curRow + s);
                indices[i * 6 + 1] = (byte) (nextRow + s);
                indices[i * 6 + 2] = (byte) (nextRow + nextSector);

                indices[i * 6 + 3] = (byte) (curRow + s);
                indices[i * 6 + 4] = (byte) (nextRow + nextSector);
                indices[i * 6 + 5] = (byte) (curRow + nextSector);
                i++;
            }
        }

        //CIRCLE
//        int slices = 180;
//        byte[] indices = new byte[slices * 3 + 3];
//        Vertex[] vertices = new Vertex[slices + 3];
//
//        vertices[0] = new Vertex().setCoord(0, 0, 0).setColor(1, 1, 1);
//        vertices[1] = new Vertex().setCoord(0, 1, 0).setColor(0, 0, 0);
//        for (int n = 0; n < slices + 1; n++) {
//            double angle = MathUtil.TAU * n / slices;
//            angle += MathUtil.TAU / 360f;
//            int index = n * 3;
//            float x = (float) Math.sin(angle);
//            float y = (float) Math.cos(angle);
//
//            indices[index + 0] = (byte) 0;
//            indices[index + 1] = (byte) (n + 1);
//            indices[index + 2] = (byte) (n + 2);
//            vertices[n + 2] = new Vertex().setCoord(x, y, 0).setColor(0, 0, 0);
//        }
//        indices[indices.length - 1] = 1;
        //SQUARE
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

    private Random rand;
    private Graph<Object, Object> graph;
    private Graph.Node<Object> node;
    private App context;
    private float temperature = 1;
    private boolean halt;
//    private Vector3f displacement;

    private final Vector3f pos;
    private Vector3f scale;
    private Vector3f angle;
    private boolean start;

    private Vector3f velocity;
    private Vector3f acceleration;

    public Node() {
        halt = true;
        pos = new Vector3f();//rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
    }

    public Node(App app, Graph<Object, Object> graph, Graph.Node<Object> node) {
        this.context = app;
        this.graph = graph;
        this.node = node;
        this.pos = new Vector3f(); //rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
    }

    public Node(App app, Graph<Object, Object> graph, Graph.Node<Object> node, Vector3f pos) {
        this.context = app;
        this.graph = graph;
        this.node = node;
        this.pos = new Vector3f(pos);
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Vector3f getPosition() {
        return pos;
    }

    @Override
    public Matrix4f getModelMatrix() {
        Matrix4f modelMatrix = new Matrix4f();
        Matrix4f.scale(scale, modelMatrix, modelMatrix);
        Matrix4f.translate(pos, modelMatrix, modelMatrix);
        Matrix4f.rotate((float) Math.toRadians(angle.z), new Vector3f(0, 0, 1), modelMatrix, modelMatrix);
        Matrix4f.rotate((float) Math.toRadians(angle.y), new Vector3f(0, 1, 0), modelMatrix, modelMatrix);
        Matrix4f.rotate((float) Math.toRadians(angle.x), new Vector3f(1, 0, 0), modelMatrix, modelMatrix);
        return modelMatrix;
    }

    @Override
    public void create() {
        rand = new Random();
//        displacement = new Vector3f();

        scale = new Vector3f(0.1f, 0.1f, 0.1f);
        angle = new Vector3f(0, 0, 0);
        start = true;

        velocity = new Vector3f();
        acceleration = new Vector3f();
    }

    @Override
    public void tick() {
        if (halt) {
            return;
        }
        if (start) {
            start = false;
        } else {
            Vector3f.add(velocity, pos, pos);
            if (velocity.length() < 0.01f) {
                halt = true;
                return;
            }
        }
        acceleration.set(0, 0, 0);
        context.getNodes()
                .forEach(n -> {
                    Vector3f localVector = Vector3f.sub(n.getPosition(), pos, null);
                    float dist = localVector.length();
                    float min = 0.001f;
                    if (dist < min) {
                        MathUtil.randomUnitVector(rand, localVector);
                        localVector.scale(min);
                        dist = min;
                    } else {
                        localVector.scale(1 / dist);
                    }
                    localVector.scale((float) MathUtil.Ke / (dist * dist));
                    Vector3f.add(localVector, acceleration, acceleration);
                });

        graph.getNeighbourNodes(node)
                .forEach(n -> {
                    Node simNode = context.getNode(n);
                    Vector3f localVector = Vector3f.sub(simNode.getPosition(), pos, null);
                    float dist = localVector.length();
                    float min = 0.001f;
                    if (dist < min) {
                        MathUtil.randomUnitVector(rand, localVector);
                        localVector.scale(min);
                        dist = min;
                    } else {
                        localVector.scale(1 / dist);
                    }
                    float springyness = 100000f;
                    float springLength = 100;
                    localVector.scale(-springyness * (dist - springLength));

                    Vector3f velocityDiff = Vector3f.sub(velocity, simNode.getVelocity(), null);
                    velocityDiff.scale(0.5f);
                    Vector3f.sub(localVector, velocityDiff, localVector);

                    Vector3f.add(localVector, acceleration, acceleration);
                });
        Vector3f.add(acceleration, velocity, velocity);

        float dist = velocity.length();
        velocity.scale(dist == 0 ? 0 : 1 / dist);
        velocity.scale(Math.min(temperature, dist));
        temperature *= 0.91;

//        System.out.println("pos: " + pos);
//        System.out.println("velocity: " + velocity);
//        System.out.println("acceleration: " + acceleration);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void handle(Exception ex) {
    }

    public Graph.Node<Object> getNode() {
        return node;
    }

    @Override
    public void run() {
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public boolean isReady() {
        return halt;
    }
}
