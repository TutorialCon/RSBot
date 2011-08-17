package org.rsbot;

import org.rsbot.bot.Bot;
import org.rsbot.gui.BotGUI;
import org.rsbot.gui.LoadScreen;
import org.rsbot.util.io.IOHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Application {
	private static BotGUI gui;
	final static File licenseFile = new File(Configuration.Paths.getLicenseAcceptance());

	public static void main(final String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
		IOHelper.write(Integer.toString(Configuration.getVersion()), licenseFile);
		if (LoadScreen.showDialog()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					gui = new BotGUI();
				}
			});
		}
	}

	public static BotGUI getUI() {
		return gui;
	}

	public static Bot getBot(final Object o) {
		return gui.getBot(o);
	}

	public static Dimension getPanelSize() {
		return gui.getPanel().getSize();
	}
}
