package org.rsbot.script;

import java.util.logging.Level;

/**
 * A background script.
 *
 * @author Timer
 */
public abstract class BackgroundScript extends Script {
	protected abstract boolean activateCondition();

	@Override
	public void run() {
		setRunning(true);
		try {
			onRun();
		} catch (final ThreadDeath ignored) {
		} catch (final Throwable ex) {
			log.log(Level.SEVERE, "Error starting task: ", ex);
		}
		try {
			while (isRunning()) {
				if (activateCondition()) {
					if (!runOnce()) {
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
		}
		super.stop();
	}
}
