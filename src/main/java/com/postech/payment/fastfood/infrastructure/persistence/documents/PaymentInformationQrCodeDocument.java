package com.postech.payment.fastfood.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_information_qr_code")
public class PaymentInformationQrCodeDocument {
    @Id
    private String id;
    private String orderId;
    private String paymentId;
    private BigDecimal totalAmount;
    private String currency;
    private String qrCode;
    private Instant expiresAt;

}
