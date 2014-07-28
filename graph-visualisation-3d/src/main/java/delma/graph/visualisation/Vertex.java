package delma.graph.visualisation;

/**
 *
 * @author delma
 */
public class Vertex {

    private final float[] xyzw;
    private final float[] rgba;

    public Vertex() {
        xyzw = new float[4];
        rgba = new float[4];
    }

    public Vertex setCoord(float x, float y, float z) {
        xyzw[0] = x;
        xyzw[1] = y;
        xyzw[2] = z;
        xyzw[3] = 1;
        return this;
    }

    public Vertex setColor(float r, float g, float b) {
        rgba[0] = r;
        rgba[1] = g;
        rgba[2] = b;
        rgba[3] = 1;
        return this;
    }

    public float[] getCoord() {
        return xyzw;
    }

    public float[] getColor() {
        return rgba;
    }

    public static int elementCount() {
        return 8;
    }

    public static int elementBytes() {
        return 4;
    }

    public static int sizeInBytes() {
        return elementCount() * elementBytes();
    }
}
