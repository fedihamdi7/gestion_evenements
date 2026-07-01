package tn.esprit.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Turns the API Gateway into an OAuth2 Resource Server: every route now requires a
 * valid Keycloak JWT (validated against Keycloak's public keys / JWKS), EXCEPT a small
 * set of public paths below. This is the single security choke point for all services.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // Stateless API with Bearer tokens — no CSRF cookie/session.
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                // CORS preflight must pass without a token
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // public: account creation + login (needed to obtain a token in the first place)
                .pathMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                // real-time chat handshake — browsers cannot send an Authorization header on a
                // WebSocket, so the token check happens inside the messaging service's handshake.
                .pathMatchers("/socket.io/**").permitAll()
                // monitoring + API documentation
                .pathMatchers("/actuator/**", "/swagger-ui.html", "/swagger-ui/**",
                        "/v3/api-docs/**", "/webjars/**", "/api/*/v3/api-docs/**").permitAll()
                // everything else (events, reservations, avis, messages, user CRUD) needs a JWT
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> { }));
        return http.build();
    }
}
