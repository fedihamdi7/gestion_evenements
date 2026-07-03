import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth';

/**
 * Attaches "Authorization: Bearer <token>" to every outgoing request when logged in,
 * so the secured endpoints accept the call.
 *
 * Also handles token expiry: Keycloak access tokens are short-lived, so when a request
 * comes back 401 while we *thought* we were logged in, the stored token is stale —
 * log out and send the user back to the login page instead of showing a cryptic error.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const isAuthCall = req.url.endsWith('/login') || req.url.endsWith('/register');
      if (err.status === 401 && token && !isAuthCall) {
        auth.logout();
        router.navigate(['/login'], { queryParams: { expired: 1 } });
      }
      return throwError(() => err);
    }),
  );
};
