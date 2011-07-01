package org.rsbot.util;

import org.rsbot.Configuration;
import org.rsbot.util.io.IOHelper;

import java.io.File;

/**
 * @author Paris
 */
public final class UpdateChecker {
	private static int latest = -1;

	public static int getLatestVersion() {
		if (latest != -1) {
			return latest;
		}
		try {
			final File cache = Configuration.Paths.getCachableResources().get(Configuration.Paths.URLs.VERSION);
			latest = Integer.parseInt(IOHelper.readString(cache).trim());
		} catch (final NumberFormatException ignored) {
			latest = Configuration.getVersion();
		}
		return latest;
	}
}
