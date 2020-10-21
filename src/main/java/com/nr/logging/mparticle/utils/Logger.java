/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle.utils;

import java.util.logging.Level;

import com.nr.logging.mparticle.Config;

public class Logger {

	private Class<?> klass;
	private Level logLevel = (Level) Config.getValue(Config.LogLevel);

	public Logger(Class<?> klass) {
		this.klass = klass;
	}

	public void fine(String format, Object... args) {
		if (logLevel
		      .intValue() <= Level.FINE.intValue())
			log(Level.FINE, format, args);
	}

	public void fine(String message, Throwable t) {
		if (logLevel
		      .intValue() <= Level.FINE.intValue())
			log(Level.FINE, message, t);
	}

	public void finer(String format, Object... args) {
		if (logLevel
		      .intValue() <= Level.FINER.intValue())
			log(Level.FINER, format, args);
	}

	public void finest(String format, Object... args) {
		if (logLevel
		      .intValue() <= Level.FINEST.intValue())
			log(Level.FINEST, format, args);
	}

	public void info(String format, Object... args) {
		if (logLevel
		      .intValue() <= Level.INFO.intValue())
			log(Level.INFO, format, args);
	}

	public void info(String message, Throwable t) {
		if (logLevel
		      .intValue() <= Level.INFO.intValue())
			log(Level.INFO, message, t);
	}

	public void severe(String format, Object... args) {
		if (logLevel
		      .intValue() <= Level.SEVERE.intValue())
			log(Level.SEVERE, format, args);
	}

	public void severe(String message, Throwable e) {
		if (logLevel
		      .intValue() <= Level.SEVERE.intValue())
			log(Level.SEVERE, message, e);
	}

	public void warning(String format, Object... args) {
		if (logLevel
		      .intValue() <= Level.WARNING.intValue())
			log(Level.WARNING, format, args);
	}

	public void warning(String message, Throwable e) {
		if (logLevel
		      .intValue() <= Level.WARNING.intValue())
			log(Level.WARNING, message, e);
	}

	private void log(Level level, String format, Object... args) {
		try {
			Object[] args2 = new Object[2 + args.length];
			args2[0] = level;
			args2[1] = klass.getSimpleName();
			for (int i = 0; i < args.length; i++)
				args2[i + 2] = args[i];

			String value = String.format(("[%s] %s: " + format), args2);

			if (level
			      .intValue() <= Level.WARNING.intValue())
				System.out.println(value);
			else
				System.err.println(value);

		} catch (Exception e) {
			System.err.println("Logger.log: Exception: " + e.getMessage());
		}
	}

	private void log(Level level, String message, Throwable t) {
		if (logLevel
		      .intValue() <= Level.WARNING.intValue())
			System.err.println(String.format(("[%s] %s: %s %s"), level, klass, message, t.getMessage()));
		else
			System.out.println(String.format(("[%s] %s: %s %s"), level, klass, message, t.getMessage()));

		if (level.intValue() >= Level.SEVERE.intValue())
			t.printStackTrace();
	}
}
