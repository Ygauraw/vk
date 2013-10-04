package com.gark.vknew.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Just another logging wrapper.
 * 
 */
public final class Log {
	private final static String tag = "KeepGO";

	public static void v(String msg) {
		if (android.util.Log.isLoggable(Log.tag, android.util.Log.VERBOSE) && msg != null) {
			android.util.Log.v(Log.tag, msg);
		}
	}

	public static void d(String msg) {
		if (android.util.Log.isLoggable(Log.tag, android.util.Log.DEBUG) && msg != null) {
			android.util.Log.d(Log.tag, msg);
		}
	}

	public static void i(String msg) {
		if (android.util.Log.isLoggable(Log.tag, android.util.Log.INFO) && msg != null) {
			android.util.Log.i(Log.tag, msg);
		}
	}

	public static void w(String msg) {
		if (android.util.Log.isLoggable(Log.tag, android.util.Log.WARN) && msg != null) {
			android.util.Log.w(Log.tag, msg);
		}
	}

	public static void e(String msg) {
		if (android.util.Log.isLoggable(Log.tag, android.util.Log.ERROR) && msg != null) {
			android.util.Log.e(Log.tag, msg);
		}
	}

	public static void e(Throwable t) {
		Log.e(Log.stacktraceToString(t));
	}

	public static void e(String msg, Throwable t) {
		if (msg == null)
			Log.e(Log.stacktraceToString(t));
		else
			Log.e(msg + " " + Log.stacktraceToString(t));
	}

	/**
	 * Stacktrace logging made easy
	 * 
	 * @param t
	 *            throwable
	 * @return a string that contains the stacktrace of the throwable
	 */
	public static String stacktraceToString(Throwable t) {
		if (t == null)
			return "";

		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return t.toString() + sw.toString();
	}
}
