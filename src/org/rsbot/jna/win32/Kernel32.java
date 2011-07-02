package org.rsbot.jna.win32;

import com.sun.jna.win32.StdCallLibrary;

/**
 * @author Paris
 */
public interface Kernel32 extends StdCallLibrary {
	final int PROCESS_SET_QUOTA = 0x0100;
	final int PROCESS_SET_INFORMATION = 0x0200;
	final int PROCESS_QUERY_INFORMATION = 0x0400;
	final int PROCESS_QUERY_LIMITED_INFORMATION = 0x1000;
	final int ABOVE_NORMAL_PRIORITY_CLASS = 0x00008000;
	final int BELOW_NORMAL_PRIORITY_CLASS = 0x00004000;
	final int HIGH_PRIORITY_CLASS = 0x00000080;
	final int IDLE_PRIORITY_CLASS = 0x00000040;
	final int NORMAL_PRIORITY_CLASS = 0x00000020;
	final int PROCESS_MODE_BACKGROUND_BEGIN = 0x00100000;
	final int PROCESS_MODE_BACKGROUND_END = 0x00200000;
	final int REALTIME_PRIORITY_CLASS = 0x00000100;

	int GetCurrentProcessId();
	boolean CloseHandle(int hObject);
	int OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
	boolean SetProcessWorkingSetSize(int hProcess, int dwMinimumWorkingSetSize, int dwMaximumWorkingSetSize);
	boolean SetPriorityClass(int hProcess, int dwPriorityClass);
}
