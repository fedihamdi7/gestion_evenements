package tn.esprit.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// No special annotation needed: the Eureka client starter on the classpath makes this
// gateway register itself in Eureka and resolve other services by name (lb://...).
// All routing rules live in application.yml.
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
