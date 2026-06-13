package tn.esprit.serviceutilisateurs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @EnableFeignClients tells Spring to scan for @FeignClient interfaces (here: ReservationClient)
// and generate HTTP clients for them. This is what powers the OpenFeign communication.
// (The Eureka client starter on the classpath auto-registers this service in Eureka — no annotation needed.)
@EnableFeignClients
@SpringBootApplication
public class ServiceUtilisateursApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceUtilisateursApplication.class, args);
	}

}
