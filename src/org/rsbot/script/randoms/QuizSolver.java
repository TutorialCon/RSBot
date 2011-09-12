package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;


//Checked 4/7/10

/**
 * Updated by Arbiter Sep 20, 10: Replaced getModelZoom with getComponentID() and new sets of possible IDs as solutions
 */
@ScriptManifest(authors = {"PwnZ"}, name = "Quiz", version = 1.0)
public class QuizSolver extends Random {

	public class QuizQuestion {
		int[] IDS = {-1, -1, -1};
		int answer;

		public QuizQuestion(final int One, final int Two, final int Three) {
			IDS = new int[]{One, Two, Three};
		}

		public boolean activateCondition() {
			for (int ID : IDS) {
				if (getSlot(ID) == -1) {
					return false;
				}
			}
			return true;
		}

		public boolean arrayContains(final int[] arr, final int i) {
			for (final int num : arr) {
				if (num == i) {
					return true;
				}
			}
			return false;
		}

		public boolean clickAnswer() {
			answer = -1;
			int count = 0;
			sleep(1000, 1700);
			for (int j = 0; j < items.length; j++) {
				for (int i = 0; i < IDS.length; i++) {
					if (arrayContains(items[j], IDS[i])) {
						log.info("Slot "+ (i + 1) + ": " + names[j]);
						count++;
					}
				}
				if (count >= 2) {
					log.info("Type Found: " + names[j]);
					answer = j;
					break;
				}
			}
			if (answer != -1) {
				int slot;
				if ((slot = findNotInAnswerArray()) != -1) {
					return atSlot(slot);
				} else {
					log.info("findNotInAnswerArray() failed.");
					return false;
				}
			} else {
				log.info("answer fail.");
				return false;
			}
		}

		public int findNotInAnswerArray() {
			for (int i = 0; i < IDS.length; i++) {
				if (!arrayContains(items[answer], IDS[i])) {
					return i + 1;
				}
			}
			return -1;
		}
	}

	private final int quizInterface = 191;
	private final int[] Fish = {6190, 6189};
	private final int[] Jewelry = {6198, 6197};
	private final int[] Weapons = {6192, 6194};
	private final int[] Farming = {6195, 6196};
	private final int[][] items = {Fish, Jewelry, Weapons, Farming};
	private final String[] names = {"Fish", "Jewelry", "Weapons", "Farming"};

	@Override
	public boolean activateCondition() {
		final RSNPC quizMaster = npcs.getNearest("Quiz Master");
		return quizMaster != null;
	}

	@Override
	public int loop() {
		final RSNPC quizMaster = npcs.getNearest("Quiz Master");
		if (quizMaster == null) {
			return -1;
		}
		if (getSlotID(1) != -1) {
			log.info("Question detected.");
			final QuizQuestion question = new QuizQuestion(getSlotID(1), getSlotID(2), getSlotID(3));
			if (question.clickAnswer()) {
				return random(1000, 1500);
			} else {
				log.info("Trying Random Answer");
				atSlot(random(1, 4));
				return random(1000, 1500);
			}
		} else {
			if (interfaces.clickContinue()) {
				return random(800, 1000);
			}
		}
		return random(1200, 2000);
	}

	int getSlot(final int id) {
		for (int i = 1; i < 3; i++) {
			if (getSlotID(i) == id) {
				return i;
			}
		}
		return -1;
	}

	int getSlotID(final int slot) {
		return interfaces.getComponent(quizInterface, (slot + 5)).getComponentID();
	}

	boolean atSlot(final int slot) {
		return interfaces.getComponent(quizInterface, (slot + 2)).doClick();
	}
}