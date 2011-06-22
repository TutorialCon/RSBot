package org.rsbot.script;

/**
 * A background script.
 *
 * @author Timer
 */
public abstract class BackgroundScript extends Script {
	protected abstract boolean activateCondition();

	protected abstract int iterationSleep();

	@Override
	public void stopScript(final boolean logout) {
		running = false;
	}

	public final void run() {
		ctx.bot.getEventManager().addListener(this);
		running = true;
		try {
			while (running) {
				if (activateCondition()) {
					final boolean start = onStart();
					if (start) {
						while (running) {
							final int timeOut = loop();
							if (timeOut == -1) {
								break;
							}
							Thread.sleep(timeOut);
						}
						onFinish();
					}
				}
				Thread.sleep(iterationSleep());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		ctx.bot.getEventManager().removeListener(this);
		running = false;
	}
}
