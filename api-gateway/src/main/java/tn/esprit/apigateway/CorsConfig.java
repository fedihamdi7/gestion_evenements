package tn.esprit.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Lets the browser-based demo frontend (opened from a file:// page or a local server) call the
 * gateway. Without this, the browser blocks the cross-origin calls to http://localhost:9090.
 *
 * Wide-open settings are fine for a local demo; tighten allowedOrigins for anything real.
 */
@Configuration
public class CorsConfig {

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.addAllowedOriginPattern("*");   // any origin (works with file:// "null" origin too)
        cfg.addAllowedMethod("*");          // GET/POST/PUT/DELETE/OPTIONS...
        cfg.addAllowedHeader("*");          // Authorization, Content-Type, ...
        cfg.setAllowCredentials(false);     // we use Bearer tokens, not cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(source);
    }
}
