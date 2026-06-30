package tn.esprit.serviceutilisateurs.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Turns this service into an OAuth2 Resource Server protected by Keycloak.
 *
 * - register / login stay PUBLIC (no token needed) so the frontend can create an account
 *   and obtain a token.
 * - every other endpoint (the user CRUD) requires a valid Keycloak JWT in the
 *   "Authorization: Bearer <token>" header.
 * - Keycloak puts the user's realm roles in the token under realm_access.roles. We translate
 *   each into a Spring authority "ROLE_<role>" so you can later use @PreAuthorize("hasRole('ADMIN')").
 */
@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // REST API with JWTs -> no CSRF cookie, no HTTP session.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // public: account creation + login + health
                .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // public: Swagger/OpenAPI docs so the API Gateway can aggregate them
                .requestMatchers("/api/users/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // IMPORTANT: when a controller throws (e.g. 409 duplicate email), Spring forwards
                // the request to /error. The security filter runs again on that forward, so /error
                // must be public — otherwise every error is masked as a misleading 401.
                .requestMatchers("/error").permitAll()
                // everything else (the user CRUD) needs a valid token
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
        return http.build();
    }

    /** Maps Keycloak realm roles (realm_access.roles) to Spring "ROLE_*" authorities. */
    private JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractRealmRoles);
        return converter;
    }

    @SuppressWarnings("unchecked")
    private static Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
