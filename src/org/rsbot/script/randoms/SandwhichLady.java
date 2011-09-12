package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;

/**
 * Jacmob was here to verify that Qauters' spelling mistake will be maintained in his memory.
 *
 * @author Qauters
 */
@ScriptManifest(authors = {"Qauters", "Drizzt1112", "TwistedMind"}, name = "SandwichLady", version = 2.3)
public class SandwhichLady extends Random {

	private final static int ID_InterfaceSandwhichWindow = 297;
	private final static int ID_InterfaceSandwhichWindowText = 48;
	private final static int ID_InterfaceTalk = 243;
	private final static int ID_InterfaceTalkText = 7;
	private final static int[] ID_Items = {10728, 10732, 10727, 10730, 10726, 45666, 10731};
	private final static int ID_SandwhichLady = 8630;
	private final static String[] Item_Names = {"chocolate", "triangle", "roll", "pie", "baguette", "doughnut", "square"};
	private final static boolean DEBUG = false; // Set to true for more info!
	RSNPC lady;

	@Override
	public boolean activateCondition() {
		return (lady = npcs.getNearest(ID_SandwhichLady)) != null;
	}

	@Override
	public int loop() {
		if (interfaces.get(243).isValid()) {
			interfaces.getComponent(243, 7).doClick();
			return random(900, 1200);
		}
		if (!activateCondition()) {
			lady = null;
			return -1;
		}
		if (getMyPlayer().getAnimation() != -1) {
			return random(500, 1000);
		}
		//Leaves random
		if (interfaces.getComponent(242, 4).containsText("The exit portal's")) {
			final RSObject portal = objects.getNearest(12731, 11373);
			if (portal != null) {
				if (!calc.tileOnScreen(portal.getLocation())) {
					walking.walkTileOnScreen(portal.getLocation());
				} else {
					if (portal.interact("Enter")) {
						return random(2000, 3000);
					}
					return 200;
				}
			}
		}
		// Check if we need to press continue, on the talk interface
		if (interfaces.get(ID_InterfaceTalk).isValid()) {
			interfaces.getComponent(ID_InterfaceTalk, ID_InterfaceTalkText).doClick();
			return random(900, 1200);
		}
		// Check if the sandwhich window is open
		if (interfaces.get(ID_InterfaceSandwhichWindow).isValid()) {
			final RSInterface window = interfaces.get(ID_InterfaceSandwhichWindow);
			int offset = -1;
			final String txt = window.getComponent(ID_InterfaceSandwhichWindowText).getText();
			for (int off = 0; off < Item_Names.length; off++) {
				if (txt.contains(Item_Names[off])) {
					offset = off;
					if (DEBUG) {
						log.info("Found: " + Item_Names[off] + " - ID: " + ID_Items[off]);
					}
				}
			}
			for (int i = 7; i < 48; i++) {
				final RSComponent inf = window.getComponent(i);
				if (DEBUG) {
					log.info("child[" + i + "] ID: " + inf.getModelID() + " == " + ID_Items[offset]);
				}
				if (inf.getModelID() == ID_Items[offset]) {
					inf.doClick();
					sleep(900, 1200); // Yea, use a sleep here! (Waits are allowed in randoms.)
					if (!interfaces.get(ID_InterfaceSandwhichWindow).isValid()) {
						log.info("Solved the Sandwich Lady, by eating a " + Item_Names[offset]);
						sleep(6000);
						return random(900, 1500);
					}
				}

			}
			return random(900, 1200);
		}
		if (interfaces.get(242).isValid()) {
			interfaces.getComponent(242, 6).doClick();
			return random(900, 1200);
		}
		// Talk to the lady
		if (lady != null && lady.getAnimation() == -1) {
			if (!calc.tileOnScreen(lady.getLocation())) {
				camera.turnTo(lady);
				camera.setPitch(false);
			} else {
				if (lady.interact("talk")) {
					return random(1000, 1500);
				}
			}
		}
		return random(900, 1200);
	}
}