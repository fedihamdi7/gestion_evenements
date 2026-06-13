package tn.esprit.serviceavis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceAvisApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAvisApplication.class, args);
	}

}
