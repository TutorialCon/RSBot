package org.rsbot;

import org.rsbot.script.util.WindowUtil;
import org.rsbot.util.io.HttpClient;

import java.io.File;
import java.net.URL;

public class BootLoader {
	protected static void loadLibrary() {
		//TODO process dialogue
		try {
			HttpClient.download(new URL(Configuration.Paths.URLs.JNA), new File(Configuration.Paths.getCacheDirectory(), "jna.jar"));
		} catch (Exception ignored) {
			WindowUtil.showDialog("Download failed.\n\nThe application failed to initialize because mandatory resources are missing from the cache directory.");
		}
	}
}