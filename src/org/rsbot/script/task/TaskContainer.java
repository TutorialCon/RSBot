package org.rsbot.script.task;

import org.rsbot.script.methods.MethodContext;

import java.util.*;

public class TaskContainer {
	private MethodContext ctx;
	private final Map<Integer, LoopTask> tasks = new HashMap<Integer, LoopTask>();

	/**
	 * Creates a new container to run tasks in with control.
	 */
	public TaskContainer() {
	}

	/**
	 * Adds a new task to the containers' pool.
	 *
	 * @param loopTask The task to add.
	 * @return The ID if the new task.
	 */
	public int pool(final LoopTask loopTask) {
		for (int off = 0; off < tasks.size(); ++off) {
			if (!tasks.containsKey(off)) {
				tasks.put(off, loopTask);
				loopTask.setID(off);
				return off;
			}
		}
		loopTask.container = this;
		loopTask.setID(tasks.size());
		tasks.put(tasks.size(), loopTask);
		return tasks.size() - 1;
	}

	/**
	 * Registers a task to the desired method context.
	 *
	 * @param loopTaskID The task.
	 */
	private void register(final int loopTaskID) {
		final LoopTask task = tasks.get(loopTaskID);
		if (task != null) {
			task.init(ctx);
		}
	}

	/**
	 * Dispatches the current task onto the executor service for future concurrency.
	 *
	 * @param loopTaskID The task id to start.
	 * @return <tt>true</tt> if started.
	 */
	private boolean start(final int loopTaskID) {
		final LoopTask task = tasks.get(loopTaskID);
		if (task != null) {
			if (task.doExecution()) {
				task.init(ctx.service.submit(task, task));
				return true;
			}
		}
		return false;
	}

	/**
	 * Invokes the start of a task.
	 *
	 * @param loopTaskID The task.
	 * @return <tt>true</tt> if the task was started; otherwise false.
	 */
	public boolean invoke(final int loopTaskID) {
		register(loopTaskID);
		return start(loopTaskID);
	}

	/**
	 * Attempts to invoke the creation of all tasks on the executor.
	 */
	public void invokeAll() {
		for (LoopTask task : tasks.values()) {
			register(task.id);
			start(task.id);
		}
	}

	/**
	 * Sets a desired task paused.
	 *
	 * @param loopTaskID The task id.
	 * @param paused     Paused or not.
	 */
	public void setPaused(final int loopTaskID, final boolean paused) {
		final LoopTask task = tasks.get(loopTaskID);
		if (task != null) {
			task.setPaused(paused);
		}
	}

	/**
	 * Sets all pooled tasks to this paused state.
	 *
	 * @param paused Paused or not.
	 */
	public void setPaused(final boolean paused) {
		for (LoopTask task : tasks.values()) {
			task.setPaused(paused);
		}
	}

	/**
	 * Stops a desired task.  Remains in the pool.
	 *
	 * @param loopTaskID The task to stop.
	 */
	public void stop(final int loopTaskID) {
		final LoopTask task = tasks.get(loopTaskID);
		if (task != null) {
			task.stop();
			tasks.remove(loopTaskID);
		}
	}

	/**
	 * Stops all running tasks; remains in the pool.
	 */
	public void stop() {
		for (LoopTask task : tasks.values()) {
			task.stop();
		}
	}

	/**
	 * Removes a task from the pool.
	 *
	 * @param loopTaskID The task ID.
	 * @return <tt>true</tt> if the task existed.
	 */
	public boolean remove(final int loopTaskID) {
		final LoopTask task = tasks.get(loopTaskID);
		if (task != null) {
			task.stop();
			tasks.remove(loopTaskID);
			return true;
		}
		return false;
	}

	/**
	 * Sets this containers context.
	 *
	 * @param ctx The context.
	 */
	protected void setContext(final MethodContext ctx) {
		this.ctx = ctx;
	}

	protected Map<Integer, LoopTask> getTasks() {
		return Collections.unmodifiableMap(tasks);
	}
}
