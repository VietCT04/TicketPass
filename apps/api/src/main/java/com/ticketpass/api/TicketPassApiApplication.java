package com.ticketpass.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.ticketpass.api.payment.PaymentProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PaymentProperties.class)
public class TicketPassApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketPassApiApplication.class, args);
    }
}
