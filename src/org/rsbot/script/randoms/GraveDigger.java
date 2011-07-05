package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This short-sighted gravedigger has managed to put five coffins in the wrong
 * graves. <br />
 * If he'd looked more closely at the headstones, he might have known where each
 * one was supposed to go! <br />
 * Help him by matching the contents of each coffin with the headstones in the
 * graveyard. Easy, huh?
 * </p>
 * <p/>
 *
 * @author Timer
 */
@ScriptManifest(authors = {"Timer"}, name = "GraveDigger", version = 1.6)
public class GraveDigger extends Random {
	private static final int
			DEPOSIT_BOX = 12731,
			INTERFACE_GRAVE = 143,
			INTERFACE_GRAVE_ID = 2,
			INTERFACE_GRAVE_CLOSE = 3,

			INTERFACE_COFFIN = 141,
			INTERFACE_COFFIN_CLOSE = 12;
	private static final int[]
			INTERFACE_COFFIN_IDS = {3, 4, 5, 6, 7, 8, 9, 10, 11};
	private static final String NPC_NAME = "Leo";

	private static final int[]
			GRAVE_STONES = {12716, 12717, 12718, 12719, 12720},
			FILLED_GRAVES = {12721, 12722, 12723, 12724, 12725},
			EMPTY_GRAVES = {12726, 12727, 12728, 12729, 12730},
			COFFINS = {7587, 7588, 7589, 7590, 7591};

	private Coffin[] coffins = {
			new Coffin(7614, new int[]{7603, 7605, 7612}),//Woodcutter
			new Coffin(7615, new int[]{7600, 7601, 7604}),//Chef
			new Coffin(7616, new int[]{7597, 7606, 7607}),//Miner
			new Coffin(7617, new int[]{7602, 7609, 7610}),//Farmer
			new Coffin(7618, new int[]{7599, 7608, 7613})//Crafter
	};

	private boolean removedGraves = false, talkToNPC = false;

	@Override
	public void onFinish() {
		removedGraves = false;
		talkToNPC = false;
		for (final Coffin coffin : coffins) {//Rinse and reuse data.
			coffin.setCoffinID(-1);
			coffin.setStoneID(-1);
			coffin.set = false;
		}
	}

	@Override
	public boolean activateCondition() {
		return settings.getSetting(696) != 0 && objects.getNearest(DEPOSIT_BOX) != null;
	}

	@Override
	public int loop() {
		if (interfaces.get(INTERFACE_GRAVE).isValid()) {
			atCloseInterface(INTERFACE_GRAVE, INTERFACE_GRAVE_CLOSE);
			sleep(random(500, 800));
		}
		if (interfaces.get(INTERFACE_COFFIN).isValid()) {
			atCloseInterface(INTERFACE_COFFIN, INTERFACE_COFFIN_CLOSE);
			sleep(random(500, 800));
		}
		RSNPC theNPC;
		if ((theNPC = npcs.getNearest(NPC_NAME)) == null) {
			return -1;
		}
		if (interfaces.get(236).isValid()) {
			if (interfaces.getComponent(236, 2).getText().trim().contains("know")) {
				interfaces.getComponent(236, 2).doClick();
			} else {
				interfaces.getComponent(236, 1).doClick();
			}
		}
		if (interfaces.canContinue()) {
			final String text = interfaces.getComponent(242, 4).getText().toLowerCase().trim();
			interfaces.clickContinue();
			if (text.contains("empty") || text.contains("not")) {
				removedGraves = false;
				talkToNPC = false;
				for (final Coffin coffin : coffins) {//Rinse and reuse data.
					coffin.setCoffinID(-1);
					coffin.setStoneID(-1);
					coffin.set = false;
				}
			}
			return random(500, 1200);
		}
		if (talkToNPC) {
			getNPCInView(theNPC);
			return theNPC.interact("Talk-to") ? random(800, 2000) : 0;
		}
		if (interfaces.get(220).isValid()) {
			return atCloseInterface(220, 16) ? random(500, 800) : 0;
		}
		if (getMyPlayer().isMoving() || getMyPlayer().getAnimation() != -1) {
			return random(100, 200);
		}
		RSObject interactionObject;
		if (!removedGraves && (interactionObject = objects.getNearest(FILLED_GRAVES)) != null) {
			getObjectInView(interactionObject);
			interactionObject.interact("Take-coffin");
			return random(1200, 2000);
		} else {
			removedGraves = true;
		}
		int undecidedID;
		if ((undecidedID = getUndecidedGrave()) != -1) {
			final RSObject theGrave = objects.getNearest(undecidedID);
			getObjectInView(theGrave);
			if (theGrave.interact("Read")) {
				final long systemTime = System.currentTimeMillis();
				while (System.currentTimeMillis() - systemTime < 8000 && !interfaces.get(INTERFACE_GRAVE).isValid()) {
					sleep(random(50, 150));
				}
				sleep(random(1200, 2500));
				RSComponent inter;
				if ((inter = interfaces.getComponent(INTERFACE_GRAVE, INTERFACE_GRAVE_ID)) != null) {
					final int theID = inter.getComponentID();
					boolean found = false;
					for (Coffin coffin : coffins) {
						if (coffin.modelID == theID) {
							coffin.setStoneID(undecidedID);
							found = true;
							break;
						}
					}
					if (!found) {
						log("IDs have changed, please alert Timer.");
						return -1;
					}
				}
			}
			if (interfaces.get(INTERFACE_GRAVE).isValid()) {
				atCloseInterface(INTERFACE_GRAVE, INTERFACE_GRAVE_CLOSE);
				sleep(random(500, 800));
			}
		} else if ((undecidedID = getUndecidedCoffin()) != -1) {
			final RSItem item = inventory.getItem(undecidedID);
			if (item != null) {
				if (item.interact("Check")) {
					final long systemTime = System.currentTimeMillis();
					while (System.currentTimeMillis() - systemTime < 8000 && !interfaces.get(INTERFACE_COFFIN).isValid()) {
						sleep(random(50, 150));
					}
					sleep(random(1200, 2500));
					if (interfaces.getComponent(INTERFACE_COFFIN) != null) {
						final Integer[] allItems = new Integer[INTERFACE_COFFIN_IDS.length];
						final List<Integer> ids = new ArrayList<Integer>();
						for (final int index : INTERFACE_COFFIN_IDS) {
							ids.add(interfaces.getComponent(INTERFACE_COFFIN, index).getComponentID());
						}
						ids.toArray(allItems);
						boolean found = false;
						for (Coffin coffin : coffins) {
							if (coffin.doesMatch(allItems)) {
								coffin.setCoffinID(undecidedID);
								found = true;
								break;
							}
						}
						if (!found) {
							log("IDs have changed, please alert Timer.");
							return -1;
						}
					}
				}
				if (interfaces.get(INTERFACE_COFFIN).isValid()) {
					atCloseInterface(INTERFACE_COFFIN, INTERFACE_COFFIN_CLOSE);
					sleep(random(500, 800));
				}
			}
		} else {
			boolean done = true;
			for (Coffin coffin : coffins) {
				if (!coffin.set) {
					done = false;
					final int graveID = getEmptyGrave(coffin.stoneID);
					final RSObject grave = objects.getNearest(graveID);
					if (grave != null) {
						getObjectInView(grave);
						final RSItem theCoffin = inventory.getItem(coffin.coffinID);
						if (theCoffin != null) {
							if (inventory.useItem(theCoffin, grave)) {
								final long systemTime = System.currentTimeMillis();
								while (System.currentTimeMillis() - systemTime < 8000 && !(getMyPlayer().getAnimation() == 827)) {
									sleep(random(50, 150));
								}
								if (getMyPlayer().getAnimation() == 827) {
									coffin.set();
								}
							}
						}
					}
					break;
				}
			}
			if (done) {
				talkToNPC = true;
			}
		}
		return random(100, 200);
	}

	/**
	 * Clicks the close option of an interface.
	 *
	 * @param parent The parent interface.
	 * @param child  The child interface.
	 * @return Clicked or not.
	 */
	private boolean atCloseInterface(final int parent, final int child) {
		final RSComponent i = interfaces.getComponent(parent, child);
		if (!i.isValid()) {
			return false;
		}
		final Rectangle pos = i.getArea();
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
			return false;
		}
		final int dx = (int) (pos.getWidth() - 4) / 2;
		final int dy = (int) (pos.getHeight() - 4) / 2;
		final int midx = (int) (pos.getMinX() + pos.getWidth() / 2);
		final int midy = (int) (pos.getMinY() + pos.getHeight() / 2);
		mouse.click(midx + random(-dx, dx) - 5, midy + random(-dy, dy), true);
		sleep(random(500, 800));
		return true;
	}

	private int getUndecidedGrave() {
		for (final int graveStone : GRAVE_STONES) {
			boolean found = false;
			for (Coffin coffin : coffins) {
				if (coffin.stoneID == graveStone) {
					found = true;
				}
			}
			if (!found) {
				return graveStone;
			}
		}
		return -1;
	}

	private int getUndecidedCoffin() {
		for (final int coffinID : COFFINS) {
			boolean found = false;
			for (Coffin coffin : coffins) {
				if (coffin.coffinID == coffinID) {
					found = true;
				}
			}
			if (!found) {
				return coffinID;
			}
		}
		return -1;
	}

	private int getEmptyGrave(final int graveStone) {
		int i = 0;
		for (int aGraveStone : GRAVE_STONES) {
			if (aGraveStone == graveStone) {
				return EMPTY_GRAVES[i];
			}
			i++;
		}
		return -1;
	}

	private class Coffin {
		private final int modelID;
		private final int[] coffinItemIDs;
		private int stoneID = -1, coffinID = -1;
		private boolean set = false;

		private Coffin(final int modelID, final int[] coffinItemIDs) {
			this.modelID = modelID;
			this.coffinItemIDs = coffinItemIDs;
		}

		private void setStoneID(final int id) {
			this.stoneID = id;
		}

		private void setCoffinID(final int id) {
			this.coffinID = id;
		}

		private void set() {
			set = true;
		}

		private boolean doesMatch(final Integer[] arr) {
			for (final int checkItem : coffinItemIDs) {
				boolean cont = false;
				for (final Integer item : arr) {
					if (checkItem == item) {
						cont = true;
						break;
					}
				}
				if (!cont) {
					return false;
				}
			}
			return true;
		}
	}

	private boolean getObjectInView(final RSObject object) {
		for (int i = 0; i < 5; i++) {
			if (object.isOnScreen()) {
				break;
			}
			tiles.interact(calc.getTileOnScreen(object.getLocation()), "Walk here");
			sleep(random(500, 800));
			while (getMyPlayer().isMoving()) {
				sleep(random(50, 120));
			}
			sleep(random(500, 800));
		}
		return object.isOnScreen();
	}

	private boolean getNPCInView(final RSNPC theNpc) {
		for (int i = 0; i < 5; i++) {
			if (theNpc.isOnScreen()) {
				break;
			}
			if (i > 1) {
				camera.setPitch(true);
				camera.setAngle(random(0, 360));
			}
			tiles.interact(calc.getTileOnScreen(theNpc.getLocation()), "Walk here");
			sleep(random(500, 800));
			while (getMyPlayer().isMoving()) {
				sleep(random(50, 120));
			}
			sleep(random(500, 800));
		}
		return theNpc.isOnScreen();
	}
}