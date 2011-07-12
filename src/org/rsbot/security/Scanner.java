package org.rsbot.security;

import org.rsbot.Configuration;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.util.Win32;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Scanner {
	private static final Logger log = Logger.getLogger("Security");
	private static volatile int selectedOption = -1;

	private static final File[] SUSPECT_DIRECTORIES;

	static {
		if (Configuration.getCurrentOperatingSystem() == Configuration.OperatingSystem.WINDOWS) {
			SUSPECT_DIRECTORIES = new File[]{
					new File(Configuration.Paths.getUnixHome()),
					new File(System.getenv("APPDATA")),
					new File(System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup"),
					new File("C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\Startup")};
		} else {
			SUSPECT_DIRECTORIES = new File[]{};
		}
	}

	private static final String[] SUSPECT_FILE_NAMES = {"jagex"};
	private static final String[] SUSPECT_FILE_EXTENSIONS = {"jar"};

	public static boolean Scan() {
		if (Configuration.getCurrentOperatingSystem() == Configuration.OperatingSystem.WINDOWS) {
			log.fine("Scanning for malicious files.");
			for (File directory : SUSPECT_DIRECTORIES) {
				if (directory.exists()) {
					final File[] directoryList = directory.listFiles();
					for (File f : directoryList) {
						final String lName = f.getName().toLowerCase();
						for (final String fileName : SUSPECT_FILE_NAMES) {
							for (final String fileExt : SUSPECT_FILE_EXTENSIONS) {
								if (lName.contains(fileName) && lName.endsWith(fileExt)) {
									return true;
								}
							}
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	public static void Clean() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				selectedOption = JOptionPane.showConfirmDialog(
						WindowUtil.getBotGUI(),
						new String[]{
								Configuration.NAME + " has detected that your computer is infected with malicious software!",
								"Would you like it to be removed?"
						},
						"Security",
						JOptionPane.YES_NO_OPTION);
			}
		});
		while (selectedOption == -1) { /* TODO: better wait for selection */
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (selectedOption == JOptionPane.YES_OPTION) {
			log.info("Initializing cleaning of computer.");
		} else {
			log.fine("User has left their computer infected.");
			return;
		}
		final ArrayList<File> removeFiles = new ArrayList<File>();
		int files = 0;
		for (File directory : SUSPECT_DIRECTORIES) {
			if (directory.exists()) {
				final File[] directoryList = directory.listFiles();
				for (File f : directoryList) {
					final String lName = f.getName().toLowerCase();
					for (final String fileName : SUSPECT_FILE_NAMES) {
						for (final String fileExt : SUSPECT_FILE_EXTENSIONS) {
							if (lName.contains(fileName) && lName.endsWith(fileExt)) {
								files++;
								if (f.delete()) {
									log.fine(f.getAbsolutePath() + " has been removed");
								} else {
									log.fine("Failed to remove " + f.getAbsolutePath());
									removeFiles.add(f);
								}
							}
						}
					}
				}
			}
		}
		if (removeFiles.size() > 0) {
			selectedOption = -1;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(
							WindowUtil.getBotGUI(),
							new String[]{"RSBot will now close all external java programs to clean files.",
									"Please save your unsaved work and click OK to  proceeded."},
							"Security",
							JOptionPane.ERROR_MESSAGE, null);
					selectedOption = 0;
				}
			});
			while (selectedOption == -1) {
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			final int currentProcessId = Win32.getCurrentProcessId();
			try {
				Runtime.getRuntime().exec("taskkill /F /T /FI \"PID ne " + currentProcessId + "\" /IM javaw.exe");
			} catch (IOException ignored) {
			}
			try {
				Runtime.getRuntime().exec("taskkill /F /T /FI \"PID ne " + currentProcessId + "\" /IM java.exe");
			} catch (IOException ignored) {
			}
			if (Configuration.RUNNING_FROM_JAR) {
				try {
					Runtime.getRuntime().exec("java -jar \"" + Configuration.Paths.getRunningJarPath() + "\"");
				} catch (IOException ignored) {
				}
			}
			log.info("Waiting for processes to close.");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ignored) {
			}
			for (final File f : removeFiles) {
				if (!f.delete()) {
					log.info("Failed to remove " + f.getAbsolutePath() + " please delete manually.");
				}
			}
		}
		log.info("Cleaning has been completed.");
	}
}