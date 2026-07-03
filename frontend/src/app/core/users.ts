import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Role, User } from './models';

/**
 * User CRUD against the gateway. These endpoints are SECURED — the JWT is attached
 * automatically by the auth interceptor.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/users`;

  /** Full directory (ADMIN only on the backend). */
  findAll(): Observable<User[]> {
    return this.http.get<User[]>(this.base);
  }

  /** The current user's own profile, resolved from the JWT server-side (any logged-in user). */
  me(): Observable<User> {
    return this.http.get<User>(`${this.base}/me`);
  }

  /** id + name only — safe for every logged-in user, used to show participant/author names. */
  publicList(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/public`);
  }

  updateRole(id: number, role: Role): Observable<User> {
    return this.http.put<User>(`${this.base}/${id}`, { role });
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
