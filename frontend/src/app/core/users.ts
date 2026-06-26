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

  findAll(): Observable<User[]> {
    return this.http.get<User[]>(this.base);
  }

  updateRole(id: number, role: Role): Observable<User> {
    return this.http.put<User>(`${this.base}/${id}`, { role });
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
