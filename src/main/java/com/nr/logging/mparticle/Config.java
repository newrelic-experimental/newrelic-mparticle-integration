/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import com.nr.logging.mparticle.utils.Strings;

import com.amazonaws.regions.Regions;

public class Config {
	public static final String EUInsightsEndpoint = "EUInsightsEndpoint";
	public static final String EventBucketName = "EventBucketName";
	public static final String EventBucketPrefix = "EventBucketPrefix";
	public static final String EventBucketRegion = "EventBucketRegion";
	public static final String LogLevel = "LogLevel";
	public static final String SaveMessages = "SaveMessages";
	public static final String USInsightsEndpoint = "USInsightsEndpoint";
	public static final String FifoQueue = "FifoQueue";
	private static Map<String, Object> config = new HashMap<>();
	private static Map<String, String> parser = new HashMap<>();
	private static String pid;

	static {
		config.put(LogLevel,
				Level.INFO);
		parser.put(LogLevel,
				"parse");

		// Strings don't need a parser method
		config.put(USInsightsEndpoint,
				"insights-collector.newrelic.com");
		config.put(EUInsightsEndpoint,
				"insights-collector.eu01.nr-data.net");
		config.put(EventBucketName,
				"new-relic-mparticle-integration-events");
		config.put(EventBucketPrefix,
				"");
		config.put(FifoQueue, "mParticle.fifo");

		config.put(SaveMessages,
				false);
		parser.put(SaveMessages,
				"parseBoolean");

		config.put(EventBucketRegion,
				Regions.US_EAST_1);
		parser.put(EventBucketRegion,
				"fromName");

		// This bit of magic is handy, HOWEVER it requires that we only use Java types that provide a static method with a single parameter of type String that can create an instance from a String
		for (String key : config.keySet()) {
			String value = System.getenv(key);
			if (value == null || Strings.isNullOrEmpty(value)) {
				System.out.println(String.format("[CONFIG] Using default. key: %s value: %s",
						key,
						config.get(key)));
			} else {
				Object defaultValue = config.get(key);
				if (defaultValue instanceof String) {
					config.put(key,
							value);
					System.out.println(String.format("[CONFIG] Set key: %s value: %s",
							key,
							config.get(key)));
				} else {
					try {
						Method method = defaultValue.getClass()
								.getMethod(parser.get(key),
										String.class);
						Object pValue = method.invoke(null,
								value);
						config.put(key,
								pValue);
						System.out.println(String.format("[CONFIG] Set key: %s value: %s",
								key,
								config.get(key)));
					} catch (NoSuchMethodException | SecurityException e) {
						System.err.println(String.format("[CONFIG] key: %s type: %s value: %s No 'parse' method or it is secured.",
								key,
								defaultValue.getClass(),
								value));
						System.err.println(String.format("[CONFIG] Using default. key: %s value: %s",
								key,
								config.get(key)));
					} catch (IllegalAccessException e) {
						System.err.println(String.format("[CONFIG] key: %s type: %s value: %sIllegalAccessException.",
								key,
								defaultValue.getClass(),
								value));
						System.err.println(String.format("[CONFIG] Using default. key: %s value: %s",
								key,
								config.get(key)));
					} catch (IllegalArgumentException e) {
						System.err.println(String.format("[CONFIG] key: %s type: %s value: %s IllegalArgumentException.",
								key,
								defaultValue.getClass(),
								value));
						System.err.println(String.format("[CONFIG] Using default. key: %s value: %s",
								key,
								config.get(key)));
					} catch (InvocationTargetException e) {
						System.err.println(String.format("[CONFIG] key: %s type: %s value: %s InvocationTargetException.",
								key,
								defaultValue.getClass(),
								value));
						System.err.println(String.format("[CONFIG] Using default. key: %s value: %s",
								key,
								config.get(key)));
					}
				}
			}
		}
	}

	public static Object getValue(String key) {
		return config.get(key);
	}

	public static String getPid() {
		if (pid == null)
			pid = ManagementFactory.getRuntimeMXBean()
					.getName();
		return pid;
	}
}