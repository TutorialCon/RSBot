package org.rsbot.script;

import org.rsbot.Configuration;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.internal.BreakHandler;
import org.rsbot.script.randoms.ImprovedLoginBot;
import org.rsbot.script.task.LoopTask;
import org.rsbot.script.util.Timer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public abstract class Script extends LoopTask {
	Set<Script> delegates = new HashSet<Script>();
	private volatile boolean random = false;
	private long lastNotice;

	/**
	 * Called before loop() is first called, after this script has
	 * been initialized with all method providers. Override to
	 * perform any initialization or prevent script start.
	 *
	 * @return <tt>true</tt> if the script can start.
	 */
	public boolean onStart() {
		return true;
	}

	/**
	 * Called when a break is initiated, before the logout.
	 * Override it to implement in your script.
	 *
	 * @return <tt>true</tt> if a break can be initiated.
	 */
	public boolean onBreakStart() {
		return true;
	}

	/**
	 * Called when a break is initiated, before the login.
	 * Override it to implement in your script.
	 */
	public void onBreakFinish() {
	}

	/**
	 * The main loop. Called if you return true from onStart, then continuously until
	 * a negative integer is returned or the script stopped externally. When this script
	 * is paused this method will not be called until the script is resumed. Avoid causing
	 * execution to pause using sleep() within this method in favor of returning the number
	 * of milliseconds to sleep. This ensures that pausing and anti-randoms perform normally.
	 *
	 * @return The number of milliseconds that the manager should sleep before
	 *         calling it again. Returning a negative number will deactivate the script.
	 */
	@Override
	public abstract int loop();

	/**
	 * Initializes the provided script with this script's
	 * method context and adds the delegate as a listener
	 * to the event manager, allowing it to receive client
	 * events. The script will be stored as a delegate of
	 * this script and removed from the event manager when
	 * this script is stopped. The onStart(), loop() and
	 * onFinish() methods are not automatically called on
	 * the delegate.
	 *
	 * @param script The script to delegate to.
	 */
	public final void delegateTo(final Script script) {
		final int id = container.pool(script);
		container.invoke(id);
		delegates.add(script);
	}

	/**
	 * Returns whether or not the loop of this script is able to
	 * receive control (i.e. not paused, stopped or in random).
	 *
	 * @return <tt>true</tt> if active; otherwise <tt>false</tt>.
	 */
	public final boolean isActive() {
		return isRunning() && !isPaused() && !random;
	}

	/**
	 * Stops the current script without logging out.
	 */
	public final void stopScript() {
		stopScript(false);
	}

	/**
	 * Stops the current script; player can be logged out before
	 * the script is stopped.
	 *
	 * @param logout <tt>true</tt> if the player should be logged
	 *               out before the script is stopped.
	 */
	public final void stopScript(final boolean logout) {
		log.info("Script stopping...");
		if (logout) {
			if (bank.isOpen()) {
				bank.close();
			}
			if (game.isLoggedIn()) {
				game.logout(false);
			}
		}
		stop();
	}

	@Override
	public void run() {
		boolean start = false;
		try {
			start = onStart();
		} catch (final ThreadDeath ignored) {
		} catch (final Throwable ex) {
			log.log(Level.SEVERE, "Error starting script: ", ex);
		}
		if (start) {
			final BreakHandler breakHandler = new BreakHandler(this);
			setRunning(true);
			register();
			log.info("Script started.");
			try {
				while (isRunning()) {
					if (!isPaused()) {
						if (AccountManager.isTakingBreaks(account.getName())) {
							if (breakHandler.isBreaking()) {
								if (System.currentTimeMillis() - lastNotice > 600000) {
									lastNotice = System.currentTimeMillis();
									log.info("Breaking for " + Timer.format(breakHandler.getBreakTime()));
								}
								if (game.isLoggedIn() && breakHandler.getBreakTime() > 60000) {
									game.logout(true);
								}
								try {
									sleep(5000);
								} catch (final ThreadDeath td) {
									break;
								}
								continue;
							} else {
								breakHandler.tick();
							}
						}
						if (checkForRandoms()) {
							continue;
						}
						if (!runOnce()) {
							break;
						}
					} else {
						try {
							sleep(1000);
						} catch (final ThreadDeath td) {
							break;
						}
					}
				}
				try {
					onFinish();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			} catch (final Throwable t) {
				try {
					onFinish();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			log.info("Script stopped.");
		} else {
			log.severe("Failed to start up.");
		}
		mouse.moveOffScreen();
		for (final Script s : delegates) {
			s.stop();
		}
		delegates.clear();
		ctx.bot.getScriptHandler().stopScript(id);
	}

	private boolean checkForRandoms() {
		if (ctx.bot.disableRandoms) {
			return false;
		}
		for (final Random random : ctx.bot.getScriptHandler().getRandoms()) {
			if (random.isEnabled() && !(ctx.bot.disableAutoLogin && random instanceof ImprovedLoginBot)) {
				if (random.activateCondition()) {
					this.random = true;
					blockEvents(false);
					random.run(this);
					unblockEvents();
					this.random = false;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get an accessible and isolated directory for reading and writing files.
	 *
	 * @return A unique per-script directory path with file IO permissions.
	 */
	public final File getCacheDirectory() {
		final File dir = new File(Configuration.Paths.getScriptCacheDirectory(), getClass().getName());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
