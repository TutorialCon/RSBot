package org.rsbot.script.provider;

import org.rsbot.Configuration;
import org.rsbot.script.Script;
import org.rsbot.script.provider.FileScriptSource.FileScriptDefinition;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IOHelper;
import org.rsbot.util.io.IniParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Paris
 */
public class ScriptDeliveryNetwork implements ScriptSource {
	private static final Logger log = Logger.getLogger("ScriptDelivery");
	private static ScriptDeliveryNetwork instance;
	private URL base;
	private final File manifest;

	private ScriptDeliveryNetwork() {
		manifest = Configuration.Paths.getCachableResources().get(Configuration.Paths.URLs.SDN_MANIFEST);
	}

	public static ScriptDeliveryNetwork getInstance() {
		if (instance == null) {
			instance = new ScriptDeliveryNetwork();
		}
		return instance;
	}

	private static void parseManifests(final Map<String, Map<String, String>> entries, final List<ScriptDefinition> defs) {
		for (final Entry<String, Map<String, String>> entry : entries.entrySet()) {
			final ScriptDefinition def = new ScriptDefinition();
			def.path = entry.getKey();
			final Map<String, String> values = entry.getValue();
			def.id = Integer.parseInt(values.get("id"));
			def.crc32 = values.containsKey("crc32") ? Long.parseLong(values.get("crc32")) : 0;
			def.name = values.get("name");
			def.version = Double.parseDouble(values.get("version"));
			def.description = values.get("description");
			def.authors = values.get("authors").split(ScriptList.DELIMITER);
			def.keywords = values.get("keywords").split(ScriptList.DELIMITER);
			def.website = values.get("website");
			defs.add(def);
		}
	}

	public void refresh(final boolean force) {
		if (force || !manifest.exists() || base == null) {
			try {
				base = HttpClient.download(new URL(Configuration.Paths.URLs.SDN_MANIFEST), manifest).getURL();
			} catch (final IOException ignored) {
				log.warning("Unable to load scripts from the network");
			}
		}
	}

	public List<ScriptDefinition> list() {
		final ArrayList<ScriptDefinition> defs = new ArrayList<ScriptDefinition>();
		refresh(false);
		try {
			parseManifests(IniParser.deserialise(manifest), defs);
		} catch (final IOException ignored) {
			log.warning("Error reading network script manifests");
		}
		for (final ScriptDefinition def : defs) {
			def.source = this;
		}
		return defs;
	}

	public Script load(final ScriptDefinition def) {
		final File cache = new File(Configuration.Paths.getScriptsNetworkDirectory(), def.path);
		final LinkedList<ScriptDefinition> defs = new LinkedList<ScriptDefinition>();
		try {
			if (!cache.exists() || IOHelper.crc32(cache) != def.crc32) {
				log.info("Downloading script " + def.name + "...");
				HttpClient.download(new URL(base, def.path), cache);
			}
			FileScriptSource.load(cache, defs, null);
			return FileScriptSource.load((FileScriptDefinition) defs.getFirst());
		} catch (final Exception ignored) {
			log.severe("Unable to load script");
		}
		return null;
	}
}
