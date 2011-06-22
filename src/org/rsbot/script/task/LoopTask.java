package org.rsbot.script.task;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;

public abstract class LoopTask extends AbstractTask {
	private final MethodContext ctx;

	public LoopTask(final MethodContext context) {
		this.ctx = context;
	}

	protected boolean executionCondition() {
		return true;
	}

	protected int executionDelay() {
		return 1000;
	}

	protected abstract int loop();

	protected void onFinish() {

	}

	public void run() {
		while (true) {
			if (executionCondition()) {
				int wait = loop();
				if (wait != -1) {
					Methods.sleep(wait);
				} else {
					stop();
					break;
				}
			} else {
				Methods.sleep(executionDelay());
			}
		}
	}

	protected void start() {
		init(ctx.service.submit(this, this));
	}
}
