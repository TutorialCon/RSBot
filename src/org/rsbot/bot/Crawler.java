package org.rsbot.bot;

import org.rsbot.loader.ClientLoader;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IniParser;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Crawler {
	private static final String id = Crawler.class.getName(), idm = id + "#misc";
	private static Map<String, String> parameters;
	private String world;

	public Crawler(final String root) {
		final File manifest = ClientLoader.getClientManifest();
		Map<String, Map<String, String>> data = null;

		if (manifest.exists()) {
			try {
				data = IniParser.deserialise(manifest);
				if (data.containsKey(id)) {
					parameters = data.get(id);
					world = data.get(idm).get("world");
					return;
				}
			} catch (final IOException ignored) {
			}
		}

		if (data == null) {
			data = new HashMap<String, Map<String, String>>(1);
		}

		final String index = firstMatch("<a id=\"continue\" class=\"barItem\" href=\"([^\"]+)\"\\s+onclick=\"[^\"]+\">Continue to Full Site for News and Game Help", downloadPage(root, null));
		final String frame = root + "game.ws";
		final String game = firstMatch("<frame id=\"[^\"]+\" style=\"[^\"]+\" src=\"([^\"]+)\"", downloadPage(frame, index));
		world = game.substring(12, game.indexOf(".runescape"));

		final Pattern pattern = Pattern.compile("<param name=\"?([^\\s]+)\"?\\s+value=\"?([^>]*)\"?>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		final Matcher matcher = pattern.matcher(downloadPage(game, frame));
		parameters = new HashMap<String, String>();
		while (matcher.find()) {
			final String key = removeTrailingChar(matcher.group(1), '"');
			final String value = removeTrailingChar(matcher.group(2), '"');
			if (!parameters.containsKey(key)) {
				parameters.put(key, value);
			}
		}

		final String ie = "haveie6";
		if (parameters.containsKey(ie)) {
			parameters.remove(ie);
		}
		parameters.put("haveie6", "0");

		try {
			data.put(id, parameters);
			final Map<String, String> misc = new HashMap<String, String>(1);
			misc.put("world", world);
			data.put(idm, misc);
			IniParser.serialise(data, manifest);
		} catch (final IOException ignored) {
		}
	}

	private String downloadPage(final String url, final String referer) {
		try {
			final HttpURLConnection con = HttpClient.getHttpConnection(new URL(url));
			if (referer != null && !referer.isEmpty()) {
				con.addRequestProperty("Referer", referer);
			}
			return HttpClient.downloadAsString(con);
		} catch (final IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private String firstMatch(final String regex, final String str) {
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getWorldPrefix() {
		return world;
	}

	private String removeTrailingChar(final String str, final char ch) {
		if (str == null || str.isEmpty()) {
			return str;
		} else if (str.length() == 1) {
			return str.charAt(0) == ch ? "" : str;
		}
		try {
			final int l = str.length() - 1;
			if (str.charAt(l) == ch) {
				return str.substring(0, l);
			}
			return str;
		} catch (final Exception e) {
			return str;
		}
	}
}
