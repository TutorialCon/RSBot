package org.rsbot;

import org.rsbot.bot.Bot;
import org.rsbot.gui.BotGUI;
import org.rsbot.gui.LicenseDialog;
import org.rsbot.gui.LoadScreen;
import org.rsbot.util.io.IOHelper;

import java.awt.*;
import java.io.File;

public class Application {
	private static BotGUI gui;
	final static File licenseFile = new File(Configuration.Paths.getLicenseAcceptance());

	public static void main(final String[] args) {
		if (!isLicenseAccepted()) {
			if (!LicenseDialog.showDialog(null)) {
				return;
			}
		}
		IOHelper.write(Integer.toString(Configuration.getVersion()), licenseFile);
		final LoadScreen loader = new LoadScreen();
		if (!loader.error) {
			gui = new BotGUI();
			loader.dispose();
			gui.setVisible(true);
		}
	}

	private static boolean isLicenseAccepted() {
		if (!licenseFile.exists() || !licenseFile.canRead()) {
			return false;
		}
		final int version;
		try {
			version = Integer.parseInt(IOHelper.readString(licenseFile));
		} catch (final Exception ignored) {
			return false;
		}
		return Configuration.getVersion() == version;
	}

	public static Bot getBot(final Object o) {
		return gui.getBot(o);
	}

	public static Dimension getPanelSize() {
		return gui.getPanel().getSize();
	}
}
