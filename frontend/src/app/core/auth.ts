import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, Role, User } from './models';

const TOKEN_KEY = 'access_token';

interface JwtClaims {
  preferred_username?: string;
  email?: string;
  realm_access?: { roles?: string[] };
  exp?: number;
}

/**
 * Handles authentication against service-utilisateurs (which delegates to Keycloak).
 * Keeps the JWT in localStorage and exposes the current user as signals.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/users`;

  /** current raw JWT (null when logged out) */
  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  readonly isLoggedIn = computed(() => !!this.token());

  /** username + roles decoded from the JWT */
  readonly currentUser = computed(() => {
    const t = this.token();
    if (!t) return null;
    const c = this.decode(t);
    const roles = (c.realm_access?.roles ?? []).filter((r): r is Role =>
      ['ADMIN', 'ORGANISATEUR', 'PARTICIPANT'].includes(r),
    );
    return { username: c.preferred_username ?? c.email ?? '—', roles };
  });

  /** current user's email — the identity used for chat (present in the JWT). */
  readonly currentEmail = computed(() => {
    const t = this.token();
    if (!t) return null;
    const c = this.decode(t);
    return c.email ?? c.preferred_username ?? null;
  });

  register(body: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.base}/register`, body);
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, body).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.accessToken);
        this.token.set(res.accessToken);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.token.set(null);
  }

  private decode(token: string): JwtClaims {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(payload));
    } catch {
      return {};
    }
  }
}
