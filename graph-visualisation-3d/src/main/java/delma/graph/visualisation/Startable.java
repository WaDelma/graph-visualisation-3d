package delma.graph.visualisation;

/**
 *
 * @author delma
 */
public interface Startable extends Runnable {

    void create();

    void tick();

    void destroy();

    void handle(Exception ex);

    @Override
    default void run() {
        create();
        tick();
        destroy();
    }
}
