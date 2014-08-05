package delma.graph.visualisation;

import delma.util.MathUtil;
import java.nio.FloatBuffer;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public class Camera {

    private final Vector3f viewVector, rotationVector;
    private final Matrix4f viewMatrix, projectionMatrix;
    private float y_scale;
    private float near, far;
    private float aspectRatio;
    private float frustum_length;
    private float width, height;
    private float fov;

    /**
     * Creates Camera
     */
    public Camera() {
        viewVector = new Vector3f(0, 0, 0);
        rotationVector = new Vector3f(0, 0, 0);
        viewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
    }

    /**
     *
     * @param x Relative x
     * @param y Relative y
     * @param z Relative z
     */
    public void move(float x, float y, float z) {
        viewVector.translate(x, y, z);
        Matrix4f.translate(viewVector, viewMatrix, viewMatrix);
    }

    /**
     *
     * @param x Absolute x
     * @param y Absolute y
     * @param z Absolute z
     */
    public void set(float x, float y, float z) {
        viewVector.set(x, y, z);
        Matrix4f.translate(viewVector, viewMatrix, viewMatrix);
    }

    public void set(Vector3f vector) {
        viewVector.set(vector.x, vector.y, vector.z);
        Matrix4f.translate(viewVector, viewMatrix, viewMatrix);
    }

    public float getX() {
        return viewVector.x;
    }

    public float getY() {
        return viewVector.y;
    }

    public float getZ() {
        return viewVector.z;
    }

    /**
     *
     * @param angle Angle to rotate
     * @param x x for vector to rotate
     * @param y y for vector to rotate
     * @param z z for vector to rotate
     */
    public void rotate(float angle, float x, float y, float z) {
        rotationVector.set(x, y, z);
        rotationVector.translate(viewVector.getX(), viewVector.getY(), viewVector.getZ());
        Matrix4f.rotate(angle, rotationVector, viewMatrix, viewMatrix);
    }

    /**
     *
     * @param x angle to rotate around X axis
     * @param y angle to rotate around Y axis
     * @param z angle to rotate around Z axis
     */
    public void rotate(float x, float y, float z) {
        Matrix4f.rotate(x, new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
        Matrix4f.rotate(y, new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
        Matrix4f.rotate(z, new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
    }

    public void scale(Vector3f scale) {
        Matrix4f.scale(scale, viewMatrix, viewMatrix);
    }

    /**
     *
     * @param width Width of view
     * @param height Height of view
     */
    public void setupView(float width, float height) {
        setFOV(90);
        setArea(0.1f, 100f);
        setAspectRatio(width, height);
        updateView();
    }

    /**
     *
     * @param fov Field of view
     */
    public void setFOV(float fov) {
        this.fov = fov;
        double fovRadian = (fov / 2.0) * (MathUtil.TAU / 360.0);
        y_scale = (float) (Math.cos(fovRadian) / Math.sin(fovRadian));
    }

    /**
     * @return the field of view
     */
    public float getFOV() {
        return fov;
    }

    /**
     *
     * @param near Closest distance camera can see
     * @param far Farthest distance camera can see
     */
    public void setArea(float near, float far) {
        this.near = near;
        this.far = far;
        frustum_length = far - near;
    }

    /**
     * @return the closest distance camera can see
     */
    public float getNear() {
        return near;
    }

    /**
     * @return the farthest distance camera can see
     */
    public float getFar() {
        return far;
    }

    /**
     *
     * @param width Width of view
     * @param height Height of view
     */
    public void setAspectRatio(float width, float height) {
        this.width = width;
        this.height = height;
        aspectRatio = width / height;
    }

    /**
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    /**
     *
     */
    public void updateView() {
        projectionMatrix.setIdentity();
        projectionMatrix.m00 = y_scale / aspectRatio;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((far + near) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * near * far) / frustum_length);
    }

    /**
     * @return the view matrix
     */
    public FloatBuffer getView() {
        return Util.getBuffer(viewMatrix);
    }

    /**
     * @return the projection matrix
     */
    public FloatBuffer getProjection() {
        return Util.getBuffer(projectionMatrix);
    }
}
