package org.rsbot.script.wrappers;

import java.awt.*;

public interface RSTarget {
	/**
	 * Returns the screen location from this target
	 *
	 * @return
	 */
	public Point getPoint();

	/**
	 * Checks if the given x and y are inside of this target
	 *
	 * @param x The x position
	 * @param y The y position
	 */
	public boolean contains(int x, int y);
}
