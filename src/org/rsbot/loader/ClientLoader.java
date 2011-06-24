package org.rsbot.loader;

import org.rsbot.loader.asm.ClassReader;
import org.rsbot.loader.script.ModScript;
import org.rsbot.loader.script.ParseException;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IOHelper;

import javax.swing.*;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Paris
 */
public class ClientLoader {

	private final Logger log = Logger.getLogger(ClientLoader.class.getName());

	private ModScript script;
	private Map<String, byte[]> classes;
	private int world = nextWorld();

	public void init(final URL script, final File cache) throws IOException, ParseException {
		byte[] data = null;
		FileInputStream fis = null;

		try {
			HttpClient.download(script, cache);
		} catch (final IOException ioe) {
			if (cache.exists()) {
				log.warning("Unable to download client patch, attempting to use cached copy");
			}
		}

		try {
			fis = new FileInputStream(cache);
			data = load(fis);
		} catch (final IOException ioe) {
			log.severe("Could not load client patch");
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (final IOException ignored) {
			}
		}

		this.script = new ModScript(data);
	}

	public void load(final File cache, final File versionFile) throws IOException {
		classes = new HashMap<String, byte[]>();
		final int[] version = { script.getVersion(), -1 };
		final String target = script.getAttribute("target");

		try {
			version[1] = Integer.parseInt(IOHelper.readString(versionFile));
		} catch (final Exception ignored) {
		}

		if (version[0] <= version[1]) {
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
		} else {
			log.info("Downloading game client");
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
							classes.put(name, load(jar.getInputStream(entry)));
						}
					}
				}
			}

			try {
				version[1] = checkVersion(new ByteArrayInputStream(classes.get("client")));
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
				entry.setCrc(IOHelper.crc32(new ByteArrayInputStream(data)));
				zip.putNextEntry(entry);
				zip.write(item.getValue());
				zip.closeEntry();
			}
			zip.close();
			IOHelper.write(Integer.toString(version[1]), versionFile);
		}
	}

	public Map<String, byte[]> getClasses() {
		return classes;
	}

	public String getTargetName() {
		return script.getAttribute("target");
	}

	private int checkVersion(final InputStream in) throws IOException {
		final ClassReader reader = new ClassReader(in);
		final VersionVisitor vv = new VersionVisitor();
		reader.accept(vv, ClassReader.SKIP_FRAMES);
		if (vv.getVersion() != script.getVersion()) {
			JOptionPane.showMessageDialog(
					null,
					"The bot is currently oudated, please wait patiently for a new version.",
					"Outdated",
					JOptionPane.INFORMATION_MESSAGE);
			throw new IOException("ModScript #" + script.getVersion() + " != #" + vv.getVersion());
		}
		return vv.getVersion();
	}

	private JarFile getJar(final String target, final boolean loader) {
		while (true) {
			try {
				String s = "jar:http://world" + world + "." + target + ".com/";
				if (loader) {
					s += "loader.jar!/";
				} else {
					s += target + ".jar!/";
				}
				final URL url = new URL(s);
				final JarURLConnection juc = (JarURLConnection) url.openConnection();
				juc.setConnectTimeout(5000);
				return juc.getJarFile();
			} catch (final Exception ignored) {
				world = nextWorld();
			}
		}
	}

	private int nextWorld() {
		return 1 + new Random().nextInt(169);
	}

	private byte[] load(final InputStream is) throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		int n;
		while ((n = is.read(buffer)) != -1) {
			os.write(buffer, 0, n);
		}
		return os.toByteArray();
	}
}
