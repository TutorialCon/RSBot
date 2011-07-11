package org.rsbot.util;

import com.sun.jna.Native;
import org.rsbot.Configuration;
import org.rsbot.Configuration.OperatingSystem;
import org.rsbot.jna.win32.Kernel32;

public class Win32 {
	private static Kernel32 getKernel32Instance() {
		return (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
	}

	public static int getCurrentProcessId() {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return -1;
		}
		final Kernel32 kernel32 = getKernel32Instance();
		return kernel32.GetCurrentProcessId();
	}

	public static void emptyWorkingSet() {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			System.gc();
			return;
		}
		try {
			final Kernel32 kernel32 = getKernel32Instance();
			final int dwProcessId = kernel32.GetCurrentProcessId();
			final int hProcess = kernel32.OpenProcess(Kernel32.PROCESS_SET_QUOTA, false, dwProcessId);
			kernel32.SetProcessWorkingSetSize(hProcess, -1, -1);
			kernel32.CloseHandle(hProcess);
		} catch (final NoClassDefFoundError ignored) {
		} catch (final UnsatisfiedLinkError ignored) {
		}
		System.gc();
	}

	public static void setProcessPriority(final int dwPriorityClass) {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return;
		}
		try {
			final Kernel32 kernel32 = getKernel32Instance();
			final int dwProcessId = kernel32.GetCurrentProcessId();
			final int hProcess = kernel32.OpenProcess(Kernel32.PROCESS_SET_INFORMATION, false, dwProcessId);
			kernel32.SetPriorityClass(hProcess, dwPriorityClass);
			kernel32.CloseHandle(hProcess);
		} catch (final NoClassDefFoundError ignored) {
		} catch (final UnsatisfiedLinkError ignored) {
		}
	}
}
