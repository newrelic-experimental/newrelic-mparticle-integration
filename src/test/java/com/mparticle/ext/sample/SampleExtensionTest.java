package com.mparticle.ext.sample;

import com.mparticle.sdk.model.MessageSerializer;
import com.mparticle.sdk.model.registration.ModuleRegistrationRequest;
import com.mparticle.sdk.model.registration.ModuleRegistrationResponse;
import com.nr.logging.mparticle.NewRelicMessageProcessor;

import org.junit.jupiter.api.Test;

/**
 * These tests can be run by executing the 'test' gradle task via `./gradlew test`
 */
public class SampleExtensionTest {

	MessageSerializer serializer = new MessageSerializer();

	@Test
	public void testProcessRegistrationRequest() throws Exception {
		NewRelicMessageProcessor sampleExtension = new NewRelicMessageProcessor();
		ModuleRegistrationResponse response = sampleExtension.processRegistrationRequest(new ModuleRegistrationRequest());
		System.out.println();
		System.out.println("Please send this JSON to mParticle:");
		System.out.println();
		System.out.println(serializer.serialize(response));
		System.out.println();
		sampleExtension.close();
	}

	@Test
	public void testProcessEventProcessingRequest() throws Exception {

	}

	@Test
	public void testProcessPushMessageReceiptEvent() throws Exception {

	}

	@Test
	public void testProcessPushSubscriptionEvent() throws Exception {

	}

	@Test
	public void testProcessUserIdentityChangeEvent() throws Exception {

	}

	@Test
	public void testProcessCustomEvent() throws Exception {

	}

	@Test
	public void testProcessAudienceMembershipChangeRequest() throws Exception {

	}

	@Test
	public void testProcessAudienceSubscriptionRequest() throws Exception {

	}
}