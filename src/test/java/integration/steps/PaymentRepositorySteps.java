package integration.steps;

import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import integration.utils.TestContext;
import integration.utils.TestDataBuilder;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentRepositorySteps {

    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;

    @Autowired
    private IPaymentEntityRepository paymentEntityRepository;

    @Autowired
    private TestDataBuilder testDataBuilder;

    private TestContext testContext;
    private Payment currentPayment;
    private Optional<Payment> queryResult;
    private List<Payment> multiplePayments;
    private Map<UUID, PaymentStatus> orderStatusMap;

    @Before
    public void setUp() {
        testContext = new TestContext();
        testContext.reset();
        paymentEntityRepository.deleteAll();
        multiplePayments = new ArrayList<>();
        orderStatusMap = new HashMap<>();
    }

    @Dado("um pagamento com orderId único, valor {double} e status PENDING")
    public void umPagamentoComOrderIdUnicoValorEStatusPending(Double valor) {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        testContext.setCurrentOrderId(orderId);
        testContext.setCurrentTransactionId(transactionId);
        currentPayment = testDataBuilder.buildPayment(orderId, transactionId, BigDecimal.valueOf(valor), PaymentStatus.PENDING);
    }

    @Dado("um orderId que não existe no banco")
    public void umOrderIdQueNaoExisteNoBanco() {
        testContext.setCurrentOrderId(UUID.randomUUID());
    }

    @Dado("os seguintes pagamentos:")
    public void osSeguintesPagamentos(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            UUID orderId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            BigDecimal valor = new BigDecimal(row.get("valor"));
            PaymentStatus status = PaymentStatus.valueOf(row.get("status"));

            Payment payment = testDataBuilder.buildPayment(orderId, transactionId, valor, status);
            multiplePayments.add(payment);
            orderStatusMap.put(orderId, status);
        }
    }

    @E("o pagamento foi salvo no repositório")
    public void oPagamentoFoiSalvoNoRepositorio() {
        paymentRepositoryPort.save(currentPayment);
    }

    @Quando("o pagamento é salvo no repositório")
    public void oPagamentoESalvoNoRepositorio() {
        paymentRepositoryPort.save(currentPayment);
    }

    @Quando("busco o pagamento pelo orderId")
    public void buscoPagamentoPeloOrderId() {
        queryResult = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
    }

    @Quando("o pagamento é deletado pelo orderId")
    public void oPagamentoEDeletadoPeloOrderId() {
        paymentRepositoryPort.delete(testContext.getCurrentOrderId());
    }

    @Quando("os pagamentos são salvos no repositório")
    public void osPagamentosSaoSalvosNoRepositorio() {
        for (Payment payment : multiplePayments) {
            paymentRepositoryPort.save(payment);
        }
    }

    @Entao("o pagamento deve ser recuperável pelo orderId")
    public void oPagamentoDeveSerRecuperavelPeloOrderId() {
        queryResult = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(queryResult).isPresent();
    }

    @E("o pagamento recuperado deve ter o mesmo valor")
    public void oPagamentoRecuperadoDeveTerOMesmoValor() {
        assertThat(queryResult.get().getAmount()).isEqualByComparingTo(currentPayment.getAmount());
    }

    @E("o pagamento recuperado deve ter o mesmo status")
    public void oPagamentoRecuperadoDeveTerOMesmoStatus() {
        assertThat(queryResult.get().getStatus()).isEqualTo(currentPayment.getStatus());
    }

    @E("o pagamento recuperado deve ter método QR_CODE")
    public void oPagamentoRecuperadoDeveTerMetodoQrCode() {
        assertThat(queryResult.get().getPaymentMethod()).isEqualTo(PaymentMethod.QR_CODE);
    }

    @E("o pagamento recuperado deve ter um id gerado")
    public void oPagamentoRecuperadoDeveTerUmIdGerado() {
        assertThat(queryResult.get().getId()).isNotNull();
    }

    @Entao("o resultado deve ser vazio")
    public void oResultadoDeveSerVazio() {
        assertThat(queryResult).isEmpty();
    }

    @Entao("o pagamento não deve mais existir no banco")
    public void oPagamentoNaoDeveMaisExistirNoBanco() {
        Optional<Payment> deleted = paymentRepositoryPort.findByOrderId(testContext.getCurrentOrderId());
        assertThat(deleted).isEmpty();
    }

    @Entao("devem existir {int} pagamentos no banco")
    public void devemExistirPagamentosNoBanco(Integer quantidade) {
        long count = paymentEntityRepository.count();
        assertThat(count).isEqualTo(quantidade);
    }

    @E("cada pagamento deve ter seu status correspondente")
    public void cadaPagamentoDeveTerSeuStatusCorrespondente() {
        for (Map.Entry<UUID, PaymentStatus> entry : orderStatusMap.entrySet()) {
            Optional<Payment> payment = paymentRepositoryPort.findByOrderId(entry.getKey());
            assertThat(payment).isPresent();
            assertThat(payment.get().getStatus()).isEqualTo(entry.getValue());
        }
    }
}

