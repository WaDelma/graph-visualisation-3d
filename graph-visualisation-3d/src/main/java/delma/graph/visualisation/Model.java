package delma.graph.visualisation;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 *
 * @author delma
 */
public class Model {

    private final int shaderID;

    private final int vaoID;
    private final int vertexVboID;
    private final int indexVboID;

    private final FloatBuffer vertices;
    private final ByteBuffer indices;
    private final int indicesCount;

    public Model(int shaderID, Vertex[] vertices, byte[] indices) {
        // Sending data to OpenGL requires the usage of (flipped) byte buffers
        this.vertices = Util.createBuffer(vertices);

        indicesCount = indices.length;
        this.indices = BufferUtils.createByteBuffer(indicesCount);
        this.indices.put(indices);
        this.indices.flip();

        // Create a new Vertex Array Object in memory and select it (bind)
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        vertexVboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertices, GL15.GL_STATIC_DRAW);
        // Put the positions in attribute list 0
        GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, Vertex.sizeInBytes(), 0);
        // Put the colors in attribute list 1
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, Vertex.sizeInBytes(), Vertex.elementBytes() * 4);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices and select it (bind) - INDICES
        indexVboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexVboID);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indices, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        this.shaderID = shaderID;
    }

    public int getShaderID() {
        return shaderID;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getIndexVboID() {
        return indexVboID;
    }

    public int getVertexVboID() {
        return vertexVboID;
    }

    public FloatBuffer getVertices() {
        return vertices;
    }

    public ByteBuffer getIndices() {
        return indices;
    }

    public int getIndiceCount() {
        return indicesCount;
    }

}
