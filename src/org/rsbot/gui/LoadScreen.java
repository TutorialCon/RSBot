package org.rsbot.gui;

import org.rsbot.Configuration;
import org.rsbot.jna.win32.Kernel32;
import org.rsbot.loader.ClientLoader;
import org.rsbot.locale.Messages;
import org.rsbot.log.LabelLogHandler;
import org.rsbot.log.LogOutputStream;
import org.rsbot.log.SystemConsoleHandler;
import org.rsbot.script.provider.ScriptDeliveryNetwork;
import org.rsbot.security.RestrictedSecurityManager;
import org.rsbot.util.UpdateChecker;
import org.rsbot.util.Win32;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IOHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadScreen extends JDialog {
	private final static Logger log = Logger.getLogger(LoadScreen.class.getName());
	private static final long serialVersionUID = 5520543482560560389L;
	private final boolean error;
	private static LoadScreen instance = null;

	private LoadScreen() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(1);
			}
		});
		setTitle(Configuration.NAME);
		setIconImage(Configuration.getImage(Configuration.Paths.Resources.ICON));
		final JPanel panel = new JPanel(new GridLayout(2, 1));
		final int pad = 10;
		panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		final JProgressBar progress = new JProgressBar();
		progress.setPreferredSize(new Dimension(350, progress.getPreferredSize().height));
		progress.setIndeterminate(true);
		panel.add(progress);
		final LabelLogHandler handler = new LabelLogHandler();
		Logger.getLogger("").addHandler(handler);
		handler.label.setBorder(BorderFactory.createEmptyBorder(pad, 0, 0, 0));
		final Font font = handler.label.getFont();
		handler.label.setFont(new Font(font.getFamily(), Font.BOLD, font.getSize()));
		handler.label.setPreferredSize(new Dimension(progress.getWidth(), handler.label.getPreferredSize().height + pad));
		panel.add(handler.label);
		log.info("Loading");
		add(panel);
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setVisible(true);
		setModal(true);
		setAlwaysOnTop(true);
		final List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(8);

		log.info("Language: " + Messages.LANGUAGE);

		log.info("Registering logs");
		bootstrap();
		Win32.setProcessPriority(Kernel32.BELOW_NORMAL_PRIORITY_CLASS);

		log.info("Extracting resources");
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				try {
					extractResources();
				} catch (final IOException ignored) {
				}
			}
		}));

		log.fine("Extracting resources");
		tasks.add(Executors.callable(new Runnable() {
			public void run() {
				try {
					extractResources();
				} catch (final IOException ignored) {
				}
			}
		}));

		log.fine("Creating directories");
		Configuration.createDirectories();

		log.fine("Enforcing security policy");
		if (Configuration.GOOGLEDNS) {
			System.setProperty("sun.net.spi.nameservice.nameservers", RestrictedSecurityManager.DNSA + "," + RestrictedSecurityManager.DNSB);
			System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
		}
		System.setProperty("java.io.tmpdir", Configuration.Paths.getGarbageDirectory());
		System.setSecurityManager(new RestrictedSecurityManager());

		log.info("Downloading resources");
		for (final Entry<String, File> item : Configuration.Paths.getCachableResources().entrySet()) {
			tasks.add(Executors.callable(new Runnable() {
				@Override
				public void run() {
					try {
						HttpClient.download(new URL(item.getKey()), item.getValue());
					} catch (final IOException ignored) {
					}
				}
			}));
		}

		log.info("Downloading network scripts");
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				ScriptDeliveryNetwork.getInstance().sync();
			}
		}));

		log.info("Starting game client");
		tasks.add(Executors.callable(new Runnable() {
			@Override
			public void run() {
				try {
					ClientLoader.getInstance().setup();
				} catch (final Exception ignored) {
				}
			}
		}));

		if (Configuration.isSkinAvailable()) {
			log.fine("Setting theme");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						UIManager.setLookAndFeel(Configuration.SKIN);
					} catch (final Exception ignored) {
					}
				}
			});
		}

		log.info("Loading client");
		final ExecutorService pool = Executors.newCachedThreadPool();
		try {
			pool.invokeAll(tasks);
		} catch (final InterruptedException ignored) {
		}

		log.info("Checking for updates");

		String error = null;

		if (UpdateChecker.isError()) {
			error = "Unable to obtain latest version information";
		} else if (Configuration.RUNNING_FROM_JAR) {
			try {
				if (UpdateChecker.isDeprecatedVersion()) {
					error = "Please update at " + Configuration.Paths.URLs.DOWNLOAD_SHORT;
				}
			} catch (final IOException ignored) {
			}
		} else {
			error = null;
		}

		log.info("Checking for client updates");
		if (ClientLoader.getInstance().isOutdated()) {
			error = "Bot is outdated, please wait and try again later";
		}

		if (error == null) {
			this.error = false;
			log.info("Loading bot");
			Configuration.registerLogging();
			Logger.getLogger("").removeHandler(handler);
		} else {
			this.error = true;
			progress.setIndeterminate(false);
			log.severe(error);
		}
	}

	public static boolean showDialog() {
		instance = new LoadScreen();
		return !instance.error;
	}

	public static void quit() {
		if (instance != null) {
			instance.dispose();
		}
	}

	private static void bootstrap() {
		Logger.getLogger("").setLevel(Level.INFO);
		Logger.getLogger("").addHandler(new SystemConsoleHandler());
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			private final Logger log = Logger.getLogger("EXCEPTION");

			public void uncaughtException(final Thread t, final Throwable e) {
				final String ex = "Exception", msg = t.getName() + ": ";
				if (Configuration.RUNNING_FROM_JAR) {
					Logger.getLogger(ex).severe(msg + e.toString());
				} else {
					log.logp(Level.SEVERE, ex, "", msg, e);
				}
			}
		});
		if (!Configuration.RUNNING_FROM_JAR) {
			System.setErr(new PrintStream(new LogOutputStream(Logger.getLogger("STDERR"), Level.SEVERE), true));
		}
	}

	private static void extractResources() throws IOException {
		if (Configuration.RUNNING_FROM_JAR) {
			IOHelper.write(Configuration.Paths.getRunningJarPath(), new File(Configuration.Paths.getPathCache()));
		}
		final String[] extract;
		if (Configuration.getCurrentOperatingSystem() == Configuration.OperatingSystem.WINDOWS) {
			extract = new String[]{Configuration.Paths.Resources.COMPILE_SCRIPTS_BAT, Configuration.Paths.Resources.COMPILE_FIND_JDK};
		} else {
			extract = new String[]{Configuration.Paths.Resources.COMPILE_SCRIPTS_SH};
		}
		for (final String item : extract) {
			IOHelper.write(Configuration.getResourceURL(item).openStream(), new File(Configuration.Paths.getHomeDirectory(), new File(item).getName()));
		}
	}
}
