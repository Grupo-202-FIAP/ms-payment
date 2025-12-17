package com.postech.payment.fastfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsPaymentFastfoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPaymentFastfoodApplication.class, args);
    }

}
