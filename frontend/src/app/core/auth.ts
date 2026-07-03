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
 * Keeps the JWT in sessionStorage (PER-TAB) so you can be logged in as different users
 * in different tabs of the same browser (e.g. to demo chat between two accounts).
 * localStorage would be shared across tabs and the second login would clobber the first.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/users`;

  /** current raw JWT (null when logged out) */
  readonly token = signal<string | null>(sessionStorage.getItem(TOKEN_KEY));

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

  /** true when the logged-in user has the ADMIN realm role. */
  readonly isAdmin = computed(() => this.currentUser()?.roles.includes('ADMIN') ?? false);

  /** true when the user can create/manage events (ADMIN or ORGANISATEUR). */
  readonly canOrganize = computed(() => {
    const roles = this.currentUser()?.roles ?? [];
    return roles.includes('ADMIN') || roles.includes('ORGANISATEUR');
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
        sessionStorage.setItem(TOKEN_KEY, res.accessToken);
        this.token.set(res.accessToken);
      }),
    );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
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
