package org.rsbot.util;

import org.rsbot.service.WebQueue;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * A threaded file writer to cache files.  Supports deletion.
 *
 * @author Timer
 */
public class CacheWriter {
	private final List<String> queue = new ArrayList<String>(), removeQueue = new ArrayList<String>(), removeStack = new ArrayList<String>();
	private final QueueWriter writer;
	private static boolean stopped = false;
	private static final Logger log = Logger.getLogger("CacheWriter");

	public CacheWriter(final String fileName) {
		writer = new QueueWriter(fileName);
		writer.start();
	}

	/**
	 * Adds to the writer queue.
	 *
	 * @param list The string.
	 */
	public void add(String list) {
		if (list != null) {
			String[] lines = list.split("\n");
			if (lines != null) {
				queue.addAll(Arrays.asList(lines));
			}
			lines = null;
		}
		list = null;
	}

	/**
	 * Destroys the writer.
	 */
	public void destroy() {
		writer.destroyWriter();
	}

	/**
	 * Gets the queue size.
	 *
	 * @param id The id to grab.
	 * @return The size of the queue.
	 */
	public int queueSize(final int id) {
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
	private class QueueWriter extends Thread { //For slow systems and reduced lag, let's make writing slow and threaded (lol at thread and reducing cpu usage..).
		private boolean destroy = false;
		private final File file, tmpFile;

		public QueueWriter(final String fileName) {
			file = new File(fileName);
			tmpFile = new File(fileName + ".tmp");
			if (!file.exists()) {
				log.info("File not created, creating: " + fileName);
				try {
					if (file.createNewFile()) {
						file.setExecutable(false);
						file.setReadable(true);
						file.setWritable(true);
					}
				} catch (Exception e) {
					destroy = true;
				}
			}
		}

		/**
		 * The main method...  doesn't stop till all data is written.
		 */
		public void run() {
			List<String> outList = new ArrayList<String>();
			while ((!destroy || queue.size() > 0 || WebQueue.weAreBuffering) && file.exists() && file.canWrite()) {
				try {
					if (removeQueue.size() > 0) {
						removeStack.clear();
						removeStack.addAll(removeQueue);
						removeQueue.clear();
						BufferedReader br = new BufferedReader(new FileReader(file));
						PrintWriter pw = new PrintWriter(new FileWriter(tmpFile));
						String line;
						while ((line = br.readLine()) != null) {
							boolean good = true;
							Iterator<String> removeLines = removeStack.listIterator();
							while (removeLines.hasNext()) {
								String str = removeLines.next();
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
					FileWriter fileWriter = new FileWriter(file, true);
					BufferedWriter out = new BufferedWriter(fileWriter);
					if (queue.size() > 0) {
						outList.clear();
						if (queue.size() >= 1000 && !destroy) {
							outList.addAll(queue.subList(0, 999));
							queue.removeAll(outList);
						} else {
							outList.addAll(queue);
							queue.clear();
						}
						Iterator<String> outLines = outList.listIterator();
						while (outLines.hasNext()) {
							String line = outLines.next();
							out.write(line + "\n");
						}
					}
					out.flush();
					out.close();
					try {
						if (!destroy) {
							Thread.sleep(5000);
						}
					} catch (InterruptedException ignored) {
					}
				} catch (IOException ignored) {
				}
			}
			stopped = true;
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
	public void remove(final String str) {
		writer.remove(str);
	}

	/**
	 * Checks if it's running.
	 *
	 * @return <tt>true</tt> if it's running.
	 */
	public static boolean IsRunning() {
		return !stopped;
	}
}
