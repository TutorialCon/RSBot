package org.rsbot.jna.win32;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * @author Paris
 */
public interface Psapi extends StdCallLibrary {
	boolean EnumProcesses(int[] pProcessIds, int cb, IntByReference pBytesReturned);
}
