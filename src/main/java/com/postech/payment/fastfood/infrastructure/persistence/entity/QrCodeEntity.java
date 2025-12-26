package com.postech.payment.fastfood.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_qr_code")
@Entity
public class QrCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID orderId;
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;
    @Column(nullable = false)
    private BigDecimal totalAmount;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false, length = 2048)
    private String qrCode;
    private OffsetDateTime expiresAt;

}
