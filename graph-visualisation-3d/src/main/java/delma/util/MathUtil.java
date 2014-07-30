package delma.util;

import java.util.Random;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public enum MathUtil {

    INSTANCE;
    public static final double TAU = 2 * Math.PI;
    public static final double Ke = 8987551787.3681764;

    public static void randomUnitVector(Random rand, Vector3f vector) {
        float angle = rand.nextFloat() * (float) TAU;
        vector.z = rand.nextFloat() * 2 - 1;
        float thingy = (float) Math.sqrt(1 - vector.z * vector.z);
        vector.x = thingy * (float) Math.cos(angle);
        vector.y = thingy * (float) Math.sin(angle);
    }
}
