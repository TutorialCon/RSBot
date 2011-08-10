package org.rsbot.security;

import org.rsbot.Configuration;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.util.Win32;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Paris
 */
public class InfectionScanner implements Runnable {
	private static final Logger log = Logger.getLogger(InfectionScanner.class.getName());
	private final static String[] SUSPECT_PROCESSNAMES = {"javaw.exe", "java.exe"};
	private final static String[] SAFE_FILENAMES = {"jagex_runescape_preferences.dat", "jagex_runescape_preferences2.dat"};
	private final static String[] SUSPECT_FILENAMES = {"jagex", "runescape", "casper", "gh0st"};
	int selectedOption;
	List<File> suspectFiles;

	public void run() {
		if (Configuration.getCurrentOperatingSystem() != Configuration.OperatingSystem.WINDOWS) {
			return;
		}

		/* Prevent infection from Evil-Java RSStealerClient */
		final File evilJavaStealerMutex = new File(System.getProperty("user.home"), "rss.lock");
		try {
			evilJavaStealerMutex.createNewFile();
		} catch (final IOException ignored) {
		}

		if (isInfected() && userConfirmedRemoval()) {
			terminateProcesses();
			try {
				Thread.sleep(2500);
			} catch (InterruptedException ignored) {
			}
			removeSuspectFiles();
		}
	}

	private boolean isInfected() {
		scanFiles();
		return suspectFiles != null && suspectFiles.size() != 0;
	}

	private boolean userConfirmedRemoval() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					selectedOption = JOptionPane.showConfirmDialog(WindowUtil.getBotGUI(), new String[]{
							"Malicious software has been detected on your computer.",
							"Would you like to perform an automatic virus removal?"}, "Security", JOptionPane.YES_NO_OPTION);
				}
			});
		} catch (final InterruptedException ignored) {
		} catch (final InvocationTargetException ignored) {
		}
		return selectedOption == JOptionPane.YES_OPTION;
	}

	private void scanFiles() {
		final String startup = "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
		final String[] paths = {
				System.getenv("APPDATA"),
				System.getenv("APPDATA") + startup,
				System.getenv("ProgramData") + startup,
				Configuration.Paths.getUnixHome(),
		};
		suspectFiles = new ArrayList<File>();
		for (final String name : paths) {
			final File dir = new File(name);
			if (!dir.isDirectory()) {
				continue;
			}
			for (final File file : dir.listFiles()) {
				if (isFileSuspect(file)) {
					suspectFiles.add(file);
				}
			}
		}
	}

	private boolean isFileSuspect(final File file) {
		if (new File(Configuration.Paths.getAccountsFile()).getAbsolutePath().equals(file.getAbsolutePath()) || !file.isFile()) {
			return false;
		}
		for (final String check : SUSPECT_FILENAMES) {
			if (file.getName().toLowerCase().contains(check) && !isFileSafe(file)) {
				return true;
			}
		}
		return false;
	}

	private boolean isFileSafe(final File file) {
		for (final String check : SAFE_FILENAMES) {
			if (file.getName().equalsIgnoreCase(check)) {
				return true;
			}
		}
		return false;
	}

	private void removeSuspectFiles() {
		if (suspectFiles == null) {
			return;
		}
		for (final File item : suspectFiles) {
			if (!item.delete()) {
				log.warning("Unable to delete " + item.getName());
				item.deleteOnExit();
			} else {
				log.info("Deleted " + item.getName());
			}
		}
	}

	private void terminateProcesses() {
		final int p = Win32.getCurrentProcessId();
		for (final int pid : Win32.EnumProcesses()) {
			if (pid == 0 || pid == p) {
				continue;
			}
			final String name = Win32.QueryFullProcessImageName(pid);
			if (name == null) {
				continue;
			}
			for (final String check : SUSPECT_PROCESSNAMES) {
				if (name.contains(check)) {
					Win32.TerminateProcess(pid);
					break;
				}
			}
		}
	}
}
