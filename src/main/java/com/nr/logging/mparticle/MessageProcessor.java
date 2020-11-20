/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.nr.logging.mparticle;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.mparticle.sdk.model.Message;
import com.mparticle.sdk.model.MessageSerializer;
import com.nr.api.insights.RetryableException;
import com.nr.logging.mparticle.utils.Logger;
import com.nr.logging.mparticle.utils.SQS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

// Superclass for NewRelicMessageProcessor, handles SQS Messages.
public class MessageProcessor implements RequestHandler<SQSEvent, String> {
    private static final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    private static final String queueUrl = sqsClient.getQueueUrl((String) Config.getValue(Config.MessageQueue))
            .getQueueUrl();
    private final MessageSerializer serializer = new MessageSerializer();
    private final Logger log = new Logger(this.getClass());
    private final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new EnvironmentVariableCredentialsProvider())
            .build();
    private final static int ConnectionTimeoutBackoff = 5;

    @Override
    public String handleRequest(SQSEvent input, Context context) {
        for (SQSMessage message : input.getRecords()) {
            Message request = null;
            try (NewRelicMessageProcessor processor = new NewRelicMessageProcessor()) {
                request = serializer.deserialize(message.getBody(), Message.class);
                processor.processMessage(request);
                if ((boolean) Config.getValue(Config.SaveMessages)) writeMessageToS3(request);
            } catch (RetryableException e) {
                SQS sqs = new SQS(e.secondsUntilRetry());
                sqs.sendMessage(message.getBody());
                log.info("%s: retrying with backoff: %d", e, e.secondsUntilRetry());
            } catch (IOException e) {
                if(e.getMessage().toLowerCase().contains("connection timed out")){
                    SQS sqs = new SQS(ConnectionTimeoutBackoff);
                    sqs.sendMessage(message.getBody());
                    log.info("IOException: ", e);
                    log.info("%s: retrying with backoff: %d", e, ConnectionTimeoutBackoff);
                }else {
                    writeMessageToS3(request);
                    log.severe("IOException: ", e);
                }
            } catch (Exception e) {
                writeMessageToS3(request);
                log.severe("handleRequest: ", e);
            } finally {
            }

        }
        return null;
    }

    // Save messages to S3 if configured to do so
    private void writeMessageToS3(Message message) {
        String bucketName = (String) Config.getValue(Config.EventBucketName);
        String keyName = String.format("%s/%s/%s", Config.getValue(Config.EventBucketPrefix), message.getType()
                .toString(), message.getId()
                .toString());
        log.fine("writeMessageToS3: bucket: %s key: %s", bucketName, keyName);
        try {
            InputStream stringStream = new ByteArrayInputStream(serializer.serialize(message)
                    .getBytes());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/json");
            s3Client.putObject(bucketName, keyName, stringStream, metadata);
        } catch (AmazonServiceException e) {
            log.severe("writeMessageToS3: ", e);
        } catch (SdkClientException e) {
            log.severe("writeMessageToS3: ", e);
        } catch (IOException e) {
            log.severe("writeMessageToS3: ", e);
        }
    }
}
