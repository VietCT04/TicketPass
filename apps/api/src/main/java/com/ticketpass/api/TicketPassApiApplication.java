package com.ticketpass.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketPassApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketPassApiApplication.class, args);
    }
}
