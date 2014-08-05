package delma.tree;

import delma.graph.visualisation.Pool;
import delma.graph.visualisation.entity.Entity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 * @param <N>
 */
public class Octree<N> {

    public static Octree<Entity> create(Pool<Entity> entities, double graduality) {
        Octree<Entity> result = new Octree(graduality);
        entities.forEach(e -> {
            result.addBody(e, e.getPosition(), 1);
        });
        return result;
    }
    private static final Deque<Vector> unused = new ArrayDeque<>();

    private static Vector getVec() {
        if (!unused.isEmpty()) {
            return unused.pop();
        } else {
            return new Vector();
        }
    }

    private static void releaseVec(Vector vec) {
        unused.push(vec);
    }

    private Node<N> root;
    private final double graduality;
    private static final double DEFAULT_GRADUALITY = 1;
    private int size;

    public Octree() {
        this(DEFAULT_GRADUALITY);
    }

    public Octree(double graduality) {
        this.graduality = graduality;
        Vector min = getVec().set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        Vector max = getVec().set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        root = new Octree.Node(min, max);
    }

    public boolean addBody(N n, Vector3f vector, float mass) {
        return addBody(root, n, getVec().set(vector), mass);
    }

    private boolean addBody(Node<N> node, N n, Vector vector, float mass) {
        while (true) {
            if (node.isExternal()) {
                if (node.data != null) {
                    Vector massCenter = node.getMassCenterInter();
                    if (equals(massCenter, vector, graduality)) {
                        node.data.add(n);
                        return true;
                    }
                    node.external = false;
                    handle(add(node, massCenter), massCenter, node.mass, node.data);
                    releaseVec(massCenter);
                    handle(node, vector, mass, (List) null);
                    node = add(node, vector);
                    continue;
                }
                handle(node, vector, mass, n);
                releaseVec(vector);
                return true;
            }
            handle(node, vector, mass, (List) null);
            node = add(node, vector);
        }
    }

    private void handle(Node<N> cur, Vector vector, float mass, N data) {
        List<N> temp = new ArrayList<>();
        temp.add(data);
        handle(cur, vector, mass, temp);
    }

    private void handle(Node<N> cur, Vector vector, float mass, List<N> data) {
        Vector thingy = getVec().set(vector);
        thingy.mul(mass);
        cur.center.add(thingy);
        releaseVec(thingy);

        cur.mass += mass;
        cur.data = data;
    }

    private Node<N> add(Node<N> cur, Vector vector) {
        size++;
        int octantX = vector.getX() < cur.getDivisionX() ? 0 : 1;
        int octantY = vector.getY() < cur.getDivisionY() ? 0 : 1;
        int octantZ = vector.getZ() < cur.getDivisionZ() ? 0 : 1;
        int index = cur.getIndex(octantX, octantY, octantZ);
        if (cur.subNodes[index] == null) {
            Vector min = cur.calcMin(octantX, octantY, octantZ);
            Vector max = cur.calcMax(octantX, octantY, octantZ);
            cur.subNodes[index] = new Node(min, max);
        }
        return cur.subNodes[index];
    }

    private boolean equals(Vector vec1, Vector vec2, double graduality) {
        Vector vector = getVec().set(vec1).sub(vec2);
        double length = vector.length();
        releaseVec(vector);
        return length < graduality;
    }

    public void forEach(Vector3f pos, float tressHold, Consumer<Node<N>> consumer) {
        Vector poss = getVec().set(pos);
        Deque<Node<N>> stack = new ArrayDeque<>((int) (Math.log(size) * 8));
        stack.push(getRoot());
        while (!stack.isEmpty()) {
            Node<N> node = stack.pop();
            boolean doit = node.isExternal();
            if (!doit) {
                Vector mass = node.getMassCenterInter();
                doit = node.getWidth() < tressHold * Vector.dist(mass, poss);
                releaseVec(mass);
            }
            if (doit) {
                consumer.accept(node);
            } else {
                for (Node<N> node2 : node.getSubNodes()) {
                    if (node2 != null) {
                        stack.push(node2);
                    }
                }
            }
        }
        releaseVec(poss);
    }

    public Node<N> getRoot() {
        return root;
    }

    public static class Node<N> {

        private final Node<N>[] subNodes;

        private final Vector min, max;

        private final Vector center;

        private float mass;

        private List<N> data;

        private boolean external;

        private Node(Vector min, Vector max) {
            subNodes = new Node[2 * 2 * 2];
            this.min = min;
            this.max = max;
            external = true;
            center = new Vector();
        }

        public List<N> getData() {
            return data == null ? Collections.emptyList() : data;
        }

        public boolean isExternal() {
            return external;
        }

        public float getMass() {
            return mass;
        }

        public Vector3f getMassCenter() {
            Vector massCenter = getMassCenterInter();
            Vector3f result = massCenter.get();
            releaseVec(massCenter);
            return result;
        }

        public float getWidth() {
            return (float) (max.getX() - min.getX());
        }

        private Vector calcMin(int octantX, int octantY, int octantZ) {
            double xx = octantX == 0 ? min.getX() : getDivisionX();
            double yy = octantY == 0 ? min.getY() : getDivisionY();
            double zz = octantZ == 0 ? min.getZ() : getDivisionZ();
            return getVec().set(xx, yy, zz);
        }

        private Vector calcMax(int octantX, int octantY, int octantZ) {
            double xx = octantX == 0 ? getDivisionX() : max.getX();
            double yy = octantY == 0 ? getDivisionY() : max.getY();
            double zz = octantZ == 0 ? getDivisionZ() : max.getZ();
            return getVec().set(xx, yy, zz);
        }

        private double getDivisionX() {
            return (max.getX() + min.getX()) / 2;
        }

        private double getDivisionY() {
            return (max.getY() + min.getY()) / 2;
        }

        private double getDivisionZ() {
            return (max.getZ() + min.getZ()) / 2;
        }

        public Node<N>[] getSubNodes() {
            return subNodes;
        }

        @Override
        public String toString() {
            if (data == null) {
                return Arrays.deepToString(subNodes);
            } else {
                return data.toString();
            }
        }

        private Vector getMassCenterInter() {
            Vector result = getVec().set(center);
            result.div(mass, 0.001f);
            return result;
        }

        public Node<N> getSubNode(int x, int y, int z) {
            return subNodes[getIndex(x, y, z)];
        }

        private int getIndex(int x, int y, int z) {
            return x + 2 * (y + 2 * z);
        }

    }

    private static class Vector {

        private static double dist(Vector a, Vector b) {
            return Math.sqrt(distSquared(a, b));
        }

        private static double distSquared(Vector a, Vector b) {
            double xx = a.x - b.x;
            double yy = a.y - b.y;
            double zz = a.z - b.z;
            return xx * xx + yy * yy + zz * zz;
        }

        private double x, y, z;

        Vector() {
        }

        Vector(Vector vector) {
            this(vector.x, vector.y, vector.z);
        }

        Vector(Vector3f vector) {
            this(vector.x, vector.y, vector.z);
        }

        Vector(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        Vector3f get() {
            return new Vector3f((float) x, (float) y, (float) z);
        }

        Vector div(Vector vec, double min) {
            x /= vec.x < min ? min : vec.x;
            y /= vec.y < min ? min : vec.z;
            z /= vec.z < min ? min : vec.y;
            return this;
        }

        Vector mul(Vector vec) {
            x *= vec.x;
            y *= vec.y;
            z *= vec.z;
            return this;
        }

        Vector add(Vector vec) {
            x += vec.x;
            y += vec.y;
            z += vec.z;
            return this;
        }

        Vector sub(Vector vec) {
            x -= vec.x;
            y -= vec.y;
            z -= vec.z;
            return this;
        }

        Vector mul(double s) {
            x *= s;
            y *= s;
            z *= s;
            return this;
        }

        Vector div(double s, double min) {
            if (s < min) {
                x = min;
                y = min;
                z = min;
                return this;
            }
            x /= s;
            y /= s;
            z /= s;
            return this;
        }

        double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        private Vector set(Vector3f vec) {
            return set(vec.x, vec.y, vec.z);
        }

        private Vector set(Vector vec) {
            return set(vec.x, vec.y, vec.z);
        }

        private Vector set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }
}
