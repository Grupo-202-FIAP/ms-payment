package com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEvent {
    @JsonProperty("action")
    private String action;
    @JsonProperty("api_version")
    private String apiVersion;
    @JsonProperty("application_id")
    private String applicationId;
    private Data data;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("live_mode")
    private boolean liveMode;
    @JsonProperty("type")
    private String type;
    @JsonProperty("user_id")
    private String userId;
}
