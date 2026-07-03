package tn.esprit.apigateway;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Turns the API Gateway into an OAuth2 Resource Server: every route requires a valid Keycloak
 * JWT (validated against Keycloak's public keys / JWKS), EXCEPT a small set of public paths.
 * This is the single security choke point for all services.
 *
 * It is ALSO the only place that can enforce role-based authorization for service-evenements,
 * service-avis and service-reservation: those services are NOT resource servers, so they cannot
 * read the roles from the token. The gateway extracts the Keycloak realm roles
 * (realm_access.roles) into ROLE_* authorities and applies the rules below.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // Stateless API with Bearer tokens — no CSRF cookie/session.
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // CORS inside the security chain (uses the CorsConfigurationSource bean): the
            // Access-Control-Allow-* headers are then present even on 401/403 responses,
            // so the browser shows the real status instead of a blocked "network error".
            .cors(cors -> { })
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

                // --- Role-based rules -------------------------------------------------------
                // Events: browsing is open to any logged-in user; creating/updating/deleting an
                // event is reserved to ORGANISATEUR and ADMIN (a PARTICIPANT can only book).
                .pathMatchers(HttpMethod.POST,   "/api/events/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                .pathMatchers(HttpMethod.PUT,    "/api/events/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                .pathMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                // Deleting any reservation is a management action.
                .pathMatchers(HttpMethod.DELETE, "/api/reservations/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                // Managing user accounts (role change / deletion) is ADMIN-only. Finer rules
                // (e.g. /me open to everyone, no self-deletion) are enforced in service-utilisateurs.
                .pathMatchers(HttpMethod.PUT,    "/api/users/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                // ---------------------------------------------------------------------------

                // everything else (reading events/avis/reservations/messages, /me, booking, rating)
                // just needs a valid token
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(realmRolesAuthenticationConverter())));
        return http.build();
    }

    /** Reactive adapter around a JWT converter that maps Keycloak realm roles to ROLE_* authorities. */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> realmRolesAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractRealmRoles);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    /** Reads realm_access.roles from the Keycloak JWT and turns each into a "ROLE_<name>" authority. */
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
