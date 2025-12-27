package com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {
    @JsonProperty("external_reference")
    private String externalReference;
    @JsonProperty("id")
    private String id;
    @JsonProperty("status")
    private String status;
    @JsonProperty("status_detail")
    private String statusDetail;
    @JsonProperty("total_amount")
    private String totalAmount;
    @JsonProperty("type")
    private String type;
    @JsonProperty("version")
    private int version;
}
