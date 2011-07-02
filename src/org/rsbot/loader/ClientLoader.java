package org.rsbot.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.rsbot.Configuration;
import org.rsbot.loader.asm.ClassReader;
import org.rsbot.loader.script.ModScript;
import org.rsbot.loader.script.ParseException;
import org.rsbot.util.io.IOHelper;

/**
 * @author Paris
 */
public class ClientLoader {
	private final static ClientLoader instance = new ClientLoader();
	private final Logger log = Logger.getLogger(ClientLoader.class.getName());
	private ModScript script;
	private Map<String, byte[]> classes;

	private ClientLoader() {
	}

	public static ClientLoader getInstance() {
		return instance;
	}

	public void setup() throws IOException, ParseException {
		script = new ModScript(IOHelper.read(Configuration.Paths.getCachableResources().get(Configuration.Paths.URLs.CLIENTPATCH)));
		load(new File(Configuration.Paths.getCacheDirectory(), "client.jar"), new File(Configuration.Paths.getCacheDirectory(), "client-info.dat"));
	}

	private void load(final File cache, final File versionFile) throws IOException {
		classes = new HashMap<String, byte[]>();
		final int[] version = { script.getVersion(), -1 };

		try {
			version[1] = Integer.parseInt(IOHelper.readString(versionFile));
		} catch (final Exception ignored) {
		}

		if (version[0] <= version[1] && cache.exists()) {
			final JarFile jar = new JarFile(cache);
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class")) {
					name = name.substring(0, name.length() - 6).replace('/', '.');
					classes.put(name, IOHelper.read(jar.getInputStream(entry)));
				}
			}
			jar.close();
		} else {
			log.info("Downloading game client");
			final String target = script.getAttribute("target");
			final JarFile loader = getJar(target, true), client = getJar(target, false);

			final List<String> replace = Arrays.asList(script.getAttribute("replace").split(" "));

			for (final JarFile jar : new JarFile[] { loader, client }) {
				final Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.endsWith(".class")) {
						name = name.substring(0, name.length() - 6).replace('/', '.');
						if (jar == client || replace.contains(name)) {
							classes.put(name, IOHelper.read(jar.getInputStream(entry)));
						}
					}
				}
				jar.close();
			}

			try {
				version[1] = getClientVersion();
			} catch (final IOException ignored) {
			}

			for (final Map.Entry<String, byte[]> entry : classes.entrySet()) {
				entry.setValue(script.process(entry.getKey(), entry.getValue()));
			}

			final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(cache));
			zip.setMethod(ZipOutputStream.STORED);
			zip.setLevel(0);
			for (final Entry<String, byte[]> item : classes.entrySet()) {
				final ZipEntry entry = new ZipEntry(item.getKey() + ".class");
				entry.setMethod(ZipEntry.STORED);
				final byte[] data = item.getValue();
				entry.setSize(data.length);
				entry.setCompressedSize(data.length);
				entry.setCrc(IOHelper.crc32(data));
				zip.putNextEntry(entry);
				zip.write(item.getValue());
				zip.closeEntry();
			}
			zip.close();
			IOHelper.write(Integer.toString(version[1]), versionFile);
		}
	}

	public Map<String, byte[]> getClasses() {
		final Map<String, byte[]> copy = new HashMap<String, byte[]>(classes.size());
		for (final Entry<String, byte[]> item : classes.entrySet()) {
			copy.put(item.getKey(), item.getValue().clone());
		}
		return copy;
	}

	public String getTargetName() {
		return script.getAttribute("target");
	}

	public boolean isOutdated() {
		final int cv;
		try {
			cv = getClientVersion();
		} catch (final IOException ignored) {
			return true;
		}
		return cv != script.getVersion();
	}

	private int getClientVersion() throws IOException {
		final ClassReader reader = new ClassReader(new ByteArrayInputStream(classes.get("client")));
		final VersionVisitor vv = new VersionVisitor();
		reader.accept(vv, ClassReader.SKIP_FRAMES);
		return vv.getVersion();
	}

	private JarFile getJar(final String target, final boolean loader) {
		while (true) {
			final int world = 1 + new Random().nextInt(169);
			try {
				final StringBuilder sb = new StringBuilder(50);
				sb.append("jar:http://world").append(world).append(".").append(target).append(".com/");
				sb.append(loader ? "loader" : target).append(".jar!/");
				final JarURLConnection juc = (JarURLConnection) new URL(sb.toString()).openConnection();
				juc.setConnectTimeout(5000);
				return juc.getJarFile();
			} catch (final Exception ignored) {
			}
		}
	}
}
