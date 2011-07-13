package org.rsbot.script.task;

import org.rsbot.event.EventMulticaster;
import org.rsbot.event.listeners.PaintListener;

import java.util.EventListener;
import java.util.logging.Level;

public abstract class LoopTask extends Containable implements EventListener {
	protected int id = -1;
	private boolean running = false, paused = false;
	protected TaskContainer container;

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
	protected void onRun() {
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
	 * The main loop. Called if you return true from doExecution, then continuously until
	 * a negative integer is returned or the script stopped externally. When this script
	 * is paused this method will not be called until the script is resumed. Avoid causing
	 * execution to pause using sleep() within this method in favor of returning the number
	 * of milliseconds to sleep. This ensures that pausing and anti-randoms perform normally.
	 *
	 * @return The number of milliseconds that the manager should sleep before
	 *         calling it again. Returning a negative number will deactivate the script.
	 */
	protected abstract int loop();

	/**
	 * {@inheritDoc}
	 */
	protected boolean runOnce() {
		int timeOut = -1;
		try {
			timeOut = loop();
		} catch (final ThreadDeath td) {
			return false;
		} catch (final Exception ex) {
			log.log(Level.WARNING, "Uncaught exception from task: ", ex);
		}
		if (timeOut == -1) {
			return false;
		}
		try {
			sleep(timeOut);
		} catch (final ThreadDeath td) {
			return false;
		}
		return true;
	}

	protected void register() {
		ctx.bot.getEventManager().addListener(this);
	}

	public void run() {
		running = true;
		try {
			onRun();
		} catch (final ThreadDeath ignored) {
		} catch (final Throwable ex) {
			log.log(Level.SEVERE, "Error starting task: ", ex);
		}
		try {
			while (running) {
				if (!paused) {
					if (!runOnce()) {
						break;
					}
				} else {
					try {
						sleep(pausedIterationDelay());
					} catch (final ThreadDeath td) {
						break;
					}
					if (scriptResume()) {
						setPaused(false);
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
		}
		stop();
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
	public void setPaused(final boolean paused) {
		if (running) {
			this.paused = paused;
			if (paused) {
				blockEvents(true);
			} else {
				unblockEvents();
			}
		}
	}

	public void setRunning(final boolean running) {
		this.running = running;
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
	 * Returns whether or not this script is paused.
	 *
	 * @return <tt>true</tt> if paused; otherwise <tt>false</tt>.
	 */
	public final boolean isPaused() {
		return paused;
	}

	/**
	 * Returns whether or not this script has started and not stopped.
	 *
	 * @return <tt>true</tt> if running; otherwise <tt>false</tt>.
	 */
	public final boolean isRunning() {
		return running;
	}

	/**
	 * Stops the currently running task, doesn't force stop.
	 */
	@Override
	public void stop() {
		this.running = false;
		ctx.bot.getEventManager().removeListener(this);
		super.stop();
	}

	protected void blockEvents(final boolean paint) {
		ctx.bot.getEventManager().removeListener(this);
		if (paint && this instanceof PaintListener) {
			ctx.bot.getEventManager().addListener(this, EventMulticaster.PAINT_EVENT);
		}
	}

	protected void unblockEvents() {
		ctx.bot.getEventManager().removeListener(this);
		ctx.bot.getEventManager().addListener(this);
	}

	public int getId() {
		return id;
	}
}