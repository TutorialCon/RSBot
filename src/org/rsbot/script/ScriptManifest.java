package org.rsbot.script;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptManifest {
	public enum Category {
		Agility("Agility"),
		Combat("Combat"),
		Construction("Construction"),
		Cooking("Cooking"),
		Crafting("Crafting"),
		Dungeoneering("Dungeoneering"),
		Farming("Farming"),
		Firemaking("Firemaking"),
		Fishing("Fishing"),
		Fletching("Fletching"),
		Herblore("Herblore"),
		Hunter("Hunter"),
		Magic("Magic"),
		Minigames("Minigames"),
		Mining("Mining"),
		Misc("Misc"),
		Money_Making("Money Making"),
		Prayer("Prayer"),
		Ranged("Ranged"),
		Runecrafting("Runecrafting"),
		Slayer("Slayer"),
		Smithing("Smithing"),
		Summoning("Summoning"),
		Thieving("Thieving"),
		Woodcutting("Woodcutting");
		final String name;

		Category(final String name) {
			this.name = name;
		}

		public String getCategory() {
			return name;
		}

		public static Category value(final String name) {
			Category cat = Misc;
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
