package org.rsbot.script.methods;

import org.rsbot.gui.AccountManager;

/**
 * Selected account information.
 */
public class Account extends MethodProvider {
	public Account(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Sets the current bot's account name.
	 *
	 * @param name The account name.
	 * @return <tt>true</tt> if the account existed.
	 */
	public boolean setAccount(final String name) {
		return methods.bot.setAccount(name);
	}

	/**
	 * The account's name.
	 *
	 * @return The currently selected account's name.
	 */
	public String getName() {
		return methods.bot.getAccountName();
	}

	/**
	 * The account's display name.
	 *
	 * @return The currently selected account's display name.
	 */
	public String getDisplayName() {
		return AccountManager.getDisplayName(getName());
	}

	/**
	 * The account's password.
	 *
	 * @return The currently selected account's password.
	 */
	public String getPassword() {
		return AccountManager.getPassword(getName());
	}

	/**
	 * The account's pin.
	 *
	 * @return The currently selected account's pin.
	 */
	public String getPin() {
		return AccountManager.getPin(getName());
	}

	/**
	 * The account's selected reward.
	 *
	 * @return The currently selected account's reward.
	 */
	public String getReward() {
		return AccountManager.getReward(getName());
	}
}
