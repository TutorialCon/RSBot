package org.rsbot.script.task;

import org.rsbot.script.methods.Methods;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Containable extends Methods implements Task {
	private Future<?> f;

	/**
	 * {@inheritDoc}
	 */
	public boolean isDone() {
		return f.isDone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		f.cancel(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void join() {
		try {
			f.get();
		} catch (InterruptedException ignored) {
		} catch (ExecutionException ignored) {
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Future<?> f) {
		this.f = f;
	}
}
