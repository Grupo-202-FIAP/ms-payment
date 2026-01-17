package com.postech.payment.fastfood.infrastructure.persistence.repository.payment;

import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IPaymentInformationQrCodeRepository extends JpaRepository<QrCodeEntity, UUID> {
}
