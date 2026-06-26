package tn.esprit.serviceutilisateurs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Binds the keycloak.* keys from application.properties into a typed object,
 * so the rest of the code reads strongly-typed config (and the IDE stops warning
 * about "unknown property").
 */
@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    /** e.g. http://localhost:8080 */
    private String baseUrl;

    /** the realm that holds our users, e.g. gestion-evenements */
    private String realm;

    /** public client used for the login (password grant), e.g. service-utilisateurs */
    private String clientId;

    /** master admin credentials used to manage users via the Admin REST API */
    private Admin admin = new Admin();

    @Data
    public static class Admin {
        /** usually admin-cli */
        private String clientId;
        private String username;
        private String password;
    }
}
