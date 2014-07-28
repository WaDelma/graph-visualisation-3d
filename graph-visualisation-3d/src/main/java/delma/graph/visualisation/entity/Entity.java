package delma.graph.visualisation.entity;

import delma.graph.visualisation.Model;
import delma.graph.visualisation.Startable;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author delma
 */
public interface Entity extends Startable {

    Model getModel();

    Vector4f getPosition();
}
