package com.postech.payment.fastfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentFastfoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentFastfoodApplication.class, args);
    }

}
