package integration.steps;

import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.services.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.domain.services.RollbackPaymentUseCaseImpl;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.PaymentEventHandler;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentFlowSteps {

    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;

    @Autowired
    private IPaymentEntityRepository paymentEntityRepository;

    @Autowired
    private PaymentPort paymentPort;

    @Autowired
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    @Autowired
    private com.postech.payment.fastfood.application.ports.output.LoggerPort loggerPort;

    @Autowired
    private TestDataBuilder testDataBuilder;

    private TestContext testContext;
    private GenerateQrCodePaymentUseCaseImpl generateQrCodePaymentUseCase;
    private RollbackPaymentUseCaseImpl rollbackPaymentUseCase;
    private PaymentEventHandler paymentEventHandler;

    private Order currentOrder;
    private EventOrder currentEvent;
    private List<History> currentHistory;
    private boolean mercadoPagoShouldFail;
    private boolean databaseShouldFail;
    private String failureMessage;

    @Before
    public void setUp() {
        testContext = new TestContext();
        testContext.reset();
        paymentEntityRepository.deleteAll();

        reset(paymentPort);
        reset(publishEventPaymentStatusPort);

        mercadoPagoShouldFail = false;
        databaseShouldFail = false;
        failureMessage = "Integration error";

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
    }

    @Dado("que o sistema de pagamentos está inicializado")
    public void queOSistemaDePagamentosEstaInicializado() {
        assertThat(paymentRepositoryPort).isNotNull();
        assertThat(paymentEntityRepository).isNotNull();
    }

    @Dado("um pedido com id único e valor de {double}")
    public void umPedidoComIdUnicoEValorDe(Double valor) {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        testContext.setCurrentOrderId(orderId);
        testContext.setCurrentTransactionId(transactionId);
        currentOrder = testDataBuilder.buildOrder(orderId, BigDecimal.valueOf(valor));
    }

    @E("que não existe pagamento para este pedido")
    public void queNaoExistePagamentoParaEstePedido() {
        Optional<Payment> existing = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(existing).isEmpty();
    }

    @E("que já existe um pagamento PENDING para este pedido")
    public void queJaExisteUmPagamentoPendingParaEstePedido() {
        Payment payment = testDataBuilder.buildPayment(
                testContext.getCurrentOrderId(),
                testContext.getCurrentTransactionId(),
                currentOrder.getTotalPrice(),
                PaymentStatus.PENDING
        );
        paymentRepositoryPort.save(payment);
    }

    @E("que a integração com MercadoPago irá falhar")
    public void queAIntegracaoComMercadoPagoIraFalhar() {
        mercadoPagoShouldFail = true;
    }

    @E("que a integração com MercadoPago irá falhar com mensagem {string}")
    public void queAIntegracaoComMercadoPagoIraFalharComMensagem(String mensagem) {
        mercadoPagoShouldFail = true;
        failureMessage = mensagem;
    }

    @E("que ocorrerá erro de banco de dados ao salvar")
    public void queOcorreraErroDeBancoDeDadosAoSalvar() {
        databaseShouldFail = true;
    }

    @E("um evento com histórico existente de ORDER com status SUCCESS")
    public void umEventoComHistoricoExistenteDeOrderComStatusSuccess() {
        currentHistory = testDataBuilder.buildHistory("ORDER", "SUCCESS", "Order confirmed");
    }

    @Quando("o evento SUCCESS é processado para geração de QR Code")
    public void oEventoSuccessEProcessadoParaGeracaoDeQrCode() {
        currentEvent = testDataBuilder.buildEventOrder(
                testContext.getCurrentOrderId(),
                testContext.getCurrentTransactionId(),
                "SUCCESS",
                currentOrder
        );

        configureMocks();

        try {
            boolean result = paymentEventHandler.handle(currentEvent);
            testContext.setOperationResult(result);
        } catch (PaymentEventNotSupportedException ex) {
            testContext.setLastException(ex);
        }
    }

    @Quando("o evento FAIL é processado para rollback")
    public void oEventoFailEProcessadoParaRollback() {
        currentEvent = testDataBuilder.buildEventOrder(
                testContext.getCurrentOrderId(),
                testContext.getCurrentTransactionId(),
                "FAIL",
                null
        );

        boolean result = paymentEventHandler.handle(currentEvent);
        testContext.setOperationResult(result);
    }

    @Quando("o evento ROLLBACK_PENDING é processado")
    public void oEventoRollbackPendingEProcessado() {
        currentEvent = testDataBuilder.buildEventOrder(
                testContext.getCurrentOrderId(),
                testContext.getCurrentTransactionId(),
                "ROLLBACK_PENDING",
                null
        );

        try {
            paymentEventHandler.handle(currentEvent);
        } catch (PaymentEventNotSupportedException ex) {
            testContext.setLastException(ex);
        }
    }

    @Quando("o evento com histórico é processado")
    public void oEventoComHistoricoEProcessado() {
        currentEvent = testDataBuilder.buildEventOrderWithHistory(
                testContext.getCurrentOrderId(),
                testContext.getCurrentTransactionId(),
                "SUCCESS",
                currentOrder,
                currentHistory
        );

        configureMocks();

        boolean result = paymentEventHandler.handle(currentEvent);
        testContext.setOperationResult(result);
    }

    @Entao("o pagamento deve ser salvo no banco de dados")
    public void oPagamentoDeveSerSalvoNoBancoDeDados() {
        Optional<Payment> savedPayment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(savedPayment).isPresent();
        testContext.setSavedPayment(savedPayment.get());
    }

    @E("o pagamento deve ter status PENDING")
    public void oPagamentoDeveTerStatusPending() {
        assertThat(testContext.getSavedPayment().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @E("o pagamento deve ter método QR_CODE")
    public void oPagamentoDeveTerMetodoQrCode() {
        assertThat(testContext.getSavedPayment().getPaymentMethod()).isEqualTo(PaymentMethod.QR_CODE);
    }

    @E("o QR Code deve ser gerado com sucesso")
    public void oQrCodeDeveSerGeradoComSucesso() {
        assertThat(testContext.getSavedPayment().getQrCode()).isNotNull();
    }

    @Entao("deve existir apenas um pagamento para o pedido")
    public void deveExistirApenasUmPagamentoParaOPedido() {
        long count = paymentEntityRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @E("o sistema não deve chamar a integração com MercadoPago novamente")
    public void oSistemaNaoDeveChamarAIntegracaoComMercadoPagoNovamente() {
        verify(paymentPort, times(0)).createQrCode(any(Payment.class), any());
    }

    @E("o valor do pagamento deve ser {double}")
    public void oValorDoPagamentoDeveSer(Double valor) {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isPresent();
        assertThat(payment.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(valor));
    }

    @Entao("o pagamento deve ser removido do banco de dados")
    public void oPagamentoDeveSerRemovidoDoBancoDeDados() {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isEmpty();
    }

    @Entao("nenhum erro deve ser lançado")
    public void nenhumErroDeveSerLancado() {
        assertThat(testContext.getLastException()).isNull();
    }

    @E("nenhum pagamento deve existir para o pedido")
    public void nenhumPagamentoDeveExistirParaOPedido() {
        Optional<Payment> payment = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(payment).isEmpty();
    }

    @Entao("um evento de rollback deve ser publicado")
    public void umEventoDeRollbackDeveSerPublicado() {
        ArgumentCaptor<EventPayment> captor = ArgumentCaptor.forClass(EventPayment.class);
        verify(publishEventPaymentStatusPort).publish(captor.capture());
        testContext.setPublishedEvent(captor.getValue());
    }

    @E("o status do evento de rollback deve ser ROLLBACK_PENDING")
    public void oStatusDoEventoDeRollbackDeveSerRollbackPending() {
        assertThat(testContext.getPublishedEvent().getStatus()).isEqualTo("ROLLBACK_PENDING");
    }

    @E("o source do evento de rollback deve ser PAYMENT")
    public void oSourceDoEventoDeRollbackDeveSerPayment() {
        assertThat(testContext.getPublishedEvent().getSource()).isEqualTo("PAYMENT");
    }

    @E("o histórico do evento deve conter a razão do erro")
    public void oHistoricoDoEventoDeveConterARazaoDoErro() {
        assertThat(testContext.getPublishedEvent().getHistory()).isNotEmpty();
        History lastEntry = testContext.getPublishedEvent().getHistory()
                .get(testContext.getPublishedEvent().getHistory().size() - 1);
        assertThat(lastEntry.getMessage()).contains("Rollback iniciado");
    }

    @E("o histórico do evento deve conter {int} entradas")
    public void oHistoricoDoEventoDeveConterEntradas(Integer quantidade) {
        assertThat(testContext.getPublishedEvent().getHistory()).hasSize(quantidade);
    }

    @E("a primeira entrada do histórico deve ser de ORDER")
    public void aPrimeiraEntradaDoHistoricoDeveSerDeOrder() {
        assertThat(testContext.getPublishedEvent().getHistory().get(0).getSource()).isEqualTo("ORDER");
    }

    @E("a última entrada do histórico deve ser de PAYMENT")
    public void aUltimaEntradaDoHistoricoDeveSerDePayment() {
        int lastIndex = testContext.getPublishedEvent().getHistory().size() - 1;
        assertThat(testContext.getPublishedEvent().getHistory().get(lastIndex).getSource()).isEqualTo("PAYMENT");
    }

    @Entao("o evento deve ser ignorado")
    public void oEventoDeveSerIgnorado() {
        assertThat(testContext.getLastException()).isInstanceOf(PaymentEventNotSupportedException.class);
    }

    @E("nenhum pagamento deve ser processado")
    public void nenhumPagamentoDeveSerProcessado() {
        verify(paymentPort, never()).createQrCode(any(), any());
    }

    private void configureMocks() {
        if (mercadoPagoShouldFail) {
            when(paymentPort.createQrCode(any(Payment.class), any()))
                    .thenThrow(new PaymentIntegrationException(failureMessage));
        } else {
            GeneratedQrCodeResponse qrCodeResponse = testDataBuilder.buildQrCodeResponse(
                    testContext.getCurrentOrderId(),
                    currentOrder.getTotalPrice()
            );
            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);
        }
    }
}

