package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;

@ScriptManifest(authors = {"Keilgo"}, name = "DrillDemon", version = 0.2)
public class DrillDemon extends Random {

	private final static int demonID = 2790;
	private final static int[] signs = {-1, -1, -1, -1};
	private final static String[] exercises = {"jumps", "push ups", "sit ups", "jog"};
	private final static int[][] settingArrays = {
			{668, 1, 2, 3, 4}, {675, 2, 1, 3, 4}, {724, 1, 3, 2, 4},
			{738, 3, 1, 2, 4}, {787, 2, 3, 1, 4}, {794, 3, 2, 1, 4},
			{1116, 1, 2, 4, 3}, {1123, 2, 1, 4, 3}, {1128, 1, 4, 2, 3},
			{1249, 4, 1, 2, 3}, {1291, 2, 4, 1, 3}, {1305, 4, 2, 1, 3},
			{1620, 1, 3, 4, 2}, {1634, 3, 1, 4, 2}, {1676, 1, 4, 3, 2},
			{1697, 4, 1, 3, 2}, {1802, 3, 4, 1, 2}, {1809, 4, 3, 1, 2},
			{2131, 2, 3, 4, 1}, {2138, 3, 2, 4, 1}, {2187, 2, 4, 3, 1},
			{2201, 4, 2, 3, 1}, {2250, 3, 4, 2, 1}, {2257, 4, 3, 2, 1}
	};

	@Override
	public void onFinish() {
		for (int i = 0; i < signs.length; i++) {
			signs[i] = -1;
		}
	}

	@Override
	public boolean activateCondition() {
		return new RSArea(3159, 4818, 3167, 4822).contains(getMyPlayer().getLocation());
	}

	@Override
	public int loop() {
		camera.setPitch(true);
		camera.setCompass('N');
		final RSNPC demon = npcs.getNearest(demonID);
		final RSObject mat1 = objects.getNearest(10076);
		final RSObject mat2 = objects.getNearest(10077);
		final RSObject mat3 = objects.getNearest(10078);
		final RSObject mat4 = objects.getNearest(10079);
		final RSObject[] mats = {mat1, mat2, mat3, mat4};
		if (getMyPlayer().isMoving() || getMyPlayer().getAnimation() != -1) {
			return random(2000, 2400);
		}
		if (demon == null) {
			return -1;
		}
		if (interfaces.clickContinue()) {
			return random(1000, 1500);
		}
		if (interfaces.get(148).isValid()) {
			final int compare = settings.getSetting(531);
			for (final int[] settingArray : settingArrays) {
				if (settingArray[0] == compare) {
					signs[0] = settingArray[1];
					signs[1] = settingArray[2];
					signs[2] = settingArray[3];
					signs[3] = settingArray[4];
					break;
				}
			}
		}
		for (int i = 0; i < exercises.length; i++) {
			if (interfaces.getComponent(148, 1).getText().contains(exercises[i])) {
				if (findAndUseMat((i + 1), mats)) {
					return random(3500, 4500);
				}
			}
		}
		if (!interfaces.clickContinue() && getMyPlayer().getAnimation() == -1) {
			demon.interact("Talk-to");
		}
		return random(2000, 2500);
	}

	public boolean findAndUseMat(final int signID, final RSObject[] mats) {
		for (int i = 0; i < signs.length; i++) {
			if (signs[i] == signID) {
				if (calc.distanceTo(mats[i].getLocation()) > 5) {
					walking.walkTileMM(mats[i].getLocation());
				} else {
					if (getMyPlayer().getAnimation() == -1) {
						if (mats[i].interact("Use")) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}