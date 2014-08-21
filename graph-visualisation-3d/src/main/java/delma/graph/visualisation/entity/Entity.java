package delma.graph.visualisation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import delma.graph.visualisation.Model;
import delma.graph.visualisation.Startable;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author delma
 */
public interface Entity extends Startable {

    @JsonIgnore()
    Model getModel();

    Vector3f getPosition();

    void setPosition(Vector3f vector);

    @JsonIgnore()
    Matrix4f getModelMatrix();
}
