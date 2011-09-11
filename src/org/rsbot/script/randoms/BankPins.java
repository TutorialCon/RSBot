package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;

@ScriptManifest(authors = {"Holo", "Gnarly", "Salty_Fish", "Pervy Shuya", "Doout"}, name = "BankPin", version = 3.0)
public class BankPins extends Random {

	@Override
	public boolean activateCondition() {
		return interfaces.get(13).isValid() || interfaces.get(14).isValid();
	}

	void enterPin(String pin) {
		int state = settings.getSetting(563);
		if (!interfaces.get(13).isValid() || !interfaces.get(759).isValid() || state == 4) {
			return;
		}
		final RSComponent[] pinNumbers = interfaces.get(759).getComponents();
		for (final RSComponent pinNumber : pinNumbers) {
			if (pinNumber.containsText(String.valueOf(pin.charAt(state)))) {
				pinNumber.doClick();
				sleep(800, 1200);
				break;
			}
		}
	}

	@Override
	public int loop() {
		if (interfaces.get(14).isValid()) {
			interfaces.getComponent(14, 33).doClick();
			return 500;
		} else {
			final String pin = account.getPin();
			if (pin == null || pin.length() != 4) {
				log.severe("You must add a bank pin to your account.");
				stopScript(false);
			}
			if (interfaces.get(14).isValid() || !interfaces.get(13).isValid()) {
				interfaces.get(14).getComponent(3).doClick();
				return -1;
			}
			enterPin(pin);
			if (interfaces.get(211).isValid()) {
				interfaces.get(211).getComponent(3).doClick();
			} else if (interfaces.get(217).isValid()) {
				sleep(random(10500, 12000));
			}
		}
		return 500;
	}
}
