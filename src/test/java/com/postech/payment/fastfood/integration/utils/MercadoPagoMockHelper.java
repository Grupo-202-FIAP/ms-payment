package com.postech.payment.fastfood.integration.utils;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Helper class for setting up WireMock stubs for Mercado Pago API.
 */
public class MercadoPagoMockHelper {

    private final WireMockServer wireMockServer;

    public MercadoPagoMockHelper(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    /**
     * Stubs a successful QR code creation response from Mercado Pago.
     *
     * @param orderId The order ID to match in the request
     */
    public void stubCreateOrderSuccess(String orderId) {
        wireMockServer.stubFor(
                post(urlPathEqualTo("/v1/orders"))
                        .withHeader("Authorization", containing("Bearer"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(buildSuccessResponse(orderId)))
        );
    }

    /**
     * Stubs a successful QR code creation response with custom data.
     *
     * @param orderId   The order ID
     * @param qrData    The QR code data to return
     * @param amount    The total amount
     */
    public void stubCreateOrderSuccess(String orderId, String qrData, double amount) {
        String responseBody = String.format("""
                {
                    "id": "order-%s",
                    "status": "created",
                    "description": "Pedido FastFood - %s",
                    "external_reference": "%s",
                    "total_amount": %.2f,
                    "currency": "BRL",
                    "created_date": "2026-01-17T10:00:00-03:00",
                    "type": "qr",
                    "config": {
                        "qr": {
                            "store_id": "SUC001",
                            "type": "dynamic"
                        }
                    },
                    "items": [],
                    "type_response": {
                        "qr_data": "%s"
                    }
                }
                """, orderId, orderId, orderId, amount, qrData);

        wireMockServer.stubFor(
                post(urlPathEqualTo("/v1/orders"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody))
        );
    }

    /**
     * Stubs a failure response from Mercado Pago (server error).
     */
    public void stubCreateOrderServerError() {
        wireMockServer.stubFor(
                post(urlPathEqualTo("/v1/orders"))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                            "message": "Internal server error",
                                            "error": "internal_error",
                                            "status": 500
                                        }
                                        """))
        );
    }

    /**
     * Stubs a bad request response from Mercado Pago.
     */
    public void stubCreateOrderBadRequest() {
        wireMockServer.stubFor(
                post(urlPathEqualTo("/v1/orders"))
                        .willReturn(aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                            "message": "Invalid request",
                                            "error": "bad_request",
                                            "status": 400
                                        }
                                        """))
        );
    }

    /**
     * Stubs an unauthorized response from Mercado Pago.
     */
    public void stubCreateOrderUnauthorized() {
        wireMockServer.stubFor(
                post(urlPathEqualTo("/v1/orders"))
                        .willReturn(aResponse()
                                .withStatus(401)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                            "message": "Unauthorized",
                                            "error": "unauthorized",
                                            "status": 401
                                        }
                                        """))
        );
    }

    /**
     * Stubs a timeout/connection error from Mercado Pago.
     */
    public void stubCreateOrderTimeout() {
        wireMockServer.stubFor(
                post(urlPathEqualTo("/v1/orders"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withFixedDelay(30000)) // 30 second delay to simulate timeout
        );
    }

    /**
     * Resets all WireMock stubs.
     */
    public void resetStubs() {
        wireMockServer.resetAll();
    }

    /**
     * Verifies that the create order endpoint was called.
     */
    public void verifyCreateOrderCalled() {
        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/v1/orders")));
    }

    /**
     * Verifies that the create order endpoint was called a specific number of times.
     */
    public void verifyCreateOrderCalledTimes(int times) {
        wireMockServer.verify(times, postRequestedFor(urlPathEqualTo("/v1/orders")));
    }

    private String buildSuccessResponse(String orderId) {
        return String.format("""
                {
                    "id": "order-%s",
                    "status": "created",
                    "description": "Pedido FastFood - %s",
                    "external_reference": "%s",
                    "total_amount": 100.00,
                    "currency": "BRL",
                    "created_date": "2026-01-17T10:00:00-03:00",
                    "type": "qr",
                    "config": {
                        "qr": {
                            "store_id": "SUC001",
                            "type": "dynamic"
                        }
                    },
                    "items": [],
                    "type_response": {
                        "qr_data": "00020126580014br.gov.bcb.pix0136a629532e-7693-4846-b028-test"
                    }
                }
                """, orderId, orderId, orderId);
    }
}
