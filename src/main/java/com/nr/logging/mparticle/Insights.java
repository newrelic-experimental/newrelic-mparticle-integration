/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.wnameless.json.JacksonJsonValue;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.mparticle.sdk.model.eventprocessing.Event;
import com.mparticle.sdk.model.eventprocessing.RuntimeEnvironment;
import com.mparticle.sdk.model.eventprocessing.UserIdentity;
import com.mparticle.sdk.model.registration.Account;
import com.nr.logging.mparticle.utils.Gzip;
import com.nr.logging.mparticle.utils.InsightsWriter;
import com.nr.logging.mparticle.utils.Logger;
import com.nr.logging.mparticle.utils.Strings;

// Here we convert mParticle to Insights and ship it to the appropriate (US/EU) Insights Insert endpoint
public class Insights implements Closeable{

	private static InsightsWriter insightsWriter = new InsightsWriter();
	private static Logger log = new Logger(Insights.class);
	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		// ignore missing fields
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		// ignore nulls and empty containers
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		// only include annotated fields
		objectMapper.setVisibility(PropertyAccessor.ALL,
				JsonAutoDetect.Visibility.NONE);
		// use toString() for enums
		objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,
				true);
		objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING,
				true);
	}

	private Integer appId;
	private List<Map<String, Object>> events = new ArrayList<>();
	private String insertKey;
	private String region;
	private Integer rpmId;
	private Map<String, Object> userIdentities;

	public void close() throws IOException{
		// Nothing to process
		if (events.size() == 0)
			return;

		try {
			log.finest("close: json: %s",
					objectMapper.writerWithDefaultPrettyPrinter()
							.writeValueAsString(events));
			byte[] json = Gzip.compress(objectMapper.writeValueAsString(events));
			if (json.length > 1000000) {
				log.severe("close: compressed data > 1M. Actual length: %d",
						json.length);
			}

			StringBuffer result = insightsWriter.post(getURL(this.region),
					this.insertKey,
					json);
			log.fine("close: insightsWriter result: %s",
					result.toString());
		} catch (JsonProcessingException e) {
			log.severe("close: ",
					e);
		} finally {
			// Clean-up
			events = new ArrayList<>();
		}
	}

	public void mParticleToInsights(Event event) {
		log.fine("mParticleToInsights: event %s",
				event);
		// I hate doing this on every event
		account(event.getRequest()
				.getAccount());
		// Convert event to Jackson tree and then flatten
//		String jstring = "{}";
//		try {
//			jstring = objectMapper.writeValueAsString(event);
//		} catch (JsonProcessingException e) {
//			log.severe("mParticleToInsights: %s",
//					e.getMessage());
//			return;
//		}
//		Map<String, Object> flatMap = JsonFlattener.flattenAsMap(jstring);
		JsonNode jsonNode = objectMapper.valueToTree(event);
		JacksonJsonValue jsonVal = new JacksonJsonValue(jsonNode);
		Map<String, Object> flatMap = JsonFlattener.flattenAsMap(jsonVal);
		flatMap.putAll(userIdentities(event.getRequest()
				.getUserIdentities()));
		flatMap.putAll(userAttributes(event.getRequest()
				.getUserAttributes()));
		flatMap.putAll(runtimeEnvironment(event.getRequest()
				.getRuntimeEnvironment()));

		// Add the common attributes
		flatMap.put("appId",
				this.appId);
		flatMap.put("eventType",
				event.getType()
						.toString());
		flatMap.put("mpid",
				event.getRequest()
						.getMpId());
		flatMap = clean(flatMap);

		events.add(flatMap);
	}

	private Map<String, Object> runtimeEnvironment(RuntimeEnvironment runtimeEnvironment) {
		RuntimeEnv re = new RuntimeEnv();
		re.runtimeEnvironment = runtimeEnvironment;
		JsonNode jsonNode = objectMapper.valueToTree(re);
		JacksonJsonValue jsonVal = new JacksonJsonValue(jsonNode);
		return JsonFlattener.flattenAsMap(jsonVal);
	}

	private Map<String, Object> userAttributes(Map<String, String> userAttributes) {
		UserAttributes ua = new UserAttributes();
		ua.userAttributes = userAttributes;
		JsonNode jsonNode = objectMapper.valueToTree(ua);
		JacksonJsonValue jsonVal = new JacksonJsonValue(jsonNode);
		return JsonFlattener.flattenAsMap(jsonVal);
	}

	private Map<String, Object> userIdentities(List<UserIdentity> userIdentities) {
		UserIdentities uid = new UserIdentities();
		uid.userIdentities = userIdentities;
		JsonNode jsonNode = objectMapper.valueToTree(uid);
		JacksonJsonValue jsonVal = new JacksonJsonValue(jsonNode);
		return JsonFlattener.flattenAsMap(jsonVal);
	}

	private void account(Account account) {
		this.rpmId = account.getIntegerSetting(NewRelicMessageProcessor.RpmId,
				true,
				null);
		this.appId = account.getIntegerSetting(NewRelicMessageProcessor.AppId,
				false,
				null);
		this.insertKey = account.getStringSetting(NewRelicMessageProcessor.InsightsInsertKey,
				true,
				null);
		this.region = account.getStringSetting(NewRelicMessageProcessor.AccountRegion,
				false,
				"US");
	}

	private Map<String, Object> clean(Map<String, Object> flatMap) {
		// Length of attribute value : 4KB maximum length
		// Number of attributes per event: 255 maximum
		if (flatMap.size() > 255)
			log.warning("clean: event contains more than 255 attributes and will be truncated upon ingestion. Attribute count: %d",
					flatMap.size());

		String eventType = (String) flatMap.get("eventType");
		String cleanEventType = cleanEventType(eventType);
		if (!cleanEventType.equalsIgnoreCase(eventType)) {
			log.warning("clean: invalid eventType: %s using: %s",
					eventType,
					cleanEventType);
			flatMap.put("eventType",
					cleanEventType);
		}
		Map<String, Object> cleanMap = new HashMap<>(flatMap.size());
		for (String key : flatMap.keySet()) {
			String cleanKey = this.cleanKey(key);
			Object value = flatMap.get(key);
			if (value instanceof Double)
				value = ((Double) value).floatValue();
			else if (value instanceof Long)
				value = ((Long) value).floatValue();
			cleanMap.put(cleanKey,
					value);
		}
		return cleanMap;
	}

	private static HashSet<String> reservedWords = new HashSet<>(Arrays.asList("ago",
			"and",
			"as",
			"auto",
			"begin",
			"begintime",
			"compare",
			"day",
			"days",
			"end",
			"endtime",
			"explain",
			"facet",
			"from",
			"hour",
			"hours",
			"in",
			"is",
			"like",
			"limit",
			"minute",
			"minutes",
			"month",
			"months",
			"not",
			"null",
			"offset",
			"or",
			"raw",
			"second",
			"seconds",
			"select",
			"since",
			"timeseries",
			"until",
			"week",
			"weeks",
			"where",
			"with"));

	private String cleanKey(String key) {
		String result = key;

		if (key == null || Strings.isNullOrEmpty(key)) {
			log.warning("cleanKey: key is null or black");
			return result;
		}

		// Length of attribute name: 255 characters
		if (key.length() > 255) {
			result = key.substring(0,
					254);
			log.warning("cleanKey: truncating key to 255 characters. Original: %s Cleaned: %s",
					key,
					result);
			return result;
		}

		if (key.equalsIgnoreCase("timestamp_ms"))
			return "timestamp";

		if (reservedWords.contains(key.toLowerCase())) {
			result = "`" + key + "`";
		}
		return result;
	}

	private String cleanEventType(String key) {
		// alphanumeric characters, colons (:), and underscores (_).
		// avoid using Metric, MetricRaw, and strings prefixed with Metric[0-9]
		String result = key;
		return result;
	}

	private String getURL(String region) {
		String host = (String) Config.getValue(Config.USInsightsEndpoint);
		if (region.equalsIgnoreCase("EU"))
			host = (String) Config.getValue(Config.EUInsightsEndpoint);
		return String.format("https://%s/v1/accounts/%s/events",
				host,
				this.rpmId);

	}
}