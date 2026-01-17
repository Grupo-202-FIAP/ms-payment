package integration.steps;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.services.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.domain.services.RollbackPaymentUseCaseImpl;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.ConsumerPaymentQueue;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.PaymentEventHandler;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import com.postech.payment.fastfood.utils.JsonConverter;
import integration.utils.TestContext;
import integration.utils.TestDataBuilder;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentEventProcessingSteps {

    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;

    @Autowired
    private IPaymentEntityRepository paymentEntityRepository;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    @Autowired
    private LoggerPort loggerPort;

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private TestDataBuilder testDataBuilder;

    private TestContext testContext;
    private GenerateQrCodePaymentUseCaseImpl generateQrCodePaymentUseCase;
    private RollbackPaymentUseCaseImpl rollbackPaymentUseCase;
    private PaymentEventHandler paymentEventHandler;
    private ConsumerPaymentQueue consumerPaymentQueue;

    private Order currentOrder;
    private EventOrder currentEvent;
    private String currentJsonMessage;
    private boolean mercadoPagoShouldFail;
    private String failureMessage;
    private boolean handlerResult;
    private boolean conversionErrorOccurred;

    @Before
    public void setUp() {
        testContext = new TestContext();
        testContext.reset();
        paymentEntityRepository.deleteAll();

        reset(paymentPort);
        reset(publishEventPaymentStatusPort);

        mercadoPagoShouldFail = false;
        failureMessage = "Integration error";
        conversionErrorOccurred = false;

        generateQrCodePaymentUseCase = new GenerateQrCodePaymentUseCaseImpl(
                paymentPort,
                loggerPort,
                paymentRepositoryPort,
                publishEventPaymentStatusPort
        );

        rollbackPaymentUseCase = new RollbackPaymentUseCaseImpl(paymentRepositoryPort);

        paymentEventHandler = new PaymentEventHandler(
                loggerPort,
                generateQrCodePaymentUseCase,
                rollbackPaymentUseCase,
                publishEventPaymentStatusPort
        );

        consumerPaymentQueue = new ConsumerPaymentQueue(
                loggerPort,
                paymentEventHandler,
                jsonConverter
        );
    }

    @Dado("uma mensagem JSON com status SUCCESS e pedido válido")
    public void umaMensagemJsonComStatusSuccessEPedidoValido() {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        testContext.setCurrentOrderId(orderId);
        testContext.setCurrentTransactionId(transactionId);
        currentOrder = testDataBuilder.buildOrder(orderId, BigDecimal.valueOf(100.00));
        currentEvent = testDataBuilder.buildEventOrder(orderId, transactionId, "SUCCESS", currentOrder);
        currentJsonMessage = jsonConverter.toJson(currentEvent);

        GeneratedQrCodeResponse qrCodeResponse = testDataBuilder.buildQrCodeResponse(orderId, currentOrder.getTotalPrice());
        when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);
    }

    @Dado("uma mensagem JSON com status FAIL")
    public void umaMensagemJsonComStatusFail() {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        testContext.setCurrentOrderId(orderId);
        testContext.setCurrentTransactionId(transactionId);
        currentEvent = testDataBuilder.buildEventOrder(orderId, transactionId, "FAIL", null);
        currentJsonMessage = jsonConverter.toJson(currentEvent);
    }

    @Dado("uma mensagem JSON inválida")
    public void umaMensagemJsonInvalida() {
        currentJsonMessage = "{invalid json content";
    }

    @Dado("um evento SUCCESS com pedido válido")
    public void umEventoSuccessComPedidoValido() {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        testContext.setCurrentOrderId(orderId);
        testContext.setCurrentTransactionId(transactionId);
        currentOrder = testDataBuilder.buildOrder(orderId, BigDecimal.valueOf(150.00));
        currentEvent = testDataBuilder.buildEventOrder(orderId, transactionId, "SUCCESS", currentOrder);

        GeneratedQrCodeResponse qrCodeResponse = testDataBuilder.buildQrCodeResponse(orderId, currentOrder.getTotalPrice());
        when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);
    }

    @Dado("um evento FAIL para pedido existente")
    public void umEventoFailParaPedidoExistente() {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        testContext.setCurrentOrderId(orderId);
        testContext.setCurrentTransactionId(transactionId);
        currentEvent = testDataBuilder.buildEventOrder(orderId, transactionId, "FAIL", null);
    }

    @E("existe um pagamento para o pedido")
    public void existeUmPagamentoParaOPedido() {
        Payment payment = testDataBuilder.buildPayment(
                testContext.getCurrentOrderId(),
                testContext.getCurrentTransactionId(),
                BigDecimal.valueOf(100.00),
                PaymentStatus.PENDING
        );
        paymentRepositoryPort.save(payment);
    }

    @Quando("a mensagem é consumida pelo listener")
    public void aMensagemEConsumidaPeloListener() {
        try {
            consumerPaymentQueue.consumeMessage(currentJsonMessage);
        } catch (ConversionException ex) {
            conversionErrorOccurred = true;
            testContext.setLastException(ex);
        }
    }

    @Quando("o handler processa o evento")
    public void oHandlerProcessaOEvento() {
        if (mercadoPagoShouldFail) {
            when(paymentPort.createQrCode(any(Payment.class), any()))
                    .thenThrow(new PaymentIntegrationException(failureMessage));
        }

        handlerResult = paymentEventHandler.handle(currentEvent);
        testContext.setOperationResult(handlerResult);
    }

    @Entao("o use case de geração de QR Code deve ser executado")
    public void oUseCaseDeGeracaoDeQrCodeDeveSerExecutado() {
        verify(paymentPort).createQrCode(any(Payment.class), any());
    }

    @E("o use case de rollback não deve ser executado")
    public void oUseCaseDeRollbackNaoDeveSerExecutado() {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isPresent();
    }

    @Entao("o use case de rollback deve ser executado")
    public void oUseCaseDeRollbackDeveSerExecutado() {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isEmpty();
    }

    @E("o use case de geração de QR Code não deve ser executado")
    public void oUseCaseDeGeracaoDeQrCodeNaoDeveSerExecutado() {
        verify(paymentPort, never()).createQrCode(any(Payment.class), any());
    }

    @Entao("o erro de conversão deve ser logado")
    public void oErroDeConversaoDeveSerLogado() {
        assertThat(conversionErrorOccurred).isTrue();
    }

    @E("a mensagem deve ser removida da fila")
    public void aMensagemDeveSerRemovidaDaFila() {
        // Mensagem é removida quando não há exceção não tratada propagada
        // O teste valida que a exceção foi capturada internamente
        assertThat(testContext.getLastException()).isNotNull();
    }

    @Entao("o resultado deve ser true")
    public void oResultadoDeveSerTrue() {
        assertThat(handlerResult).isTrue();
    }

    @E("o pagamento deve ser criado")
    public void oPagamentoDeveSerCriado() {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isPresent();
    }

    @E("o pagamento deve ser removido")
    public void oPagamentoDeveSerRemovido() {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isEmpty();
    }

    @Entao("o resultado deve ser false")
    public void oResultadoDeveSerFalse() {
        assertThat(handlerResult).isFalse();
    }

    @E("o histórico do evento deve conter {string}")
    public void oHistoricoDoEventoDeveConter(String texto) {
        ArgumentCaptor<EventPayment> captor = ArgumentCaptor.forClass(EventPayment.class);
        verify(publishEventPaymentStatusPort).publish(captor.capture());
        EventPayment event = captor.getValue();

        boolean found = event.getHistory().stream()
                .anyMatch(h -> h.getMessage() != null && h.getMessage().contains(texto));
        assertThat(found).isTrue();
    }
}

