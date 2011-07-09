package org.rsbot.util;

import org.rsbot.Configuration;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IOHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Paris
 */
public final class UpdateChecker {
	private static int latest = -1;
	private static final Logger log = Logger.getLogger("UpdateChecker");

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

	public static boolean downloadLatest() {
		try {
			log.info("Downloading " + Configuration.NAME + " v" + getLatestVersion());
			final String newJarName = Configuration.NAME + "-" + getLatestVersion() + ".jar";
			final String oldJarName = Configuration.NAME + "-" + Configuration.getVersion() + ".jar";
			HttpClient.download(new URL(Configuration.Paths.URLs.DOWNLOAD), new File(newJarName));
			Runtime.getRuntime().exec("java -jar " + newJarName + " delete " + oldJarName);
			System.exit(0);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	public static boolean checkUp(final String website) {
		try {
			final String hostAddress = website;
			java.net.SocketAddress socketAddress = new java.net.InetSocketAddress(java.net.InetAddress.getByName(hostAddress), 80);
			java.net.Socket socket = new java.net.Socket();
			socket.connect(socketAddress, 2500);
			boolean connected = socket.isConnected();
			socket.close();
			return connected;
		} catch (final IOException ignored) {
			return false;
		}
	}
}
