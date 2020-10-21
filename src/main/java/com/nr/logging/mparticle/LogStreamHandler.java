/*
 *  Copyright (c) 2020 New Relic Corporation. All rights reserved.
 */

package com.nr.logging.mparticle;

/**
 * This is the Lambda that mParticle calls with the exported data
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.mparticle.sdk.model.Message;
import com.mparticle.sdk.model.MessageSerializer;
import com.nr.logging.mparticle.utils.Logger;
import com.nr.logging.mparticle.utils.Strings;

public class LogStreamHandler implements RequestStreamHandler {
	private static final Logger log = new Logger(LogStreamHandler.class);
	private static final NewRelicMessageProcessor processor = new NewRelicMessageProcessor();
	private static final MessageSerializer serializer = new MessageSerializer();
	private static final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
	private static final String queueUrl = sqsClient.getQueueUrl((String) Config.getValue(Config.FifoQueue))
			.getQueueUrl();

	private static SendMessageRequest sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl)
			.withMessageGroupId("mParticle-message")
			.withDelaySeconds(0);

	// Lambda entry point
	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		long start = System.currentTimeMillis();
		String message;
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {
			byte[] buffer = inputStream.readAllBytes();
			result.write(buffer);
			message = result.toString(StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			log.severe("streamToString: ",
					e);
			message = "";
		}
		result.close();
		inputStream.close();

		log.fine("handleRequest: streamToString time: %d",
				System.currentTimeMillis() - start);

		// Handle a registration request
		if (message.contains(Message.Type.MODULE_REGISTRATION_REQUEST.name()) ||
				message.contains(Message.Type.MODULE_REGISTRATION_REQUEST.name()
						.toLowerCase())) {
			Message request = serializer.deserialize(message,
					Message.class);
			Message response = processor.processMessage(request);
			serializer.serialize(outputStream,
					response);
		} else {
			// Process the exported data
			// Due to the mParticle 200ms latency limit we have to take the Event and push it onto a queue
			long t0 = System.currentTimeMillis();

			if (Strings.isNullOrEmpty(message))
				message = "-";

			t0 = System.currentTimeMillis();
			sendMessageRequest.setMessageBody(message);
			sendMessageRequest.setMessageDeduplicationId(Config.getPid() + System.currentTimeMillis());
			log.fine("handleRequest: update sendMessageRequest: %d",
					System.currentTimeMillis() - t0);

			t0 = System.currentTimeMillis();
			sqsClient.sendMessage(sendMessageRequest);
			log.fine("handleRequest: sendMessage: %d",
					System.currentTimeMillis() - t0);
			message = null;

			log.fine("handleRequest: total time: %d",
					System.currentTimeMillis() - start);
		}
		outputStream.close();
	}
}