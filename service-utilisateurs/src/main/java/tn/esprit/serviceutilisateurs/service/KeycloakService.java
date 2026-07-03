package tn.esprit.serviceutilisateurs.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import tn.esprit.serviceutilisateurs.config.KeycloakProperties;
import tn.esprit.serviceutilisateurs.model.Role;

/**
 * Thin wrapper around Keycloak's REST API. We talk to Keycloak over plain HTTP with Spring's
 * RestClient (no heavy Keycloak SDK needed):
 *
 *   - getAdminToken()   -> log in as the master admin to be allowed to manage users
 *   - createUser()      -> create a user in our realm, set the password, assign the role
 *   - login()           -> exchange email+password for a real Keycloak JWT (password grant)
 *   - updateUser()      -> update name/email (+ assign a new role)
 *   - deleteUser()      -> remove the user from Keycloak
 */
@Service
@Slf4j
public class KeycloakService {

    private final KeycloakProperties props;
    private final RestClient http = RestClient.create();

    public KeycloakService(KeycloakProperties props) {
        this.props = props;
    }

    private String adminBase() {
        return props.getBaseUrl() + "/admin/realms/" + props.getRealm();
    }

    // ----------------------------------------------------------------------------------------
    // Authentication helpers
    // ----------------------------------------------------------------------------------------

    /** Logs in as the master admin (admin-cli) and returns the access token used for Admin calls. */
    private String getAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", props.getAdmin().getClientId());
        form.add("username", props.getAdmin().getUsername());
        form.add("password", props.getAdmin().getPassword());

        Map<String, Object> token = http.post()
                .uri(props.getBaseUrl() + "/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(MAP);
        return (String) token.get("access_token");
    }

    /**
     * Logs a normal user in against our realm (OAuth2 "password" grant) and returns the raw
     * token response from Keycloak: access_token, refresh_token, expires_in, token_type, ...
     */
    public Map<String, Object> login(String email, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", props.getClientId());
        form.add("username", email);
        form.add("password", password);
        try {
            return http.post()
                    .uri(props.getBaseUrl() + "/realms/" + props.getRealm() + "/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(MAP);
        } catch (RestClientResponseException e) {
            // Keycloak returns 401 for bad credentials
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
    }

    // ----------------------------------------------------------------------------------------
    // User management (Admin REST API)
    // ----------------------------------------------------------------------------------------

    /**
     * Creates the user in Keycloak with a password and a realm role.
     * @return the new user's Keycloak id (UUID).
     */
    public String createUser(String email, String prenom, String nom, String password, Role role) {
        String adminToken = getAdminToken();

        Map<String, Object> body = Map.of(
                "username", email,
                "email", email,
                "firstName", prenom,
                "lastName", nom,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false)));

        try {
            var response = http.post()
                    .uri(adminBase() + "/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            // Keycloak returns the new user's URL in the Location header: .../users/{id}
            String location = response.getHeaders().getFirst("Location");
            String keycloakId = location.substring(location.lastIndexOf('/') + 1);

            assignRealmRole(adminToken, keycloakId, role);
            return keycloakId;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise dans Keycloak: " + email);
            }
            log.error("Keycloak createUser a echoue: {} {} (url={})", e.getStatusCode(), e.getResponseBodyAsString(), adminBase() + "/users");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Erreur Keycloak lors de la creation de l'utilisateur");
        }
    }

    /** Gives the user a realm role (ADMIN / ORGANISATEUR / PARTICIPANT). */
    private void assignRealmRole(String adminToken, String keycloakId, Role role) {
        // 1. fetch the full role representation (Keycloak needs its id, not just the name)
        Map<String, Object> roleRep = http.get()
                .uri(adminBase() + "/roles/" + role.name())
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(MAP);

        // 2. assign it to the user
        http.post()
                .uri(adminBase() + "/users/" + keycloakId + "/role-mappings/realm")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRep))
                .retrieve()
                .toBodilessEntity();
    }

    /** Updates the user's profile fields in Keycloak, and (if provided) assigns a new realm role. */
    public void updateUser(String keycloakId, String prenom, String nom, String email, Role role) {
        if (keycloakId == null) {
            return; // local-only row with no Keycloak counterpart; nothing to do
        }
        String adminToken = getAdminToken();

        var fields = new java.util.HashMap<String, Object>();
        if (prenom != null) fields.put("firstName", prenom);
        if (nom != null)    fields.put("lastName", nom);
        if (email != null) {
            fields.put("email", email);
            fields.put("username", email);
        }
        if (!fields.isEmpty()) {
            http.put()
                    .uri(adminBase() + "/users/" + keycloakId)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fields)
                    .retrieve()
                    .toBodilessEntity();
        }
        if (role != null) {
            assignRealmRole(adminToken, keycloakId, role);
        }
    }

    /** Deletes the user from Keycloak. Safe to call even if the user is already gone. */
    public void deleteUser(String keycloakId) {
        if (keycloakId == null) {
            return;
        }
        try {
            http.delete()
                    .uri(adminBase() + "/users/" + keycloakId)
                    .header("Authorization", "Bearer " + getAdminToken())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("Keycloak deleteUser({}) -> {}", keycloakId, e.getStatusCode());
        }
    }

    /** Reused parameterized type for parsing JSON object responses into a Map. */
    private static final org.springframework.core.ParameterizedTypeReference<Map<String, Object>> MAP =
            new org.springframework.core.ParameterizedTypeReference<>() {};
}
