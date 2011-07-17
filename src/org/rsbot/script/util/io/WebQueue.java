package org.rsbot.script.util.io;

import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The web queue class, passes data to Cache writer.
 *
 * @author Timer
 */
public class WebQueue {
	public static boolean weAreBuffering = false;
	private static final List<String> queue = new ArrayList<String>(), queueOutList = new ArrayList<String>(), removeQueue = new ArrayList<String>(), removeStack = new ArrayList<String>();
	private static final QueueWriter writer;
	private static final Logger log = Logger.getLogger(WebQueue.class.getName());
	private static final Object queueLock = new Object(), bufferLock = new Object(), removeLock = new Object();

	static {
		writer = new QueueWriter(Web.CACHE);
	}

	/**
	 * Adds collected data to the queue.
	 *
	 * @param gameTiles The data.
	 */
	public static void Add(final HashMap<RSTile, Integer> gameTiles) {
		Web.rs_map.putAll(gameTiles);
		new Thread() {
			@Override
			public void run() {
				try {
					final HashMap<RSTile, Integer> safeMapData = new HashMap<RSTile, Integer>();
					safeMapData.putAll(gameTiles);
					for (Map.Entry<RSTile, Integer> tileData : safeMapData.entrySet()) {
						final RSTile tile = tileData.getKey();
						final int key = tileData.getValue();
						synchronized (queueLock) {
							queue.add(tile.getX() + "," + tile.getY() + "," + tile.getZ() + "k" + key);
						}
						synchronized (bufferLock) {
							try {
								weAreBuffering = true;
								Thread.sleep(1);
							} catch (final InterruptedException ignored) {
							}
						}
					}
					weAreBuffering = false;
				} catch (final Exception e) {
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
		synchronized (removeLock) {
			Web.rs_map.remove(tile);
			Remove(tile.getX() + "," + tile.getY() + "," + tile.getZ());
		}
	}

	/**
	 * Starts the cache writer.
	 */
	public static void Start() {
		if (writer.destroy && !writer.isAlive()) {
			try {
				writer.destroy = false;
				writer.start();
			} catch (IllegalThreadStateException ignored) {
			}
		}
	}

	/**
	 * Destroys the cache writer.
	 */
	public static void Destroy() {
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

	public static boolean isEmpty() {
		return queue.size() == 0 && queueOutList.size() == 0 && removeQueue.size() == 0 && removeStack.size() == 0;
	}

	/**
	 * The threaded writer class.
	 *
	 * @author Timer
	 */
	private static class QueueWriter extends Thread {
		private boolean destroy = true;
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
				}
			}
		}

		/**
		 * The main method...  doesn't stop until all data is written.
		 */
		@Override
		public void run() {
			while ((!destroy || queue.size() > 0 || WebQueue.weAreBuffering) && file.exists() && file.canWrite()) {
				try {
					if (removeQueue.size() > 0) {
						synchronized (removeLock) {
							removeStack.clear();
							if (removeQueue.size() > 100) {
								final List<String> removeQueueSub = new ArrayList<String>();
								removeQueueSub.addAll(removeQueue.subList(0, 99));
								removeStack.addAll(removeQueueSub);
								removeQueue.removeAll(removeQueueSub);
							} else {
								removeStack.addAll(removeQueue);
								removeQueue.clear();
							}
						}
						final BufferedReader br = new BufferedReader(new FileReader(file));
						final PrintWriter pw = new PrintWriter(new FileWriter(tmpFile));
						String line;
						while ((line = br.readLine()) != null) {
							boolean good = true;
							for (String str : removeStack) {
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
					synchronized (queueLock) {
						if (queue.size() > 0) {
							final FileWriter fileWriter = new FileWriter(file, true);
							final BufferedWriter out = new BufferedWriter(fileWriter);
							queueOutList.clear();
							queueOutList.addAll(queue);
							queue.clear();
							for (String line : queueOutList) {
								out.write(line + "\n");
							}
							out.flush();
							out.close();
						}
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
			synchronized (removeLock) {
				removeQueue.add(str);
			}
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
		synchronized (removeLock) {
			writer.remove(str);
		}
	}
}