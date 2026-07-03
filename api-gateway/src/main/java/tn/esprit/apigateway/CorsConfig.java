package tn.esprit.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Lets the browser-based demo frontend (opened from a file:// page or a local server) call the
 * gateway. Without this, the browser blocks the cross-origin calls to http://localhost:9090.
 *
 * Exposed as a CorsConfigurationSource consumed by the security chain (SecurityConfig) rather
 * than a standalone CorsWebFilter: this way the CORS headers are also added to 401/403 responses
 * written by Spring Security, so the browser reports the real status instead of a network error.
 *
 * Wide-open settings are fine for a local demo; tighten allowedOrigins for anything real.
 */
@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.addAllowedOriginPattern("*");   // any origin (works with file:// "null" origin too)
        cfg.addAllowedMethod("*");          // GET/POST/PUT/DELETE/OPTIONS...
        cfg.addAllowedHeader("*");          // Authorization, Content-Type, ...
        cfg.setAllowCredentials(false);     // we use Bearer tokens, not cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
