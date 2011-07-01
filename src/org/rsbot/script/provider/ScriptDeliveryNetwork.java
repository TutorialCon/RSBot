package org.rsbot.script.provider;

import org.rsbot.Configuration;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.provider.FileScriptSource.FileScriptDefinition;
import org.rsbot.service.ServiceException;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IniParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
		manifest = new File(Configuration.Paths.getCacheDirectory(), "sdn-manifests.txt");
	}

	public static ScriptDeliveryNetwork getInstance() {
		if (instance == null) {
			instance = new ScriptDeliveryNetwork();
		}
		return instance;
	}

	private static void parseManifests(final HashMap<String, HashMap<String, String>> entries, final List<ScriptDefinition> defs) {
		for (final Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
			final ScriptDefinition def = new ScriptDefinition();
			def.path = entry.getKey();
			final HashMap<String, String> values = entry.getValue();
			def.id = Integer.parseInt(values.get("id"));
			def.crc32 = values.containsKey("crc32") ? Long.parseLong(values.get("crc32")) : 0;
			def.name = values.get("name");
			def.version = Double.parseDouble(values.get("version"));
			def.description = values.get("description");
			def.authors = values.get("authors").split(ScriptList.DELIMITER);
			def.keywords = values.get("keywords").split(ScriptList.DELIMITER);
			def.categories = getCategories(values.get("categories"));
			def.website = values.get("website");
			defs.add(def);
		}
	}

	private static ScriptManifest.Category[] getCategories(String cats) {
		try {
			final String[] categories = cats.split(ScriptList.DELIMITER);
			final ScriptManifest.Category[] catList = new ScriptManifest.Category[categories.length];
			int i = -1;
			for (String categoryName : categories) {
				catList[i++] = ScriptManifest.Category.value(categoryName);
			}
			return catList;
		} catch (NullPointerException ignored) {
			return new ScriptManifest.Category[0];
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

	Map<String, ScriptDefinition> listMap() {
		final List<ScriptDefinition> list = list();
		final Map<String, ScriptDefinition> map = new LinkedHashMap<String, ScriptDefinition>(list.size());
		for (final ScriptDefinition def : list) {
			map.put(def.path, def);
		}
		return map;
	}

	public List<String> listPaths() {
		final List<ScriptDefinition> list = list();
		final ArrayList<String> files = new ArrayList<String>(list.size());
		for (final ScriptDefinition def : list) {
			files.add(def.path);
		}
		return files;
	}

	private static File getCacheDirectory() {
		final File store = new File(Configuration.Paths.getScriptsNetworkDirectory());
		if (!store.exists()) {
			store.mkdirs();
		}
		if (Configuration.getCurrentOperatingSystem() == Configuration.OperatingSystem.WINDOWS) {
			final String path = "\"" + store.getAbsolutePath() + "\"";
			try {
				Runtime.getRuntime().exec("attrib +H " + path);
			} catch (final IOException ignored) {
			}
		}
		return store;
	}

	void download(final ScriptDefinition def) {
		final File cache = new File(getCacheDirectory(), def.path);
		try {
			HttpClient.download(new URL(base, def.path), cache);
		} catch (final IOException ignored) {
		}
	}

	public void sync() {
		final List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(8);
		final Map<String, ScriptDefinition> list = listMap();
		for (final File file : getCacheDirectory().listFiles()) {
			final String path = file.getName();
			if (!list.keySet().contains(path)) {
				file.delete();
			} else {
				tasks.add(Executors.callable(new Runnable() {
					public void run() {
						download(list.get(path));
					}
				}));
			}
		}
		final ExecutorService pool = Executors.newCachedThreadPool();
		try {
			pool.invokeAll(tasks);
		} catch (final InterruptedException ignored) {
		}
	}

	public Script load(final ScriptDefinition def) throws ServiceException {
		final File cache = new File(getCacheDirectory(), def.path);
		final LinkedList<ScriptDefinition> defs = new LinkedList<ScriptDefinition>();
		try {
			if (!cache.exists() || getScriptVersion(cache) < def.version) {
				log.info("Downloading script " + def.name + "...");
				download(def);
			}
			FileScriptSource.load(cache, defs, null);
			return FileScriptSource.load((FileScriptDefinition) defs.getFirst());
		} catch (final Exception ignored) {
			log.severe("Unable to load script");
		}
		return null;
	}

	private double getScriptVersion(final File cache) {
		final LinkedList<ScriptDefinition> defs = new LinkedList<ScriptDefinition>();
		try {
			FileScriptSource.load(cache, defs, null);
		} catch (final IOException ignored) {
			return -1;
		}
		return defs.getFirst().version;
	}
}
