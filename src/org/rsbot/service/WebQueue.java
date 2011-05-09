package org.rsbot.service;

import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * The web queue class, passes data to Cache writer.
 *
 * @author Timer
 */
public class WebQueue {
	public static boolean weAreBuffering = false, speedBuffer = false;
	public static int bufferingCount = 0;
	private static final List<String> queue = new ArrayList<String>(), removeQueue = new ArrayList<String>(), removeStack = new ArrayList<String>();
	private static QueueWriter writer;
	private static final Logger log = Logger.getLogger(WebQueue.class.getName());

	static {
		writer = new QueueWriter(GlobalConfiguration.Paths.getWebCache());
	}

	/**
	 * Adds collected data to the queue.
	 *
	 * @param theFlagsList The data.
	 */
	public static void Add(final HashMap<RSTile, TileFlags> theFlagsList) {
		Web.map.putAll(theFlagsList);
		final int count = theFlagsList.size();
		new Thread() {
			@Override
			public void run() {
				try {
					String addedString = "";
					final HashMap<RSTile, TileFlags> theFlagsList2 = new HashMap<RSTile, TileFlags>();
					theFlagsList2.putAll(theFlagsList);
					final Map<RSTile, TileFlags> tl = Collections.unmodifiableMap(theFlagsList2);
					bufferingCount = bufferingCount + count;
					final Iterator<Map.Entry<RSTile, TileFlags>> tileFlagsIterator = tl.entrySet().iterator();
					while (tileFlagsIterator.hasNext()) {
						final TileFlags tileFlags = tileFlagsIterator.next().getValue();
						if (tileFlags != null) {
							addedString += tileFlags.toString() + "\n";
							bufferingCount--;
							try {
								weAreBuffering = true;
								if (!speedBuffer) {
									Thread.sleep(1);
								}
							} catch (final InterruptedException ignored) {
							}
						}
					}
					if (bufferingCount < 0) {
						bufferingCount = 0;
					}
					queue.add(addedString);
					addedString = null;
					theFlagsList2.clear();
					weAreBuffering = false;
				} catch (final Exception e) {
					bufferingCount = count;
					if (bufferingCount < 0) {
						bufferingCount = 0;
					}
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Removes a tile from the database.
	 *
	 * @param tile The tile to remove.
	 */
	public static void Remove(final RSTile tile) {
		Web.map.remove(tile);
		Remove(tile.getX() + "," + tile.getY() + tile.getZ());
	}

	/**
	 * Destroys the cache writer.
	 */
	public static void Destroy() {
		speedBuffer = true;
		writer.destroyWriter();
	}

	/**
	 * Gets the queue size.
	 *
	 * @param id The id to grab.
	 * @return The size of the queue.
	 */
	public static int queueSize(final int id) {
		switch (id) {
			case 0:
				return queue.size();
			case 1:
				return removeQueue.size();
			case 2:
				return removeStack.size();
		}
		return -1;
	}

	/**
	 * The threaded writer class.
	 *
	 * @author Timer
	 */
	private static class QueueWriter extends Thread {
		private boolean destroy = false;
		private final File file, tmpFile;

		public QueueWriter(final String fileName) {
			file = new File(fileName);
			tmpFile = new File(fileName + ".tmp");
			if (!file.exists()) {
				log.fine("File not created, creating: " + fileName);
				try {
					if (file.createNewFile()) {
						file.setExecutable(false);
						file.setReadable(true);
						file.setWritable(true);
					}
				} catch (final Exception e) {
					destroy = true;
				}
			}
		}

		/**
		 * The main method...  doesn't stop until all data is written.
		 */
		@Override
		public void run() {
			final List<String> outList = new ArrayList<String>();
			while ((!destroy || queue.size() > 0 || WebQueue.weAreBuffering) && file.exists() && file.canWrite()) {
				try {
					if (removeQueue.size() > 0) {
						removeStack.clear();
						removeStack.addAll(removeQueue);
						removeQueue.clear();
						final BufferedReader br = new BufferedReader(new FileReader(file));
						final PrintWriter pw = new PrintWriter(new FileWriter(tmpFile));
						String line;
						while ((line = br.readLine()) != null) {
							boolean good = true;
							final Iterator<String> removeLines = removeStack.listIterator();
							while (removeLines.hasNext()) {
								final String str = removeLines.next();
								if (str != null && line.contains(str)) {
									good = false;
									break;
								}
							}
							if (good) {
								pw.println(line);
								pw.flush();
							}
						}
						pw.close();
						br.close();
						if (file.delete()) {
							if (!tmpFile.renameTo(file)) {
								destroyWriter();
								continue;
							}
						}
						removeStack.clear();
					}
					if (queue.size() > 0) {
						final FileWriter fileWriter = new FileWriter(file, true);
						final BufferedWriter out = new BufferedWriter(fileWriter);
						outList.clear();
						outList.addAll(queue);
						queue.clear();
						final Iterator<String> outLines = outList.listIterator();
						while (outLines.hasNext()) {
							final String line = outLines.next();
							out.write(line + "\n");
						}
						out.flush();
						out.close();
					}
					try {
						if (!destroy) {
							Thread.sleep(5000);
						}
					} catch (final InterruptedException ignored) {
					}
				} catch (final IOException ignored) {
				}
			}
		}

		public void remove(final String str) {
			removeQueue.add(str);
		}

		public void destroyWriter() {
			destroy = true;
		}
	}

	/**
	 * Adds a string to remove to the queue.
	 *
	 * @param str The string to remove.
	 */
	public static void Remove(final String str) {
		writer.remove(str);
	}
}