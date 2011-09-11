package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = {"PwnZ", "Taha", "Zenzie"}, name = "Mime", version = 1.3)
public class Mime extends Random {

	private enum Stage {
		click, findMime, findAnimation, clickAnimation, wait
	}

	private static int animation;
	private static RSNPC mime;
	private final static int[] animations = {857, 860, 861, 866, 1128, 1129, 1130, 1131};
	private final static String[] names = {"Think", "Cry", "Laugh", "Dance", "Glass Wall",
			"Lean", "Rope", "Glass Box"};

	@Override
	public void onFinish() {
		mime = null;
		animation = -1;
	}

	@Override
	public boolean activateCondition() {
		final RSNPC mime = npcs.getNearest(1056);
		return mime != null && calc.distanceTo(mime.getLocation()) < 15;
	}

	private boolean clickAnimation(final String find) {
		if (!interfaces.get(188).isValid()) {
			return false;
		}
		for (int a = 0; a < interfaces.get(188).getChildCount(); a++) {
			if (interfaces.get(188).getComponent(a).getText().contains(find)) {
				log("Clicked on: " + find);
				sleep(500, 1000);
				interfaces.getComponent(188, a).doClick();
				sleep(1000, 1200);
				return true;
			}
		}
		return false;
	}

	private RSNPC getMimeFromTile() {
		for (final RSNPC npc : npcs.getAll()) {
			if (npc.getLocation().equals(new RSTile(2011, 4762))) {
				return npc;
			}
		}
		return null;
	}

	private Stage getStage() {
		if (interfaces.canContinue() && getMyPlayer().getLocation().equals(new RSTile(2008, 4764))) {
			return Stage.click;
		} else if (mime == null) {
			return Stage.findMime;
		} else if ((interfaces.get(372).getComponent(2).getText().contains("Watch") || interfaces.get(372).getComponent(
				3).getText().contains("Watch")) && mime.getAnimation() != -1 && mime.getAnimation() != 858) {
			return Stage.findAnimation;
		} else if (interfaces.get(188).isValid()) {
			return Stage.clickAnimation;
		} else {
			return Stage.wait;
		}
	}

	@Override
	public int loop() {
		if (!activateCondition()) {
			return -1;
		}
		switch (getStage()) {
			case click:
				interfaces.clickContinue();
				sleep(1500, 2000);
				return random(200, 400);

			case findMime:
				if ((mime = npcs.getNearest(1056)) == null && (mime = getMimeFromTile()) == null) {
					log.warning("ERROR: Mime not found!");
					return -1;
				}
				return random(200, 400);

			case findAnimation:
				animation = mime.getAnimation();
				log.info("Found Mime animation: " + animation);
				sleep(1000);
				if (interfaces.get(188).isValid()) {
					return random(4000, 4500);
				}
				final long start = System.currentTimeMillis();
				while (System.currentTimeMillis() - start >= 5000) {
					if (interfaces.get(188).isValid()) {
						return random(1000, 1600);
					}
					sleep(400, 500);
				}
				return random(700, 1000);

			case clickAnimation:
				if (animation != -1 && animation != 858) {
					for (int i = 0; i < animations.length; i++) {
						if (animations[i] == animation) {
							log.info("Clicking text according to animation: " + animation);
							clickAnimation(names[i]);
							return random(1200, 1800);
						}
					}
					log.info("Unknown Animation: " + animation + " Please inform a developer at powerbot.org!");
					return random(4000, 4500);
				}
			case wait:
				return random(200, 400);
		}
		return random(200, 400);
	}
}
