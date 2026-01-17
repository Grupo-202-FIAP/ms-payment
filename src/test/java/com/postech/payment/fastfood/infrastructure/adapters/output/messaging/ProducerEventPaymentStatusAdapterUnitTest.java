package com.postech.payment.fastfood.infrastructure.adapters.output.messaging;

import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.producer.ProducerEventPaymentStatusAdapter;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerEventPaymentStatusAdapterUnitTest {

    @Mock
    private SnsTemplate snsTemplate;

    @Mock
    private LoggerPort logger;

    @InjectMocks
    private ProducerEventPaymentStatusAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        // set private topicArn field so adapter calls a non-null topic
        Field topicField = ProducerEventPaymentStatusAdapter.class.getDeclaredField("topicArn");
        topicField.setAccessible(true);
        topicField.set(adapter, "arn:test:topic");
    }

    @Test
    void whenPublishSucceeds_thenNoExceptionAndLogsInfo() {
        // Given/When
        adapter.publish("message");

        // Then
        verify(snsTemplate).convertAndSend(eq("arn:test:topic"), eq("message"));
        verify(logger).info("[ADAPTER][SNS] Publishing event to topic: {}", "arn:test:topic");
    }

    @Test
    void whenSnsThrows_thenWrapInMessagingException_andLogError() {
        // Given
        final SnsException ex = mock(SnsException.class);
        doThrow(ex).when(snsTemplate).convertAndSend(eq("arn:test:topic"), eq("message"));

        // When / Then
        assertThrows(MessagingException.class, () -> adapter.publish("message"));

        // the adapter calls logger.error("[ADAPTER][SNS] SNS service error for topic: {}", topicArn, e);
        verify(logger).error("[ADAPTER][SNS] SNS service error for topic: {}", "arn:test:topic", ex);
    }

    @Test
    void whenSdkClientThrows_thenWrapInMessagingException_andLogError() {
        // Given
        final SdkClientException ex = mock(SdkClientException.class);
        doThrow(ex).when(snsTemplate).convertAndSend(eq("arn:test:topic"), eq("message"));

        // When / Then
        assertThrows(MessagingException.class, () -> adapter.publish("message"));

        verify(logger).error("[ADAPTER][SNS] AWS SNS connectivity error: {}", "arn:test:topic", ex);
    }

    @Test
    void whenPublishNullMessage_thenSendNullAndLogInfo() {
        // Given / When
        adapter.publish(null);

        // Then
        verify(snsTemplate).convertAndSend(eq("arn:test:topic"), isNull(Object.class));
        verify(logger).info("[ADAPTER][SNS] Publishing event to topic: {}", "arn:test:topic");
    }

}
