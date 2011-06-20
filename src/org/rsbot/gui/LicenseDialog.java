package org.rsbot.gui;

import org.rsbot.Configuration;
import org.rsbot.util.io.IOHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class LicenseDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 6626607972871221956L;
	private boolean accepted = false;

	private LicenseDialog(final Frame owner) {
		super(owner, owner != null);
		try {
			UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
			JDialog.setDefaultLookAndFeelDecorated(true);
		} catch (final Exception ignored) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (final Exception ignored2) {
			}
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle(Configuration.NAME + " License");
		setIconImage(Configuration.getImage(Configuration.Paths.Resources.ICON));

		String license = "Could not find license file, please visit http://www.gnu.org/licenses/gpl.html";
		try {
			license = IOHelper.readString(Configuration.getResourceURL(Configuration.Paths.Resources.LICENSE));
		} catch (final IOException ignored) {
		}

		final JTextArea licenseText = new JTextArea(license);
		licenseText.setFont(new Font(Font.SANS_SERIF, licenseText.getFont().getStyle(), (int) (licenseText.getFont().getSize() * 0.8)));
		licenseText.setColumns(48);
		licenseText.setRows(15);
		licenseText.setMargin(new Insets(4, 4, 4, 4));
		licenseText.setEditable(false);
		final JScrollPane scroll = new JScrollPane(licenseText);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		final JButton accept = new JButton("Accept"), decline = new JButton("Decline");
		accept.addActionListener(this);
		decline.addActionListener(this);

		final JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bar.add(accept);
		bar.add(decline);

		if (owner != null) {
			decline.setEnabled(false);
		}

		add(scroll);
		add(bar, BorderLayout.SOUTH);

		setResizable(false);
		pack();
		if (owner == null) {
			setAlwaysOnTop(true);
		}
		setLocationRelativeTo(getOwner());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				decline.doClick();
			}
		});
	}

	public static boolean showDialog(final Frame frame) {
		final LicenseDialog instance = new LicenseDialog(frame);
		instance.setModal(true);
		instance.setVisible(true);
		return instance.accepted;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final String text = arg0.getActionCommand();
		if (text.equals("Accept")) {
			accepted = true;
		}
		dispose();
	}
}
