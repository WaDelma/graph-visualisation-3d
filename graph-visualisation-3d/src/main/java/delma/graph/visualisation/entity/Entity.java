package delma.graph.visualisation.entity;

import delma.graph.visualisation.Model;
import delma.graph.visualisation.Startable;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public interface Entity extends Startable {

    Model getModel();

    Vector3f getPosition();

    Matrix4f getModelMatrix();
}
