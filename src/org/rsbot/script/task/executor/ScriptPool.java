package org.rsbot.script.task.executor;

import org.rsbot.script.internal.ScriptHandler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ScriptPool implements ThreadFactory {
	private static final AtomicInteger threadNumber = new AtomicInteger(1);

	public Thread newThread(Runnable r) {
		Thread thread = new Thread(ScriptHandler.THREAD_GROUP, r, ScriptHandler.THREAD_GROUP_NAME + "-" + threadNumber.getAndIncrement());
		if (thread.isDaemon()) {
			thread.setDaemon(false);
		}
		if (thread.getPriority() != Thread.NORM_PRIORITY) {
			thread.setPriority(Thread.NORM_PRIORITY);
		}
		return thread;
	}
}