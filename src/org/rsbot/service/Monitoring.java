package org.rsbot.service;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.rsbot.Configuration;
import org.rsbot.util.io.HttpClient;

public final class Monitoring extends TimerTask {
	private static Monitoring instance;
	private Timer timer = new Timer(true);
	private boolean running = false;

	public static Monitoring getInstance() {
		if (instance == null) {
			instance = new Monitoring();
		}
		return instance;
	}

	private Monitoring() {
	}
	
	public void start() {
		if (!running) {
			final long p = getPeriod() * 1000;
			timer.schedule(this, p, p);
			running = true;
		}
	}

	public void stop() {
		if (running) {
			timer.cancel();
			running = false;
		}
	}

	@Override
	public void run() {
		getPeriod();
	}

	private int getPeriod() {
		String result = null;
		try {
			result = HttpClient.downloadAsString(new URL(Configuration.Paths.URLs.TRACK_HITS));
		} catch (final IOException ignored) {
		}
		return result == null || result.length() == 0 ? -1 : Integer.parseInt(result.trim());
	}
}
