package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSObject;

@ScriptManifest(authors = {"Swipe, Poxer"}, name = "GrimReaper", version = 1.1)
public class FirstTimeDeath extends Random {
	boolean talkDone;

	@Override
	public boolean activateCondition() {
		if (!game.isLoggedIn()) {
			return false;
		} else if (objects.getNearest(45802) != null && npcs.getNearest(8868) != null) {
			sleep(random(2000, 3000));
			return (objects.getNearest(45802) != null && npcs.getNearest(8868) != null);
		}
		return false;
	}

	private boolean canContinue() {
		return interfaces.canContinue() || interfaces.getComponent(65, 6).isValid();
	}

	@Override
	protected int loop() {
		if (!activateCondition()) {
			return -1;
		}
		if (canContinue()) {
			interfaces.clickContinue();
			sleep(random(200, 600));
		}
		if (!talkDone && !interfaces.canContinue()) {
			if (objects.getNearest(45802) != null) {
				camera.turnTo(objects.getNearest(45802));
				objects.getNearest(45802).doClick();
				sleep(random(500, 900));
			}
		}
		if (interfaces.getComponent(236, 2).getText().contains("No")) {
			interfaces.getComponent(236, 2).doClick();
			talkDone = true;
			return random(200, 650);
		} else if (talkDone) {//We have finished talking to reaper
			final RSObject portal = objects.getNearest(45803);//get portal
			camera.turnTo(portal);//turn
			if (calc.distanceTo(portal) <= 4) {
				portal.interact("Enter");//Leave
				return random(1200, 1400);
			} else {
				walking.walkTileMM(portal.getLocation()); //walk portal
				return random(1000, 1300);
			}
		}
		return random(200, 400);
	}
}
