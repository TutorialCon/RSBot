package org.rsbot.script.randoms;

import org.rsbot.Configuration;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Lobby;
import org.rsbot.script.wrappers.RSComponent;

import java.awt.*;

/**
 * A simple script to login to the game.
 *
 * @author Timer
 */
@ScriptManifest(authors = {"Timer"}, name = "Improved Login", version = 0.1)
public class ImprovedLoginBot extends Random {
	public static final int INTERFACE_LOGIN_SCREEN = 596;
	public static final int INTERFACE_LOGIN_SCREEN_ENTER_GAME = 60;
	public static final int INTERFACE_LOGIN_SCREEN_USERNAME_TEXT = 73;
	public static final int INTERFACE_LOGIN_SCREEN_PASSWORD_TEXT = 79;
	public static final int INTERFACE_LOGIN_SCREEN_ALERT_TEXT = 14;
	public static final int INTERFACE_LOGIN_SCREEN_ALERT_BACK = 68;
	public static final int INTERFACE_GRAPHICS_NOTICE = 976;
	public static final int INTERFACE_GRAPHICS_LEAVE_ALONE = 6;
	public static final int INTERFACE_LOBBY_HIGH_RISK_WORLD_TEXT = 98;
	public static final int INTERFACE_LOBBY_HIGH_RISK_WORLD_LOGIN_BUTTON = 104;
	private final solution[] loginSolutions = {new solution() {
		private int invalidCount = 0;

		public boolean canApply(String message) {
			return message.contains("no reply from login server");
		}

		public int apply() {
			if (invalidCount > 10) {
				log.severe("It seems the login server is down.");
				stopScript(false);
			}
			invalidCount++;
			return random(500, 2000);
		}
	}, new solution() {
		public boolean canApply(String message) {
			return message.contains("update");
		}

		public int apply() {
			log("The game has been updated, please reload " + Configuration.NAME);
			stopScript(false);
			return 0;
		}
	}, new solution() {
		public boolean canApply(String message) {
			return message.contains("disable");
		}

		public int apply() {
			log.severe("It seems that your account has been disabled, that's unfortunate.");
			stopScript(false);
			return 0;
		}
	}, new solution() {
		private int invalidCount = 0;

		public boolean canApply(String message) {
			return message.contains("your account has not logged out");
		}

		public int apply() {
			if (invalidCount > 10) {
				log.severe("Your account is already logged in, change your password!");
				stopScript(false);
			}
			invalidCount++;
			log.warning("Waiting for logout..");
			return random(5000, 15000);
		}
	}, new solution() {
		private int fails = 0;

		public boolean canApply(String message) {
			return message.contains("invalid") || message.contains("incorrect");
		}

		public int apply() {
			fails++;
			if (fails > 3) {
				stopScript(false);
				return 0;
			}
			log.info("Login information incorrect, attempting again.");
			return random(1200, 3000);
		}
	}, new solution() {
		public boolean canApply(String message) {
			return message.contains("error connecting");
		}

		public int apply() {
			log.severe("No internet connection.");
			stopScript(false);
			return 0;
		}
	}, new solution() {
		private int invalidCount = 0;

		public boolean canApply(String message) {
			return message.contains("login limit exceeded");
		}

		public int apply() {
			if (invalidCount > 10) {
				log.warning("Unable to login after 10 attempts. Stopping script.");
				log.severe("It seems you are actually already logged in?");
				stopScript(false);
			}
			invalidCount++;
			return random(5000, 15000);
		}
	}, new solution() {
		public boolean canApply(String message) {
			return message.contains("world") || message.contains("performing login");
		}

		public int apply() {
			return random(4000, 5000);
		}
	}};
	private final solution[] lobbySolutions = {new solution() {
		public boolean canApply(String message) {
			return message.contains("total skill level of");
		}

		public int apply() {
			log.severe("Your combat level does not meet this world's requirements, run a skilling bot!");
			stopScript(true);
			return 0;
		}
	}, new solution() {
		public boolean canApply(String message) {
			return message.contains("login limit exceeded");
		}

		public int apply() {
			return random(3000, 8000);
		}
	}, new solution() {
		private int invalidCount = 0;

		public boolean canApply(String message) {
			return message.contains("your account has not logged out");
		}

		public int apply() {
			if (invalidCount > 10) {
				log.severe("You're already logged in!  Change your password!");
				stopScript(false);
			}
			invalidCount++;
			log.warning("Waiting for logout..");
			return random(5000, 15000);
		}
	}, new solution() {
		public boolean canApply(String message) {
			return message.contains("member");
		}

		public int apply() {
			log.severe("Please run on a non-members world.");
			stopScript(false);
			return 0;
		}
	}, new solution() {
		public boolean canApply(String message) {
			return interfaces.getComponent(Lobby.INTERFACE_LOBBY, INTERFACE_LOBBY_HIGH_RISK_WORLD_TEXT).getText().toLowerCase().trim().contains("high-risk wilderness world");
		}

		public int apply() {
			interfaces.getComponent(Lobby.INTERFACE_LOBBY, INTERFACE_LOBBY_HIGH_RISK_WORLD_LOGIN_BUTTON).doClick();
			return 500;
		}
	}};

	private interface solution {
		boolean canApply(final String message);

		int apply();
	}

	@Override
	public boolean activateCondition() {
		final int idx = game.getClientState();
		return (idx == Game.INDEX_LOGIN_SCREEN || idx == Game.INDEX_LOBBY_SCREEN) && !switchingWorlds() && account.getName() != null;
	}

	@Override
	public int loop() {
		if (lobby.inLobby()) {
			if (lobby.getSelectedTab() != Lobby.TAB_PLAYERS) {
				lobby.open(Lobby.TAB_PLAYERS);
				return random(500, 800);
			}
			if (lobby.clickPlay()) {
				for (int i = 0; i < 4 && game.getClientState() == 6; i++) {
					sleep(500);
				}
				final String returnText = interfaces.getComponent(Lobby.INTERFACE_LOBBY, Lobby.INTERFACE_LOBBY_ALERT_TEXT).getText().toLowerCase().trim();
				if (interfaces.getComponent(Lobby.INTERFACE_LOBBY, Lobby.INTERFACE_LOBBY_ALERT_CLOSE).isValid()) {
					interfaces.getComponent(Lobby.INTERFACE_LOBBY, Lobby.INTERFACE_LOBBY_ALERT_CLOSE).doClick();
				}
				for (solution subSolution : lobbySolutions) {
					if (subSolution.canApply(returnText)) {
						return subSolution.apply();
					}
				}
			}
		}
		if (game.getClientState() == Game.INDEX_LOGIN_SCREEN) {
			if (interfaces.getComponent(INTERFACE_GRAPHICS_NOTICE, INTERFACE_GRAPHICS_LEAVE_ALONE).isValid()) {
				interfaces.getComponent(INTERFACE_GRAPHICS_NOTICE, INTERFACE_GRAPHICS_LEAVE_ALONE).doClick();
				return random(500, 600);
			}
			final String returnText = interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_ALERT_TEXT).getText().toLowerCase().trim();
			if (interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_ALERT_BACK).isValid()) {
				interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_ALERT_BACK).doClick();
			}
			for (solution subSolution : loginSolutions) {
				if (subSolution.canApply(returnText)) {
					return subSolution.apply();
				}
			}
			if (isUsernameCorrect() && isPasswordValid()) {
				attemptLogin();
				return random(1200, 1500);
			}
			if (!isUsernameCorrect()) {
				final String username = account.getName().toLowerCase().trim();
				atLoginInterface(interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_USERNAME_TEXT));
				sleep(random(500, 700));
				final int textLength = interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_USERNAME_TEXT).getText().length();
				if (textLength > 0) {
					for (int i = 0; i <= textLength + random(1, 5); i++) {
						keyboard.sendText("\b", false);
						if (random(0, 2) == 1) {
							sleep(random(25, 100));
						}
					}
					return random(500, 600);
				}
				keyboard.sendText(username, false);
				return random(500, 600);
			}
			if (isUsernameCorrect() && !isPasswordValid()) {
				atLoginInterface(interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_PASSWORD_TEXT));
				sleep(random(500, 700));
				final int textLength = interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_PASSWORD_TEXT).getText().length();
				if (textLength > 0) {
					for (int i = 0; i <= textLength + random(1, 5); i++) {
						keyboard.sendText("\b", false);
						if (random(0, 2) == 1) {
							sleep(random(25, 100));
						}
					}
					return random(500, 600);
				}
				String passWord = AccountManager.getPassword(account.getName());
				if (passWord.isEmpty()) {
					passWord = ctx.client.getCurrentPassword();
				}
				keyboard.sendText(passWord, false);
				return random(500, 600);
			}
		}
		return -1;
	}

	private boolean switchingWorlds() {
		return interfaces.getComponent(Lobby.INTERFACE_LOBBY, Lobby.INTERFACE_LOBBY_ALERT_TEXT).isValid() &&
				interfaces.getComponent(Lobby.INTERFACE_LOBBY, Lobby.INTERFACE_LOBBY_ALERT_TEXT).containsText("just left another world");
	}

	private boolean atLoginInterface(final RSComponent i) {
		if (!i.isValid()) {
			return false;
		}
		final Rectangle pos = i.getArea();
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
			return false;
		}
		final int dy = (int) (pos.getHeight() - 4) / 2;
		final int maxRandomX = (int) (pos.getMaxX() - pos.getCenterX());
		final int midx = (int) pos.getCenterX();
		final int midy = (int) (pos.getMinY() + pos.getHeight() / 2);
		if (i.getIndex() == INTERFACE_LOGIN_SCREEN_PASSWORD_TEXT) {
			mouse.click(minX(i), midy + random(-dy, dy), true);
		} else {
			mouse.click(midx + random(1, maxRandomX), midy + random(-dy, dy), true);
		}
		return true;
	}

	private int minX(final RSComponent a) {
		int x = 0;
		final Rectangle pos = a.getArea();
		final int dx = (int) (pos.getWidth() - 4) / 2;
		final int midx = (int) (pos.getMinX() + pos.getWidth() / 2);
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
			return 0;
		}
		for (int i = 0; i < interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_PASSWORD_TEXT).getText().length(); i++) {
			x += 11;
		}
		if (x > 44) {
			return (int) (pos.getMinX() + x + 15);
		} else {
			return midx + random(-dx, dx);
		}
	}

	private void attemptLogin() {
		if (random(0, 2) == 0) {
			keyboard.sendKey('\n');
		} else {
			interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_ENTER_GAME).doClick();
		}
	}

	private boolean isUsernameCorrect() {
		final String userName = account.getName().toLowerCase().trim();
		return interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_USERNAME_TEXT).getText().toLowerCase().equalsIgnoreCase(userName);
	}

	private boolean isPasswordValid() {
		String passWord = AccountManager.getPassword(account.getName());
		if (passWord.isEmpty()) {
			passWord = ctx.client.getCurrentPassword();
		}
		return interfaces.getComponent(INTERFACE_LOGIN_SCREEN, INTERFACE_LOGIN_SCREEN_PASSWORD_TEXT).getText().length() == (passWord == null ? 0 : passWord.length());
	}
}
