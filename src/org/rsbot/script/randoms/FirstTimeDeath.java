package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSObject;

@ScriptManifest(authors = {"Poxer"}, name = "FirstTimeDeath", version = 1.0)
public class FirstTimeDeath extends Random {

	final int portalID = 45803;
	boolean exit = false;
	private RSObject reaperChair;

	@Override
	public boolean activateCondition() {
		return (reaperChair = objects.getNearest(45802)) != null;
	}

	@Override
	protected int loop() {
		if (!activateCondition()) {
			return -1;
		} else {
			camera.setPitch(true);
			if (!exit && calc.distanceTo(reaperChair) > 4) {
				walking.walkTileOnScreen(reaperChair.getLocation());
				return random(1000, 1300);
			} else if (!exit && calc.distanceTo(reaperChair) < 4) {
				reaperChair.interact("Talk-to");
				return random(1200, 1500);
			} else if (interfaces.canContinue()) {
				interfaces.clickContinue();
				return random(400, 650);
			} else if (interfaces.getComponent(236, 2).getText().contains("No")) {
				exit = interfaces.getComponent(236, 2).doClick();
				return random(400, 650);
			} else if (exit) {
				final RSObject portal = objects.getNearest(portalID);
				camera.turnTo(portal);
				if (calc.distanceTo(portal) < 4) {
					portal.interact("Enter");
					return random(1000, 1300);
				} else {
					walking.walkTileOnScreen(portal.getLocation());
					return random(1000, 1300);
				}
			}
		}

		return 0;
	}

	public void onFinish() {
		exit = false;
		reaperChair = null;
	}

}