package com.postech.payment.fastfood.infrastructure.adapters.logging;


import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class LoggerAdapter implements LoggerPort {

    private final Logger logger = LoggerFactory.getLogger(LoggerAdapter.class);

    @Override
    public void info(String msg, Object... args) {
        // Fix: Sanitize arguments before passing to logger
        logger.info(msg, sanitize(args));
    }

    @Override
    public void debug(String msg, Object... args) {
        // Optimization: Check level before sanitizing to fix "Invoke method(s) only conditionally"
        if (logger.isDebugEnabled()) {
            logger.debug(msg, sanitize(args));
        }
    }

    @Override
    public void warn(String msg, Object... args) {
        logger.warn(msg, sanitize(args));
    }

    @Override
    public void error(String msg, Throwable t, Object... args) {
        // Sanitize args before formatting
        logger.error(String.format(msg, sanitize(args)), t);
    }

    @Override
    public void error(String msg, Object... args) {
        logger.error(msg, sanitize(args));
    }

    /**
     * Helper method to prevent Log Injection (CRLF).
     * Iterates through all log arguments and removes newlines.
     */
    private Object[] sanitize(Object... args) {
        if (args == null) {
            return new Object[0];
        }
        return Arrays.stream(args)
                .map(this::cleanString)
                .toArray();
    }

    private Object cleanString(Object input) {
        if (input == null) {
            return null;
        }

        return input.toString().replaceAll("[\n\r]", "_");
    }
}

