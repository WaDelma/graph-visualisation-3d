package delma.graph.visualisation;

import delma.graph.visualisation.entity.Node;
import delma.util.FunctionalUtil;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public class Renderer implements Startable {

    // Setup variables
    private final String WINDOW_TITLE = "Title pending";
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // Shader variables
    private int vertexShaderID = 0;
    private int fragmentShaderID = 0;
    private int programID = 0;

    private static final List<Model> models = new ArrayList<>();
    private Camera camera;
    private int projectionMatrixID;
    private int viewMatrixID;
    private int modelMatrixID;
    private final App context;
    private FloatBuffer lineBuffer;
    private int lineVboID;
    private int lineVaoID;
    private static final int MAX_LINES = 20000;

    public Renderer(App context) {
        this.context = context;
    }

    @Override
    public void create() {
        camera = new Camera();
        setupOpenGL();
        setupShaders();
        setupLineBuffer();
    }

    private void setupOpenGL() {
        // Setup an OpenGL context with API version 3.2
        try {
            PixelFormat pixelFormat = new PixelFormat();
            ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                    .withForwardCompatible(true)
                    .withProfileCore(true);

            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle(WINDOW_TITLE);
            Display.create(pixelFormat, contextAtrributes);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Setup an XNA like background color
        GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthRange(0.0f, 1.0f);

        //GL11.glDepthFunc(GL11.GL_LEQUAL);
        //GL11.glShadeModel(GL11.GL_SMOOTH);
        //GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        resizeGL(WIDTH, HEIGHT);

        this.exitOnGLError("Error in setupOpenGL");
    }

    private void resizeGL(int width, int height) {
        camera.setFOV(90);
        camera.setArea(0f, 100f);
        camera.setAspectRatio(width, height);
        camera.updateView();

        GL11.glViewport(0, 0, width, height);
    }

    public static Model createModel(Vertex[] vertices, byte[] indices) {
        Model result = new Model(0, vertices, indices);
        models.add(result);
        return result;
    }

    private void setupShaders() {
        int errorCheckValue = GL11.glGetError();

        vertexShaderID = Util.loadShader("rsc/shaders/vertex.glsl", GL20.GL_VERTEX_SHADER);
        fragmentShaderID = Util.loadShader("rsc/shaders/fragment.glsl", GL20.GL_FRAGMENT_SHADER);

        // Create a new shader program that links both shaders
        programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);

        // Position information will be attribute 0
        GL20.glBindAttribLocation(programID, 0, "in_Position");
        // Color information will be attribute 1
        GL20.glBindAttribLocation(programID, 1, "in_Color");

        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        projectionMatrixID = GL20.glGetUniformLocation(programID, "projectionMatrix");
        viewMatrixID = GL20.glGetUniformLocation(programID, "viewMatrix");
        modelMatrixID = GL20.glGetUniformLocation(programID, "modelMatrix");

        errorCheckValue = GL11.glGetError();
        if (errorCheckValue != GL11.GL_NO_ERROR) {
            System.out.println("ERROR - Could not create the shaders: " + GLU.gluErrorString(errorCheckValue));
            System.exit(-1);
        }
    }

    private void setupLineBuffer() {
        // Sending data to OpenGL requires the usage of (flipped) byte buffers
        lineBuffer = BufferUtils.createFloatBuffer(MAX_LINES * Vertex.elementCount());
//        this.vertices = Util.createBuffer(vertices);

//        indicesCount = indices.length;
//        this.indices = BufferUtils.createByteBuffer(indicesCount);
//        this.indices.put(indices);
//        this.indices.flip();
        // Create a new Vertex Array Object in memory and select it (bind)
        lineVaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(lineVaoID);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        lineVboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lineVboID);
        //TODO: How to change buffer size depending how many edges there are?
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, lineBuffer, GL15.GL_DYNAMIC_DRAW);

        // Put the positions in attribute list 0
        GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, Vertex.sizeInBytes(), 0);
        // Put the colors in attribute list 1
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, Vertex.sizeInBytes(), Vertex.elementBytes() * 4);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices and select it (bind) - INDICES
//        int indexVboID = GL15.glGenBuffers();
//        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexVboID);
//        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indices, GL15.GL_STATIC_DRAW);
//        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void tick() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearDepth(1.0);

        context.getEntities().forEach(entity -> {
            Model model = entity.getModel();
            GL20.glUseProgram(programID);
            GL20.glUniformMatrix4(projectionMatrixID, false, camera.getProjection());
            GL20.glUniformMatrix4(viewMatrixID, false, camera.getView());
            GL20.glUniformMatrix4(modelMatrixID, false, Util.getBuffer(entity.getModelMatrix()));

            // Bind to the VAO that has all the information about the vertices
            GL30.glBindVertexArray(model.getVaoID());
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);

            // Bind to the index VBO that has all the information about the order of the vertices
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.getIndexVboID());

            // Draw the vertices
            GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndiceCount(), GL11.GL_UNSIGNED_BYTE, 0);

            // Put everything back to default (deselect)
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(0);
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);
        });

        lineBuffer.clear();
        context.getEntities().forEach(entity -> {
            FunctionalUtil.acceptIfCan(Node.class, entity, node -> {
                context.getGraph()
                        .getNeighbourEdges(node.getNode())
                        .forEach(edge -> {
                            edge.getOther(node.getNode()).ifPresent(otherEdge -> {
                                Vector3f pos = new Vector3f(node.getPosition());
                                pos.scale(0.1f);
                                Vector3f opos = new Vector3f(context.getNode(otherEdge).getPosition());
                                opos.scale(0.1f);

                                Vertex[] vertices = new Vertex[]{
                                    new Vertex(pos),
                                    new Vertex(opos)
                                };
                                for (Vertex vertex : vertices) {
                                    lineBuffer.put(vertex.getCoord());
                                    lineBuffer.put(vertex.getColor());
                                }
                            });
                        });
            });
        });
        lineBuffer.flip();

        GL11.glLineWidth(0.9f);
        GL20.glUseProgram(programID);
        GL20.glUniformMatrix4(projectionMatrixID, false, camera.getProjection());
        GL20.glUniformMatrix4(viewMatrixID, false, camera.getView());
        GL20.glUniformMatrix4(modelMatrixID, false, Util.getBuffer(Matrix4f.setIdentity(new Matrix4f())));

        // Bind to the VAO that has all the information about the vertices
        GL30.glBindVertexArray(lineVaoID);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        // Bind to the index VBO that has all the information about the order of the vertices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, lineVboID);

        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, lineBuffer);
        // Draw the vertices
        GL11.glDrawArrays(GL11.GL_LINES, 0, MAX_LINES);

        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);

        this.exitOnGLError("Error in loopCycle");

        Display.sync(60);
        Display.update();
    }

    @Override
    public void destroy() {
        // Delete the shaders
        GL20.glUseProgram(0);
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);

        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
        GL20.glDeleteProgram(programID);

        models.forEach(model -> {
            // Select the VAO
            GL30.glBindVertexArray(model.getVaoID());

            // Disable the VBO index from the VAO attributes list
            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);

            // Delete the vertex VBO
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL15.glDeleteBuffers(model.getVertexVboID());

            // Delete the index VBO
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL15.glDeleteBuffers(model.getIndexVboID());

            // Delete the VAO
            GL30.glBindVertexArray(0);
            GL30.glDeleteVertexArrays(model.getVaoID());
        });

        Display.destroy();
    }

    public void exitOnGLError(String errorMessage) {
        int errorValue = GL11.glGetError();

        if (errorValue != GL11.GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);

            if (Display.isCreated()) {
                Display.destroy();
            }
            System.exit(-1);
        }
    }

    @Override
    public void handle(Exception ex) {
    }

    public Camera getCamera() {
        return camera;
    }
}
