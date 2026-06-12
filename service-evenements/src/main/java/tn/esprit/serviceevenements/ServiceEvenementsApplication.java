package tn.esprit.serviceevenements;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceEvenementsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceEvenementsApplication.class, args);
	}

}
