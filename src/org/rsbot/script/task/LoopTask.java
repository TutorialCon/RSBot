package org.rsbot.script.task;

import java.util.EventListener;
import java.util.logging.Level;

public abstract class LoopTask extends Containable implements EventListener {
	protected int id = -1;
	private boolean running = false, paused = false;

	/**
	 * Checks if this loop task is allowed to run when invoked, DOES NOT LOOP!
	 *
	 * @return <tt>true</tt> if the task can be invoked.
	 */
	protected boolean doExecution() {
		return true;
	}

	/**
	 * Is executed upon task invoking.
	 */
	protected void onStart() {
	}

	/**
	 * Is executed upon task completion.
	 */
	protected void onFinish() {
	}

	/**
	 * The iteration delay to wait while sleeping.
	 *
	 * @return
	 */
	protected int pausedIterationDelay() {
		return 1000;
	}

	/**
	 * The main task's loop.
	 *
	 * @return
	 */
	protected abstract int loop();

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		running = true;
		try {
			onStart();
		} catch (final ThreadDeath ignored) {
		} catch (final Throwable ex) {
			log.log(Level.SEVERE, "Error starting task: ", ex);
		}
		ctx.bot.getEventManager().addListener(this);
		try {
			while (running) {
				if (!paused) {
					int timeOut = -1;
					try {
						timeOut = loop();
					} catch (final ThreadDeath td) {
						break;
					} catch (final Exception ex) {
						log.log(Level.WARNING, "Uncaught exception from task: ", ex);
					}
					if (timeOut == -1) {
						break;
					}
					try {
						sleep(timeOut);
					} catch (final ThreadDeath td) {
						break;
					}
				} else {
					try {
						sleep(pausedIterationDelay());
					} catch (final ThreadDeath td) {
						break;
					}
				}
			}
			try {
				onFinish();
			} catch (final ThreadDeath ignored) {
			} catch (final RuntimeException e) {
				e.printStackTrace();
			}
		} catch (final Throwable t) {
			onFinish();
			if (scriptResume()) {
				setPaused(false);
			}
		}
		ctx.bot.getEventManager().removeListener(this);
		super.stop();
	}

	/**
	 * Sets the id of this task, INTERNAL USE ONLY.
	 *
	 * @param id The id of the task.
	 */
	protected void setID(final int id) {
		this.id = id;
	}

	/**
	 * Sets this task paused or not.
	 *
	 * @param paused If the task is paused.
	 */
	protected void setPaused(final boolean paused) {
		this.paused = paused;
	}

	/**
	 * Allow the script to resume itself.
	 *
	 * @return <tt>true</tt> to resume script;
	 */
	protected boolean scriptResume() {
		return false;
	}

	/**
	 * Stops the currently running task, doesn't force stop.
	 */
	@Override
	public void stop() {
		this.running = false;
	}
}