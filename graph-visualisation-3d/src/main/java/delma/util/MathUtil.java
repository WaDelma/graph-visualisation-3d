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
        double angle = rand.nextDouble() * TAU;
        double z = rand.nextDouble() * 2 - 1;
        vector.z = (float) z;
        double thingy = (float) Math.sqrt(1 - (z * z));
        vector.x = (float) (thingy * Math.cos(angle));
        vector.y = (float) (thingy * Math.sin(angle));
    }
}
