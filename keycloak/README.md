# Keycloak for service-utilisateurs

Keycloak is the **login server** (identity provider). It stores users + passwords and
issues signed **JWT** tokens. `service-utilisateurs` no longer checks passwords itself:
it creates users in Keycloak, gets tokens from Keycloak, and only *validates* the JWT.

## 1. Start Keycloak (no Docker needed)

```powershell
./keycloak/run-keycloak.ps1
```

- First run downloads Keycloak 26.1.4 (~120 MB) and unzips it here.
- It auto-imports the realm **gestion-evenements** with roles `ADMIN`, `ORGANISATEUR`,
  `PARTICIPANT` and the client `service-utilisateurs`.
- Admin console: <http://localhost:8080> &nbsp;→&nbsp; login **admin / admin**

Keep this window open while you work. `Ctrl+C` stops it.

## 2. Start order

1. `eureka-server`
2. **Keycloak** (`run-keycloak.ps1`)
3. `service-utilisateurs`
4. the other services / gateway

> `service-utilisateurs` still **boots even if Keycloak is down** (token validation is
> lazy). But register/login and securing endpoints only work once Keycloak is up.

## 3. What each endpoint does now

| Endpoint | Auth | Behaviour |
|---|---|---|
| `POST /api/users/register` | public | Creates the user **in Keycloak** (+ a local profile row) |
| `POST /api/users/login` | public | Returns a real **Keycloak JWT** (`accessToken`) |
| `GET/PUT/DELETE /api/users/**` | **JWT required** | Send header `Authorization: Bearer <accessToken>` |

### Quick test (after register)
```
POST /api/users/login   { "email": "...", "motDePasse": "..." }
-> { "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 300 }

GET /api/users          Header: Authorization: Bearer eyJ...
```

## Config (service-utilisateurs/application.properties)
- `keycloak.base-url=http://localhost:8080`
- `keycloak.realm=gestion-evenements`
- `keycloak.client-id=service-utilisateurs`
- `keycloak.admin.username=admin` / `keycloak.admin.password=admin` (master admin, used to manage users)

These are dev defaults — change the admin password for anything real.
