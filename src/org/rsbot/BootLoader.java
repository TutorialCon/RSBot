package org.rsbot;

import org.rsbot.Configuration;
import org.rsbot.log.LabelLogHandler;
import org.rsbot.log.LogOutputStream;
import org.rsbot.log.SystemConsoleHandler;
import org.rsbot.util.io.HttpClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

class BootLoader extends JDialog {
	private final static Logger log = Logger.getLogger(BootLoader.class.getName());
	private static BootLoader instance = null;
	private static final long serialVersionUID = 2L;
	private final boolean error;

	private BootLoader() {
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
		setTitle(Configuration.NAME + " loader");
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
		log.info("Beginning initialization of " + Configuration.NAME);
		add(panel);
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setVisible(true);
		setModal(true);
		setAlwaysOnTop(true);
		String error = null;
		try {
			log.fine("Loading resources");
			HttpClient.download(new URL(Configuration.Paths.URLs.JNA), new File(Configuration.Paths.getCacheDirectory(), "jna.jar"));
		} catch (Exception ignored) {
			progress.setIndeterminate(false);
			error = "Download failed.\n\nThe application failed to initialize because mandatory resources are missing from the cache directory.";
		}
		if (error != null) {
			this.error = true;
			log.fine(error);
			log.severe("Initialization failed, check your connection.");
		} else {
			this.error = false;
			log.info("Entering boot sequence.");
		}
	}

	public static boolean load() {
		instance = new BootLoader();
		return !instance.error;
	}

	public static void quit() {
		if (instance != null) {
			instance.dispose();
		}
	}

}
