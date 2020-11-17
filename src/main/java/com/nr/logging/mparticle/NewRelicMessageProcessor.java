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
import java.util.List;

import com.mparticle.sdk.MessageProcessor;
import com.mparticle.sdk.model.eventprocessing.ApplicationStateTransitionEvent;
import com.mparticle.sdk.model.eventprocessing.AttributionEvent;
import com.mparticle.sdk.model.eventprocessing.CustomEvent;
import com.mparticle.sdk.model.eventprocessing.DeviceIdentity;
import com.mparticle.sdk.model.eventprocessing.ErrorEvent;
import com.mparticle.sdk.model.eventprocessing.Event;
import com.mparticle.sdk.model.eventprocessing.EventProcessingRequest;
import com.mparticle.sdk.model.eventprocessing.EventProcessingResponse;
import com.mparticle.sdk.model.eventprocessing.Identity;
import com.mparticle.sdk.model.eventprocessing.ImpressionEvent;
import com.mparticle.sdk.model.eventprocessing.ProductActionEvent;
import com.mparticle.sdk.model.eventprocessing.PromotionActionEvent;
import com.mparticle.sdk.model.eventprocessing.PushMessageReceiptEvent;
import com.mparticle.sdk.model.eventprocessing.PushSubscriptionEvent;
import com.mparticle.sdk.model.eventprocessing.RuntimeEnvironment;
import com.mparticle.sdk.model.eventprocessing.ScreenViewEvent;
import com.mparticle.sdk.model.eventprocessing.SessionEndEvent;
import com.mparticle.sdk.model.eventprocessing.SessionStartEvent;
import com.mparticle.sdk.model.eventprocessing.UserAttributeChangeEvent;
import com.mparticle.sdk.model.eventprocessing.UserIdentity;
import com.mparticle.sdk.model.eventprocessing.UserIdentityChangeEvent;
import com.mparticle.sdk.model.registration.DeviceIdentityPermission;
import com.mparticle.sdk.model.registration.EventProcessingRegistration;
import com.mparticle.sdk.model.registration.ModuleRegistrationRequest;
import com.mparticle.sdk.model.registration.ModuleRegistrationResponse;
import com.mparticle.sdk.model.registration.Permissions;
import com.mparticle.sdk.model.registration.Setting;
import com.mparticle.sdk.model.registration.TextSetting;
import com.mparticle.sdk.model.registration.UserIdentityPermission;

/**
 * Only mParticle specific code here. Our translation work goes in the Insights class.
 */
public class NewRelicMessageProcessor extends MessageProcessor implements Closeable {

	// this name will show up in the mParticle UI
	public static final String CompanyName = "New Relic, Inc.";
	// These come from mParticle's UI for us
	public static final String AccountRegion = "accountRegion";
	public static final String InsightsInsertKey = "insightsInsertKey";
	public static final String RpmId = "rpmId";
	public static final String AppId = "appId";
	// sample segment-level setting
	public static final String SETTING_MAILING_LIST_ID = "mailingListId";
	private Insights insights = new Insights();

	@Override
	public void processApplicationStateTransitionEvent(ApplicationStateTransitionEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processAttributionEvent(AttributionEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processCustomEvent(CustomEvent event) throws IOException {
		insights.mParticleToInsights(event);
		// super.processCustomEvent( event);
	}

	@Override
	public void processErrorEvent(ErrorEvent event) throws IOException {
	}

	/**
	 * When a MessageProcessor is given a batch of data/events, it will first call this method. This is a good time to do some setup. For example since a given batch will all be for the same device, you could contact the server once here and make sure that that device/user
	 * exists in the system, rather than doing that every time one of the more specific methods (ie processCustomEvent) is called.
	 */
	@Override
	public EventProcessingResponse processEventProcessingRequest(EventProcessingRequest request) throws IOException {
		// do some setup, then call super. if you don't call super, you'll effectively short circuit
		// the whole thing, which isn't really fun for anyone.
		return super.processEventProcessingRequest(request);
	}

	@Override
	public void processImpressionEvent(ImpressionEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processProductActionEvent(ProductActionEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processPromotionActionEvent(PromotionActionEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processPushMessageReceiptEvent(PushMessageReceiptEvent event) throws IOException {
		insights.mParticleToInsights(event);
		// super.processPushMessageReceiptEvent( event);
	}

	@Override
	public void processPushSubscriptionEvent(PushSubscriptionEvent event) throws IOException {
		insights.mParticleToInsights(event);
		// super.processPushSubscriptionEvent( event);
	}

	@Override
	public ModuleRegistrationResponse processRegistrationRequest(ModuleRegistrationRequest request) {
		// Set the permissions - the device and user identities that this service can have access to
		Permissions permissions = new Permissions()
				// There are a bunch of these to add
				.setAllowAccessDeviceApplicationStamp(true)
				.setAllowAccessHttpUserAgent(true)
				.setAllowAccessIpAddress(true)
				.setAllowAccessLocation(true)
				.setAllowAccessMpid(true)
				.setAllowAudienceUserAttributeSharing(false)
				// .setAllowConsentState(true)
				.setAllowDeviceInformation(true)
				.setAllowUserAttributes(true)
				.setUserIdentities(Arrays.asList(new UserIdentityPermission(UserIdentity.Type.EMAIL, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.OTHER, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.FACEBOOK, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.GOOGLE, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.MICROSOFT, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.OTHER2, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.OTHER3, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.OTHER4, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.TWITTER, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.YAHOO, Identity.Encoding.RAW),
						new UserIdentityPermission(UserIdentity.Type.CUSTOMER, Identity.Encoding.RAW)))
				.setDeviceIdentities(Arrays.asList(new DeviceIdentityPermission(DeviceIdentity.Type.GOOGLE_CLOUD_MESSAGING_TOKEN,
						Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.APPLE_PUSH_NOTIFICATION_TOKEN,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.IOS_VENDOR_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.ANDROID_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.GOOGLE_ADVERTISING_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.IOS_ADVERTISING_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.APPLE_PUSH_NOTIFICATION_TOKEN,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.ROKU_ADVERTISING_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.MICROSOFT_ADVERTISING_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.MICROSOFT_PUBLISHER_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.FIRE_ADVERTISING_ID,
								Identity.Encoding.RAW),
						new DeviceIdentityPermission(DeviceIdentity.Type.ROKU_PUBLISHER_ID,
								Identity.Encoding.RAW)));

		// the extension needs to define the settings it needs in order to connect to its respective service(s).
		// you can using different settings for Event Processing vs. Audience Processing, but in this case
		// we'll just use the same object, specifying a New Relic Account ID (RPM ID) and insights Insert Key are required
		List<Setting> processorSettings = new ArrayList<>();
		processorSettings.add(new TextSetting(RpmId,
				"Account ID").setIsRequired(true)
						.setIsConfidential(false)
						.setDescription("New Relic Account ID"));
		processorSettings.add(new TextSetting(AppId,
				"Application ID").setIsRequired(false)
						.setIsConfidential(false)
						.setDescription("New Relic Application ID added to the Insights Event. Integer value of the Application ID or blank."));
		processorSettings.add(new TextSetting(InsightsInsertKey,
				"Insights Insert Key").setIsRequired(true)
						.setIsConfidential(true)
						.setDescription("New Relic insights Insert Key"));
		processorSettings.add(new TextSetting(AccountRegion,
				"Account Region").setIsRequired(true)
						.setIsConfidential(false)
						.setDefaultValue("US")
						.setDescription("Which region your New Relic account is registered with- US or EU. Default value US."));

		// specify the supported event types. you should override the parent MessageProcessor methods
		// that correlate to each of these event types.
		List<Event.Type> supportedEventTypes = Arrays.asList(Event.Type.APPLICATION_STATE_TRANSITION,
				Event.Type.ATTRIBUTION,
				Event.Type.CUSTOM_EVENT,
				Event.Type.ERROR,
				Event.Type.IMPRESSION,
				// Event.Type.PRIVACY_SETTING_CHANGE,
				Event.Type.PRODUCT_ACTION,
				Event.Type.PROMOTION_ACTION,
//				Event.Type.PUSH_MESSAGE_OPEN,
//				Event.Type.PUSH_SUBSCRIPTION,
//				Event.Type.PUSH_MESSAGE_RECEIPT,
				Event.Type.SCREEN_VIEW,
				Event.Type.SESSION_END,
				Event.Type.SESSION_START,
				Event.Type.USER_ATTRIBUTE_CHANGE,
				Event.Type.USER_IDENTITY_CHANGE);

		// this extension supports event data coming from all devices
		List<RuntimeEnvironment.Type> environments = Arrays.asList(RuntimeEnvironment.Type.ALEXA,
				RuntimeEnvironment.Type.ANDROID,
				RuntimeEnvironment.Type.FIRETV,
				RuntimeEnvironment.Type.IOS,
				RuntimeEnvironment.Type.MOBILEWEB,
				RuntimeEnvironment.Type.ROKU,
				RuntimeEnvironment.Type.SMARTTV,
				RuntimeEnvironment.Type.TVOS,
				RuntimeEnvironment.Type.UNKNOWN,
				RuntimeEnvironment.Type.XBOX);

		// finally use all of the above to assemble the EventProcessingRegistration object and set it in the response
		EventProcessingRegistration eventProcessingRegistration = new EventProcessingRegistration().setSupportedRuntimeEnvironments(environments)
				.setAccountSettings(processorSettings)
				.setSupportedEventTypes(supportedEventTypes);

		// Segmentation/Audience registration and processing is treated separately from Event processing
		// Audience integrations are configured separately in the mParticle UI
		// Customers can configure a different set of account-level settings (such as API key here), and
		// Segment-level settings (Mailing List ID here).
		//List<Setting> subscriptionSettings = new LinkedList<>();
//		subscriptionSettings.add(new IntegerSetting(SETTING_MAILING_LIST_ID,
//				"Mailing List ID"));

//		AudienceProcessingRegistration audienceRegistration = new AudienceProcessingRegistration().setAccountSettings(processorSettings)
//				.setAudienceConnectionSettings(subscriptionSettings);

		return new ModuleRegistrationResponse(CompanyName,
				"1.0").setDescription("<a href=\"http://newrelic.com\" target=\"_blank\">New Relic</a> Real-time insights for the modern enterprise")
//						.setAudienceProcessingRegistration(audienceRegistration)
						.setEventProcessingRegistration(eventProcessingRegistration)
						.setPermissions(permissions);
	}

	@Override
	public void processScreenViewEvent(ScreenViewEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processSessionEndEvent(SessionEndEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processSessionStartEvent(SessionStartEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processUserAttributeChangeEvent(UserAttributeChangeEvent event) throws IOException {
		insights.mParticleToInsights(event);
	}

	@Override
	public void processUserIdentityChangeEvent(UserIdentityChangeEvent event) throws IOException {
		insights.mParticleToInsights(event);
		// super.processUserIdentityChangeEvent( event);
	}

	public void close() throws IOException {
		insights.close();
	}
}