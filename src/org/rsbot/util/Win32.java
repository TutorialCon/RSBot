package org.rsbot.util;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import org.rsbot.Configuration;
import org.rsbot.Configuration.OperatingSystem;
import org.rsbot.jna.win32.Kernel32;
import org.rsbot.jna.win32.Psapi;

/**
 * @author Paris
 */
public class Win32 {
	private static Kernel32 getKernel32Instance() {
		return (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
	}

	private static Psapi getPsapiInstance() {
		return (Psapi) Native.loadLibrary("Psapi", Psapi.class);
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
		} catch (final Throwable ignored) {
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
		} catch (final Throwable ignored) {
		}
	}

	public static int[] EnumProcesses() {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return null;
		}
		try {
			final int[] pProcessIds = new int[256];
			final IntByReference pBytesReturned = new IntByReference();
			final Psapi psapi = getPsapiInstance();
			final int b = Integer.SIZE / 8;
			psapi.EnumProcesses(pProcessIds, pProcessIds.length * b, pBytesReturned);
			final int[] result = new int[pBytesReturned.getValue() / b];
			System.arraycopy(pProcessIds, 0, result, 0, result.length);
			return result;
		} catch (final Throwable ignored) {
			return new int[0];
		}
	}

	public static String QueryFullProcessImageName(final int dwProcessId) {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return null;
		}
		try {
			final Kernel32 kernel32 = getKernel32Instance();
			final int hProcess = kernel32.OpenProcess(Kernel32.PROCESS_QUERY_LIMITED_INFORMATION, false, dwProcessId);
			final char[] lpExeName = new char[1024];
			final IntByReference lpdwSize = new IntByReference(lpExeName.length);
			kernel32.QueryFullProcessImageNameW(hProcess, 0, lpExeName, lpdwSize);
			kernel32.CloseHandle(hProcess);
			return Native.toString(lpExeName);
		} catch (final Throwable ignored) {
			return null;
		}
	}

	public static boolean TerminateProcess(final int dwProcessId) {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return false;
		}
		try {
			boolean result;
			final Kernel32 kernel32 = getKernel32Instance();
			final int hProcess = kernel32.OpenProcess(Kernel32.PROCESS_TERMINATE, false, dwProcessId);
			result = kernel32.TerminateProcess(hProcess, 0);
			kernel32.CloseHandle(hProcess);
			return result;
		} catch (final Throwable ignored) {
			return false;
		}
	}

	public static boolean SetFileAttributes(final String lpFileName, final int dwFileAttributes) {
		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
			return false;
		}
		try {
			return getKernel32Instance().SetFileAttributesW(new WString("\\\\?\\" + lpFileName), dwFileAttributes);
		} catch (final Throwable ignored) {
			return false;
		}
	}
}
