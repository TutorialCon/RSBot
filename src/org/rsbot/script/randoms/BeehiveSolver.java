package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;

import java.awt.*;

/**
 * Update by Iscream (Apr 24,2010)
 * Update by Iscream (Apr 15,2010)
 * Update by Arbiter (Sep 25,2010)
 *
 * @author Pwnaz0r & Velocity
 * @version 2.3 - 04/03/09
 */
@ScriptManifest(authors = {"Pwnaz0r", "Velocity"}, name = "BeeHive", version = 2.5)
public class BeehiveSolver extends Random {

	private static RSNPC beeKeeper;
	private static boolean solved;
	private static final int BEEHIVE_KEEPER_ID = 8649;
	private static final int INTERFACE_BEEHIVE_WINDOW = 420;
	private static final int BUILD_BEEHIVE = 40;
	private static final int CLOSE_WINDOW = 38;
	private static final int LID = 8, UP_MID = 9, LOW_MID = 10, LEGS = 11;
	private static final int LOWERMID = 16022, UPPERMID = 16025, BOTTOM = 16034, TOP = 16036;
	private static final int[][] MODELS = //ModelID, Component for un-built hive, Hive slot
			{{TOP, -1, LID},
			{UPPERMID, -1, UP_MID},
			{LOWERMID, -1, LOW_MID},
			{BOTTOM, -1, LEGS}};

	@Override
	public boolean activateCondition() {
		return npcs.getNearest(BEEHIVE_KEEPER_ID) != null && objects.getNearest(16168) != null && game.isLoggedIn();
	}

	@Override
	public int loop() {
		if ((beeKeeper = npcs.getNearest(BEEHIVE_KEEPER_ID)) == null) {
			return -1;
		}
		if (clickContinue()) {
			return 200;
		}
		if (interfaces.getComponent(236, 2).doClick()) {
			return random(800, 1200);
		}
		if (hiveIFace().isValid()) {
			findHivePieces();
			for (int i = 0; i < 4; i++) {
				dragInterfaces(hiveIFace().getComponent(MODELS[i][1]),
						hiveIFace().getComponent(MODELS[i][2]));
				sleep(200);
			}
			sleep(1500);
			//Wait is necessary for delay in the change of a setting.
			solved = settings.getSetting(805) == 109907968;
			if (solved) {
				log("All bee pieces have been place, now finishing random");
				if (hiveIFace().getComponent(BUILD_BEEHIVE).doClick()) {
					return random(900, 1600);
				}
			} else {
				closeHive();
				return random(500, 1000);
			}
		} else {
			log.info("Interfaces not valid.");
		}
		if (getMyPlayer().getInteracting() == null && !solved) {
			if (beeKeeper != null) {
				if (!beeKeeper.interact("Talk-to")) {
					sleep(400);
				}
			}
		}
		return random(500, 1000);
	}

	@Override
	public void onFinish() {
		beeKeeper = null;
	}

	private boolean clickContinue() {
		sleep(random(800, 1000));
		return interfaces.clickContinue();
	}

	private void dragInterfaces(final RSComponent child1, final RSComponent child2) {
		Point c2C = child2.getCenter();
		mouse.move(child1.getCenter(), 5, 5);
		mouse.drag(new Point(random(c2C.x - 25, c2C.x + 25), random(c2C.y - 10, c2C.y + 11)));
	}

	private RSInterface hiveIFace() {
		return interfaces.get(INTERFACE_BEEHIVE_WINDOW);
	}

	private void findHivePieces() {
		for (RSComponent comp : hiveIFace().getComponents()) {
			if (comp.getIndex() < 30) {
				for (int i = 0; i < MODELS.length; i++) {
					if (comp.getModelID() == MODELS[i][0]) {
						MODELS[i][1] = comp.getIndex();
						break;
					}
				}
			}
		}
	}

	private boolean closeHive() {
		return interfaces.getComponent(INTERFACE_BEEHIVE_WINDOW, CLOSE_WINDOW).doClick();
	}
}
