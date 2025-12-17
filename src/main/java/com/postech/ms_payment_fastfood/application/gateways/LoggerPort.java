package com.postech.ms_payment_fastfood.application.gateways;

public interface LoggerPort {
    void info(String msg, Object... args);

    void debug(String msg, Object... args);

    void warn(String msg, Object... args);

    void error(String msg, Throwable t, Object... args);

    void error(String msg, Object... args);
}
