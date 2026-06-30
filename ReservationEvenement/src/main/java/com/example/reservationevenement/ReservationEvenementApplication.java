package com.example.reservationevenement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReservationEvenementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationEvenementApplication.class, args);
    }

}
