package delma.graph.visualisation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author delma
 * @param <T>
 */
public class Pool<T> implements Iterable<T> {

    private final Set<T> data;
    private final Set<T> toBeAdded;
    private final Set<T> toBeRemoved;

    public Pool() {
        data = new HashSet<>();
        toBeAdded = new HashSet<>();
        toBeRemoved = new HashSet<>();
    }

    public void add(T object) {
        toBeAdded.add(object);
    }

    public void remove(T object) {
        toBeRemoved.add(object);
    }

    public void update() {
        toBeAdded.stream()
                .filter(t -> t instanceof Startable)
                .map(t -> (Startable) t)
                .forEach(t -> t.create());
        toBeAdded.forEach(data::add);
        toBeAdded.clear();

        toBeRemoved.stream()
                .filter(t -> t instanceof Startable)
                .map(t -> (Startable) t)
                .forEach(t -> t.destroy());
        toBeRemoved.forEach(data::remove);
        toBeRemoved.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    public void clear() {
        toBeRemoved.addAll(data);
    }
}
