package integration.utils;

import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestContext {

    private UUID currentOrderId;
    private UUID currentTransactionId;
    private Payment savedPayment;
    private EventPayment publishedEvent;
    private boolean operationResult;
    private Throwable lastException;

    public void reset() {
        this.currentOrderId = null;
        this.currentTransactionId = null;
        this.savedPayment = null;
        this.publishedEvent = null;
        this.operationResult = false;
        this.lastException = null;
    }

    public UUID getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(UUID currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

    public UUID getCurrentTransactionId() {
        return currentTransactionId;
    }

    public void setCurrentTransactionId(UUID currentTransactionId) {
        this.currentTransactionId = currentTransactionId;
    }

    public Payment getSavedPayment() {
        return savedPayment;
    }

    public void setSavedPayment(Payment savedPayment) {
        this.savedPayment = savedPayment;
    }

    public EventPayment getPublishedEvent() {
        return publishedEvent;
    }

    public void setPublishedEvent(EventPayment publishedEvent) {
        this.publishedEvent = publishedEvent;
    }

    public boolean isOperationResult() {
        return operationResult;
    }

    public void setOperationResult(boolean operationResult) {
        this.operationResult = operationResult;
    }

    public Throwable getLastException() {
        return lastException;
    }

    public void setLastException(Throwable lastException) {
        this.lastException = lastException;
    }
}

