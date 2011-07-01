package org.rsbot.script;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptManifest {
	public enum Category {
		AGILITY("Agility"),
		COMBAT("Combat"),
		CONSTRUCTION("Construction"),
		COOKING("Cooking"),
		CRAFTING("Crafting"),
		DUNGEONEERING("Dungeoneering"),
		FARMING("Farming"),
		FIREMAKING("Firemaking"),
		FISHING("Fishing"),
		FLETCHING("Fletching"),
		HERBLORE("Herblore"),
		HUNTER("Hunter"),
		MAGIC("Magic"),
		MINIGAME("Minigame"),
		MINING("Mining"),
		MISC("Misc"),
		MONEY_MAKING("Money Making"),
		PRAYER("Prayer"),
		RANGED("Ranged"),
		RUNECRAFTING("Runecrafting"),
		SLAYER("Slayer"),
		SMITHING("Smithing"),
		SUMMONING("Summoning"),
		THIEVING("Thieving"),
		WOODCUTTING("Woodcutting");
		final String name;

		Category(final String name) {
			this.name = name;
		}

		public String getCategory() {
			return name;
		}

		public static Category value(final String name) {
			Category cat = MISC;
			for (Category category : Category.values()) {
				if (category.name.equalsIgnoreCase(name)) {
					cat = category;
					break;
				}
			}
			return cat;
		}
	}

	String name();

	double version() default 1.0;

	String description() default "";

	String[] authors();

	String[] keywords() default {};

	Category[] categories() default {};

	String website() default "";

	int requiresVersion() default 200;
}
