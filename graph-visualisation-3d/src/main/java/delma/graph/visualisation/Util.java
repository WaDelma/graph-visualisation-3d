package delma.graph.visualisation;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author delma
 */
public enum Util {

    INSTANCE;

    public static FloatBuffer createBuffer(Vertex... vertices) {
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length * Vertex.elementCount());
        for (Vertex vertice : vertices) {
            verticesBuffer.put(vertice.getCoord());
            verticesBuffer.put(vertice.getColor());
        }
        verticesBuffer.flip();
        return verticesBuffer;
    }

    public static int loadShader(String filename, int type) {
        try {
            Path path = new File(filename).toPath();
            StringBuilder shaderSource = Files.lines(path, StandardCharsets.UTF_8)
                    .map(l -> l + "\n")
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

            int shaderID = GL20.glCreateShader(type);
            GL20.glShaderSource(shaderID, shaderSource);
            GL20.glCompileShader(shaderID);

            if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.err.println("Could not compile shader.");
                System.exit(-1);
            }

            return shaderID;
        } catch (IOException ex) {
            System.err.println("Could not compile shader.");
            System.exit(-1);
            return -1;
        }
    }
}
