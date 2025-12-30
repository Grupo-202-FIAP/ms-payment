package com.postech.payment.fastfood.infrastructure.config;

import com.postech.payment.fastfood.application.exception.FastFoodException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void fastFoodException_whenException_thenReturnProblemDetail() {
        final String message = "Payment not found";
        final String title = "Payment Error";
        final HttpStatus status = HttpStatus.NOT_FOUND;
        final FastFoodException exception = new FastFoodException(message, title, status);

        final ProblemDetail result = globalExceptionHandler.fastFoodException(exception);

        assertNotNull(result);
        assertEquals(status.value(), result.getStatus());
        assertEquals(message, result.getDetail());
        assertEquals(title, result.getTitle());
    }

    @Test
    void handleValidationExceptions_whenMethodArgumentNotValidException_thenReturnBadRequestWithErrors() {
        final MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        final BindingResult bindingResult = mock(BindingResult.class);
        final List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("payment", "amount", "Amount must be positive"));
        fieldErrors.add(new FieldError("payment", "orderId", "Order ID is required"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        final ResponseEntity<Map<String, String>> result = globalExceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        final Map<String, String> errors = result.getBody();
        assertEquals(2, errors.size());
        assertEquals("Amount must be positive", errors.get("amount"));
        assertEquals("Order ID is required", errors.get("orderId"));
    }

    @Test
    void handleIllegalArgumentException_whenException_thenReturnConflict() {
        final String message = "Invalid argument";
        final IllegalArgumentException exception = new IllegalArgumentException(message);

        final ResponseEntity<String> result = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals(message, result.getBody());
    }
}

