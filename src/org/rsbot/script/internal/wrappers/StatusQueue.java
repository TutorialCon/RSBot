package org.rsbot.script.internal.wrappers;

import org.rsbot.client.StatusNode;
import org.rsbot.client.StatusNodeList;

class StatusQueue {
	private final StatusNodeList statusNodeList;
	private StatusNode c_node;

	public StatusQueue(final StatusNodeList statusNodeList) {
		this.statusNodeList = statusNodeList;
	}

	public StatusNode getFirst() {
		final StatusNode node = statusNodeList.getHead().getNext();
		if (node == statusNodeList.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();
		return node;
	}

	public StatusNode getLast() {
		final StatusNode node = statusNodeList.getHead().getPrevious();
		if (node == statusNodeList.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getPrevious();
		return node;
	}

	public StatusNode getNext() {
		final StatusNode node = c_node;
		if (node == statusNodeList.getHead() || node == null) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();
		return node;
	}

	public StatusNode getPrevious() {
		final StatusNode node = c_node;
		if (node == statusNodeList.getHead() || node == null) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();
		return node;
	}

	public int size() {
		int size = 0;
		StatusNode node = statusNodeList.getHead().getPrevious();
		while (node != statusNodeList.getHead()) {
			node = node.getPrevious();
			size++;
		}
		return size;
	}
}