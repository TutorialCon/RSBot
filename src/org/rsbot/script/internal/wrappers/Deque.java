package org.rsbot.script.internal.wrappers;

import org.rsbot.client.Node;
import org.rsbot.client.NodeDeque;

@SuppressWarnings("unchecked")
public class Deque<N> {
	private final NodeDeque nodeDeque;
	private Node current;

	public Deque(final NodeDeque nodeDeque) {
		this.nodeDeque = nodeDeque;
	}

	public int size() {
		int size = 0;
		Node node = nodeDeque.getTail().getPrevious();
		while (node != nodeDeque.getTail()) {
			node = node.getPrevious();
			size++;
		}
		return size;
	}

	public N getHead() {
		final Node node = nodeDeque.getTail().getNext();
		if (node == nodeDeque.getTail()) {
			current = null;
			return null;
		}
		current = node.getNext();
		return (N) node;
	}

	public N getTail() {
		final Node node = nodeDeque.getTail().getPrevious();
		if (node == nodeDeque.getTail()) {
			current = null;
			return null;
		}
		current = node.getPrevious();
		return (N) node;
	}

	public N getNext() {
		final Node node = current;
		if (node == nodeDeque.getTail()) {
			current = null;
			return null;
		}
		current = node.getNext();
		return (N) node;
	}
}