package tn.esprit.serviceutilisateurs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What POST /api/users/login returns now: the real Keycloak token.
 * The frontend stores accessToken and sends it on every secured request as
 * "Authorization: Bearer &lt;accessToken&gt;".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;   // "Bearer"
    private Long expiresIn;     // seconds until the access token expires
}
