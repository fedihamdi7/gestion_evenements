package tn.esprit.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

// @EnableConfigServer turns this plain Spring Boot app into a "configuration server":
// other microservices ask it for their settings at startup instead of reading their own
// application.properties. All the real config files live in src/main/resources/config/.
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
