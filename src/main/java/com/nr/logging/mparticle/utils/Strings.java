/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle.utils;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

public class Strings {
	private static Logger log = new Logger(Strings.class);

	public static boolean isBoolean(String value) {
		if ("true".equalsIgnoreCase(value))
			return true;
		if ("false".equalsIgnoreCase(value))
			return true;
		return false;
	}

	public static boolean isDateTime(String value) {
		try {
			new DateTime(value);
			return true;
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			return false;
		}
	}

	public static boolean isLong(String value) {
		try {
			Long.parseLong(value);
			return true;
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			return false;
		}
	}

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public static boolean isNotNullOrEmpty(String s) {
		return s != null && (!s.isEmpty());
	}

	public static Set<String> pathSplit(String path) {
		Set<String> result = new HashSet<>();
		StringBuffer sb = new StringBuffer();
		for (char c : path.toCharArray()) {
			sb.append(c);
			if (c == '/')
				result.add(sb.toString());
		}
		result.add(sb.toString());
		return result;
	}

	public static DateTime toDateTime(String dateTime) {
		return new DateTime(dateTime);
	}

	public static int toInteger(String i, int defaultValue) {
		int value = defaultValue;
		try {
			value = Integer.parseInt(i);
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			log.warning("toInteger: invalid value: %s",
					i);
		}
		return value;
	}
}
