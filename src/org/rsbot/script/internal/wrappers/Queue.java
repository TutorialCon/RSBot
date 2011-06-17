package org.rsbot.script.internal.wrappers;

import org.rsbot.client.NodeSub;
import org.rsbot.client.NodeSubQueue;

@SuppressWarnings("unchecked")
public class Queue<N extends NodeSub> {
	private final NodeSubQueue nodeSubQueue;
	private NodeSub current;

	public Queue(final NodeSubQueue nodeSubQueue) {
		this.nodeSubQueue = nodeSubQueue;
	}

	public int size() {
		int size = 0;
		NodeSub node = nodeSubQueue.getTail().getPrevSub();
		while (node != nodeSubQueue.getTail()) {
			node = node.getPrevSub();
			size++;
		}
		return size;
	}

	public N getHead() {
		final NodeSub node = nodeSubQueue.getTail().getNextSub();
		if (node == nodeSubQueue.getTail()) {
			current = null;
			return null;
		}
		current = node.getNextSub();
		return (N) node;
	}

	public N getNext() {
		final NodeSub node = current;
		if (node == nodeSubQueue.getTail()) {
			current = null;
			return null;
		}
		current = node.getNextSub();
		return (N) node;
	}
}
