package com.postech.payment.fastfood.integration.utils;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;

/**
 * Utility class for SQS operations during integration tests.
 */
@Component
public class SqsTestSupport {

    private final SqsAsyncClient sqsAsyncClient;

    public SqsTestSupport(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
    }

    /**
     * Resolves the full queue URL from a queue name.
     *
     * @param sqsClient The SQS async client to use
     * @param queueName The name of the queue
     * @return The full queue URL
     */
    public String resolveQueueUrl(SqsAsyncClient sqsClient, String queueName) {
        return sqsClient.getQueueUrl(
                GetQueueUrlRequest.builder()
                        .queueName(queueName)
                        .build()
        ).join().queueUrl();
    }

    /**
     * Resolves the full queue URL from a queue name using the injected client.
     *
     * @param queueName The name of the queue
     * @return The full queue URL
     */
    public String resolveQueueUrl(String queueName) {
        return resolveQueueUrl(this.sqsAsyncClient, queueName);
    }

    /**
     * Sends a message to an SQS queue.
     *
     * @param queueUrl    The URL of the queue
     * @param messageBody The message body to send
     */
    public void sendMessage(String queueUrl, String messageBody) {
        sqsAsyncClient.sendMessage(req -> req.queueUrl(queueUrl).messageBody(messageBody))
                .join();
    }

    /**
     * Sends a message to an SQS queue by queue name.
     *
     * @param queueName   The name of the queue
     * @param messageBody The message body to send
     */
    public void sendMessageByQueueName(String queueName, String messageBody) {
        String queueUrl = resolveQueueUrl(queueName);
        sendMessage(queueUrl, messageBody);
    }

    /**
     * Purges all messages from a queue.
     *
     * @param queueUrl The URL of the queue to purge
     */
    public void purgeQueue(String queueUrl) {
        sqsAsyncClient.purgeQueue(
                PurgeQueueRequest.builder()
                        .queueUrl(queueUrl)
                        .build()
        ).join();
    }

    /**
     * Purges all messages from a queue by name.
     *
     * @param queueName The name of the queue to purge
     */
    public void purgeQueueByName(String queueName) {
        String queueUrl = resolveQueueUrl(queueName);
        purgeQueue(queueUrl);
    }

    /**
     * Gets the injected SqsAsyncClient.
     *
     * @return The SQS async client
     */
    public SqsAsyncClient getSqsClient() {
        return sqsAsyncClient;
    }
}
