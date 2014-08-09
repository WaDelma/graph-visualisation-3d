package delma.set;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author delma
 * @param <T>
 */
public class DisjointSet<T> implements Iterable<Set<T>> {

    private static final Deque<Set> unused = new ArrayDeque<>();

    private static Set getSet() {
        if (unused.isEmpty()) {
            return new HashSet<>();
        } else {
            return unused.pop();
        }
    }

    private static void releaseSet(Set set) {
        set.clear();
        unused.push(set);
    }

    private final Map<T, Node<T>> nodes;

    public DisjointSet() {
        nodes = new HashMap<>();
    }

    public void add(T object) {
        nodes.put(object, new Node<>(object));
    }

    public T find(T object) {
        Node<T> node = nodes.get(object);
        if (node == null) {
            return null;
        }
        return find(node).data;
    }

    private Node<T> find(Node<T> node) {
        if (node.parent != node) {
            node.parent.children.remove(node);
            node.parent = find(node.parent);
            node.parent.children.add(node);
        }
        return node.parent;
    }

    public boolean remove(T object) {
        Node<T> node = nodes.get(object);
        if (node == null) {
            return false;
        }
        if (!node.children.isEmpty()) {
            Node<T> newParent = node.children.iterator().next();
            node.children.forEach(n -> {
                if (n != newParent) {
                    newParent.children.add(n);
                }
                n.parent = newParent;
            });
            if (node.parent != node) {
                newParent.parent = node.parent;
            }
        } else if (node != node.parent) {
            node.parent.children.remove(node);
        }
        releaseSet(node.children);
        return nodes.remove(object) != null;
    }

    public void union(T first, T second) {
        if (first.equals(second)) {
            return;
        }
        Node<T> firstR = find(nodes.get(first));
        Node<T> secondR = find(nodes.get(second));
        if (firstR.rank < secondR.rank) {
            secondR.children.add(firstR);
            firstR.parent = secondR;
        } else if (firstR.rank > secondR.rank) {
            firstR.children.add(secondR);
            secondR.parent = firstR;
        } else {
            firstR.children.add(secondR);
            secondR.parent = firstR;
            firstR.rank = firstR.rank + 1;
        }
    }

    @Override
    public Iterator<Set<T>> iterator() {
        Map<T, Set<T>> result = new HashMap<>();
        nodes.forEach((k, v) -> {
            T root = find(k);
            result.computeIfAbsent(root, key -> new HashSet<>())
                    .add(k);
        });
        return result.values().iterator();
    }

    public int size() {
        return nodes.size();
    }

    private class Node<T> {

        private final T data;
        private final Set<Node<T>> children;

        private Node<T> parent;
        private int rank;

        private Node(T data) {
            this.data = data;
            parent = this;
            children = getSet();
        }
    }
}
