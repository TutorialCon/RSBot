package org.rsbot.util;

import org.rsbot.Configuration;
import org.rsbot.Configuration.OperatingSystem;
import org.rsbot.jna.win32.Kernel32;

public class Win32 {
	public static void emptyWorkingSet() {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			System.gc();
			return;
		}
		final int dwProcessId = Kernel32.INSTANCE.GetCurrentProcessId();
		final int hProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_SET_QUOTA, false, dwProcessId);
		Kernel32.INSTANCE.SetProcessWorkingSetSize(hProcess, -1, -1);
		Kernel32.INSTANCE.CloseHandle(hProcess);
		System.gc();
	}

	public static void setProcessPriority(final int dwPriorityClass) {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return;
		}
		final int dwProcessId = Kernel32.INSTANCE.GetCurrentProcessId();
		final int hProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_SET_INFORMATION, false, dwProcessId);
		Kernel32.INSTANCE.SetPriorityClass(hProcess, dwPriorityClass);
		Kernel32.INSTANCE.CloseHandle(hProcess);
	}
}
