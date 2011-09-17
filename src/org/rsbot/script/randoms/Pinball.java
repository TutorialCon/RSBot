package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSObject;

@ScriptManifest(authors = {"Iscream", "Aelin", "LM3", "IceCandle", "Taha"}, name = "Pinball", version = 2.7)
public class Pinball extends Random {

	private static final int[] INACTIVE_PILLARS = {15001, 15003, 15005, 15007, 15009};
	private static final int[] ACTIVE_PILLARS = {15000, 15002, 15004, 15006, 15008};
	private static final int INTERFACE_PINBALL = 263;

	@Override
	public boolean activateCondition() {
		return game.isLoggedIn() && objects.getNearest(INACTIVE_PILLARS) != null;
	}

	private int getScore() {
		final RSComponent score = interfaces.get(INTERFACE_PINBALL).getComponent(1);
		try {
			return Integer.parseInt(score.getText().split(" ")[1]);
		} catch (final java.lang.ArrayIndexOutOfBoundsException t) {
			return -1;
		}
	}

	@Override
	public int loop() {
		if (!activateCondition()) {
			return -1;
		}
		if (getMyPlayer().isMoving() || getMyPlayer().getAnimation() != -1) {
			return random(300, 500);
		}
		if (getScore() >= 10) {
			final RSObject exit = objects.getNearest(15010);
			if (exit != null) {
				if (calc.tileOnScreen(exit.getLocation())) {
					if (exit.interact("Exit")) {
						return random(4000, 4200);
					}
				} else {
					camera.setCompass('s');
					walking.walkTileOnScreen(exit.getLocation());
					return random(1400, 1500);
				}
			}
		}
		final RSObject pillar = objects.getNearest(ACTIVE_PILLARS);
		if (pillar != null) {
			if (calc.distanceTo(pillar) > 2 && !pillar.isOnScreen()) {
				walking.walkTileOnScreen(pillar.getLocation());
				return random(500, 600);
			}
			if (pillar.interact("Tag")) {
				final int before = getScore();
				for (int i = 0; i < 50; i++) {
					if (getScore() > before) {
						return random(50, 100);
					}
					sleep(70, 100);
				}
			}
		}
		return random(50, 100);
	}
}