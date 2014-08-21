package delma.set;

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

    private final Map<T, Node<T>> nodes;
    private final Map<T, Set<T>> sets;

    public DisjointSet() {
        nodes = new HashMap<>();
        sets = new HashMap<>();
    }

    public boolean add(T object) {
        if (nodes.containsKey(object)) {
            return false;
        }
        nodes.put(object, new Node<>(object));
        return true;
    }

    public T find(T object) {
        Node<T> node = nodes.get(object);
        if (node == null || node.removed) {
            return null;
        }
        return findInter(node).data;
    }

    private Node<T> findInter(Node<T> node) {
        if (node.parent == node) {
            if (node.removed) {
                return node.next;
            }
            return node;
        }
        get(node.parent).remove(node.data);
        node.parent = findInter(node);
        if (!get(node).isEmpty() || !node.removed) {
            get(node.parent).add(node.data);
        }
        return node.parent;
    }

    public void union(T first, T second) {
        Node<T> firstNode = nodes.get(first);
        Node<T> secondNode = nodes.get(second);
        if (firstNode == null || secondNode == null) {
            return;
        }
        Node<T> firstRoot = findInter(firstNode);
        Node<T> secondRoot = findInter(secondNode);
        if (firstRoot == null || secondRoot == null) {
            return;
        }
        if (firstRoot.rank < secondRoot.rank) {
            unionRoot(firstRoot, secondRoot);
            return;
        }
        if (firstRoot.rank == secondRoot.rank) {
            firstRoot.rank++;
        }
        unionRoot(secondRoot, firstRoot);
    }

    private void unionRoot(Node<T> first, Node<T> second) {
        first.parent = second;
        get(second).add(first.data);
        first.next = second.next;
        second.next = first;
        first.prev = second;
    }

    public boolean remove(T object) {
        Node<T> node = nodes.get(object);
        if (node == null) {
            return false;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.removed = true;
        if (get(node).isEmpty()) {
            get(node.parent).remove(node.data);
        }
        return true;
    }

    @Override
    public Iterator<Set<T>> iterator() {
        return sets.values().iterator();
    }

    private Set<T> get(Node<T> node) {
        return sets.computeIfAbsent(node.data, k -> new HashSet<>());
    }

    int size() {
        return nodes.size();
    }

    private static class Node<T> {

        private Node parent;
        private Node prev;
        private Node next;
        private boolean removed;
        private int rank;
        private final T data;

        Node(T object) {
            data = object;
            next = this;
            prev = this;
            parent = this;
            rank = 0;
            removed = false;
        }
    }
}
