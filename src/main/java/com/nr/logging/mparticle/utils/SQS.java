/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle.utils;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.nr.logging.mparticle.Config;

public class SQS {
	private static final Logger log = new Logger(SQS.class);
	private static final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
	private static final String queueUrl = sqsClient.getQueueUrl((String) Config.getValue(Config.MessageQueue))
			.getQueueUrl();

	private SendMessageRequest sendMessageRequest;

	private SQS(){}
	public SQS(int delayInSeconds){
		sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl)
				//.withMessageGroupId("mParticle-message")
				.withDelaySeconds(delayInSeconds);
	}

	// The return from this method is VERY expensive, probably due to GC on body but that's not clear.
	public void sendMessage(String body) {
		if (Strings.isNullOrEmpty(body))
			body = "-";

		// TODO should the body be compressed + base64 encoded? Saves about 1/3 of the original size at the expense of cpu

		long t0 = System.currentTimeMillis();
		sendMessageRequest.setMessageBody(body);
		// Can't set this param on a Standard queue or it throws an error
		//sendMessageRequest.setMessageDeduplicationId(Config.getPid() + System.currentTimeMillis());
		log.fine("SQS.sendMessage: create sendMessageRequest: %d",
				System.currentTimeMillis() - t0);

		t0 = System.currentTimeMillis();
		sqsClient.sendMessage(sendMessageRequest);
		log.fine("SQS.sendMessage: sendMessage: %d",
				System.currentTimeMillis() - t0);
		body = null;
		// The delay is here, on the void return
	}
}