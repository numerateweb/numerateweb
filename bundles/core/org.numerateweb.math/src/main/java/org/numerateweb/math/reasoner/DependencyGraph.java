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
 *
 * @param <T>
 *            The node type
 */
public class DependencyGraph<T> {
	static class Node<T> {
		final T value;
		final Set<Node<T>> predecessors = new HashSet<>();
		final Set<Node<T>> successors = new HashSet<>();

		Node(T value) {
			this.value = value;
		}

		boolean addSuccessor(Node<T> succ) {
			if (successors.add(succ)) {
				succ.predecessors.add(this);
				return true;
			}
			return false;
		}

		void clearSuccessors() {
			successors.forEach(succ -> {
				succ.predecessors.remove(this);
			});
			successors.clear();
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

	final Map<T, Node<T>> nodes = new HashMap<>();

	protected Node<T> node(T value) {
		return nodes.get(value);
	}

	protected Node<T> ensureNode(T value) {
		return nodes.computeIfAbsent(value, v -> new Node<>(v));
	}

	public boolean addDependency(T from, T to) {
		return ensureNode(from).addSuccessor(ensureNode(to));
	}

	public void invalidate(T value, BiFunction<T, Boolean, Void> callback) {
		Node<T> node = node(value);
		if (node != null) {
			Set<Node<T>> seen = new HashSet<>();
			invalidate(node, callback, seen);
			// refresh all outgoing dependencies of processed nodes
			for (Node<T> s : seen) {
				s.clearSuccessors();
			}
		} else {
			// node has never existed or been cleaned up in an earlier removal
			callback.apply(value, true);
		}
	}

	protected void invalidate(final Node<T> node, BiFunction<T, Boolean, Void> callback, Set<Node<T>> seen) {
		if (seen.add(node)) {
			// notify listener that node is invalidated
			callback.apply(node.value, node.predecessors.isEmpty());

			// refresh all predecessor nodes
			node.predecessors.forEach(pred -> {
				invalidate(pred, callback, seen);
			});
		}
	}
}
