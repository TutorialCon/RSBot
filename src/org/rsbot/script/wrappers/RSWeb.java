package org.rsbot.script.wrappers;

import org.rsbot.script.web.Route;

import java.util.LinkedList;

/**
 * A transportation action consisting of a list of routes.
 *
 * @author Timer
 */
public class RSWeb {
	private final LinkedList<Route> routes = new LinkedList<Route>();

	public RSWeb(final Route[] routes) {
		for (Route route : routes) {
			this.routes.addLast(route);
		}
	}

	public Route[] getRoutes() {
		return routes.toArray(new Route[routes.size()]);
	}

	public boolean traverse() {
		if (routes.size() > 0) {
			Route route = routes.poll();
			return route.execute();
		}
		return false;
	}
}