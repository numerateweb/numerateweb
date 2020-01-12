package org.numerateweb.math.reasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.base.Objects;

/**
 * A simple graph to capture computation dependencies e.g. within a set of
 * mathematical formulas.
 * The formula <code>A = B + C</code> would be captured by this graph as <code>{(A -&gt; B), (A -&gt; C)}</code>.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Dependency_graph">https://en.wikipedia.org/wiki/Dependency_graph</a>
 *
 * @param <T>
 *            The node type
 */
public class DependencyGraph<T> {
	static class Node<T> {
		final T value;
		final Set<Node<T>> incoming = new HashSet<>();
		final Set<Node<T>> outgoing = new HashSet<>();

		Node(T value) {
			this.value = value;
		}

		boolean addIncoming(Node<T> in) {
			if (incoming.add(in)) {
				in.outgoing.add(this);
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Node))
				return false;
			Node<?> other = (Node<?>) obj;
			return Objects.equal(this.value, other.value);
		}
	}

	protected final Map<T, Node<T>> nodes = new HashMap<>();

	protected Node<T> node(T value) {
		return nodes.get(value);
	}

	protected Node<T> ensureNode(T value) {
		return nodes.computeIfAbsent(value, v -> new Node<>(v));
	}

	public boolean addDependency(T from, T to) {
		return ensureNode(to).addIncoming(ensureNode(from));
	}
	
	public boolean contains(T value) {
		return nodes.containsKey(value);
	}
	
	/**
	 * Removes node and its dependencies completely from the graph.
	 * 
	 * @param key The corresponding identifier
	 * @return <code>true</code> if the node exists in this graph, else <code>false</code>
	 */
	public boolean remove(T key) {
		Node<T> node = node(key);
		if (node != null) {
			remove(node);
			return true;
		}
		return false;
	}
	
	protected void remove(Node<T> node) {
		// remove edges from other nodes to node
		node.incoming.forEach(in -> {
			in.outgoing.remove(node);
			if (in.incoming.isEmpty() && in.outgoing.isEmpty()) {
				// also remove in node if it has no other connections left
				nodes.remove(in.value);
			}
		});
		node.incoming.clear();
		// remove edges from node to other nodes
		node.outgoing.forEach(out -> {
			out.incoming.remove(node);
			if (out.incoming.isEmpty() && out.outgoing.isEmpty()) {
				// also remove out node if it has no other connections left
				nodes.remove(out.value);
			}
		});
		node.outgoing.clear();
		// delete node object from map
		nodes.remove(node.value);
	}

	public void invalidate(T value, BiFunction<T, Boolean, Void> callback) {
		Node<T> node = node(value);
		if (node != null) {
			Set<Node<T>> seen = new HashSet<>();
			invalidate(node, callback, seen);
			// remove invalidated nodes from dependency graph
			for (Node<T> s : seen) {
				remove(s);
			}
		}
	}

	protected void invalidate(final Node<T> node, BiFunction<T, Boolean, Void> callback, Set<Node<T>> seen) {
		if (seen.add(node)) {
			// notify listener that node is invalidated
			callback.apply(node.value, node.incoming.isEmpty());

			// refresh all dependent nodes
			node.incoming.forEach(pred -> {
				invalidate(pred, callback, seen);
			});
		}
	}
}
