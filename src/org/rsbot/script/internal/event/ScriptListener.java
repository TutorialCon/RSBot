package org.rsbot.script.internal.event;

import org.rsbot.bot.Bot;
import org.rsbot.script.Script;
import org.rsbot.script.internal.ScriptHandler;

public interface ScriptListener {
	public void scriptStarted(ScriptHandler handler);

	public void scriptStopped(ScriptHandler handler);

	public void scriptResumed(ScriptHandler handler);

	public void scriptPaused(ScriptHandler handler);

	public void inputChanged(Bot bot, int mask);
}
