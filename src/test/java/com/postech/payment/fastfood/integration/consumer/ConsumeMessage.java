package com.postech.payment.fastfood.integration.consumer;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for consuming messages from SQS queues during integration tests.
 */
@Component
public class ConsumeMessage {

    /**
     * Receives messages from an SQS queue.
     *
     * @param sqsClient   The SQS client to use
     * @param queueUrl    The URL of the queue to read from
     * @param maxMessages Maximum number of messages to retrieve (1-10)
     * @return List of message bodies as strings
     */
    public List<String> receiveMessages(SqsAsyncClient sqsClient, String queueUrl, int maxMessages) {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(Math.min(maxMessages, 10))
                .waitTimeSeconds(5)
                .visibilityTimeout(0)
                .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(request).join();

        return response.messages().stream()
                .map(Message::body)
                .collect(Collectors.toList());
    }

    /**
     * Receives messages from an SQS queue with default max of 10 messages.
     *
     * @param sqsClient The SQS client to use
     * @param queueUrl  The URL of the queue to read from
     * @return List of message bodies as strings
     */
    public List<String> receiveMessages(SqsAsyncClient sqsClient, String queueUrl) {
        return receiveMessages(sqsClient, queueUrl, 10);
    }

    /**
     * Waits for a message to appear in the queue with a timeout.
     *
     * @param sqsClient      The SQS client to use
     * @param queueUrl       The URL of the queue to read from
     * @param timeoutSeconds Maximum time to wait for a message
     * @return List of message bodies, or empty list if timeout
     */
    public List<String> waitForMessages(SqsAsyncClient  sqsClient, String queueUrl, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            List<String> messages = receiveMessages(sqsClient, queueUrl, 1);
            if (!messages.isEmpty()) {
                return messages;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return List.of();
    }
}

