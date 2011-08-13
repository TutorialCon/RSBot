package org.rsbot.script;

public @interface SDN {
	/**
	 * Whether or not to obfuscate scripts on the server.
	 * This will be force-set to true if a license token is specified,
	 * in other words this should only be false if the script is free and open source.
	 */
	boolean obfuscate() default true;

	/**
	 * Tokens for DRM licensing.
	 * 
	 * A token is a unique string (usually a random SHA1 digest) that links this scripts
	 * authorisation controls against verified users on the database or isolated serial keys.
	 * 
	 * Special values:
	 * 		""				allows everyone access
	 * 		"!VIP"			access to VIP and above
	 * 		"!SPONSOR" 		access to Sponsors and above
	 * 		"!STAFF"		access to all staff members (Moderators and Administrators)
	 * 		"!DEVS"			access to Contributors and Script Writers
	 * 
	 * Note: administrators have superuser access.
	 * 
	 * Multiple tokens can be used to broaden access to different groups, only one token needs to
	 * satisfy a valid license in order to function for the user.
	 */
	String[] licenseTokens() default { "" };
}
