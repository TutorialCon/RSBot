package org.rsbot.gui.component;

import org.rsbot.Configuration;
import org.rsbot.script.methods.Environment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Paris
 */
public class BotToolBar extends JToolBar {
	private static final long serialVersionUID = -1861866523519184211L;

	public static final int RUN_SCRIPT = 0;
	public static final int PAUSE_SCRIPT = 1;
	public static final int RESUME_SCRIPT = 2;

	private static final ImageIcon ICON_HOME;
	private static final ImageIcon ICON_BOT;

	private static Image IMAGE_CLOSE;
	private static final Image IMAGE_CLOSE_OVER;

	private static final int TAB_INDEX = 1;
	private static final int BUTTON_COUNT = 7;
	private static final int OPTION_BUTTONS = 4;

	static {
		ICON_HOME = new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_HOME));
		ICON_BOT = new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_BOT));
		IMAGE_CLOSE = Configuration.getImage(Configuration.Paths.Resources.ICON_CLOSE_SEMI);
		IMAGE_CLOSE_OVER = Configuration.getImage(Configuration.Paths.Resources.ICON_CLOSE);
	}

	private final JButton addTabButton;
	private final JButton screenshotButton;
	private final JButton userInputButton;
	public final JButton runScriptButton;
	private final JButton stopScriptButton;
	private final JButton toggleLogButton;

	private final ActionListener listener;
	private int idx;
	private int inputState = Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE;
	private boolean inputOverride = true;

	public BotToolBar(final ActionListener listener, final BotMenuBar menu) {
		this.listener = listener;

		screenshotButton = new JButton(Messages.SAVESCREENSHOT, new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_PHOTO)));
		screenshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				menu.doClick(Messages.SAVESCREENSHOT);
			}
		});
		screenshotButton.setFocusable(false);
		screenshotButton.setToolTipText(screenshotButton.getText());
		screenshotButton.setText("");

		stopScriptButton = new JButton(Messages.STOPSCRIPT, new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_DELETE)));
		stopScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menu.doClick(Messages.STOPSCRIPT);
			}
		});
		stopScriptButton.setFocusable(false);
		stopScriptButton.setToolTipText(stopScriptButton.getText());
		stopScriptButton.setText("");

		userInputButton = new JButton(Messages.FORCEINPUT, new ImageIcon(getInputImage(inputOverride, inputState)));
		userInputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				menu.doTick(Messages.FORCEINPUT);
			}
		});
		userInputButton.setFocusable(false);
		userInputButton.setToolTipText(userInputButton.getText());
		userInputButton.setText("");

		runScriptButton = new JButton(Messages.RUNSCRIPT, new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_PLAY)));
		runScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (getScriptButton()) {
					case RUN_SCRIPT:
						menu.doClick(Messages.RUNSCRIPT);
						break;
					case RESUME_SCRIPT:
					case PAUSE_SCRIPT:
						menu.doClick(Messages.PAUSESCRIPT);
						break;
				}
			}
		});
		runScriptButton.setFocusable(false);
		runScriptButton.setToolTipText(runScriptButton.getText());
		runScriptButton.setText("");

		toggleLogButton = new JButton(Messages.HIDELOGPANE, new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_APPGET)));
		toggleLogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				toggleLogButton.setIcon(new ImageIcon(Configuration.getImage(menu.isTicked(Messages.HIDELOGPANE) ?
						Configuration.Paths.Resources.ICON_APPGET : Configuration.Paths.Resources.ICON_APPPUT)));
				menu.doTick(Messages.HIDELOGPANE);
			}
		});
		toggleLogButton.setFocusable(false);
		toggleLogButton.setToolTipText(toggleLogButton.getText());
		toggleLogButton.setText("");

		final JButton home = new JButton("", ICON_HOME);
		home.setFocusable(false);
		home.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setSelection(getComponentIndex(home));
			}
		});

		addTabButton = new JButton("", new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_ADD_SEMI)));
		addTabButton.setToolTipText(Messages.NEWBOT);
		addTabButton.setPressedIcon(new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_ADD)));
		addTabButton.setFocusable(false);
		addTabButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				listener.actionPerformed(new ActionEvent(this, e.getID(), Messages.FILE + "." + Messages.NEWBOT));
			}
		});

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setFloatable(false);
		add(home);
		add(addTabButton);
		add(Box.createHorizontalGlue());
		add(screenshotButton);
		add(toggleLogButton);
		add(runScriptButton);
		add(stopScriptButton);
		add(userInputButton);
		updateSelection(false);
	}

	public void setAddTabVisible(final boolean visible) {
		addTabButton.setVisible(visible);
	}

	public void setInputButtonVisible(final boolean visible) {
		userInputButton.setVisible(visible);
	}

	public void toggleLogPane() {
		toggleLogButton.doClick();
	}

	public void addTab() {
		final int idx = getComponentCount() - BUTTON_COUNT - TAB_INDEX + 1;
		add(new BotButton(Messages.TABDEFAULTTEXT, ICON_BOT), idx);
		validate();
		setSelection(idx);
	}

	public void removeTab(int idx) {
		final int current = getCurrentTab() + TAB_INDEX;
		final int select = idx == current ? idx - TAB_INDEX : current;
		idx += TAB_INDEX;
		remove(idx);
		revalidate();
		repaint();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setSelection(Math.max(0, select - 1));
			}
		});
	}

	public void setTabLabel(final int idx, final String label) {
		final Component c = getComponentAtIndex(idx + TAB_INDEX);
		if (c instanceof BotButton) {
			((BotButton) c).setText(label);
		}
	}

	public int getCurrentTab() {
		if (idx > -1 && idx < getComponentCount() - OPTION_BUTTONS) {
			return idx - TAB_INDEX;
		} else {
			return -1;
		}
	}

	int getScriptButton() {
		final String label = runScriptButton.getToolTipText();
		if (label.equals(Messages.RUNSCRIPT)) {
			return RUN_SCRIPT;
		} else if (label.equals(Messages.PAUSESCRIPT)) {
			return PAUSE_SCRIPT;
		} else if (label.equals(Messages.RESUMESCRIPT)) {
			return RESUME_SCRIPT;
		} else {
			throw new IllegalStateException("Illegal script button state!");
		}
	}

	public void setHome(final boolean home) {
		for (final JButton button : new JButton[]{screenshotButton, stopScriptButton, userInputButton, runScriptButton}) {
			button.setEnabled(!home);
			button.setVisible(!home);
		}
	}

	public void setInputState(final int state) {
		inputState = state;
	}

	public void setOverrideInput(final boolean selected) {
		inputOverride = selected;
	}

	public void updateInputButton() {
		userInputButton.setIcon(new ImageIcon(getInputImage(inputOverride, inputState)));
	}

	public void setScriptButton(final int state) {
		String text = null, pathResource = null;
		boolean running = true;

		switch (state) {
			case RUN_SCRIPT:
				text = Messages.RUNSCRIPT;
				pathResource = Configuration.Paths.Resources.ICON_PLAY;
				running = false;
				break;
			case PAUSE_SCRIPT:
				text = Messages.PAUSESCRIPT;
				pathResource = Configuration.Paths.Resources.ICON_PAUSE;
				break;
			case RESUME_SCRIPT:
				text = Messages.RESUMESCRIPT;
				pathResource = Configuration.Paths.Resources.ICON_START;
				break;
		}

		stopScriptButton.setVisible(running);
		runScriptButton.setToolTipText(text);
		runScriptButton.setIcon(new ImageIcon(Configuration.getImage(pathResource)));
		runScriptButton.repaint();
		revalidate();
	}

	private void setSelection(final int idx) {
		updateSelection(true);
		this.idx = idx;
		updateSelection(false);
		listener.actionPerformed(new ActionEvent(this, 0, "Tab"));
	}

	private void updateSelection(final boolean enabled) {
		final int idx = getCurrentTab() + TAB_INDEX;
		if (idx >= 0) {
			getComponent(idx).setEnabled(enabled);
			getComponent(idx).repaint();
		}
	}

	private Image getInputImage(final boolean override, final int state) {
		if (override || state == (Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE)) {
			return Configuration.getImage(Configuration.Paths.Resources.ICON_TICK);
		} else if (state == Environment.INPUT_KEYBOARD) {
			return Configuration.getImage(Configuration.Paths.Resources.ICON_KEYBOARD);
		} else if (state == Environment.INPUT_MOUSE) {
			return Configuration.getImage(Configuration.Paths.Resources.ICON_MOUSE);
		} else {
			return Configuration.getImage(Configuration.Paths.Resources.ICON_DELETE);
		}
	}

	/**
	 * @author Tekk
	 */
	private class BotButton extends JPanel {
		private static final long serialVersionUID = 329845763420L;

		private final JLabel nameLabel;
		private boolean hovered;
		private boolean close;

		public BotButton(final String text, final Icon icon) {
			super(new BorderLayout());
			setBorder(new EmptyBorder(3, 6, 2, 3));
			nameLabel = new JLabel(text);
			nameLabel.setIcon(icon);
			nameLabel.setPreferredSize(new Dimension(85, 22));
			nameLabel.setMaximumSize(new Dimension(85, 22));
			add(nameLabel, BorderLayout.WEST);

			setPreferredSize(new Dimension(110, 22));
			setMaximumSize(new Dimension(110, 22));
			setFocusable(false);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(final MouseEvent e) {
					if (hovered && close) {
						final int idx = getComponentIndex(BotButton.this) - TAB_INDEX;
						listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Messages.CLOSEBOT + "." + idx));
					} else {
						setSelection(getComponentIndex(BotButton.this));
					}
				}

				@Override
				public void mouseEntered(final MouseEvent e) {
					hovered = true;
					repaint();
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					hovered = false;
					repaint();
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(final MouseEvent e) {
					close = e.getX() > 95;
					repaint();
				}
			});
		}

		public void setText(final String label) {
			nameLabel.setText(label);
		}

		@Override
		public void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final boolean selected = getComponentIndex(this) == idx;
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(selected ? Color.GRAY : (Configuration.isSkinAvailable() ? Color.DARK_GRAY : new Color(0xcc, 0xcc, 0xcc)));
			g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 1, 4, 4);
			g.drawImage(hovered && close ? IMAGE_CLOSE_OVER : IMAGE_CLOSE, 90, 3, null);
		}
	}
}
