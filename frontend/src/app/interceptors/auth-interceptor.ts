import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../config/api';
import { AuthService } from '../services/auth-service';

// Agrega "Authorization: Bearer <token>" a los pedidos al backend cuando hay sesión.
// El buscador de juegos es público: el backend lo deja pasar con o sin token.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token();
  const isLogin = req.url === `${API_URL}/users/login`;
  const withToken = !!token && req.url.startsWith(API_URL) && !isLogin;

  const authReq = withToken ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Mandamos token y el backend dijo 401: venció o ya no es válido
      if (withToken && error.status === 401) {
        auth.logout();
        router.navigate(['/login'], { queryParams: { expired: 1 } });
      }
      return throwError(() => error);
    }),
  );
};
